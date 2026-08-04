import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
  private static final String URL = "jdbc:mysql://localhost:3306/mytix";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "";

  private Database() {
      // Private constructor to prevent instantiation
  }

  public static Connection connect() throws SQLException {
      return DriverManager.getConnection(URL, USERNAME, PASSWORD);
  }
}