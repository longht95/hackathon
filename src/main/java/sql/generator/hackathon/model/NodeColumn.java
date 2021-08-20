package sql.generator.hackathon.model;

public class NodeColumn {

	public String tableColumnName;
	public String val;
	public int index; 
	
	public NodeColumn(String tableColumnName, String val, int index) {
		this.tableColumnName = tableColumnName;
		this.val = val;
		this.index = index;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (! (o instanceof NodeColumn)) {
			return false;
		}
		
		NodeColumn node = (NodeColumn) o;
		return node.tableColumnName.equals(this.tableColumnName)  
				&& node.val.equals(this.val);
	}
	
	@Override
	public int hashCode() {
		int k = 31;
		return (this.tableColumnName.hashCode() + this.val.hashCode()) * k; 
	}
}
