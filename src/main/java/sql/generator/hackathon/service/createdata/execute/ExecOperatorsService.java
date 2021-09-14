package sql.generator.hackathon.service.createdata.execute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import sql.generator.hackathon.model.createdata.ColumnCondition;
import sql.generator.hackathon.model.createdata.constant.Constant;

public class ExecOperatorsService {

	@Autowired
	private ExecInAndNotInService execInAndNotInService;
	
	/**
	 * Get last values from list conditions for each column
	 * @param conditions (Key -> tablesName-aliasName-colName)
	 * @return
	 */
	public Map<String, List<String>> calcLastValue(Map<String, List<ColumnCondition>> mapCondition) {
		mapCondition.entrySet().forEach(x -> {
			String tableColName = x.getKey();
			List<ColumnCondition> conditions = x.getValue();
		});
		return new HashMap<>();
	}
	
	private List<String> processCalcValue(List<ColumnCondition> conditions) {
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
				execInAndNotInService.processExpressionIn(lastValue, values);
				break;
			case Constant.EXPRESSION_LIKE:
				break;
			case Constant.EXPRESSION_GREATER_EQUALS: // Just use dataType number, date
				break;
			case Constant.EXPRESSION_LESS_EQUALS: // Just use dataType number, date
				break;
			case Constant.EXPRESSION_GREATER: // Just use dataType number, date
				break;
			case Constant.EXPRESSION_LESS: // Just use dataType number, date
				break;
			case Constant.EXPRESSION_NOT_IN:
				break;
			case Constant.EXPRESSION_DIFF_1:
				break;
			case Constant.EXPRESSION_DIFF_2:
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
}
