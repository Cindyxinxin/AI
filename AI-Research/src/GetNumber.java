import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GetNumber {
	final static File folder = new File(
			"/home/xin/Documents/AI-Research/output");
	public static int count = 0;
	public static int validCount = 0;
	public static Set<String> keyword;
	public static Set<String> blacklist;
	public static Set<String> badUnit;
	public static OutputStream out;

	public static void main(String[] args) throws IOException {
		//
		out = new FileOutputStream("/home/xin/Documents/AI-Research/output.txt");
		addBlackList();
		addKey();
		addBadUnit();
		listFilesForFolder(folder);
		System.out.println("The percentage of valid file is " + validCount
				* 1.0 / count);
		out.close();
	}

	public static void addBadUnit() {
		badUnit = new HashSet<String>();
		badUnit.add("kb");
		badUnit.add("mb");
		badUnit.add("$");
		badUnit.add("mp3");
	}

	public static void addBlackList() {
		blacklist = new HashSet<String>();
		blacklist.add("download");
		blacklist.add("bit");
		blacklist.add("file");
	}

	public static void addKey() {
		keyword = new HashSet<String>();
		keyword.add("size");
		keyword.add("height");
		keyword.add("length");
		keyword.add("dimension");
	}

	public static void listFilesForFolder(final File folder) throws IOException {
		// for every file
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				boolean findHeader = false;
				boolean notValidFile = false;
				List<Integer> index = new ArrayList<Integer>();
				// key is column number
				Map<Integer, List<String>> memo = new HashMap<Integer, List<String>>();
				List<Boolean> allContainNumber = new ArrayList<Boolean>();

				if (fileEntry.isFile() && fileEntry.getName().endsWith(".csv")) {
					count++;
					InputStream in = new FileInputStream(fileEntry);

					@SuppressWarnings("resource")
					Scanner s = new Scanner(in);
					String header = s.nextLine();
					String[] tokens = header.split(",");
					for (int i = 0; i < tokens.length; i++) {
						index.add(0);
						allContainNumber.add(true);
					}
					for (String key : keyword) {
						if (tokens[0].toLowerCase().contains(key))
							notValidFile = true;
					}
					if (!notValidFile) {
						for (int i = 0; i < tokens.length; i++) {
							boolean breakOut = false;
							// if contain the key word, eg size
							for (String key : keyword) {
								if (tokens[i].toLowerCase().contains(key)) {
									findHeader = true;
									index.set(i, 1);
								}
							}
							// if contains "download"
							for (String black : blacklist) {
								if (tokens[i].toLowerCase().contains(black)) {
									findHeader = false;
									for (int j = 0; j < index.size(); j++) {
										index.set(j, 0);
									}
									breakOut = true;
								}
							}
							// test if contains the word in the blacklist, break
							if (breakOut)
								break;
						}
						// when there are input and the file has header
						// search
						while (s.hasNextLine() && findHeader) {
							String str = s.nextLine();
							String[] t = str.split(",");
							// check for each column, if all the contents
							// contains
							// numbers
							for (int i = 0; i < tokens.length && i < t.length; i++) {
								// Todo: need to modify how to remove the bad unit in other non-letter places
								Pattern q = Pattern.compile("(\\d+(.\\d+)?)");
								Matcher n = q.matcher(t[i]);
								if (n.find() && allContainNumber.get(i)) {
									// another condition
									// if contains the mb... kb ..... etc
									boolean breakOut = false;
									for (String unit : badUnit) {
										if (t[i].toLowerCase().contains(unit)) {
											allContainNumber.set(i, false);
											breakOut = true;
											break;
										}
									}
									if (!breakOut) {
										if (!memo.containsKey(i)) {
											memo.put(i, new ArrayList<String>());
										}
										memo.get(i).add(t[i]);
									}
								} else {
									allContainNumber.set(i, false);
								}
							}
						}

						// print the header and the content
						// for (int i = 0; i < tokens.length; i++) {
						// if (allContainNumber.get(i) && index.get(i) == 1) {
						// System.out
						// .println("------------------------------");
						// System.out.println(tokens[i]);
						// System.out
						// .println("------------------------------");
						// if (memo.containsKey(i))
						// for (String item : memo.get(i)) {
						// System.out.println(item);
						// }
						// }
						// }

						// check again to see if this is a valid file
						// i.e. if there is a header that has been printed
						boolean usefulFile = false;
						for (int i = 0; i < tokens.length; i++) {
							if (index.get(i) == 1) {
								usefulFile |= allContainNumber.get(i);
							}
						}
						// print the file name if it is valid
						if (usefulFile) {
							validCount++;
							out.write(fileEntry.getName().getBytes());
							out.write("\n".getBytes());
							// System.out.println();
							// System.out.println("file number: " + count);
							// System.out.println("file name: " +
							// fileEntry.getName());
							// System.out.println("this is the " + validCount +
							// " valid file");
							// System.out.println();
							// System.out.println();
						}
					}
				}
			}
		}
	}
}
