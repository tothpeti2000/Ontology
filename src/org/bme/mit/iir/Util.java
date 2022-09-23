package org.bme.mit.iir;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {

	public static String readFileAsString(String fname) throws IOException {
		return join(readLinesIntoList(fname), "\n");
	}

	public static List<String> readLinesIntoList(String fname) throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(fname), Charset.forName("ISO-8859-1")));
		try {
			ArrayList<String> lines = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} finally {
			br.close();
		}
	}

	public static <T> String join(final Iterable<T> objs, final String delimiter) {
		Iterator<T> iter = objs.iterator();
		if (!iter.hasNext())
			return "";
		StringBuilder sb = new StringBuilder(String.valueOf(iter.next()));
		while (iter.hasNext())
			sb.append(delimiter).append(String.valueOf(iter.next()));
		return sb.toString();
	}
}
