import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReportOperations {
    private ReportOperations() {
    }

    public static void report1() {
        System.out.println("Report 1 selected.");
    }

    public static void report2() {
        System.out.println("Report 2 selected.");
    }

    public static void report3() {
        System.out.println("Report 3 selected.");
    }

    public static void report4() {
        String sql = """
            WITH purchased AS (
                SELECT
                    co.customer_email,
                    v.city,
                    COUNT(t.ticket_id) AS tickets_purchased
                FROM customerOrder co
                JOIN ticket t
                ON t.order_id = co.order_id
                JOIN performance p
                ON p.performance_id = co.performance_id
                JOIN venue v
                ON v.venue_id = p.venue_id
                WHERE co.order_date >= NOW() - INTERVAL 1 YEAR
                GROUP BY co.customer_email, v.city
            ),
            listed AS (
                SELECT
                    rl.seller_email AS customer_email,
                    v.city,
                    COUNT(DISTINCT rl.ticket_id) AS tickets_listed
                FROM resaleListing rl
                JOIN ticket t
                ON t.ticket_id = rl.ticket_id
                JOIN customerOrder co
                ON co.order_id = t.order_id
                JOIN performance p
                ON p.performance_id = co.performance_id
                JOIN venue v
                ON v.venue_id = p.venue_id
                WHERE rl.listed_at >= NOW() - INTERVAL 1 YEAR
                GROUP BY rl.seller_email, v.city
            )
            SELECT
                p.city,
                p.customer_email,
                p.tickets_purchased,
                COALESCE(l.tickets_listed, 0) AS tickets_listed
            FROM purchased p
            LEFT JOIN listed l
            ON l.customer_email = p.customer_email
            AND l.city = p.city
            WHERE p.tickets_purchased >= 10
            AND COALESCE(l.tickets_listed, 0) > p.tickets_purchased / 2.0
            ORDER BY p.city, tickets_listed DESC
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()) {

            boolean found = false;

            System.out.printf("%-20s %-30s %-20s %-15s%n",
            "City",
                "Customer",
                "Tickets Purchased",
                "Tickets Listed"
            );

            while (result.next()) {
                found = true;

                System.out.printf(
                    "%-20s %-30s %-20d %-15d%n",
                    result.getString("city"),
                    result.getString("customer_email"),
                    result.getInt("tickets_purchased"),
                    result.getInt("tickets_listed")
                );
            }
            if (!found) {
                System.out.println("No possible ticket scalpers were found.");
            }
        } catch (SQLException exception) {
            System.err.println("Failed to generate Report 4: " + exception.getMessage());
        }
    }

    public static void report5() {
        System.out.println("Report 5 selected.");
    }

    public static void report6() {
        System.out.println("Report 6 selected.");
    }

    public static void report7(Scanner scanner) {
        System.out.println("Sell-Through Reports:");
        System.out.println("1. Sell-through by performance");
        System.out.println("2. Sell-through by price tier");
        System.out.println("3. Sold-out and low-sales performances by month and city");

        System.out.print("Select an option: ");
        int choice = Integer.parseInt(scanner.nextLine());

        String SECTION_SELL_THROUGH_SQL = """
            WITH reserved_capacity AS (
                SELECT
                    p.performance_id,
                    p.venue_id,
                    s.section_name,
                    COUNT(seat.seat_num)
                    - COUNT(b.seat_num) AS sellable_capacity
                FROM performance p
                JOIN section s
                ON s.venue_id = p.venue_id
                AND s.section_type = 'reserved'
                JOIN seat
                ON seat.venue_id = s.venue_id
                AND seat.section_name = s.section_name
                LEFT JOIN blocks b
                ON b.performance_id = p.performance_id
                AND b.venue_id = seat.venue_id
                AND b.section_name = seat.section_name
                AND b.row_name = seat.row_name
                AND b.seat_num = seat.seat_num
                GROUP BY p.performance_id, p.venue_id, s.section_name
            ),
            general_capacity AS (
                SELECT
                    p.performance_id,
                    p.venue_id,
                    ga.section_name,
                    ga.capacity AS sellable_capacity
                FROM performance p
                JOIN generalAdmissionSection ga
                ON ga.venue_id = p.venue_id
            ),
            capacities AS (
                SELECT * FROM reserved_capacity
                UNION ALL
                SELECT * FROM general_capacity
            ),
            sales AS (
                SELECT
                    co.performance_id,
                    t.venue_id,
                    t.section_name,
                    COUNT(*) AS tickets_sold
                FROM customerOrder co
                JOIN ticket t
                ON t.order_id = co.order_id
                WHERE t.ticket_status = 'active'
                GROUP BY co.performance_id, t.venue_id, t.section_name
            )
        """;

        switch (choice) {
            case 1:
                performanceSellThrough(SECTION_SELL_THROUGH_SQL);
                break;
            case 2:
                System.out.print("Performance ID: ");
                int performanceId = Integer.parseInt(scanner.nextLine());
                tierSellThrough(performanceId, SECTION_SELL_THROUGH_SQL);
                break;
            case 3:
                System.out.print("Year (e.g. 2026): ");
                int year = Integer.parseInt(scanner.nextLine());

                System.out.print("Month (1-12): ");
                int month = Integer.parseInt(scanner.nextLine());

                monthlyCitySellThrough(year, month, SECTION_SELL_THROUGH_SQL);
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private static void performanceSellThrough(String SECTION_SELL_THROUGH_SQL) {
        String sql = SECTION_SELL_THROUGH_SQL + """
            SELECT
                p.performance_id,
                e.event_name,
                v.city,
                SUM(c.sellable_capacity) AS sellable_capacity,
                SUM(COALESCE(s.tickets_sold, 0)) AS tickets_sold,
                ROUND(
                    SUM(COALESCE(s.tickets_sold, 0))
                    / NULLIF(SUM(c.sellable_capacity), 0),
                    4
                ) AS sell_through_rate
            FROM performance p
            JOIN events e
            ON e.event_id = p.event_id
            JOIN venue v
            ON v.venue_id = p.venue_id
            JOIN capacities c
            ON c.performance_id = p.performance_id
            LEFT JOIN sales s
            ON s.performance_id = c.performance_id
            AND s.venue_id = c.venue_id
            AND s.section_name = c.section_name
            GROUP BY p.performance_id, e.event_name, v.city
            ORDER BY p.performance_id
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()) {

            System.out.printf(
                    "%-15s %-30s %-18s %-15s %-12s %-15s%n",
                    "Performance ID", "Event", "City",
                    "Capacity", "Sold", "Sell-Through"
            );

            while (result.next()) {
                System.out.printf(
                        "%-15d %-30s %-18s %-15d %-12d %.2f%%%n",
                        result.getInt("performance_id"),
                        result.getString("event_name"),
                        result.getString("city"),
                        result.getInt("sellable_capacity"),
                        result.getInt("tickets_sold"),
                        result.getDouble("sell_through_rate") * 100
                );
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to generate sell-through report: "
                    + exception.getMessage()
            );
        }
    }

    private static void tierSellThrough(int performanceId, String SECTION_SELL_THROUGH_SQL) {
        String sql = SECTION_SELL_THROUGH_SQL + """
            SELECT
                att.tier_name,
                pt.price,
                SUM(c.sellable_capacity) AS sellable_capacity,
                SUM(COALESCE(s.tickets_sold, 0)) AS tickets_sold,
                ROUND(
                    SUM(COALESCE(s.tickets_sold, 0))
                    / NULLIF(SUM(c.sellable_capacity), 0),
                    4
                ) AS sell_through_rate
            FROM assignedToTier att
            JOIN priceTier pt
            ON pt.performance_id = att.performance_id
            AND pt.tier_name = att.tier_name
            JOIN capacities c
            ON c.performance_id = att.performance_id
            AND c.venue_id = att.venue_id
            AND c.section_name = att.section_name
            LEFT JOIN sales s
            ON s.performance_id = c.performance_id
            AND s.venue_id = c.venue_id
            AND s.section_name = c.section_name
            WHERE att.performance_id = ?
            GROUP BY att.tier_name, pt.price
            ORDER BY pt.price DESC
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, performanceId);

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;

                System.out.printf(
                        "%-15s %-12s %-15s %-12s %-15s%n",
                        "Tier", "Price", "Capacity", "Sold", "Sell-Through"
                );

                while (result.next()) {
                    found = true;

                    System.out.printf(
                            "%-15s $%-11.2f %-15d %-12d %.2f%%%n",
                            result.getString("tier_name"),
                            result.getDouble("price"),
                            result.getInt("sellable_capacity"),
                            result.getInt("tickets_sold"),
                            result.getDouble("sell_through_rate") * 100
                    );
                }

                if (!found) {
                    System.out.println("No tiers found for this performance.");
                }
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to generate tier report: "
                    + exception.getMessage()
            );
        }
    }

    private static void monthlyCitySellThrough(int year, int month, String SECTION_SELL_THROUGH_SQL) {
        if (month < 1 || month > 12) {
            System.err.println("Month must be between 1 and 12.");
            return;
        }

        String sql = SECTION_SELL_THROUGH_SQL + """
            SELECT *
            FROM (
                SELECT
                    v.city,
                    p.performance_id,
                    e.event_name,
                    p.performance_date,
                    SUM(c.sellable_capacity) AS sellable_capacity,
                    SUM(COALESCE(s.tickets_sold, 0)) AS tickets_sold,
                    SUM(COALESCE(s.tickets_sold, 0))
                        / NULLIF(SUM(c.sellable_capacity), 0)
                        AS sell_through_rate
                FROM performance p
                JOIN events e
                ON e.event_id = p.event_id
                JOIN venue v
                ON v.venue_id = p.venue_id
                JOIN capacities c
                ON c.performance_id = p.performance_id
                LEFT JOIN sales s
                ON s.performance_id = c.performance_id
                AND s.venue_id = c.venue_id
                AND s.section_name = c.section_name
                WHERE YEAR(p.performance_date) = ?
                AND MONTH(p.performance_date) = ?
                GROUP BY
                    v.city,
                    p.performance_id,
                    e.event_name,
                    p.performance_date
            ) totals
            WHERE sell_through_rate >= 1
            OR sell_through_rate < 0.25
            ORDER BY city, sell_through_rate DESC
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, year);
            statement.setInt(2, month);

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;

                System.out.printf(
                        "%-18s %-15s %-30s %-15s %-12s %-15s%n",
                        "City", "Performance ID", "Event",
                        "Date", "Rate", "Category"
                );

                while (result.next()) {
                    found = true;

                    double rate = result.getDouble("sell_through_rate");
                    String category = rate >= 1
                            ? "Sold Out"
                            : "Under 25%";

                    System.out.printf(
                            "%-18s %-15d %-30s %-15s %-11.2f%% %-15s%n",
                            result.getString("city"),
                            result.getInt("performance_id"),
                            result.getString("event_name"),
                            result.getDate("performance_date"),
                            rate * 100,
                            category
                    );
                }

                if (!found) {
                    System.out.println(
                            "No sold-out or under-25% performances found."
                    );
                }
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to generate monthly report: "
                    + exception.getMessage()
            );
        }
    }

    public static void report8(Scanner scanner) {
        System.out.println("Resale Reports:");
        System.out.println("1. Resale statistics for every event");
        System.out.println("2. Top 10 events by resale volume in a period");

        System.out.print("Select an option: ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                eventResaleStatistics();
                break;
            case 2:
                System.out.print("Start date (YYYY-MM-DD): ");
                String startDate = scanner.nextLine().trim();

                System.out.print("End date (YYYY-MM-DD): ");
                String endDate = scanner.nextLine().trim();

                topResaleEvents(startDate, endDate);
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private static void eventResaleStatistics() {
        String sql = """
            SELECT
                e.event_id,
                e.event_name,
                COUNT(pl.listing_id) AS completed_resales,
                ROUND(
                    AVG(
                        CASE
                            WHEN pl.listing_id IS NOT NULL
                            THEN (pl.sale_price - t.face_value) / t.face_value
                        END
                    ) * 100,
                    2
                ) AS average_markup_percent,
                ROUND(
                    SUM(
                        CASE
                            WHEN rl.listing_price = t.face_value * e.resale_cap
                            THEN 1
                            ELSE 0
                        END
                    ) / NULLIF(COUNT(rl.listing_id), 0),
                    4
                ) AS fraction_at_cap
            FROM events e
            LEFT JOIN performance p
            ON p.event_id = e.event_id
            LEFT JOIN customerOrder co
            ON co.performance_id = p.performance_id
            LEFT JOIN ticket t
            ON t.order_id = co.order_id
            LEFT JOIN resaleListing rl
            ON rl.ticket_id = t.ticket_id
            LEFT JOIN purchaseListing pl
            ON pl.listing_id = rl.listing_id
            GROUP BY e.event_id, e.event_name
            ORDER BY completed_resales DESC, e.event_name
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()) {

            System.out.printf(
                    "%-10s %-30s %-20s %-22s %-18s%n",
                    "Event ID",
                    "Event",
                    "Completed Resales",
                    "Average Markup",
                    "Fraction at Cap"
            );

            while (result.next()) {
                System.out.printf(
                        "%-10d %-30s %-20d %-21.2f%% %-17.2f%%%n",
                        result.getInt("event_id"),
                        result.getString("event_name"),
                        result.getInt("completed_resales"),
                        result.getDouble("average_markup_percent"),
                        result.getDouble("fraction_at_cap") * 100
                );
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to generate resale statistics: "
                    + exception.getMessage()
            );
        }
    }

    private static void topResaleEvents(String startDate, String endDate) {
        String sql = """
            SELECT
                e.event_id,
                e.event_name,
                COUNT(pl.listing_id) AS resale_volume,
                SUM(pl.sale_price) AS total_resale_value
            FROM purchaseListing pl
            JOIN resaleListing rl
            ON rl.listing_id = pl.listing_id
            JOIN ticket t
            ON t.ticket_id = rl.ticket_id
            JOIN customerOrder co
            ON co.order_id = t.order_id
            JOIN performance p
            ON p.performance_id = co.performance_id
            JOIN events e
            ON e.event_id = p.event_id
            WHERE pl.purchased_at >= ?
            AND pl.purchased_at < DATE_ADD(?, INTERVAL 1 DAY)
            GROUP BY e.event_id, e.event_name
            ORDER BY resale_volume DESC, total_resale_value DESC
            LIMIT 10
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(
                    1,
                    java.sql.Date.valueOf(startDate)
            );

            statement.setDate(
                    2,
                    java.sql.Date.valueOf(endDate)
            );

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;

                System.out.printf(
                        "%-10s %-30s %-18s %-20s%n",
                        "Event ID",
                        "Event",
                        "Resale Volume",
                        "Total Resale Value"
                );

                while (result.next()) {
                    found = true;

                    System.out.printf(
                            "%-10d %-30s %-18d $%-19.2f%n",
                            result.getInt("event_id"),
                            result.getString("event_name"),
                            result.getInt("resale_volume"),
                            result.getDouble("total_resale_value")
                    );
                }

                if (!found) {
                    System.out.println(
                            "No completed resales were found in that period."
                    );
                }
            }

        } catch (IllegalArgumentException exception) {
            System.err.println("Dates must use YYYY-MM-DD.");

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to generate top resale events: "
                    + exception.getMessage()
            );
        }
    }

    public static void report9() {
        String sql = """
            SELECT e.event_id, e.event_name, r.comment
            FROM events e
            JOIN performance p
            ON p.event_id = e.event_id
            JOIN reviews r
            ON r.performance_id = p.performance_id
            ORDER BY e.event_id
            """;

        Map<Integer, String> eventNames = new HashMap<>();
        Map<Integer, Map<String, Integer>> phraseCounts = new HashMap<>();

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int eventId = result.getInt("event_id");
                String eventName = result.getString("event_name");
                String comment = result.getString("comment");

                eventNames.put(eventId, eventName);

                Map<String, Integer> counts =
                        phraseCounts.computeIfAbsent(
                                eventId,
                                key -> new HashMap<>()
                        );

                for (String phrase : extractPhrases(comment)) {
                    counts.merge(phrase, 1, Integer::sum);
                }
            }

            if (eventNames.isEmpty()) {
                System.out.println("No review comments were found.");
                return;
            }

            for (Map.Entry<Integer, String> event : eventNames.entrySet()) {
                int eventId = event.getKey();

                System.out.println();
                System.out.println(
                        eventId + " - " + event.getValue()
                );
                System.out.println("Most popular phrases:");

                phraseCounts.get(eventId)
                        .entrySet()
                        .stream()
                        .sorted(
                            Map.Entry.<String, Integer>comparingByValue()
                                    .reversed()
                                    .thenComparing(Map.Entry.comparingByKey())
                        )
                        .limit(10)
                        .forEach(entry ->
                            System.out.println(
                                entry.getKey() + " (" + entry.getValue() + ")"
                            )
                        );
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to generate noun phrase report: "
                    + exception.getMessage()
            );
        }
    }

    private static List<String> extractPhrases(String comment) {
        Set<String> stopWords = Set.of(
            "a", "an", "the", "and", "or", "but", "of", "to",
            "in", "on", "at", "for", "from", "with", "was",
            "were", "is", "are", "be", "been", "being", "it",
            "this", "that", "these", "those", "very", "really",
            "had", "has", "have", "would", "could", "should",
            "as", "by", "through", "throughout", "than"
        );

        String cleaned = comment
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String[] words = cleaned.split(" ");
        List<String> phrases = new ArrayList<>();

        for (int start = 0; start < words.length; start++) {
            if (stopWords.contains(words[start])
                    || words[start].length() < 3) {
                continue;
            }

            StringBuilder phrase = new StringBuilder();

            for (int length = 1;
                length <= 3 && start + length <= words.length;
                length++) {

                String word = words[start + length - 1];

                if (stopWords.contains(word) || word.length() < 3) {
                    break;
                }

                if (!phrase.isEmpty()) {
                    phrase.append(" ");
                }

                phrase.append(word);
                phrases.add(phrase.toString());
            }
        }

        return phrases;
    }
}
