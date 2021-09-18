package sql.generator.hackathon.service.createdata.execute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import sql.generator.hackathon.model.Condition;
import sql.generator.hackathon.model.ObjectMappingTable;
import sql.generator.hackathon.model.ParseObject;
import sql.generator.hackathon.model.TableSQL;
import sql.generator.hackathon.model.createdata.ColumnCondition;
import sql.generator.hackathon.model.createdata.ExpressionObject;
import sql.generator.hackathon.model.createdata.InnerReturnObjectWhere;
import sql.generator.hackathon.model.createdata.ReturnObjectWhere;
import sql.generator.hackathon.model.createdata.constant.Constant;

public class ExecWhereService {

	@Autowired
	private ExecExpressionService execExpressionService;
	
	// Key = tableName-aliasName
	private Map<String, List<ObjectMappingTable>> mappingTables;
	
	public ReturnObjectWhere processWhere(ParseObject parseObject) throws SQLException {
		init(parseObject);
		
		Map<String, ExpressionObject> validValueForColumn = execExpressionService.calcLastValue(mappingTables);
		
		// TODO
//		Map<String, List<String>> inValidValueForColumn = execExpressionService.getInValidValueForColumn(mappingTables);
		Map<String, List<String>> inValidValueForColumn = new HashMap<>();
		
		ReturnObjectWhere objWhere = new ReturnObjectWhere();
		processReturn(objWhere, validValueForColumn, inValidValueForColumn);
		return objWhere;
	}

	private void init(ParseObject parseObject) {
		List<TableSQL> tables = parseObject.getListTableSQL();
		getMappingTables(tables);
	}
	
	private void getMappingTables(List<TableSQL> tables) {
		tables.stream().forEach(x -> {
			String tableName = x.getTableName();
			String aliasName = x.getAlias();
			String tableAliasName = tableName + Constant.STR_DOT + aliasName;
			Set<String> columns = x.getColumns();
			List<Condition> conditions = x.getCondition();
			conditions.stream()
				.filter(cond -> cond.getRight() != null && !cond.getRight().startsWith(Constant.STR_KEYS))
				.forEach(cond -> {
				String left = cond.getLeft();
				String right = cond.getRight();
				String expression = cond.getExpression();
				List<String> listRight = cond.getListRight();
				List<String> groupValues = processSetValueForColumn(right, listRight);
				if (mappingTables.containsKey(tableAliasName)) {
					List<ObjectMappingTable> listMappingTable = mappingTables.get(tableAliasName);
					processWhenExistsKeyInMappingTables(listMappingTable, left, expression, groupValues);
				} else {
					List<ObjectMappingTable> listMappingTable = new ArrayList<>();
					processSetValueForMappingTables(listMappingTable, left, expression, groupValues);
					mappingTables.put(tableAliasName, listMappingTable);
				}
			});
		});
	}
	
	
	/**
	 * Process when exists key MappingTable variable
	 * @param listMappingTable
	 * @param columnName
	 * @param expression
	 * @param groupValues
	 */
	private void processWhenExistsKeyInMappingTables(List<ObjectMappingTable> listMappingTable,
			String columnName, String expression, List<String> groupValues) {
		// Find ObjectMappingTable in listMappingTable => left(columnName)
		boolean existsColumn = listMappingTable.stream().anyMatch(x -> x.getColumnName().equals(columnName));
		if (existsColumn) {
			ObjectMappingTable objMappingTable = listMappingTable.stream()
					.filter(x -> x.getColumnName().equals(columnName))
					.findFirst()
					.orElse(null);
			if (objMappingTable == null) {
				throw new IllegalArgumentException("Not found column name in list column!");
			}
			List<ColumnCondition> listColumnCondition = objMappingTable.getColumnsCondition();
			listColumnCondition.add(new ColumnCondition(expression, groupValues));
		} else {
			processSetValueForMappingTables(listMappingTable, columnName, expression, groupValues);
		}
	}
	
	/**
	 * Group value from right and listRight values
	 * @param value
	 * @param values
	 * @return
	 */
	private List<String> processSetValueForColumn(String value, List<String> values){
		List<String> res = new ArrayList<>();
		if (values != null && !values.isEmpty()) {
			res.addAll(values);
		} else {
			res.add(value);
		}
		return res;
	}
	
	/**
	 * Process Set Value for mapingTables variable
	 * @param listMappingTable
	 * @param columnName
	 * @param expression
	 * @param groupValues
	 */
	private void processSetValueForMappingTables(List<ObjectMappingTable> listMappingTable,
			String columnName, String expression, List<String> groupValues) {
		ObjectMappingTable objMappingTable = new ObjectMappingTable();
		objMappingTable.setColumnName(columnName);
		List<ColumnCondition> listColumnCondition = new ArrayList<>();
		listColumnCondition.add(new ColumnCondition(expression, groupValues));
		objMappingTable.setColumnsCondition(listColumnCondition);
		listMappingTable.add(objMappingTable);
	}
	
	private void processReturn(ReturnObjectWhere objWhere, 
			Map<String, ExpressionObject> validValueForColumn, 
			Map<String, List<String>> inValidValueForColumn) {
		
		validValueForColumn.entrySet().forEach(x -> {
			String tableAliasColumnName = x.getKey();
			ExpressionObject expressionObject = x.getValue(); 
			List<String> listValidValue = expressionObject.getListValidValue();
			String lastValue = expressionObject.getLastValue();
			if (objWhere.getValueMappingTableAliasColumn() == null) {
				Map<String, InnerReturnObjectWhere> valueMappingTableAliasColumn = new HashMap<>();
				objWhere.setValueMappingTableAliasColumn(valueMappingTableAliasColumn);
			}
			if (objWhere.getValueMappingTableAliasColumn().containsKey(tableAliasColumnName)) {
				throw new IllegalArgumentException("Duplicate process valid values!");
			}
			Map<String, InnerReturnObjectWhere> valueMappingTableAliasColumn = objWhere.getValueMappingTableAliasColumn();
			InnerReturnObjectWhere innerReturnObjectWhere = new InnerReturnObjectWhere();
			innerReturnObjectWhere.setValidValueForColumn(listValidValue);
			innerReturnObjectWhere.setLastValue(lastValue);
			innerReturnObjectWhere.setMarkColor(Constant.KEY_MARK_COLOR + Constant.STR_UNDERLINE + Constant.DEFAULT_NUM_MARK_COLOR);
			valueMappingTableAliasColumn.put(tableAliasColumnName, innerReturnObjectWhere);
		});
		
		inValidValueForColumn.entrySet().forEach(x -> {
			String tableAliasColumnName = x.getKey();
			List<String> listInValidValue = x.getValue(); 
			if (objWhere.getValueMappingTableAliasColumn() == null) {
				Map<String, InnerReturnObjectWhere> valueMappingTableAliasColumn = new HashMap<>();
				objWhere.setValueMappingTableAliasColumn(valueMappingTableAliasColumn);
			}
			if (objWhere.getValueMappingTableAliasColumn().containsKey(tableAliasColumnName)) {
				throw new IllegalArgumentException("Duplicate process valid values!");
			}
			if (objWhere.getValueMappingTableAliasColumn().containsKey(tableAliasColumnName)) {
				InnerReturnObjectWhere innerReturnObjectWhere = objWhere.getValueMappingTableAliasColumn().get(tableAliasColumnName);
				innerReturnObjectWhere.setInValidValueForColumn(listInValidValue);
			} else {
				Map<String, InnerReturnObjectWhere> valueMappingTableAliasColumn = objWhere.getValueMappingTableAliasColumn();
				InnerReturnObjectWhere innerReturnObjectWhere = new InnerReturnObjectWhere();
				innerReturnObjectWhere.setInValidValueForColumn(listInValidValue);
				valueMappingTableAliasColumn.put(tableAliasColumnName, innerReturnObjectWhere);
			}
		});
	}
}
