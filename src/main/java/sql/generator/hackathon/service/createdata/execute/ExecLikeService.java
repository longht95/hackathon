package sql.generator.hackathon.service.createdata.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sql.generator.hackathon.model.createdata.constant.Constant;

public class ExecLikeService {
	
	private List<String> operators = Arrays.asList("_", "%");
	
	public List<String> processLike(String value) {
		if (value == null) {
			return new ArrayList<>(); 
		}
		
		// Find other character in operators has define.
		long c = operators.stream().filter(x -> value.contains(x)).count();
		if (c <= 0) {
			System.out.println("Error operators in LIKE condition");
			return new ArrayList<>();
		}
		
		String[] arr = value.split("");
		return new ArrayList<>();
	}
	
	private void processCreateValueForUnderLine(List<String> currentValue) {
		
	}
}
