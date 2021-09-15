package sql.generator.hackathon.service.createdata.execute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.createdata.ColumnCondition;
import sql.generator.hackathon.model.createdata.constant.Constant;
import sql.generator.hackathon.service.createdata.CommonService;

public class ExecOperatorsService {

	@Autowired
	private ExecInAndNotInService execInAndNotInService;
	
	@Autowired
	private ExecLikeService execLikeService;
	
	/**
	 * Get last values from list conditions for each column
	 * @param conditions (Key -> tablesName-aliasName-colName)
	 * @return
	 */
	public Map<String, List<String>> calcLastValue(Map<String, List<ColumnCondition>> mapCondition,
			Map<String, ColumnInfo> informTable) {
		HashMap<String, List<String>> res = new HashMap<>();
		mapCondition.entrySet().forEach(x -> {
			String tableColName = x.getKey();
			List<ColumnCondition> conditions = x.getValue();
			String dataType = CommonService.getCommonDataType(informTable.get(tableColName).getTypeName());
			int length = CommonService.convertLength(informTable.get(tableColName).getTypeValue());
			res.put(tableColName, processCalcValue(conditions, dataType, length));
		});
		return new HashMap<>();
	}
	
	/**
	 * Define compare priority for operators
	 * @param conditions
	 * @return
	 */
	public Comparator<ColumnCondition> processComparatorPriority() {
		Comparator<ColumnCondition> comparator = new Comparator<ColumnCondition>() {
			@Override
			public int compare(ColumnCondition o1, ColumnCondition o2) {
				return Constant.priorityOperators.get(o1.getExpression()) - Constant.priorityOperators.get(o2.getExpression()); 
			}
		};
		return comparator;
	}
	
	
	/**
	 * Calculator last value from list condition for 1 column
	 * @param conditions
	 * @param dataType
	 * @param length
	 * @return
	 */
	private List<String> processCalcValue(List<ColumnCondition> conditions, String dataType
			, int length) {
		List<String> lastValue = new ArrayList<>();
		
		boolean flgEquals = false;
		boolean flgIn = false;
		for(ColumnCondition x : conditions) {
			String expression = x.getExpression();
			List<String> values = x.getValues();
			switch (expression) {
			case Constant.EXPRESSION_EQUALS:
				flgEquals = true;
				break;
			case Constant.EXPRESSION_IN:
				flgIn = true;
				lastValue.addAll(execInAndNotInService.processExpressionIn(lastValue, values));
				break;
			case Constant.EXPRESSION_LIKE:
				if (!flgIn) {
					lastValue.addAll(execLikeService.processLike(values.get(0)));	
				}
				break;
			case Constant.EXPRESSION_GREATER_EQUALS: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					
				}
				break;
			case Constant.EXPRESSION_LESS_EQUALS: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					
				}
				break;
			case Constant.EXPRESSION_GREATER: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					
				}
				break;
			case Constant.EXPRESSION_LESS: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					
				}
				break;
			case Constant.EXPRESSION_NOT_IN:
				lastValue = execInAndNotInService.processNotIn(lastValue, values);
				break;
			case Constant.EXPRESSION_DIFF_1:
			case Constant.EXPRESSION_DIFF_2:
				lastValue = execInAndNotInService.processNotIn(lastValue, values.get(0));
				break;
			default:
				// Other expression?
				break;
			}
			
			// Stop when use expression equals
			if (flgEquals) {
				lastValue.add(x.getValues().get(0));
				break;
			}
		}
		
		return lastValue;
	}
	
	/**
	 * Confirm execute with dataType is number or date
	 * @param dataType
	 * @return
	 */
	private boolean checkOtherChar(String dataType) {
		return dataType.equals("number") || dataType.equals("date");
	}
}
