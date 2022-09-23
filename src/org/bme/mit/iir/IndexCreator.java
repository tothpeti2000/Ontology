package org.bme.mit.iir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IndexCreator {

	final TermRecognizer termRecognizer;
	final Map<String, Map<File, Integer>> index;

	public IndexCreator() throws IOException {
		termRecognizer = new TermRecognizer();
		index = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public IndexCreator(File indexFile) throws IOException, ClassNotFoundException {
		termRecognizer = new TermRecognizer();

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile))) {
			index = (Map<String, Map<File, Integer>>) ois.readObject();
		}
	}

	public void createIndexFile(File indexFile) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile))) {
			oos.writeObject(index);
		}
	}

	public void processFile(File file) throws IOException {
		String content = Util.readFileAsString(file.toString());
		Map<String, Integer> termFrequencies = termRecognizer.termFrequency(content);

		for (Map.Entry<String, Integer> termFrequency : termFrequencies.entrySet()) {
			String term = termFrequency.getKey();
			Integer frequency = termFrequency.getValue();

			if (!index.containsKey(term)) {
				index.put(term, new HashMap<>());
			}

			index.get(term).put(file, frequency);
		}
	}

	public void processFolder(File folder) throws IOException {
		if (!folder.exists() || !folder.isDirectory()) {
			throw new RuntimeException("'" + folder + "' isn't a directory!");
		}

		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				processFolder(f);
			} else {
				processFile(f);
			}
		}
	}

	public Map<File, Integer> getOccurences(String keyword) {
		return index.containsKey(keyword) ? Collections.unmodifiableMap(index.get(keyword)) : Collections.emptyMap();
	}

	public static void main(String args[]) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage:");
			System.err.println("  java " + IndexCreator.class.getName() + " <corpus folder> <index file>");
			System.err.println(
					"Creates a binary index file containing the occurrences of the keywords from the articles in the corpus folder");

			System.exit(-1);
		}

		File corpusFolder = new File(args[0]);
		File indexFile = new File(args[1]);

		IndexCreator indexCreator = new IndexCreator();

		System.out.println("Reading corpus...");
		indexCreator.processFolder(corpusFolder);

		System.out.println("Creating index file...");
		indexCreator.createIndexFile(indexFile);

		System.out.println("Index file created");
	}
}
