import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.Set;
import number.UnitConverter;
import number.NumberTranslator;
import number.TestNumberTranslator;

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
		System.out.println(NumberTranslator.translate("3 1/4 inches"));
//		UnitConverter converter = new UnitConverter("meters");
		// returns 3250 m
//		System.out.println(converter.convert(3.25, "km"));
		// returns -1
		// System.out.println(converter.convert(3.25, "kb"));
		/*
		 * System.out.println(NumberTranslator.translate("3 1/4"));
		 * System.out.println(NumberTranslator.translate("3 kb"));
		 * System.out.println(NumberTranslator.translate("3 1/4"));
		 * System.out.println(NumberTranslator.translate("~ 15"));
		 * System.out.println(NumberTranslator.translate("~twenty-seven"));
		 * System.out.println(NumberTranslator.translate("twenty-seven"));
		 * System
		 * .out.println(NumberTranslator.translate("eight seven one seven"));
		 * System.out.println(NumberTranslator.translate("eight to twelve"));
		 * System.out.println(NumberTranslator .translate(
		 * "two hundred and seven million thirteen thousand two hundred and ninety eight"
		 * )); System.out.println(NumberTranslator.translate("2e6"));
		 * System.out.println(NumberTranslator.translate("1/2"));
		 * System.out.println(NumberTranslator.translate("2 1/2"));
		 * System.out.println(NumberTranslator.translate("5,000"));
		 * System.out.println(NumberTranslator.translate("6-10"));
		 * System.out.println(NumberTranslator.translate("15mm"));
		 */
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
		while (s.hasNextLine()) {
			// out.write(s.nextLine().getBytes());
			out.write(StringEscapeUtils.unescapeHtml(s.nextLine()).getBytes());
			out.write("\n".getBytes());
		}
		out.write("\n".getBytes());
		out.write("\n".getBytes());
		out.write("\n".getBytes());
	}
}
