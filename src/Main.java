import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = Database.connect()) {
            System.out.println("Connected to MyTix successfully.");
        } catch (SQLException exception) {
            System.err.println(
                    "Database connection failed: " + exception.getMessage()
            );
        }
    }
}