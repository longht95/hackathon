package sql.generator.hackathon.service.createdata;

import java.text.SimpleDateFormat;
import java.util.Date;

import sql.generator.hackathon.model.createdata.constant.Constant;

public class CommonService {

	/**
	 * All data type will to 3 dataType below
	 * number - char - date
	 * @param dataType
	 * @return
	 */
	public static String getCommonDataType(String dataType) {
		String res = "";
		switch(dataType) {
		case "number":
		case "int":
		case "bigint":
			res = "number";
			break;
		case "char":
		case "nchar":
		case "varchar":
		case "nvarchar":
			res = "char";
			break;
		case "date":
			res = "date";
			break;
		default:
			res = "unknow";
		}
		return res;
	}
	
	/**
	 * Convert String to int
	 * When Numberformatexception will get default length
	 * @param val
	 * @return
	 */
	public static int convertLength(String val) {
		int len;
		try {
			len = Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return Constant.DEFAULT_LENGTH;
		}
		return len;
	}
	
	/**
	 * Convert string to date
	 * Format will get from input value
	 */
	@SuppressWarnings("finally")
	public static Date convertStringToDate(String input) {
		Date res = new Date();
		try {
			String format = readFormatDate(input);
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			res = sdf.parse(input);
		} catch (java.text.ParseException e) {
			return res;
		} finally {
			// Get current date
			SimpleDateFormat sdf = new SimpleDateFormat(Constant.DEFAULT_FORMAT_DATE);
			Date date = new Date();
			try {
				return sdf.parse(sdf.format(date));
			} catch (java.text.ParseException e) {
				return res;
			}
		}
	}

	/**
	 * Convert Date to string 
	 * Format will get from input value
	 */
	public static String convertDateToString(String format, Date input) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(input);
	}
	/**
	 * Convert string to int
	 */
	public static int convertStringToInt(String input) {
		int res;
		try {
			res = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return Constant.DEFAULT_LENGTH;
		}
		return res;
	}
	
	
	/**
	 * Read format date from input string
	 * @param input
	 * @return
	 */
	public static String readFormatDate(String input) {
		// TODO
		return Constant.DEFAULT_FORMAT_DATE;
	}
}
