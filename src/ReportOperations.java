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

    public static void report1(Scanner scanner) {
        System.out.println("Report 1 selected.");
        try (Connection conn = Database.connect()) {
            String start_date;
            String end_date;

            System.out.println();
            boolean back = false;
            while (!back) {
                System.out.println("Total number of tickets sold and the gross revenue in a specific date range:");
                System.out.println("1. by city.");
                System.out.println("2. venue within a city");
                System.out.println("3. Back to report menu");
                System.out.println();
                System.out.print("Please select an option: ");


                String choice = scanner.nextLine();
                PreparedStatement ps;
                ResultSet rs;
                System.out.println();
                switch (choice) {
                    case "1":
                        System.out.print("Enter Start Date: ");
                        start_date = scanner.nextLine();
                        System.out.print("Enter End Date: ");
                        end_date = scanner.nextLine();
                        ps = conn.prepareStatement(
                            "SELECT city, COUNT(t.ticket_id) as totalTicketsSold, SUM(t.face_value) as grossRevenue " +
                            "FROM ticket AS t " +
                            "JOIN customerOrder AS o ON o.order_id = t.order_id " +
                            "JOIN performance AS p ON p.performance_id = o.performance_id " +
                            "JOIN venue AS v ON v.venue_id = p.venue_id " +
                            "WHERE p.performance_date >= ? AND p.performance_date <= ? AND t.ticket_status = 'active' " +
                            "GROUP BY city"
                        );
                        ps.setString(1, start_date);
                        ps.setString(2, end_date);
                        rs = ps.executeQuery();
                        System.out.println();
                        System.out.printf("Total number of tickets sold and the gross revenue between %s and %s range by city: \n", start_date, end_date);
                        System.out.printf("%-15s %-20s %-20s%n", "City", "TotalTicketsSold", "GrossRevenue");
                        while (rs.next()) {
                            System.out.printf("%-15s %-20d %-20.3f%n",
                                rs.getString("city"),
                                rs.getInt("totalTicketsSold"),
                                rs.getDouble("grossRevenue")
                            );
                        }
                        break;
                    case "2":
                        System.out.print("Enter Start Date: ");
                        start_date = scanner.nextLine();
                        System.out.print("Enter End Date: ");
                        end_date = scanner.nextLine();
                        ps = conn.prepareStatement(
                            "SELECT city, venue_name, COUNT(t.ticket_id) as totalTicketsSold, SUM(t.face_value) as grossRevenue " +
                            "FROM ticket AS t " +
                            "JOIN customerOrder AS o ON o.order_id = t.order_id " +
                            "JOIN performance AS p ON p.performance_id = o.performance_id " +
                            "JOIN venue AS v ON v.venue_id = p.venue_id " +
                            "WHERE p.performance_date >= ? AND p.performance_date <= ? AND t.ticket_status = 'active' " +
                            "GROUP BY city, venue_name"
                        );
                        ps.setString(1, start_date);
                        ps.setString(2, end_date);
                        rs = ps.executeQuery();
                        System.out.println();
                        System.out.printf("Total number of tickets sold and the gross revenue between %s and %s range by venue within a city: \n", start_date, end_date);
                        System.out.printf("%-15s %-30s %-20s %-20s%n", "City", "VenueName", "TotalTicketsSold", "GrossRevenue");
                        while (rs.next()) {
                            System.out.printf("%-15s %-30s %-20d %-20.3f%n",
                                rs.getString("city"),
                                rs.getString("venue_name"),
                                rs.getInt("totalTicketsSold"),
                                rs.getDouble("grossRevenue")
                            );
                        }
                        break;
                    case "3":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while executing report 1: " + e.getMessage());
        }
    }


    public static void report2(Scanner scanner) {
        System.out.println("Report 2 selected.");
        try (Connection conn = Database.connect()) {
            boolean back = false;
            while (!back) {
                System.out.println("Total Number of Events and Performances:");
                System.out.println("1. Per Segment and Genre");
                System.out.println("2. Per Country");
                System.out.println("3. Per Country and City");
                System.out.println("4. Per Country, City and Venue");
                System.out.println("5. Back to report menu");
                System.out.println();
                System.out.print("Please select an option: ");
                String choice = scanner.nextLine();
                Statement stmt = conn.createStatement();
                ResultSet rs;
                System.out.println();
                switch (choice) {
                    case "1":
                        rs = stmt.executeQuery(
                            "SELECT seg_name, genre_name, COUNT(DISTINCT e.event_id) AS TotalEvents, COUNT(p.performance_id) AS TotalPerformances " +
                            "FROM events AS e " +
                            "JOIN performance AS p ON p.event_id = e.event_id " +
                            "JOIN belongsTo AS b ON b.event_id = e.event_id " +
                            "GROUP BY seg_name, genre_name"
                        );
                        System.out.println("Per Segment and Genre");
                        System.out.printf("%-25s %-10s %-15s %-20s%n", "Segment", "Genre", "TotalEvents", "TotalPerformances");
                        while (rs.next()) {
                            System.out.printf("%-25s %-10s %-15d %-20d%n",
                                rs.getString("seg_name"),
                                rs.getString("genre_name"),
                                rs.getInt("TotalEvents"),
                                rs.getInt("TotalPerformances")
                            );
                        }
                        break;
                    case "2":
                        rs = stmt.executeQuery(
                            "SELECT country, COUNT(DISTINCT e.event_id) as TotalEvents , COUNT(p.performance_id)  as TotalPerformances " +
                            "FROM events AS e " +
                            "JOIN performance AS p ON p.event_id = e.event_id " +
                            "JOIN venue AS v ON v.venue_id = p.venue_id " +
                            "GROUP BY country"
                        );
                        System.out.println("Per Country");
                        System.out.printf("%-20s %-20s %-20s%n", "Country", "TotalEvents", "TotalPerformances");
                        while (rs.next()) {
                            System.out.printf("%-20s %-20d %-20d%n",
                                rs.getString("country"),
                                rs.getInt("TotalEvents"),
                                rs.getInt("TotalPerformances")
                            );
                        }
                        break;
                    case "3":
                        rs = stmt.executeQuery(
                            "SELECT country, city, COUNT(DISTINCT e.event_id) as TotalEvents , COUNT(p.performance_id)  as TotalPerformances " +
                            "FROM events AS e " +
                            "JOIN performance AS p ON p.event_id = e.event_id " +
                            "JOIN venue AS v ON v.venue_id = p.venue_id " +
                            "GROUP BY country, city"
                        );
                        System.out.println("Per Country and City");
                        System.out.printf("%-20s %-15s %-15s %-20s%n", "Country", "City", "TotalEvents", "TotalPerformances");
                        while (rs.next()) {
                            System.out.printf("%-20s %-15s %-15d %-20d%n",
                                rs.getString("country"),
                                rs.getString("city"),
                                rs.getInt("TotalEvents"),
                                rs.getInt("TotalPerformances")
                            );
                        }
                        break;
                    case "4":
                        rs = stmt.executeQuery(
                            "SELECT country, city, venue_name, COUNT(DISTINCT e.event_id) as TotalEvents , COUNT(p.performance_id)  as TotalPerformances " +
                            "FROM events AS e " +
                            "JOIN performance AS p ON p.event_id = e.event_id " +
                            "JOIN venue AS v ON v.venue_id = p.venue_id " +
                            "GROUP BY country, city, venue_name"
                        );
                        System.out.println("Per Country, City and Venue");
                        System.out.printf("%-20s %-15s %-30s %-15s %-20s%n", "Country", "City", "Venue", "TotalEvents", "TotalPerformances");
                        while (rs.next()) {
                            System.out.printf("%-20s %-15s %-30s %-15d %-20d%n",
                                rs.getString("country"),
                                rs.getString("city"),
                                rs.getString("venue_name"),
                                rs.getInt("TotalEvents"),
                                rs.getInt("TotalPerformances")
                            );
                        }
                        break;
                    case "5":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while executing report 2: " + e.getMessage());
        }
    }

    public static void report3(Scanner scanner) {
        System.out.println("Report 3 selected.");
        try (Connection conn = Database.connect()) {
            boolean back = false;
            while (!back) {
                System.out.println("1. Rank the organizers by their gross revenue overall");
                System.out.println("2. Rank the organizers by their gross revenue per country");
                System.out.println("3. Rank the organizers by their gross revenue per city");
                System.out.println("4. Back to report menu");
                System.out.println();

                System.out.print("Please select an option: ");
                String choice = scanner.nextLine();
                Statement stmt = conn.createStatement();
                ResultSet rs;
                System.out.println();
                switch (choice) {
                    case "1":
                        rs = stmt.executeQuery(
                            "SELECT organizer_email, SUM(t.face_value) as grossRevenue, RANK() Over w as 'rank' " +
                            "From events AS e " +
                            "JOIN performance AS p ON p.event_id = e.event_id " +
                            "JOIN customerOrder AS o ON o.performance_id = p.performance_id " +
                            "JOIN ticket AS t ON t.order_id = o.order_id AND t.venue_id = p.venue_id " +
                            "WHERE t.ticket_status = 'active' " +
                            "GROUP BY organizer_email " +
                            "WINDOW w AS (ORDER BY SUM(t.face_value) DESC)"
                        );
                        System.out.println("Organizers by their gross revenue overall:");
                        System.out.printf("%-25s %-25s %-20s%n", "Email", "GrossRevenue", "Rank");
                        while (rs.next()) {
                            System.out.printf("%-25s %-25.3f %-20d%n",
                                rs.getString("organizer_email"),
                                rs.getDouble("grossRevenue"),
                                rs.getInt("rank")
                            );
                        }
                        break;
                    case "2":
                        rs = stmt.executeQuery(
                            "SELECT organizer_email, country, SUM(t.face_value) as grossRevenue, RANK() Over (PARTITION BY country ORDER BY SUM(t.face_value) DESC) as 'rank' " +
                            "From events AS e " +
                            "JOIN performance AS p ON p.event_id = e.event_id " +
                            "JOIN venue AS v ON v.venue_id = p.venue_id " +
                            "JOIN customerOrder AS o ON o.performance_id = p.performance_id " +
                            "JOIN ticket AS t ON t.order_id = o.order_id AND t.venue_id = v.venue_id " +
                            "WHERE t.ticket_status = 'active' " +
                            "GROUP BY country, organizer_email"
                        );
                        System.out.println();
                        System.out.println("Organizers by their gross revenue per country:");
                        System.out.printf("%-25s %-25s %-25s %-20s%n", "Email", "Country", "GrossRevenue", "Rank");
                        while (rs.next()) {
                            System.out.printf("%-25s %-25s %-25.3f %-20d%n",
                                rs.getString("organizer_email"),
                                rs.getString("country"),
                                rs.getDouble("grossRevenue"),
                                rs.getInt("rank")
                            );
                        }
                        break;
                    case "3":
                        rs = stmt.executeQuery(
                            "SELECT organizer_email, country, city, SUM(t.face_value) as grossRevenue, RANK() Over(PARTITION BY city ORDER BY SUM(t.face_value) DESC) as 'rank' " +
                            "From events AS e " +
                            "JOIN performance AS p ON p.event_id = e.event_id " +
                            "JOIN venue AS v ON v.venue_id = p.venue_id " +
                            "JOIN customerOrder AS o ON o.performance_id = p.performance_id " +
                            "JOIN ticket AS t ON t.order_id = o.order_id AND t.venue_id = v.venue_id " +
                            "WHERE t.ticket_status = 'active' " +
                            "GROUP BY organizer_email, country, city"
                        );
                        System.out.println();
                        System.out.println("Organizers by their gross revenue per country and city:");
                        System.out.printf("%-25s %-25s %-25s %-25s %-20s%n", "Email", "Country", "City", "GrossRevenue", "Rank");
                        while (rs.next()) {
                            System.out.printf("%-25s %-25s %-25s %-25.3f %-20d%n",
                                rs.getString("organizer_email"),
                                rs.getString("country"),
                                rs.getString("city"),
                                rs.getDouble("grossRevenue"),
                                rs.getInt("rank")
                            );
                        }
                        break;
                    case "4":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while executing report3: " + e.getMessage());
        }
    }


    public static void report4() {
        System.out.println("Report 4 selected.");
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

    public static void report5(Scanner scanner) {
        System.out.println("Report 5 selected.");
        try (Connection conn = Database.connect()) {
            String start_date;
            String end_date;

            System.out.println();
            boolean back = false;
            while (!back) {
                System.out.println("1. Rank the customers by the number of orders they placed in a specific time period");
                System.out.println("2. Rank them by the number of orders per city");
                System.out.println("3. Back to report menu");
                System.out.println();
                System.out.print("Please select an option: ");

                String choice = scanner.nextLine();
                PreparedStatement ps;
                ResultSet rs;
                System.out.println();
                switch (choice) {
                    case "1":
                        System.out.print("Enter Start Date: ");
                        start_date = scanner.nextLine();

                        System.out.print("Enter End Date: ");
                        end_date = scanner.nextLine();
                        ps = conn.prepareStatement(
                            "SELECT customer_email, COUNT(o.order_id) AS numOrders, RANK() Over w as 'rank' " +
                            "FROM customerOrder AS o " +
                            "WHERE o.order_date >= ? AND o.order_date <= ? " +
                            "GROUP BY customer_email " +
                            "WINDOW w AS (ORDER BY COUNT(o.order_id) DESC)"
                        );
                        ps.setString(1, start_date);
                        ps.setString(2, end_date);

                        rs = ps.executeQuery();
                        System.out.println();
                        System.out.printf("Rank customers by the total number of orders placed by them between %s and %s range: \n", start_date, end_date);
                        System.out.printf("%-25s %-20s %-15s%n", "Email", "NumOrders", "Rank");
                    
                        while (rs.next()) {
                            System.out.printf("%-25s %-20d %-15d%n",
                                rs.getString("customer_email"),
                                rs.getInt("numOrders"),
                                rs.getInt("rank")
                            );
                        }
                        break;
                    case "2":
                        // we assume that for R5 second sub report, the year is the past year which seems a reasonable design choice as businesses intend to look at most recent orders.
                        Statement stmt = conn.createStatement();
                        rs = stmt.executeQuery(
                        "SELECT o.customer_email, city, COUNT(o.order_id) as NumOrders, RANK() Over (PARTITION BY city ORDER BY COUNT(o.order_id) DESC) as 'rank' " +
                        "FROM customerOrder AS o " +
                        "JOIN performance AS p ON p.performance_id = o.performance_id " +
                        "JOIN venue AS v ON v.venue_id = p.venue_id " +
                        "WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 365 DAY) " +
                        "GROUP BY customer_email, city " +
                        "Having COUNT(o.order_id) >= 2"
                        );

                        System.out.println();
                        System.out.printf("Rank customers by the total number of orders per city within past year: \n");
                        System.out.printf("%-25s %-25s %-20s %-15s%n", "Email", "City", "NumOrders", "Rank");
                    
                        while (rs.next()) {
                            System.out.printf("%-25s %-25s %-20d %-15d%n",
                                rs.getString("customer_email"),
                                rs.getString("city"),
                                rs.getInt("numOrders"),
                                rs.getInt("rank")
                            );
                        }
                        break;
                    case "3":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while executing report5: " + e.getMessage());
        }
    }

    public static void report6(Scanner scanner) {
        System.out.println("Report 6 selected.");
        try (Connection conn = Database.connect()) {
            boolean back = false;
            while (!back) {
                System.out.println("1. Find customers with the largest number of cancelled tickets");
                System.out.println("2. Find organizers with the largest number of cancelled performances");
                System.out.println("3. Back to report menu");
                System.out.println();

                System.out.print("Please select an option: ");
                String choice = scanner.nextLine();
                Statement stmt = conn.createStatement();
                ResultSet rs;
                System.out.println();
                switch (choice) {
                    case "1":
                        rs = stmt.executeQuery(
                            "WITH cancelled AS ( " +
                            "SELECT customer_email, COUNT(customer_email) as cancelledTickets " +
                            "FROM cancelsTicket " +
                            "WHERE DATE_SUB(CURDATE(), INTERVAL 365 DAY) <= cancelled_at " +
                            "GROUP BY customer_email) " +
                            "SELECT customer_email, cancelledTickets " +
                            "FROM cancelled " +
                            "WHERE cancelledTickets = (SELECT MAX(CancelledTickets) FROM cancelled)"
                        );
                        System.out.println("Customers with the largest number of Cancelled Tickets:");
                        System.out.printf("%-25s %-20s%n", "Email", "CancelledTickets");
                        while (rs.next()) {
                            System.out.printf("%-25s %-20d%n",
                                rs.getString("customer_email"),
                                rs.getInt("cancelledTickets")
                            );
                        }
                        break;
                    case "2":
                        rs = stmt.executeQuery(
                            "WITH cancelledPerf AS ( " +
                            "SELECT organizer_email, COUNT(organizer_email) as cancelledPerformances " +
                            "FROM cancelsPerformance " +
                            "WHERE DATE_SUB(CURDATE(), INTERVAL 365 DAY) <= cancelled_at " +
                            "GROUP BY organizer_email) " +
                            "SELECT organizer_email, cancelledPerformances " +
                            "FROM cancelledPerf " +
                            "WHERE cancelledPerformances = (SELECT MAX(cancelledPerformances) FROM cancelledPerf)"
                        );
                        System.out.println();
                        System.out.println("Organizers with the largest number of Cancelled Performances:");
                        System.out.printf("%-25s %-20s%n", "Email", "CancelledPerformances");
                        while (rs.next()) {
                            System.out.printf("%-25s %-20d%n",
                                rs.getString("organizer_email"),
                                rs.getInt("cancelledPerformances")
                            );
                        }
                        break;
                    case "3":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while executing report6: " + e.getMessage());
        }
    }


    public static void report7(Scanner scanner) {
        System.out.println("Report 7 selected.");
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
        System.out.println("Report 8 selected.");
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
        System.out.println("Report 9 selected.");
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