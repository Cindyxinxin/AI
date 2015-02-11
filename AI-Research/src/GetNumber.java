import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

public class ShowContent {
	final static File folder = new File(
			"/home/xin/Documents/AI-Research/output");
	public static int count = 0;
	public static int validCount = 0;
	public static Set<String> keyword;
	public static OutputStream out;
	public static InputStream in;
	public static File fileName = new File(
			"/home/xin/Documents/AI-Research/output.txt");

	public static void main(String[] args) throws IOException {
		listFilesForFolder(folder);
		out.close();
	}

	public static void listFilesForFolder(File folder) throws IOException {
		out = new FileOutputStream(
				"/home/xin/Documents/AI-Research/out_content.txt");
		in = new FileInputStream(fileName);
		@SuppressWarnings("resource")
		Scanner s = new Scanner(in);
		while (s.hasNextLine()) {
			String filename = s.nextLine();
			out.write(filename.getBytes());
			out.write("\n".getBytes());
			printContent(new File("/home/xin/Documents/AI-Research/output/"
					+ filename));
		}
	}

	public static void printContent(File file) throws IOException {
		@SuppressWarnings("resource")
		Scanner s = new Scanner(file);
		int count = 0;
		while (s.hasNextLine() && count < 5) {
			count++;
			out.write(StringEscapeUtils.unescapeHtml(s.nextLine()).getBytes());
			out.write("\n".getBytes());
		}
		out.write("\n".getBytes());
		out.write("\n".getBytes());
		out.write("\n".getBytes());
	}
}
