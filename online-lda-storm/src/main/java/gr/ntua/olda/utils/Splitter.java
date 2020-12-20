package gr.ntua.olda.utils;

import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits String to ArrayList of Strings for processing one by one
 */
public class Splitter extends BaseFunction {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {

        String sentence = tuple.getString(0);

        if (sentence == null)
            return;

        sentence = removeSplitters(sentence);

        List<String> words = new ArrayList<>();

        for (String word : sentence.split(" ")) {
            word = word.trim();

            if (!word.isEmpty())
                words.add(word);
        }
        collector.emit(new Values(words));
    }

    private static String removeSplitters(String sentence) {
        sentence = sentence.replace("\t", " ");
        sentence = sentence.replace(".", " ");
        sentence = sentence.replace("!", " ");
        sentence = sentence.replace("\\", " ");
        sentence = sentence.replace(":", " ");
        sentence = sentence.replace(";", " ");
        sentence = sentence.replace("(", " ");
        sentence = sentence.replace(")", " ");
        sentence = sentence.replace(",", " ");
        sentence = sentence.replace("?", " ");
        sentence = sentence.replace("/", " ");
        sentence = sentence.replace("*", " ");
        sentence = sentence.replace("#", " ");
        sentence = sentence.replace("'", " ");
        sentence = sentence.replace("\"", "");
        return sentence;
    }
}


