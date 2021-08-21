package sql.generator.hackathon.model;

public class ColumnInfo {
	public String name;
	public String typeName;
	public int typeValue;
	public String val;
	public boolean isPrimaryKey;
	public boolean isForeignKey;
	
	public ColumnInfo() {
		
	}
	
	public ColumnInfo(String name, String val) {
		this.name = name;
		this.val = val;
	}
	
	public ColumnInfo(String name, String typeName, String val) {
		this.name = name;
		this.typeName = typeName;
		this.val = val;
	}
	
	public ColumnInfo(String name, String typeName, int typeValue, String val) {
		this.name = name;
		this.typeName = typeName;
		this.typeValue = typeValue;
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
		return this.name.equals(ci.name) && this.typeName.equals(ci.typeName) && this.val.equals(ci.val);
	}
	
	@Override
	public int hashCode() {
		int k = 31;
		return (this.name.hashCode() + this.val.hashCode()) * k;
	}
	
	public boolean isKey() {
		return this.isForeignKey || this.isPrimaryKey;
	}
}
