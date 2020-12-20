package gr.ntua.olda.LDAStorm;

import java.io.IOException;
import java.net.InetSocketAddress;

import gr.ntua.olda.db.RedisMapState;
import gr.ntua.olda.spouts.RandomSentenceSpout;
import gr.ntua.olda.spouts.TwitterSampleSpout;
import gr.ntua.olda.spouts.TextFileSpout;

import gr.ntua.olda.utils.*;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.spout.RichSpoutBatchExecutor;
import org.apache.storm.trident.state.StateFactory;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

public class LdaStormImpl {

    private static StormTopology buildTopology(StateFactory state)
            throws IOException {

        TridentTopology topology = new TridentTopology();

        // Spouts is the way to feed data into Trident Topology
        TwitterSampleSpout spout = new TwitterSampleSpout(LocalConfig.get("twitter.account.name"), LocalConfig.get("twitter.account.password"));
        //RandomSentenceSpout spout = new RandomSentenceSpout();
        //TextFileSpout spout = new TextFileSpout(LocalConfig.get("file.lda.tweets.path"));

        topology
                .newStream("spout", spout)
                .parallelismHint(1)
                .shuffle()
                .each(new Fields("sentence"), new Splitter(), new Fields("wordList")).name("splitter")
                .parallelismHint(4)
//                .each(new Fields("wordList"), new Stemmer(), new Fields("stemwordList")).name("stemmer")
                .each(new Fields("wordList"), new StopWordsRemover(), new Fields("filteredList")).name("filter")
                .aggregate(new Fields("filteredList"), new ListAggregator(), new Fields("aggregated"))
                .parallelismHint(4)
                .persistentAggregate(state, new Fields("aggregated"), new LDAAggregator(), new Fields("output"));

        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        conf.put(RichSpoutBatchExecutor.MAX_BATCH_SIZE_CONF, LocalConfig.getInt("topology.batch.size", 100));
        conf.setNumWorkers(2);

        // Before generating JAR to run in cluster, remember to change the RedisClient init in LDAAggregator
        // Also uncomment the "scope provided" line for storm-core in pom.xml
        if (args.length > 0) {
            run_in_cluster(args[0], conf);
        }
        else {
            run_locally(conf);
        }

    }

    // for running on cluster
    // this submits the topology into the master
    private static void run_in_cluster(String args, Config conf) throws Exception {
        int redis_port = LocalConfig.getInt("redis.server.cluster.port", 0);
        StateFactory redis = new RedisMapState.Factory(new InetSocketAddress(LocalConfig.get("redis.server.cluster.address"), redis_port), LocalConfig.get("redis.server.global.key"));
        StormSubmitter.submitTopologyWithProgressBar(args, conf, buildTopology(redis));
    }

    // for running in a Local Cluster
    private static void run_locally(Config conf) throws Exception {
        LocalCluster cluster = new LocalCluster();
        int redis_port = LocalConfig.getInt("redis.server.local.port", 0);
        StateFactory redis = new RedisMapState.Factory(new InetSocketAddress(LocalConfig.get("redis.server.local.address"), redis_port), LocalConfig.get("redis.server.global.key"));
        cluster.submitTopology(LocalConfig.get("topology.name"), conf, buildTopology(redis));
        Utils.sleep(600*1000);
        cluster.shutdown();
    }
}

