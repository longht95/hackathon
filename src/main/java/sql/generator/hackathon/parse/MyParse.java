package sql.generator.hackathon.parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MyParse {

	public static void main(String[] args) {
		// Test
		MyParse myParse = new MyParse();
		
		String type = "text";
		String sql = "Select   * "
				+ "FROM   A "
				+ "WHere colum1 = 1";
		myParse.parse(type, sql);
	}

	/**
	 * 
	 * @param type (file or text)
	 * @param content (FileName when file, sqlContent when text)
	 */
	public void parse(String type, String content) {
		String sql;
		if (type.equals("file")) {
			sql = readContentSQL(content);
		} else {
			sql = content;
		}
		
		// format sql
		String formatSQL = formatSQL(sql);
		System.out.println(formatSQL);
	}
	
	/**
	 * 
	 * @param fileName
	 * @return content of file
	 */
	private String readContentSQL(String fileName) {
		StringBuilder res = new StringBuilder();
		
		try {
			String curLine;
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			while ((curLine = br.readLine()) != null) {
				res.append(curLine + "\n");
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Read file error!");
			e.printStackTrace();
		}
		return res.toString();
	}

	/**
	 * Format multiline SQL to one line
	 * @param sql
	 * @return formated sql
	 */
	private String formatSQL(String sql) {
		StringBuilder res = new StringBuilder();
		String[] mulLine = sql.split("\n");
		for (int i = 0; i < mulLine.length; ++i) {
			String line = mulLine[i].trim();
			String[] splitSpace = line.split("\\s+");
			for (int j = 0; j < splitSpace.length; ++j) {
				String txt = splitSpace[j].trim();
				res.append(txt + " ");
			}
		}
		return res.toString().trim();
	}
}
