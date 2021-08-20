package sql.generator.hackathon.model;

public class Cond{
	public String operator;
	public String value;
	
	public Cond() {
		
	}
	
	public Cond(String operator, String value) {
		this.operator = operator;
		this.value = value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Cond)) {
			return false;
		}
		Cond obj = (Cond) o;
		return this.value.equals(obj.value);
	}
	
	@Override
	public int hashCode() {
		return operator.hashCode() + value.hashCode() * 31;
	}
}
