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

public class Ex1CreateIndex {

	final TermRecognizer termRec;
	final Map<String, Map<File, Integer>> index;

	// üres index létrehozása
	public Ex1CreateIndex() throws IOException {
		termRec = new TermRecognizer();
		index = new HashMap<>();
	}

	// index betöltése bináris file-ból
	@SuppressWarnings("unchecked")
	public Ex1CreateIndex(File indexFile) throws IOException, ClassNotFoundException {
		termRec = new TermRecognizer();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile))) {
			index = (Map<String, Map<File, Integer>>) ois.readObject();
		}
	}

	// index mentése bináris file-ba
	public void saveIndex(File indexFile) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile))) {
			oos.writeObject(index);
		}
	}

	// egy cikk hozzáadása az indexhez
	public void addFile(File file) throws IOException {
		String content = Util.readFileAsString(file.toString());
		Map<String, Integer> termFreqs = termRec.termFrequency(content);
		for (Map.Entry<String, Integer> termFreq : termFreqs.entrySet()) {
			String term = termFreq.getKey();
			Integer freq = termFreq.getValue();
			if (!index.containsKey(term)) {
				index.put(term, new HashMap<>());
			}
			index.get(term).put(file, freq);
		}
	}

	// könyvtár (korpusz) rekurzív hozzáadása az indexhez
	public void addFolder(File folder) throws IOException {
		if (!folder.exists() || !folder.isDirectory())
			throw new RuntimeException("A '" + folder + "' nem egy könyvtár!");
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				addFolder(f);
			} else {
				addFile(f);
			}
		}
	}

	// kulcsszó előfordulásainak és gyakoriságának lekérdezése
	public Map<File, Integer> getOccurences(String keyword) {
		if (index.containsKey(keyword))
			return Collections.unmodifiableMap(index.get(keyword));
		else
			return Collections.emptyMap();
	}

	// index létrehozása korpusz alapján
	public static void main(String args[]) throws IOException {
		if (args.length != 2) {
			System.err.println("Használat:");
			System.err.println("  java " + Ex1CreateIndex.class.getName() + " korpusz_könyvtár index_file");
			System.err.println(
					"A korpusz könyvtárban található cikkekre létrehoz egy bináris index file-t a kulcsszavak előfordulásaival.");
			System.exit(-1);
		}
		File corpusFolder = new File(args[0]);
		File indexFile = new File(args[1]);
		Ex1CreateIndex idx = new Ex1CreateIndex();
		System.out.println("Korpusz beolvasása...");
		idx.addFolder(corpusFolder);
		System.out.println("Index mentése...");
		idx.saveIndex(indexFile);
		System.out.println("Kész.");
	}
}
