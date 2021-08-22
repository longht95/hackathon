package sql.generator.hackathon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import sql.generator.hackathon.create.main.TestInsertDB;

@SpringBootApplication
public class HackathonApplication {
	
	public static void main(String[] args) throws JSQLParserException {
		Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM MY_TABLE1 tb1 INNER JOIN MY_TABLE2 tb2 ON tb1.column1 = tb2.column2 WHERE tb1.aa = '1'");
		PlainSelect plain = (PlainSelect) ((Select)stmt).getSelectBody();
		System.out.println(plain.getJoins().get(0).getRightItem().getAlias());
		System.out.println(plain.getJoins().get(0).getOnExpression());
		System.out.println(plain.getWhere());
		SpringApplication.run(HackathonApplication.class, args);
	}

}
