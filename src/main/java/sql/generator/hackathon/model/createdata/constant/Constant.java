package sql.generator.hackathon.model.createdata.constant;

import java.util.HashMap;
import java.util.Map;

public class Constant {
	public static final Map<String, Integer> priorityOperators = new HashMap<>();
	{
		priorityOperators.put("=", 1);
		priorityOperators.put("IN", 2);
		priorityOperators.put("LIKE", 3);
		priorityOperators.put(">=", 4);
		priorityOperators.put("<=", 5);
		priorityOperators.put(">", 6);
		priorityOperators.put("<", 7);
		priorityOperators.put("NOT IN", 8);
		priorityOperators.put("!=", 9);
		priorityOperators.put("<>", 10);
	}
	
	public static final String EXPRESSION_EQUALS = "=";
	public static final String EXPRESSION_IN = "IN";
	public static final String EXPRESSION_LIKE = "LIKE";
	public static final String EXPRESSION_GREATER_EQUALS = ">=";
	public static final String EXPRESSION_LESS_EQUALS = "<=";
	public static final String EXPRESSION_GREATER = ">";
	public static final String EXPRESSION_LESS = "<";
	public static final String EXPRESSION_NOT_IN = "NOT IN";
	public static final String EXPRESSION_DIFF_1 = "!=";
	public static final String EXPRESSION_DIFF_2 = "<>";

	public static final char DEFAULT_CHAR = 'A';
	
	public static final int DEFAULT_LENGTH = 9;
}
