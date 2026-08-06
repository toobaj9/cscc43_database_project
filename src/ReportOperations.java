import java.sql.*;
import java.util.Scanner;

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

  public static void report7() {
    System.out.println("Report 7 selected.");
  }
  
  public static void report8() {
    System.out.println("Report 8 selected.");
  }

  public static void report9() {
    System.out.println("Report 9 selected.");
  }
}
