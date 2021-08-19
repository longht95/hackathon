package sql.generator.hackathon.controller;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestCreateController {

	@Autowired
    private EntityManagerFactory entityManagerFactory;

    public void testInsert() {
        EntityManager session = entityManagerFactory.createEntityManager();
        try {
            session.createNativeQuery("INSERT PERSON(first_name, last_name) values('Tuan', 'Test')")
                    .executeUpdate();

        }
        catch (NoResultException e){
        	e.printStackTrace();
            return;
        }
        finally {
            if(session.isOpen()) session.close();
        }
    }
	
	@RequestMapping(value = "/testCreate")
	public String testCreate() {
		// Test insert H2 with native query
		String tableName = "PERSON";
		String colNames = "first_name, last_name";
		String vals = "'TuanNVQ', 'Hello'";
		testInsert();
		return "index";
	}
}
