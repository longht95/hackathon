package sql.generator.hackathon.model;

import java.util.List;

public class InfoDisplayScreen {
	public List<List<String>> listData;
	public List<String> listColumnName;
	
	public List<List<String>> getListData() {
		return listData;
	}
	public void setListData(List<List<String>> listData) {
		this.listData = listData;
	}
	public List<String> getListColumnName() {
		return listColumnName;
	}
	public void setListColumnName(List<String> listColumnName) {
		this.listColumnName = listColumnName;
	}
}
