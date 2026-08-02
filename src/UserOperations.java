import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;

public class UserOperations {
    private UserOperations() {
    }

    public static void createUser(String email, String name, String address, String dob, String type) {
        Connection connection = null;

        try {
            connection = Database.connect();
            connection.setAutoCommit(false);

            PreparedStatement userStatement = connection.prepareStatement(
                """
                INSERT INTO USERS(email, name, address, dob) 
                VALUES (?, ?, ?, ?)
                """
            );

            userStatement.setString(1, email);
            userStatement.setString(2, name);
            userStatement.setString(3, address);
            userStatement.setString(4, dob);

            userStatement.executeUpdate();

            PreparedStatement subtypeStatement;

            if (type.equalsIgnoreCase("customer")) {
                subtypeStatement = connection.prepareStatement(
                    """
                    INSERT INTO CUSTOMER(email) 
                    VALUES (?)
                    """
                );
            } else if (type.equalsIgnoreCase("organizer")) {
                subtypeStatement = connection.prepareStatement(
                    """
                    INSERT INTO ORGANIZER(email) 
                    VALUES (?)
                    """
                );
            } else {
                System.err.println("Invalid user type. Must be 'customer' or 'organizer'.");
                return;
            }

            subtypeStatement.setString(1, email);
            subtypeStatement.executeUpdate();

            connection.commit();
            System.out.println("User successfully created.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error occurred while rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error occurred while creating user: " + e.getMessage());
        }
    }

    public static void deleteUser(String email) {
        if (email == null || email.isEmpty()) {
            System.err.println("Email cannot be null or empty.");
            return;
        }

        String findUserQuery = "SELECT email FROM users WHERE email = ?";
        String deleteUserQuery = "DELETE FROM users WHERE email = ?";
        
        Connection connection = null;

        try {
            connection = Database.connect();
            connection.setAutoCommit(false);

            // check whether the user exists before attempting to delete
            try (PreparedStatement findUserStatement = connection.prepareStatement(findUserQuery)) {
                findUserStatement.setString(1, email);
                try (ResultSet userResultSet = findUserStatement.executeQuery()) {
                    if (!userResultSet.next()) {
                        System.err.println("User with email " + email + " does not exist.");
                        return;
                    }
                }

            }
            
            // delete users - automatically deletes subtype rows too
            try (PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery)) {
                deleteUserStatement.setString(1, email);
                int rowsDeleted =deleteUserStatement.executeUpdate();
                if (rowsDeleted == 1) {
                    System.out.println("User successfully deleted.");
                } else {
                    System.err.println("No user found with email " + email + ".");
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error occurred while rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            System.err.println("This user cannot be deleted because existing records depend on the account.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error occurred while rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error occurred while deleting user: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error occurred while closing database connection: " + e.getMessage());
                }
            }
        }
    }
}