import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
			"/home/xin/Documents/AI-Research/part15001-20000");
	public static int count = 0;
	public static Set<String> keyword;

	public static void main(String[] args) throws FileNotFoundException {
		addKey();
		listFilesForFolder(folder);
	}

	public static void addKey() {
		keyword = new HashSet<String>();
		keyword.add("size");
		keyword.add("height");
		keyword.add("length");
		keyword.add("dimension");
	}

	public static void listFilesForFolder(final File folder)
			throws FileNotFoundException {
		// for every file
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				boolean findHeader = false;
				List<Integer> index = new ArrayList<Integer>();
				//List<String> memo = new ArrayList<String>();
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
						for (String key : keyword) {
							if (tokens[i].toLowerCase().contains(key)) {
								findHeader = true;
								index.add(i);
							}
						}
					}
					while (s.hasNextLine() && findHeader) {
						String str = s.nextLine();
						String[] t = str.split(",");
						// check for each column, if all the contents are numbers
						for (int i = 0; i < index.size(); i++) {
							Pattern q = Pattern.compile("(\\d+(.\\d+)?)");
							Matcher n = q.matcher(t[index.get(i)]);
							if (n.find()) {
								if (!memo.containsKey(index.get(i))) {
									memo.put(index.get(i), new ArrayList<String>());
								}
								memo.get(index.get(i)).add(t[index.get(i)]);
								allContainNumber.add(i, true);
							} else {
								allContainNumber.add(i, false);
							}
						}
					}
					for (int i = 0; i < index.size(); i++) {
						if (allContainNumber.get(i)) {
							System.out
									.println("------------------------------");
							System.out.println(tokens[index.get(i)]);
							System.out
									.println("------------------------------");
							for (String item : memo.get(index.get(i))) {
								System.out.println(item);
							}
						}
					}
					boolean usefulFile = false;
					for (Boolean useful : allContainNumber) {
						usefulFile |=  useful;
					}
					if (usefulFile) {
						System.out.println();
						System.out.println("file number: " + count);
						System.out.println("file name: "
								+ fileEntry.getName());
						System.out.println();
						System.out.println();
					}
				}
			}
		}
	}
}
