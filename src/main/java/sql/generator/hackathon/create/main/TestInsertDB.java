package sql.generator.hackathon.create.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestInsertDB {
	private static String DB_URL = "jdbc:h2:mem:testdb";
    private static String USER_NAME = "sa";
    private static String PASSWORD = "password";
    private static Connection conn;

    public static Connection connect() {
    	// connect to database
    	Connection conn;
    	try {
    		Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection(DB_URL, 
                    USER_NAME, PASSWORD);
            return conn;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	} catch (ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public void create() {
    	String sqlInsert = "INSERT INTO PERSONS (first_name, last_name) VALUES (?, ?)";
        try {
            // crate statement to insert student
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);
            stmt.setString(1, "Vinh");
            stmt.setString(2, "Hanoi");
            int c = stmt.executeUpdate();
            if (c == 0) {
            	System.out.println("Error");
            }
            
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void show() {
    	String sql = "Select * from Persons";
    	PreparedStatement stmt;
    	try {
    		// select all student
            stmt = conn.prepareStatement(sql);
            // get data from table 'student'
            ResultSet rs = stmt.executeQuery();
            // show data
            while (rs.next()) {
                System.out.println(rs.getInt(1) + "  " + rs.getString(2) 
                        + "  " + rs.getString(3));
            }
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
}
