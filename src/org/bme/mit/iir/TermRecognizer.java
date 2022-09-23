package org.bme.mit.iir;

import java.io.IOException;
import java.util.*;

public class TermRecognizer {

	final static String DEFAULT_STOPWORD_FNAME = "stopwords.txt";
	final static String DEFAULT_DELIMITERS = " .,;:?!\"()\t\r\n";

	Set<String> stopwords;
	String delimiters;

	TermRecognizer() throws IOException {
		this(new HashSet<String>(
				Util.readLinesIntoList(DEFAULT_STOPWORD_FNAME)));
	}

	TermRecognizer(Set<String> stopwords) {
		this(stopwords, DEFAULT_DELIMITERS);
	}

	TermRecognizer(Set<String> stopwords, String delimiters) {
		this.stopwords = stopwords;
		this.delimiters = delimiters;
	}

	Map<String, Integer> termFrequency(String text) {
		StringTokenizer st = new StringTokenizer(text, delimiters);
		Map<String, Integer> freq = new HashMap<String, Integer>();
		while (st.hasMoreTokens()) {
			String term = st.nextToken();
			// skip non-words
			if ((term.length() == 1)
					&& (!Character.isLetter(term.charAt(0))))
				continue;
			if ((term.length() == 2)
					&& (!Character.isLetter(term.charAt(0)))
					&& (!Character.isLetter(term.charAt(1))))
				continue;
			// skip stopwords
			if (stopwords.contains(term))
				continue;

			if (freq.containsKey(term)) {
				freq.put(term, freq.get(term) + 1);
			} else {
				freq.put(term, 1);
			}
		}
		return freq;
	}
}
