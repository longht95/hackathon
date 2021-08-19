package sql.generator.hackathon.model;

import java.util.List;

public class ConditionTest {

	public String left;
	public String operator;
	public String right; // Maybe is key or value or ("123", "456")
	public List<String> listRight; // use for IN or NOT IN
	
	public ConditionTest(String left, String operator, String right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}
}
