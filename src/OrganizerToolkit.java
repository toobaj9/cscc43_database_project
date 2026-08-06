import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public final class OrganizerToolkit {
    private OrganizerToolkit() {
    }

    public static void suggestPricing(Scanner scanner) {
        System.out.println("Welcome to the Organizer Pricing Toolkit!");
        System.out.println("Event Segment:");
        String segment = scanner.nextLine();
        System.out.println("Event Genre:");
        String genre = scanner.nextLine();
        System.out.println("Event city:");
        String city = scanner.nextLine();
        System.out.println("Venue ID:");
        Integer venueId = Integer.parseInt(scanner.nextLine());

        String pricingSql = """
            SELECT
                pt.tier_name,
                ROUND(AVG(pt.price), 2) AS average_price
            FROM performance p
            JOIN events e
              ON e.event_id = p.event_id
            JOIN belongsTo bt
              ON bt.event_id = e.event_id
            JOIN venue v
              ON v.venue_id = p.venue_id
            JOIN priceTier pt
              ON pt.performance_id = p.performance_id
            WHERE bt.genre_name = ?
              AND bt.seg_name = ?
              AND v.city = ?
              AND p.performance_date >= CURDATE() - INTERVAL 1 YEAR
            GROUP BY pt.tier_name
            ORDER BY average_price DESC
            """;

        String capacitySql = """
            SELECT
                (
                    SELECT COUNT(*)
                    FROM seat s
                    WHERE s.venue_id = ?
                )
                +
                (
                    SELECT COALESCE(SUM(ga.capacity), 0)
                    FROM generalAdmissionSection ga
                    WHERE ga.venue_id = ?
                ) AS total_capacity
            """;

        try (Connection connection = Database.connect()) {
            int totalCapacity;
            try (PreparedStatement capacityStatement = connection.prepareStatement(capacitySql)) {
                capacityStatement.setInt(1, venueId);
                capacityStatement.setInt(2, venueId);
                try (ResultSet result = capacityStatement.executeQuery()) {
                    result.next();
                    totalCapacity = result.getInt("total_capacity");
                }
            }

            if (totalCapacity == 0) {
                System.err.println("Venue does not exist or has no capacity.");
                return;
            }

            boolean foundComparables = false;

            System.out.println("\nSuggested tier prices:");

            try (PreparedStatement pricingStatement =
                         connection.prepareStatement(pricingSql)) {

                pricingStatement.setString(1, genre);
                pricingStatement.setString(2, segment);
                pricingStatement.setString(3, city);

                try (ResultSet result = pricingStatement.executeQuery()) {
                    while (result.next()) {
                        foundComparables = true;

                        System.out.printf(
                                "%-15s $%.2f%n",
                                result.getString("tier_name"),
                                result.getDouble("average_price")
                        );
                    }
                }
            }

            if (!foundComparables) {
                System.out.println("No comparable performances found. Using default prices:");
                System.out.println("Premium         $120.00");
                System.out.println("Standard         $80.00");
                System.out.println("General          $50.00");
            }

            int premiumCapacity = (int) Math.round(totalCapacity * 0.25);
            int standardCapacity = (int) Math.round(totalCapacity * 0.50);
            int generalCapacity = totalCapacity - premiumCapacity - standardCapacity;

            System.out.println("\nSuggested tier structure:");
            System.out.printf("Premium:  25%% (%d seats)%n", premiumCapacity);
            System.out.printf("Standard: 50%% (%d seats)%n", standardCapacity);
            System.out.printf("General:  25%% (%d seats)%n", generalCapacity);
            System.out.println("Total venue capacity: " + totalCapacity);

        } catch (NumberFormatException exception) {
            System.err.println("Venue ID must be a valid number.");

        } catch (SQLException exception) {
            System.err.println("Failed to generate pricing suggestions: " + exception.getMessage());
        }
    }   
}
