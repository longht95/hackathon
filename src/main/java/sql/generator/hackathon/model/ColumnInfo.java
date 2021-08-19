package sql.generator.hackathon.model;

public class ColumnInfo {
	public String name;
	public String dataType;
	public String val;
	
	public ColumnInfo() {
		
	}
	
	public ColumnInfo(String name, String dataType, String val) {
		this.name = name;
		this.dataType = dataType;
		this.val = val;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ColumnInfo)) {
			return false;
		}
		ColumnInfo ci = (ColumnInfo) o;
		return this.name.equals(ci.name) && this.dataType.equals(ci.dataType) && this.val.equals(ci.val);
	}
	
	@Override
	public int hashCode() {
		int k = 31;
		return (this.name.hashCode() + this.val.hashCode()) * k;
	}
}
