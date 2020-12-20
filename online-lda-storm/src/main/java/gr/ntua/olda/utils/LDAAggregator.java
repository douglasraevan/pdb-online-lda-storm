package gr.ntua.olda.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONObject;
import org.apache.storm.trident.operation.ReducerAggregator;
import org.apache.storm.trident.tuple.TridentTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vagueobjects.ir.lda.online.OnlineLDA;
import vagueobjects.ir.lda.online.Result;
import vagueobjects.ir.lda.tokens.Documents;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

public class LDAAggregator implements ReducerAggregator<OnlineLDA> {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(LDAAggregator.class);

    private LDAVocabulary voc;

    // LDA algorithm parameters
    private static int D = LocalConfig.getInt("lda.algorithm.D ", 8524840);
    private static int K = LocalConfig.getInt("lda.algorithm.K ", 5);
    private static double tau = LocalConfig.getDouble("lda.algorithm.tau ", 1.0);
    private static double kappa =  LocalConfig.getDouble("lda.algorithm.kappa ", 0.8);
    private static double alpha = LocalConfig.getDouble("lda.algorithm.alpha ", 1.d/K);
    private static double eta = LocalConfig.getDouble("lda.algorithm.eta ", 1.d/K);

    // Redis client
    private RedisConnection<String, String> redis;

    private Map<String, Integer> doubleBraceMap;

    @Override
    public OnlineLDA init() {
        OnlineLDA lda = null;

        redis = _getClient().connect();

        try {
            logger.info("LDAAggregator: Initializing Values...");
            voc = new LDAVocabulary(LocalConfig.get("file.lda.dictionary.path"));

            // Initialize HashMap
            doubleBraceMap = new HashMap<String, Integer>();
            for (String topic: voc.getStrings()) {
                doubleBraceMap.put(topic, 0);
            }

            lda = new OnlineLDA(voc.size(), K, D, alpha, eta, tau, kappa);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lda;
    }

    private RedisClient _getClient() {
        // Local Mode
        RedisClient client = new RedisClient(LocalConfig.get("redis.server.local.address"),
                LocalConfig.getInt("redis.server.local.port", 6379));

//        // Cluster Mode
//        RedisClient client = new RedisClient(LocalConfig.get("redis.server.cluster.address"),
//                LocalConfig.getInt("redis.server.cluster.port", 6379));

        return client;
    }


    @Override
    public OnlineLDA reduce(OnlineLDA curr, TridentTuple tuple) {
        if (tuple.isEmpty())
            return curr;

        if (redis == null) {
            redis = _getClient().connect();
        }

        // Didn't figure out why init is not called but lost my patience so ...
        if (voc == null) {
            try {
                logger.info("LDAAggregator: Initializing Values...");
                voc = new LDAVocabulary(LocalConfig.get("file.lda.dictionary.path"));

                // Initialize HashMap
                doubleBraceMap = new HashMap<String, Integer>();
                for (String topic: voc.getStrings()) {
                    doubleBraceMap.put(topic, 0);
                }

                OnlineLDA lda = new OnlineLDA(voc.size(), K, D, alpha, eta, tau, kappa);
                return process_current_batch(lda, tuple);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return process_current_batch(curr, tuple);
    }


    @SuppressWarnings("unchecked")
    private OnlineLDA process_current_batch(OnlineLDA curr, TridentTuple tuple) {
        List<String> docs = null;

        try {
            docs = (List<String>) tuple.getValue(0); // get docs here;
            Documents documents = new Documents(docs, voc);

            logger.info("LDAAggregator: Executing LDA algorithm");
            Result result = curr.workOn(documents);
            String resultString = result.toString();
            logger.info(resultString);

            countTweet(resultString);
            System.out.println("hashmap : " +  doubleBraceMap);

            JSONObject jsonObject = new JSONObject(doubleBraceMap);
            redis.publish("TwitterLDAStream", String.valueOf(jsonObject));

        } catch (ArrayIndexOutOfBoundsException ex) {
            logger.error("LDAAggregator: Exception happened for docs size {} and exception error was {} ",
                    (docs != null ? docs.size() : 0), ex.getLocalizedMessage());
        }
        return curr;
    }

    private Map<String,Integer> countTweet(String resultString){
        List<String> tupleList = Arrays.asList(resultString.split("\n"));
        for(String tuple : tupleList){
            String category = tuple.split(" ")[0].split("->")[0];
            Integer oldValue = doubleBraceMap.get(category);
            doubleBraceMap.replace(category, oldValue+1);
        }
        return doubleBraceMap;

    }
}
