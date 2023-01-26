import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ArtWorldConnect {
	private final String databaseURL = "jdbc:sqlserver://titan.csse.rose-hulman.edu;databaseName=ArtWorldDatabaseS4G5";

	private Connection connection = null;
	
	public ArtWorldConnect() {
		
	}
	
	public boolean connect() {
		String sodabaseURL = "jdbc:sqlserver://titan.csse.rose-hulman.edu;databaseName=SodaBasecsomosdm;user=SodaBaseUsercsomosdm;password={Password123}";
		String newURL = databaseURL + ";user=AWDUser;password={Awd93>X!5v}";
		try {
			this.connection = DriverManager.getConnection(newURL);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean connect(String user, String password) {
		try {
			this.connection = DriverManager.getConnection(databaseURL, user, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
	public void closeConnection() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
