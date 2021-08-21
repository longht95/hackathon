package sql.generator.hackathon.create.main;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestMain {

	public static void main(String[] args) throws JsonProcessingException {
//		 SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
//		Date d1;
//		try {
//			d1 = sdformat.parse("2019-04-15");
//			Date d2 = sdformat.parse("2019-08-10");
//			System.out.println("The date 1 is: " + sdformat.format(d1));
//			System.out.println("The date 2 is: " + sdformat.format(d2));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		// Create ObjectMapper object.
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        List<List<String>> test = new ArrayList<>();
        List < String > progLangs = new ArrayList < > ();
        progLangs.add("C");
        progLangs.add("C++");
        progLangs.add("Java");
        progLangs.add("Java EE");
        progLangs.add("Python");
        progLangs.add("Scala");
        progLangs.add("JavaScript");
        List < String > progLangs2 = new ArrayList < > ();
        progLangs2.add("tes1");
        progLangs2.add("test2");
        
        test.add(progLangs);
        test.add(progLangs2);
        
        // Serialize Object to JSON.
        String json = mapper.writeValueAsString(test);

        // Print json
        System.out.println(json);
	}
}
