package sql.generator.hackathon.service.createdata;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.ObjectGenate;
import sql.generator.hackathon.model.ParseObject;
import sql.generator.hackathon.service.ExecuteDBSQLServer;
import sql.generator.hackathon.service.createdata.execute.ExecWhereService;

public class ServiceCreateData {

	@Autowired
	private ExecWhereService execWhereService;
	
	public ExecuteDBSQLServer dbService = new ExecuteDBSQLServer();
	
	/**
	 * Call first
	 * @param objectGenate
	 * @param parseObject
	 * @param dataPicker
	 * @param rowCreate
	 * @param flagInsert
	 * @throws SQLException
	 */
	public void processCreate(ObjectGenate objectGenate, ParseObject parseObject, 
			Map<String, List<List<ColumnInfo>>> dataPicker, int rowCreate, boolean flagInsert) throws SQLException {
		try {
			// Open connection
			openConnection(objectGenate);
			
			execWhereService.processWhere(parseObject);
		} catch (Exception e) {
		} finally {
			// Close connection
			dbService.disconnectDB();
		}
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
}
