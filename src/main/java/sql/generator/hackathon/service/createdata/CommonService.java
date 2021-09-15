package sql.generator.hackathon.service.createdata;

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
}
