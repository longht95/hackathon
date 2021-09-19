package sql.generator.hackathon.service.createdata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.CreateObject;
import sql.generator.hackathon.model.InforTableReferFK;
import sql.generator.hackathon.model.ObjectGenate;
import sql.generator.hackathon.model.ParseObject;
import sql.generator.hackathon.model.createdata.InnerReturnObjectFrom;
import sql.generator.hackathon.model.createdata.InnerReturnObjectWhere;
import sql.generator.hackathon.model.createdata.ReturnObjectFrom;
import sql.generator.hackathon.model.createdata.ReturnObjectWhere;
import sql.generator.hackathon.model.createdata.constant.Constant;
import sql.generator.hackathon.service.ExecuteDBSQLServer;
import sql.generator.hackathon.service.ServerFaker;
import sql.generator.hackathon.service.createdata.execute.ExecClientService;
import sql.generator.hackathon.service.createdata.execute.ExecFromService;
import sql.generator.hackathon.service.createdata.execute.ExecWhereService;

public class ServiceCreateData {

	@Autowired
	private ExecWhereService execWhereService;
	
	@Autowired
	private ExecFromService execFromService;
	
	@Autowired
	private ExecClientService execClientService;
	
	@Autowired
	private ServerFaker fakerService;
	
	private Map<String, InforTableReferFK> foreignKeyNotExistsInmainTable;
	
	public static ExecuteDBSQLServer dbService = new ExecuteDBSQLServer();
	
	public static int indexColor;
	
	/**
	 * Call first
	 * @param objectGenate
	 * @param parseObject
	 * @param dataPicker
	 * @param rowCreate
	 * @param flagInsert
	 * @throws SQLException
	 */
	public CreateObject process(ObjectGenate objectGenate, ParseObject parseObject, 
			Map<String, List<List<ColumnInfo>>> dataPicker, int rowCreate, boolean flgInsert) throws SQLException {
		CreateObject response = new CreateObject();
		try {
			init(objectGenate, parseObject);
			
			ReturnObjectWhere objWhere = execWhereService.processWhere(parseObject);
			ReturnObjectFrom objFrom = execFromService.processFrom(parseObject, objWhere);
			
			Map<String, List<ColumnInfo>> lastValue = processCalcLastValue(objFrom, objWhere);
			
			response = processMultipleRow(lastValue, dataPicker, rowCreate, flgInsert);
		} catch (Exception e) {
		} finally {
			// Close connection
			if (dbService != null) {
				dbService.disconnectDB();
			}
		}
		return response;
	}
	
	private void init(ObjectGenate objectGenate, ParseObject parseObject) throws Exception {
		indexColor = 0;
		
		foreignKeyNotExistsInmainTable = new HashMap<>();
		// Open connection
		openConnection(objectGenate);
		CommonService.init(objectGenate, parseObject);
		execClientService.init(parseObject);
	}
	/**
	 * Open connection
	 * @param objectGenate
	 * @throws Exception
	 */
	private void openConnection(ObjectGenate objectGenate) throws Exception {
		dbService.connectDB(objectGenate.infoDatabase.getType(), objectGenate.infoDatabase.getUrl(), 
				objectGenate.infoDatabase.getSchema(), objectGenate.infoDatabase.getUser(), 
				objectGenate.infoDatabase.getPassword());
	}
	
	private Map<String, List<ColumnInfo>> processCalcLastValue(ReturnObjectFrom objFrom, ReturnObjectWhere objWhere) {
		Map<String, List<ColumnInfo>> res = new HashMap<>();
		
		CommonService.objCommon.getTableInfo().entrySet().forEach(x -> {
			String tableNameInfo = x.getKey();
			List<ColumnInfo> listColumnInfo = x.getValue();
			listColumnInfo.stream().forEach(y -> {
				String aliasTableInfo = y.tableAlias;
				String columnNameInfo = y.name;
				String tableAliasColumnName = tableNameInfo + Constant.STR_DOT + aliasTableInfo + Constant.STR_DOT + columnNameInfo;
				
				ColumnInfo resColumnInfo = new ColumnInfo();
				resColumnInfo.setName(columnNameInfo);
				String lastValue = "";
				String markColor = "";
				InnerReturnObjectFrom innerReturnObjFrom = objFrom.getMappingTableAliasColumn().get(tableAliasColumnName);
				if (innerReturnObjFrom != null) {
					if (!innerReturnObjFrom.getLastValue().isEmpty()) {
						lastValue = innerReturnObjFrom.getLastValue();
						markColor = innerReturnObjFrom.getMarkColor();
					} else if (!innerReturnObjFrom.getListValidValue().isEmpty()){
						lastValue = innerReturnObjFrom.getListValidValue().get(0);
						markColor = innerReturnObjFrom.getMarkColor();
					} else {
						throw new IllegalArgumentException("Not contains values for condition!");
					}
				} else {
					InnerReturnObjectWhere innerReturnObjWhere = objWhere.getValueMappingTableAliasColumn().get(tableAliasColumnName);
					if (innerReturnObjWhere != null) {
						if (!innerReturnObjWhere.getLastValue().isEmpty()) {
							lastValue = innerReturnObjWhere.getLastValue();
							markColor = innerReturnObjWhere.getMarkColor();
						} else if (!innerReturnObjWhere.getValidValueForColumn().isEmpty()){
							lastValue = innerReturnObjWhere.getValidValueForColumn().get(0);
							markColor = innerReturnObjWhere.getMarkColor();
						} else {
							throw new IllegalArgumentException("Not contains values for condition!");
						}
					}
				}
				resColumnInfo.setVal(lastValue);
				resColumnInfo.setColor(markColor);
				List<ColumnInfo> resListColumnInfo;
				if (res.containsKey(tableNameInfo)) {
					resListColumnInfo = res.get(tableNameInfo);
				} else {
					resListColumnInfo = new ArrayList<>();
					res.put(tableNameInfo, resListColumnInfo);
				}
				resListColumnInfo.add(resColumnInfo);
			});
		});
		return res;
	}
	
	private CreateObject processMultipleRow(Map<String, List<ColumnInfo>> lastValue,
			Map<String, List<List<ColumnInfo>>> dataPicker, int row, boolean flgInsert) {
		Map<String, List<List<ColumnInfo>>> responseListData = new HashMap<>();
		List<String> listMarkColor = new ArrayList<>();
		for (int idxRow = 1; idxRow <= row; ++idxRow) {
			Map<String, List<ColumnInfo>> dataOneRow = processOneRow(lastValue, dataPicker, idxRow, listMarkColor);
			dataOneRow.entrySet().forEach(m -> {
				String tableName = m.getKey();
				List<List<ColumnInfo>> t;
				if (responseListData.containsKey(tableName)) {
					t = responseListData.get(tableName);
				} else {
					t = new ArrayList<>();
					responseListData.put(tableName, t);
				}
				t.add(m.getValue());
			});
		}
		processWithForeignKey(responseListData);
		CreateObject createObj = new CreateObject();
		createObj.setListData(responseListData);
		createObj.setListMarkColor(listMarkColor);
		return createObj;
	}
	
	private Map<String, List<ColumnInfo>> processOneRow(Map<String, List<ColumnInfo>> lastValueTable,
			Map<String, List<List<ColumnInfo>>> dataPicker,
			int idxRow, List<String> listMarkColor) {
		Map<String, List<ColumnInfo>> res = new HashMap<>();
		CommonService.objCommon.getTableInfo().entrySet().forEach(e -> {
			String tableName = e.getKey();
			List<ColumnInfo> l = new ArrayList<>();
			Set<String> hasColumn = new HashSet<>();
			List<String> listAliasTable = new ArrayList<>();
			// Init
			for (ColumnInfo colInfo : e.getValue()) {
				if (!hasColumn.contains(colInfo.getName())) {
					ColumnInfo innerColumnInfo = new ColumnInfo(colInfo.getName(), "", colInfo.getTypeName(),
							colInfo.getTypeValue(), colInfo.getIsNull(), colInfo.getIsPrimarykey(),
							colInfo.getIsForeignKey(), colInfo.getUnique());
					innerColumnInfo.setTableAlias(colInfo.getTableAlias());
					listAliasTable.add(colInfo.getTableAlias());
					l.add(innerColumnInfo);
					hasColumn.add(colInfo.getName());
				}
			}
			
			List<ColumnInfo> data = lastValueTable.get(tableName);
			
			// confirm KEY no value
			ColumnInfo colNoVal = null;
						
			if (data != null && !data.isEmpty()) {
				for (ColumnInfo colInfo : l) {
					for (ColumnInfo d : data) {
						if (colInfo.getName().equals(d.getName()) && colInfo.getTableAlias().equals(d.getTableAlias())) {
							colInfo.setVal(CommonService.removeSpecifyCharacter("'", d.getVal()));
							colInfo.setColor(d.getColor());
						}
					}
					
					if (colInfo.isKey() && colInfo.getVal().isEmpty()) {
						colNoVal = colInfo;
					}
				}
			} 
			
			if (colNoVal != null) {
				Map<String, ColumnInfo> mapVal = new HashMap<>();
				try {
					mapVal = genValueForKey(tableName, colNoVal);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				for (ColumnInfo colInfo : l) {
					if (colInfo.isKey() && colInfo.getVal().isEmpty()) {
						colInfo.setVal(CommonService.removeSpecifyCharacter("'", mapVal.get(tableName + "." + colInfo.getName()).getVal()));
					}
				}
			}
			
			// Get unique val
			for (ColumnInfo colInfo : l) {
				if (colInfo.getUnique() && colInfo.getVal().isEmpty()) {
					try {
						colInfo.setVal(CommonService.removeSpecifyCharacter("'", dbService.genListUniqueVal(tableName, colInfo, "", "").get(0)));
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
			
			listAliasTable.stream().forEach(x -> execClientService.addColumnGetFromSelect(l, tableName, x));
			execClientService.setClientData(tableName, idxRow, l, dataPicker);
			
			// Add default value
			for (ColumnInfo colInfo : l) {
				if (colInfo.getVal().isEmpty()) {
					String dataType = colInfo.getTypeName().equals(Constant.STR_TYPE_DATE) ? Constant.STR_TYPE_DATE : ""; 
					colInfo.setVal(fakerService.getDataByColumn(colInfo.getName(), dataType));	
				}
			}
			
			// Check foreign key has exists
			for (ColumnInfo colInfo : l) {
				if (colInfo.getIsForeignKey()) {
					try {
						InforTableReferFK foreingKeyInfo = dbService.checkInforFK(
								CommonService.objCommon.getObjectGenate().getInfoDatabase().getSchema(), 
								CommonService.removeSpecifyCharacter("'", tableName), colInfo);
						if (!foreingKeyInfo.isHasExist()) {
							foreignKeyNotExistsInmainTable.put(foreingKeyInfo.getTableReferFKName(), foreingKeyInfo);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			
			// Set list markColor
			l.stream().filter(y -> !listMarkColor.contains(y.getColor())).forEach(y -> {
				listMarkColor.add(y.getColor());
			});
			
			res.put(tableName, l);
		});
		return res;
	}
	
	
	/**
	 * Gen value for key no condition
	 * @param tableName
	 * @param colInfo
	 * @return Map<String, ColumnInfo> => colName = Key
	 * @throws SQLException
	 */
	private Map<String, ColumnInfo> genValueForKey(String tableName, ColumnInfo colInfo) throws SQLException {
		Map<String, ColumnInfo> res = new HashMap<>();
		
		List<String> listVal = dbService.genListUniqueVal(tableName, colInfo, "", "");
		
		// Gen value for key with no condition
		if (!CommonService.isCompositeKey(tableName)) {
			res.put(tableName + "." + colInfo.name, new ColumnInfo(colInfo.name, listVal.get(0)));
		} else {
			for (String val : listVal) {
				Map<String, String> m = dbService.genUniqueCol(CommonService.objCommon.getObjectGenate().getInfoDatabase().getSchema(), tableName, colInfo, val);
				if (m.size() > 0) {
					res.put(tableName + "." + colInfo.name, new ColumnInfo(colInfo.name, val));
					for (Map.Entry<String, String> e : m.entrySet()) {
						res.put(e.getKey(), new ColumnInfo(e.getKey(), e.getValue()));
					}
					break;
				}
			}
		}
		return res;
	}
	
	/**
	 * Add value for main table when foreign key not exists
	 * @param reponseData
	 */
	private void processWithForeignKey(Map<String, List<List<ColumnInfo>>> reponseData) {
		foreignKeyNotExistsInmainTable.entrySet().forEach(e -> {
			String tableRefer = e.getKey();
			List<ColumnInfo> columnsRefer = e.getValue().getColumnInfoLst();
			if (reponseData.containsKey(tableRefer)) {
				Map<String, String> mapVal = new HashMap<>();
				for (ColumnInfo colInfo : columnsRefer) {
					ColumnInfo currentInfo = CommonService.getColumnInfo(tableRefer, colInfo.getName());
					if (currentInfo.getIsPrimarykey()) {
						mapVal.put(colInfo.getName(), colInfo.getVal());
					}
				}
				List<List<ColumnInfo>> t = reponseData.get(tableRefer);
				boolean hasExists = true;
				for (List<ColumnInfo> colsInfo : t) {
					int cnt = 0;
					for (ColumnInfo innerInfo : colsInfo) {
						if (innerInfo.getIsPrimarykey() && mapVal.get(innerInfo.getName()).equals(innerInfo.getVal())) {
							cnt++;
						}
					}
					if (cnt == mapVal.size()) {
						hasExists = false;
					}
				}
				if (hasExists) {
					t.add(columnsRefer);
				}
			} else {
				List<List<ColumnInfo>> t = new ArrayList<>();
				t.add(columnsRefer);
				reponseData.put(tableRefer, t);
			}
		});
	}
}
