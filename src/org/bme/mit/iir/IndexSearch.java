package org.bme.mit.iir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;

public class IndexSearch {
    final IndexCreator indexCreator;
    final Map<File, Map<String, Integer>> fileWords;
    final LinkedHashMap<File, Map<String, Integer>> sortedFileWords;

    public IndexSearch(File indexFile) throws IOException, ClassNotFoundException {
        this.indexCreator = new IndexCreator(indexFile);
        this.fileWords = new HashMap<>();
        sortedFileWords = new LinkedHashMap<>();
    }

    public void collectFileWords(List<String> words) {
        for (String word : words) {
            Map<File, Integer> wordOccurrences = indexCreator.getOccurences(word);

            for (Map.Entry<File, Integer> wordOccurrence : wordOccurrences.entrySet()) {
                File file = wordOccurrence.getKey();
                Integer count = wordOccurrence.getValue();

                if (!fileWords.containsKey(file)) {
                    fileWords.put(file, new HashMap<>());
                }

                Map<String, Integer> wordMap = fileWords.get(file);

                if (!wordMap.containsKey(word)) {
                    wordMap.put(word, 0);
                }

                int currentCount = wordMap.get(word);
                wordMap.put(word, currentCount + count);
            }
        }
    }

    public void sortFileWords() {
        List<Map.Entry<File, Map<String, Integer>>> fileWordsList = new LinkedList<>(fileWords.entrySet());

        Collections.sort(fileWordsList, (item1, item2) -> item2.getValue().size() - item1.getValue().size());
        Collections.sort(fileWordsList,
                (item1, item2) -> item2.getValue().values().stream().reduce(0, Integer::sum)
                        - item1.getValue().values().stream().reduce(0, Integer::sum));

        for (Map.Entry<File, Map<String, Integer>> entry : fileWordsList) {
            sortedFileWords.put(entry.getKey(), entry.getValue());
        }

        System.out.println(sortedFileWords + "\n");
    }

    public void printMatchFiles() {
        for (File file : sortedFileWords.keySet()) {
            System.out.println(file);
        }

        System.out.println("Total matches: " + sortedFileWords.size());
    }

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        File indexFile = new File("test/index.txt");
        IndexSearch indexSearch = new IndexSearch(indexFile);

        List<String> argList = new ArrayList<>(Arrays.asList(args));
        List<String> queryKeys = new ArrayList<>(argList);

        // Reasoning reasoning = new Reasoning(
        // "pc_shop.owl");

        // for (String arg : argList) {
        // Set<OWLClass> descendants = reasoning.getSubClasses(arg, false);

        // for (OWLClass owlClass : descendants) {
        // if (!owlClass.isBuiltIn()) {
        // queryKeys.add(owlClass.getIRI().getFragment());

        // Set<String> labels = reasoning.getClassAnnotations(owlClass);
        // queryKeys.addAll(new ArrayList<>(labels));
        // }
        // }
        // }

        // System.out.println("Query keys after query expansion: " + queryKeys);

        indexSearch.collectFileWords(queryKeys);
        indexSearch.sortFileWords();
        indexSearch.printMatchFiles();
    }
}
