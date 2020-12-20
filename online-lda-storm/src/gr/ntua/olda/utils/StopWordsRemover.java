package gr.ntua.olda.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.storm.shade.org.apache.commons.lang.StringUtils;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Applies StopFilter to each String List and removes usernames
 *
 * @author Sofia
 */
public class StopWordsRemover extends BaseFunction {

    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(StopWordsRemover.class);
    private static List<String> stopWords;

    @Override
    public void prepare(Map conf, TridentOperationContext context) {

        try {
            stopWords = readFromFile(LocalConfig.get("file.lda.stopwords.path"));

            // Twitter common words
            stopWords.add("RT");
            stopWords.add("via");
            stopWords.add("www");
            stopWords.add("http");
            stopWords.add("com");

            logger.info("Parsed stopWords file successfully");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {

        @SuppressWarnings("unchecked")
        List<String> wordList = (List<String>) tuple.getValue(0);
        List<String> filteredList = new ArrayList<>();

        for (String word : wordList) {
            if (!(stopWords.contains(word)
                    || word.startsWith("@")
                    || !containsLetter(word))
                    ) {
                filteredList.add(word);
            }
        }

        String result = StringUtils.join(filteredList, " ");
        collector.emit(new Values(result));
    }

    private List<String> readFromFile(String path) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

        List<String> wordList = new ArrayList<>();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                wordList.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close(); // close file
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return wordList;
    }


    private boolean containsLetter(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (Character.isLetter(c))
                return true;
        }

        return false;
    }
}	
