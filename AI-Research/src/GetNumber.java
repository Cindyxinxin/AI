import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import number.UnitConverter;

class GetNumber {
	final static File folder = new File(
			"/home/xin/Documents/AI-Research/output");
	public static int count = 0; // count the total number of files processed
	public static int validCount = 0; // count the total number of valid files
	public static Set<String> keyword; // e.g. length, size, dimension
	public static Set<String> blacklist; // e.g download, file
	public static Set<String> badUnit; // e.g. kb, mb, bit
	public static OutputStream out; // output stream
	public static UnitConverter converter;

	public static void main(String[] args) throws IOException {
		out = new FileOutputStream("/home/xin/Documents/AI-Research/output.txt");
		converter = new UnitConverter("meters");

		addBlackList();
		addKey();
		addBadUnit();
		listFilesForFolder(folder);
		System.out.println("The total number of files is " + count);
		System.out.println("The total number of valid files is " + validCount);
		System.out.println("The percentage of valid file is " + validCount
				* 1.0 / count);
		out.close();
	}

	public static void addBadUnit() {
		badUnit = new HashSet<String>();
		badUnit.add("kb");
		badUnit.add("mb");
		badUnit.add("mp3");
		badUnit.add("bit");
//		 blacklist.addAll(badUnit);
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
	
	public static boolean containBadUnit(String s) {
		for (String unit : badUnit) {
			if (s.toLowerCase().contains(unit)) {
				return true;
			}
		}
		return false;
	}

	public static void listFilesForFolder(final File folder) throws IOException {
		// check if it is a directory
		for (final File fileEntry : folder.listFiles()) {
			System.out.println("count + " + count);
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				/*
				 * this is a regular file A file is valid if: 1. Only English
				 * words (Todo) 2. Has a header line (i.e. has more than two
				 * lines in the file, TBD) 3. The first word in the header line
				 * is not keyword (just check token[0]) 4. The header contains
				 * at least one keyword and no blacklist 5. At least one column
				 * for the keywords has numbers; At least one column of the
				 * numbers can pass the UnitConverter (i.e. not return -1) 6.
				 * ... (TBD)
				 * 
				 * Notice: if can find the header, it does not necessarily mean
				 * that the file is valid, still need to check the blacklist and
				 * the unit
				 */
				boolean validFile = true;
				// a map of <column index, list of content at each line>
				Map<Integer, List<String>> memo = new HashMap<Integer, List<String>>();
				// for each file, only [process the one ends with .csv]
				if (fileEntry.isFile() && fileEntry.getName().endsWith(".csv")) {
					System.out.println(fileEntry.getName());
					count++; // record the total number of files
					InputStream in = new FileInputStream(fileEntry);
					Scanner s = new Scanner(in);
					String header = StringEscapeUtils.unescapeHtml(s.nextLine());
					// tokens contains all the elements in the headline
					String[] tokens = header.split(",");
					// rule 3
					// The first word in the header line is not keyword (just
					// check token[0])
					for (String key : keyword) {
						if (tokens[0].toLowerCase().contains(key)) {
							validFile = false;
							break;
						}
					}
					if (!validFile) {
						continue;
					}

					// rule 4
					// The header contains at least one keyword and no blacklist

					// contain blacklist word, not valid
					outerLoop1: for (String blackWord : blacklist) {
						for (String head : tokens) {
							if (head.toLowerCase().contains(blackWord)) {
								validFile = false;
								break outerLoop1;
							}
						}
					}
					if (!validFile)
						continue;

					for (int i = 0; i < tokens.length; i++) {
						for (String key : keyword) {
							if (tokens[i].toLowerCase().contains(key)) {
								if (!memo.containsKey(i)) {
									memo.put(i, new ArrayList<String>());
									memo.get(i).add(tokens[i]);
								}
								break;
							}
						}
					}

					if (memo.keySet().size() == 0) {
						continue;
					}

					// Todo: need to proccess all the data
					outerloop2: while (s.hasNextLine()) {
						// Test 1: check if the invald unit exists in the line
						// and rule out the file if true
						String currentLine = StringEscapeUtils.unescapeHtml(s.nextLine());
						for (String blackWord : blacklist) {
							if (currentLine.toLowerCase().contains(blackWord)) {
								validFile = false;
								break outerloop2;
							}
						}
						String[] contents = currentLine.split(",");
						// wanna put all the column on the memo
						for (Integer i : memo.keySet()) {
							// consider the out of bound exception
							if (i < contents.length)
								memo.get(i).add(contents[i]);
						}
					}

					if (!validFile)
						continue;

					/*
					 * Rule 5. At least one column for the keywords has numbers;
					 * At least one column of the numbers can pass the
					 * UnitConverter (i.e. not return -1)
					 */
					// check this is do not break out of the for loop below
					// i.e. not a whole column all contains number

					boolean lastColumnResult = false;
					for (Integer i : memo.keySet()) {
						List<String> currentContent = memo.get(i);
						boolean allContainNumber = true;
						for (int j = 1; j < currentContent.size(); j++) {
							Pattern q = Pattern.compile("(\\d+(.\\d+)?)");
							Matcher n = q.matcher(currentContent.get(j));
							if (!n.find()) {
								allContainNumber = false;
								break;
							} else if (containBadUnit(currentContent.get(j))) {
								allContainNumber = false;
								break;
							}
						}
						// if all contain number (at least one column contains
						// number)
						// this file is valid
						lastColumnResult = allContainNumber;
						if (lastColumnResult) {
							break;
						}
					}
					// not a valid file, escape
					if (!lastColumnResult) {
						continue;
					}
					// Todo: pass the conventor

					// print the file name if it is valid
					validCount++;
					out.write(fileEntry.getName().getBytes());
					out.write("\n".getBytes());
				}
			}
		}
	}
}
