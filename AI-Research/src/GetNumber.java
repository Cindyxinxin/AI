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

import number.UnitConverter;
import number.NumberTranslator;

import org.apache.commons.lang.StringEscapeUtils;

class GetNumber {
	final static File folder = new File(
			"/home/xin/Documents/AI-Research/output");
	public static int count = 0; // count the total number of files processed
	public static int validCount = 0; // count the total number of valid files
	public static Set<String> keyword; // e.g. length, size, dimension
	public static Set<String> blacklist; // e.g download, file
	public static Set<String> badUnit; // e.g. kb, mb, bit
	public static OutputStream out; // output stream
	public static OutputStream out_invalid;
	public static UnitConverter converter;
	public static Map<String, List<String>> output;
	public static Set<Integer> m;

	public static void main(String[] args) throws IOException {
		out = new FileOutputStream("/home/xin/Documents/AI-Research/output.txt");
		out_invalid = new FileOutputStream(
				"/home/xin/Documents/AI-Research/output_invalid.txt");
		converter = new UnitConverter("meters");
		// output = new HashMap<String, List<String>>();

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
		badUnit.add(":");
		badUnit.add("-");
		badUnit.add("kg");
		badUnit.add("/");
		badUnit.add("%");
		badUnit.add("hrs");
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
		keyword.add("width");
	}

	public static boolean containBadUnit(String s) {
		for (String unit : badUnit) {
			if (s.toLowerCase().contains(unit)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isWhitespace(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	// only number and times
	public static boolean onlyNumber(String str) {
		String temp = str.toLowerCase();
		temp = temp.replace("'", "");
		temp = temp.replace(" ", "");
		boolean atleastOneAlpha = temp.matches(".*[a-zA-Z]+.*");
		return isNumeric(temp) || !atleastOneAlpha || temp.contains("x");
	}

	public static void listFilesForFolder(final File folder) throws IOException {
		// check if it is a directory
		outerloop: for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {

				// This is a regular file.
				// A file is valid if:
				// 1. Only English words (Todo)
				// 2. Has a header line (i.e. has more than two lines in the
				// file, TBD)
				// 3. The first word in the header line is not keyword (just
				// check token[0])
				// 4. The header contains at least one keyword and no blacklist
				// 5. At least one column for the keywords has numbers; At least
				// one column of the
				// numbers can pass the UnitConverter (i.e. not return -1)
				// 6. if the first column is sequence of numbers, not valid
				// 7. test if the column with the number pass the converter test
				//
				// Notice: if can find the header, it does not necessarily mean
				// that the file is valid, still need to check the blacklist and
				// the unit
				boolean validFile = true;
				// a map of <column index, list of content at each line>
				Map<Integer, List<String>> memo = new HashMap<Integer, List<String>>();
				m = new HashSet<Integer>();
				// for each file, only [process the one ends with .csv]
				if (fileEntry.isFile() && fileEntry.getName().endsWith(".csv")) {
					// System.out.println(fileEntry.getName() + count);
					count++; // record the total number of files
					InputStream in = new FileInputStream(fileEntry);
					Scanner s = new Scanner(in);
					String header = StringEscapeUtils
							.unescapeHtml(s.nextLine());
					// tokens contains all the elements in the headline
					String[] tokens = header.split(",");

					// rule 3
					// The first word in the header line is not keyword (just
					// check token[0])
					// or if it contains #, just ignore
					// if the length > 20, continue
					if (tokens.length < 2 || tokens[0].length() > 20) {
						in.close();
						s.close();
						out_invalid.write(fileEntry.getName().getBytes());
						// out_invalid.write("\n".getBytes());
						continue outerloop;
					}

					for (String key : keyword) {
						if (tokens[0].toLowerCase().contains(key)
								|| tokens[0].contains("[0-9]+")) {
							in.close();
							s.close();
							out_invalid.write(fileEntry.getName().getBytes());
							// out.write("\n".getBytes());
							continue outerloop;
						}
					}

					// rule 4
					// The header contains at least one keyword and no blacklist

					// contain blacklist word, not valid
					for (String blackWord : blacklist) {
						for (String head : tokens) {
							if (head.toLowerCase().contains(blackWord)) {
								in.close();
								s.close();
								out_invalid.write(fileEntry.getName()
										.getBytes());
								// out.write("\n".getBytes());
								continue outerloop;
							}
						}
					}

					m.add(0);
					for (int i = 0; i < tokens.length; i++) {
						memo.put(i, new ArrayList<String>());
						memo.get(i).add(tokens[i]);
						for (String key : keyword) {
							if (tokens[i].toLowerCase().contains(key)) {
								m.add(i);
							}
						}
					}

					// need at least two column
					if (m.size() < 2) {
						in.close();
						s.close();
						out_invalid.write(fileEntry.getName().getBytes());
						// out.write("\n".getBytes());
						continue outerloop;
					}

					// Add the first column to find the target object
					// if (!memo.keySet().contains(0))
					// memo.put(0, new ArrayList<String>());
					outerloop2: while (s.hasNextLine()) {
						// Test 1: check if the invald unit exists in the line
						// and rule out the file if true
						String currentLine = StringEscapeUtils.unescapeHtml(s
								.nextLine());
						for (String blackWord : blacklist) {
							if (currentLine.toLowerCase().contains(blackWord)) {
								validFile = false;
								break outerloop2;
							}
						}
						String[] contents = currentLine.split(",");
						for (int i = 0; i < contents.length; i++) {
							if (i < memo.keySet().size()) {
								memo.get(i).add(contents[i]);
							}
						}
					}

					if (!validFile) {
						in.close();
						s.close();
						out_invalid.write(fileEntry.getName().getBytes());
						// out.write("\n".getBytes());
						continue outerloop;
					}

					/*
					 * Rule 5. At least one column for the keywords has numbers;
					 * At least one column of the numbers can pass the
					 * UnitConverter (i.e. not return -1)
					 */
					Set<Integer> index_with_number = new HashSet<Integer>();
					loop: for (Integer i : m) {
						index_with_number.add(i);
						// vertical table.....
						if (!memo.keySet().contains(i)) {
							in.close();
							s.close();
							out_invalid.write(fileEntry.getName().getBytes());
							// out.write("\n".getBytes());
							continue outerloop;
						}
						List<String> currentContent = memo.get(i);
						for (int j = 1; j < currentContent.size(); j++) {
							// Pattern q = Pattern.compile("(\\d+(.\\d+)?)");
							Pattern q = Pattern.compile("\\d+(.\\d+)?");
							Matcher n = q.matcher(currentContent.get(j));
							if (!n.find()) {
								index_with_number.remove(i);
								out_invalid.write(fileEntry.getName()
										.getBytes());
								// out.write("\n".getBytes());
								continue loop;
							} else if (containBadUnit(currentContent.get(j))) {
								index_with_number.remove(i);
								out_invalid.write(fileEntry.getName()
										.getBytes());
								// out.write("\n".getBytes());
								continue loop;
							}
						}
					}
					// not a valid file, escape
					if (index_with_number.size() < 2) {
						in.close();
						s.close();
						out_invalid.write(fileEntry.getName().getBytes());
						// out.write("\n".getBytes());
						continue outerloop;
					}
					m = index_with_number;

					// Rule 6. If the first column is sequence of numbers, not
					// valid
					// when seeing "#", suppose this is the number, switch to
					// the next column

					List<String> targetObject = memo.get(0);
					int header_index = 0;
					if (targetObject.size() > 0) {
						String firstHeader = targetObject.get(0);
						if (firstHeader == null || firstHeader.length() < 2) {
							in.close();
							s.close();
							out_invalid.write(fileEntry.getName().getBytes());
							// out.write("\n".getBytes());
							continue outerloop;
						}
						firstHeader = firstHeader.substring(1,
								firstHeader.length() - 1);
						firstHeader = firstHeader.toLowerCase();
						boolean allContainNumber = true;
						if (firstHeader.contains("n/a")
								|| firstHeader.contains("#")
								|| firstHeader.contains("null")
								|| isWhitespace(firstHeader)) {
							for (int i = 1; i < targetObject.size(); i++) {
								String target = targetObject.get(i);
								if (target == null || target.length() < 2) {
									break;
								}
								target = target.substring(1,
										target.length() - 1);
								target = target.toLowerCase();
								if (target.contains("n/a")
										|| target.contains("#")
										|| target.contains("null")
										|| isWhitespace(target)) {
									continue;
								}

								if (!onlyNumber(target)) {
									allContainNumber = false;
									break;
								}

							}
							if (allContainNumber) {
								m.remove(0);
								m.add(1);
								header_index = 1;
								targetObject = memo.get(1);
							}
						}
					}


					// 7. test if the column with the number pass the converter
					// test

					Set<Integer> temp = new HashSet<Integer>();
					m.remove(header_index);
					for (Integer i : m) {
						temp.add(i);
						List<String> current = memo.get(i);
						if (!memo.keySet().contains(i)) {
							in.close();
							s.close();
							temp.clear();
							break;
						}
						boolean passConverter = true;
						for (int index = 1; index < current.size(); index++) {
							// pass the converter test
							// 0. if number only, just print
							// 1. parse by the last space
							// 2. contains # in the first token in the header
							String content = current.get(index);
							content = content
									.substring(1, content.length() - 1);
							content = content.trim();
							if (!onlyNumber(content)) {
								// if not pure number
								// case 1: split by " ", if size less than 2,
								// break
								String[] split_content = content.split(" ");
								if (split_content.length < 2) {
									passConverter = false;
									break;
								} else {
									// case 2: parse by the last space
									String unit = split_content[split_content.length - 1]
											.trim();
									String number = split_content[0].trim();
									for (int t = 1; t < split_content.length - 1; t++) {
										number += split_content[t].trim();
									}
									double num = NumberTranslator
											.translate(number);
									if (num == Double.NaN) {
										passConverter = false;
										break;
									} else {
										// pass it into the converter
										System.out.println("number is "
												+ number);
										double result = converter.convert(num,
												unit);
										if (result == -1.0) {
											// output.remove(current.get(0));
											passConverter = false;
											break;
										}
									}
								}
							}
						}
						// if does not pass the converter, remove from the memo
						if (!passConverter) {
							temp.remove(i);
						}
					}

					if (temp.size() == 0) {
						in.close();
						s.close();
						out_invalid.write(fileEntry.getName().getBytes());
						// out.write("\n".getBytes());
						continue outerloop;
					}

					m = temp;
					m.add(header_index);

					in.close();
					s.close();
					// print the file name if it is valid
					validCount++;
					out.write(fileEntry.getName().getBytes());
					out.write("\n".getBytes());
				}
			}
		}
	}
}
