package sql.generator.hackathon.model;

import java.util.ArrayList;
import java.util.List;
public class TableSQL {
	public String tableName;
	public String alias;
	public List<ConditionTest> condition ;
	
	public TableSQL() {
		condition = new ArrayList<ConditionTest>();
	}

	@Override
	public String toString() {
		return "TableSQL [tableName=" + tableName + ", alias=" + alias + ", condition=" + condition + "]";
	}
	
}
