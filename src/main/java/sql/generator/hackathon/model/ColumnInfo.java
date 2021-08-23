package sql.generator.hackathon.model;

public class ColumnInfo {
	public String name;
	public String val;
	public String typeName;
	public String typeValue;
	public boolean isNull;
	public boolean isPrimarykey;
	public boolean isForeignKey;
	public boolean unique;
	
	public ColumnInfo() {
		
	}
	
	public ColumnInfo(String name, String val) {
		this.name = name;
		this.val = val;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getTypeValue() {
		return typeValue;
	}
	public void setTypeValue(String typeValue) {
		this.typeValue = typeValue;
	}
	public Boolean getIsNull() {
		return isNull;
	}
	public void setIsNull(Boolean isNull) {
		this.isNull = isNull;
	}
	public Boolean getIsPrimarykey() {
		return isPrimarykey;
	}
	public void setIsPrimarykey(Boolean isPrimarykey) {
		this.isPrimarykey = isPrimarykey;
	}
	public Boolean getIsForeignKey() {
		return isForeignKey;
	}
	public void setIsForeignKey(Boolean isForeignKey) {
		this.isForeignKey = isForeignKey;
	}
	public Boolean getUnique() {
		return unique;
	}
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}
	
	public Boolean isKey() {
		return isPrimarykey || isForeignKey;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}
}
