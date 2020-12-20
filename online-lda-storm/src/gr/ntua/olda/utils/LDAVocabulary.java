package gr.ntua.olda.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import vagueobjects.ir.lda.tokens.Vocabulary;

/**
 * Jolda Implementation Vocabulary Class
 */
public class LDAVocabulary implements Vocabulary {
    private List<String> strings = new ArrayList<String>();

    public LDAVocabulary(String path) throws IOException {
        Scanner scanner = new Scanner(new File(path));
        while (scanner.hasNextLine()) {
            strings.add(scanner.nextLine().trim());
        }

        try {
            scanner.close();
        } catch (IllegalStateException ex) {
            // ... :/
        }
    }

    private LDAVocabulary(String[] strings2) {
        strings = Arrays.asList(strings2);
    }

    public void addWordsToVocab(String[] stringsToBeAdded) {
        for (String str : stringsToBeAdded) {
            if (!strings.contains(str))
                strings.add(str);
        }
    }

    @Override
    public boolean contains(String token) {
        return strings.contains(token);
    }

    @Override
    public int size() {
        return strings.size();
    }

    @Override
    public int getId(String token) {
        for (int i = 0; i < strings.size(); ++i) {
            if (strings.get(i).equals(token)) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String getToken(int id) {
        return strings.get(id);
    }

}