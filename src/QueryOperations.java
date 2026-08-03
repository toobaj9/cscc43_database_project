import java.sql.*;
import java.util.Scanner;

public class QueryOperations {
  private QueryOperations() {

  }
  public static void main(String[] args) {
    performancesInRange();
  }

  // query 1
  public static void searchByDistance() {
    try (Connection conn = Database.connect()) {
      Scanner scanner = new Scanner(System.in);

      System.out.print("Enter Latitude: ");
      double lat = Double.parseDouble(scanner.nextLine());

      System.out.print("Enter Longitude: ");
      double lon = Double.parseDouble(scanner.nextLine());

      System.out.print("Enter Distance in Km (press enter to use default distance 15km): ");
      String tmp = scanner.nextLine();
      int dist = 15;
      if (!tmp.isBlank()) {
        dist = Integer.parseInt(tmp);
      }

      String order_by = "ASC";
      String stmt = "";
      System.out.print("Rank By cheapest available ticket or distance? (type either 'cheapest' or distance'): ");
      String rank = scanner.nextLine();
      if (!rank.equalsIgnoreCase("cheapest") && !rank.equalsIgnoreCase("distance")) {
        System.err.println("Invalid rank type. Must be 'cheapest' or 'distance'.");
        return;
      }

      if (rank.equalsIgnoreCase("cheapest")) {
        System.out.print("Ascending or Descending? (type either 'ASC' or 'DSC'): ");
        order_by = scanner.nextLine();
        if (!order_by.equalsIgnoreCase("ASC") && !order_by.equalsIgnoreCase("DSC")) {
          System.err.println("Invalid order type. Must be 'ASC' or 'DSC'.");
          return;
        }
        stmt = "ORDER BY cheapest ASC ";
        if (order_by.equalsIgnoreCase("DSC")) {
          stmt = "ORDER BY cheapest DESC ";
        }
      } else {
        stmt = "ORDER BY distance ASC ";
      }
      // we used harversine formula to calculate distance in kms between 2 coordinates
      // default distance we choose is 15 km as an example
      // Reference: https://easycalculator.org/haversine-distance
      String prepare_stmnt = 
        "WITH upcoming_perf AS ( " +
        "SELECT performance_id, performance_time, performance_date, event_name, venue_name, city, country, " +
        "(2 * 6371 * ATAN2(SQRT(POWER(SIN((RADIANS(?) - RADIANS(latitude)) / 2), 2) + COS(RADIANS(latitude)) * COS(RADIANS(?)) * POWER(SIN((RADIANS(?) -  RADIANS(longitude)) / 2), 2)), SQRT(1 - (POWER(SIN((RADIANS(?) - RADIANS(latitude)) / 2),  2) + COS(RADIANS(latitude)) * COS(RADIANS(?)) * POWER(SIN((RADIANS(?) - RADIANS(longitude)) / 2), 2))))) AS distance " +
        "FROM events AS e " +
        "JOIN performance AS p ON p.event_id = e.event_id " +
        "JOIN venue AS v ON v.venue_id = p.venue_id " +
        "WHERE p.performance_date > CURRENT_DATE OR (p.performance_date = CURRENT_DATE AND p.performance_time >= CURRENT_TIME)), " +
        "cheapest_ticket AS (SELECT performance_id, MIN(price) AS cheapest " +
        "FROM seatMap as s " +
        "WHERE s.available > 0 " +
        "GROUP BY s.performance_id) " +
        "SELECT u.performance_id, performance_date, performance_time, event_name, venue_name, distance, cheapest as cheapestTicket, city, country " +
        "FROM upcoming_perf AS u " +
        "JOIN cheapest_ticket AS c ON c.performance_id = u.performance_id " +
        "WHERE distance <= ? ";

      prepare_stmnt += stmt;
      PreparedStatement upcoming_perf = conn.prepareStatement(prepare_stmnt);

      upcoming_perf.setDouble(1, lat);
      upcoming_perf.setDouble(2, lat);
      upcoming_perf.setDouble(3, lon);
      upcoming_perf.setDouble(4, lat);
      upcoming_perf.setDouble(5, lat);
      upcoming_perf.setDouble(6, lon);
      upcoming_perf.setInt(7, dist);

      ResultSet rs = upcoming_perf.executeQuery();
      System.out.println("Upcoming performances at venues within the same distance");
      System.out.printf("%-15s %-20s %-20s %-30s %-30s %-15s %-15s %-15s %-15s%n", "PerformanceID", "PerformanceDate", "PerformanceTime", "EventName", "VenueName", "Distance", "CheapestTicket", "City", "Country");
      
      while (rs.next()) {
        System.out.printf("%-15d %-20s %-20s %-30s %-30s %-15.4f %-15.4f %-15s %-15s%n",
          rs.getInt("performance_id"),
          rs.getDate("performance_date"),
          rs.getTime("performance_time"),
          rs.getString("event_name"),
          rs.getString("venue_name"),
          rs.getDouble("distance"),
          rs.getDouble("cheapestTicket"),
          rs.getString("city"),
          rs.getString("country")
        );
      }
    } catch (SQLException e) {
        System.err.println("Error occurred while executing searchByDistance query: " + e.getMessage());
    }
  }

  // query 2
  public static void searchByPostalCode() {
    try (Connection conn = Database.connect()) {
      Scanner scanner = new Scanner(System.in);

      System.out.print("Enter postal code: ");
      String postal_code = scanner.nextLine();

      String fsa = postal_code.substring(0, 3);
      String adjacent_match = fsa + "%";

      PreparedStatement upcoming_perf = conn.prepareStatement(
        "SELECT performance_id, performance_date, performance_time, event_name, venue_name, city, country, postal_code  " +
        "FROM venue " +
        "JOIN performance ON venue.venue_id = performance.venue_id " +
        "JOIN events ON performance.event_id = events.event_id " +
        "WHERE (postal_code = ? OR postal_code LIKE ?) AND performance.performance_status = 'scheduled' AND (performance.performance_date > CURRENT_DATE OR (performance.performance_date = CURRENT_DATE AND performance.performance_time >= CURRENT_TIME)) " +
        "ORDER BY performance_date, performance_time"
      );

      upcoming_perf.setString(1, postal_code);
      upcoming_perf.setString(2, adjacent_match);

      ResultSet rs = upcoming_perf.executeQuery();
      System.out.println();
      System.out.println("Upcoming performances at venues in the same and adjacent postal codes");
      System.out.printf("%-15s %-20s %-20s %-30s %-30s %-15s %-15s %-15s%n", "PerformanceID", "PerformanceDate", "PerformanceTime", "EventName", "VenueName", "City", "Country", "PostalCode");
      
      while (rs.next()) {
        System.out.printf("%-15d %-20s %-20s %-30s %-30s %-15s %-15s %-15s%n",
          rs.getInt("performance_id"),
          rs.getDate("performance_date"),
          rs.getTime("performance_time"),
          rs.getString("event_name"),
          rs.getString("venue_name"),
          rs.getString("city"),
          rs.getString("country"),
          rs.getString("postal_code")
        );
      }
    } catch (SQLException e) {
        System.err.println("Error occurred while executing searchByPostalCode query: " + e.getMessage());
    }
  }

  // query 3
  public static void upcomingPerformancesAtAddress() {
    try (Connection conn = Database.connect()) {
      Scanner scanner = new Scanner(System.in);
      
      System.out.print("Enter street address: ");
      String street_address = scanner.nextLine();

      System.out.print("Enter city: ");
      String city = scanner.nextLine();

      System.out.print("Enter country: ");
      String country = scanner.nextLine();

      System.out.print("Enter postal code: ");
      String postal_code = scanner.nextLine();

      PreparedStatement upcoming_perf = conn.prepareStatement(
        "SELECT performance_id, performance_date, performance_time, event_name, venue_name  " +
        "FROM venue " +
        "JOIN performance ON venue.venue_id = performance.venue_id " +
        "JOIN events ON performance.event_id = events.event_id " +
        "WHERE street_address = ? AND city = ? AND country = ? AND postal_code = ? AND (performance.performance_date > CURRENT_DATE OR (performance.performance_date = CURRENT_DATE AND performance.performance_time >= CURRENT_TIME)) " +
        "ORDER BY performance_date, performance_time"
      );
      upcoming_perf.setString(1, street_address);
      upcoming_perf.setString(2, city);
      upcoming_perf.setString(3, country);
      upcoming_perf.setString(4, postal_code);

      ResultSet rs = upcoming_perf.executeQuery();
      System.out.println("Upcoming performances at the venue located at provided address");
      System.out.printf("%-30s %-25s %-25s %-30s %-30s%n", "PerformanceID", "PerformanceDate", "PerformanceTime", "EventName", "VenueName");
      while (rs.next()) {
        System.out.printf("%-30d %-25s %-25s %-30s %-30s%n",
          rs.getInt("performance_id"),
          rs.getDate("performance_date"),
          rs.getTime("performance_time"),
          rs.getString("event_name"),
          rs.getString("venue_name")
        );
      }
    } catch (SQLException e) {
        System.err.println("Error occurred while executing upcomingPerformancesAtAddress query: " + e.getMessage());
    }
  }

  // query 4
  public static void performancesInRange() {
    try (Connection conn = Database.connect()) {
      Scanner scanner = new Scanner(System.in);

      System.out.print("Enter a performance Start Date (YYYY-MM-DD): ");
      String start_date = scanner.nextLine();

      System.out.print("Enter a performance End Date (YYYY-MM-DD): ");
      String end_date = scanner.nextLine();

      System.out.print("Enter number of tickets: ");
      int num_tickets = Integer.parseInt(scanner.nextLine());

      PreparedStatement upcoming_perf = conn.prepareStatement(
        "SELECT performance_id, performance_date, performance_time, event_name, venue_name, city, country " +
        "FROM events " +
        "JOIN performance as p ON p.event_id  = events.event_id " +
        "JOIN venue ON venue.venue_id = p.venue_id " +
        "WHERE p.performance_date >= ? AND p.performance_date <= ? AND p.performance_status = 'scheduled' AND (p.performance_date > CURRENT_DATE OR (p.performance_date = CURRENT_DATE AND p.performance_time >= CURRENT_TIME)) AND ? <= ((SELECT COUNT(*) " + 
        "FROM seat " +
        "WHERE p.venue_id = seat.venue_id) - " +
        "(SELECT COUNT(*) " + 
        "FROM blocks " +
        "WHERE blocks.performance_id = p.performance_id AND blocks.venue_id = p.venue_id) - " +
        "(SELECT COUNT(*) " +
        "FROM ticket " +
        "JOIN customerOrder ON p.performance_id = customerOrder.performance_id AND ticket.order_id = customerOrder.order_id " +
        "WHERE ticket_status = 'active' AND ticket.venue_id = p.venue_id) + " +
        "(SELECT IFNULL(SUM(capacity), 0) " +
        "FROM generalAdmissionSection AS gas " +
        "WHERE p.venue_id = gas.venue_id)) "
      );
      upcoming_perf.setString(1, start_date);
      upcoming_perf.setString(2, end_date);
      upcoming_perf.setInt(3, num_tickets);

      ResultSet rs = upcoming_perf.executeQuery();
      System.out.printf("All performances taking place between %s and %s have at least %d of tickets available.\n", start_date, end_date, num_tickets);

      System.out.printf("%-30s %-25s %-25s %-30s %-30s %-15s %-15s%n", "PerformanceID", "PerformanceDate", "PerformanceTime", "EventName", "VenueName", "City", "Country");
      while (rs.next()) {
        System.out.printf("%-30d %-25s %-25s %-30s %-30s %-15s %-15s%n",
          rs.getInt("performance_id"),
          rs.getDate("performance_date"),
          rs.getTime("performance_time"),
          rs.getString("event_name"),
          rs.getString("venue_name"),
          rs.getString("city"),
          rs.getString("country")
        );
      }
    } catch (SQLException e) {
        System.err.println("Error occurred while executing performancesInRange query: " + e.getMessage());
    }
  }

  // query 6
  public static void seatMapSummary() {
    try (Connection conn = Database.connect()) {
      Scanner scanner = new Scanner(System.in);

      System.out.print("Enter a performance ID: ");
      int p_id = Integer.parseInt(scanner.nextLine());

      PreparedStatement seats_summary = conn.prepareStatement(
        "SELECT section_name, tier_name, price, available, sold, blocked " +
        "FROM seatMap " +
        "WHERE performance_id = ? "
      );
      
      seats_summary.setInt(1, p_id);
      ResultSet rs = seats_summary.executeQuery();
      System.out.printf("A seat map summary for performance ID: %d\n", p_id);

      System.out.printf("%-30s %-25s %-15s %-30s %-15s %-15s%n", "SectionName", "TierName", "Price", "AvailableSeats/Capacity", "Sold", "Blocked");
      while (rs.next()) {
        System.out.printf("%-30s %-25s %-15.2f %-30d %-15d %-15d%n",
          rs.getString("section_name"),
          rs.getString("tier_name"),
          rs.getDouble("price"),
          rs.getInt("available"),
          rs.getInt("sold"),
          rs.getInt("blocked")
        );
      }
    } catch (SQLException e) {
        System.err.println("Error occurred while executing seatMapSummary query: " + e.getMessage());
    }
  }

  // query 5
  public static void filtersPerformances() {
    try (Connection conn = Database.connect()) {
      Scanner scanner = new Scanner(System.in);
      System.out.println("For upcoming performances, these filters below are all optional, so if you want to skip press enter otherwise type a value: ");

      System.out.print("Enter a city (press enter if you want to skip): ");
      String city = scanner.nextLine();
      if (city.isBlank()) {
        city = null;
      }

      System.out.print("Enter a segment (press enter if you want to skip): ");
      String segment = scanner.nextLine();
      if (segment.isBlank()) {
        segment = null;
      }

      System.out.print("Enter a genre (press enter if you want to skip): ");
      String genre = scanner.nextLine();
      if (genre.isBlank()) {
        genre = null;
      }

      System.out.print("Enter a performance start date (YYYY-MM-DD) (press enter if you want to skip): ");
      String start_date = scanner.nextLine();
      if (start_date.isBlank()) {
        start_date = null;
      }

      System.out.print("Enter a performance end date (YYYY-MM-DD) (press enter if you want to skip): ");
      String end_date = scanner.nextLine();
      if (end_date.isBlank()) {
        end_date = null;
      }

      System.out.print("Enter a starting price ticket (press enter if you want to skip): ");
      String tmp = scanner.nextLine();
      Double start_price = null;

      if (!tmp.isBlank()) {
        start_price = Double.parseDouble(tmp);
      }

      System.out.print("Enter a ending price ticket (press enter if you want to skip): ");
      tmp = scanner.nextLine();
      Double end_price = null;
      if (!tmp.isBlank()) {
        end_price = Double.parseDouble(tmp);
      }

      System.out.print("Enter number of tickets (press enter if you want to skip): ");
      tmp = scanner.nextLine();
      Integer num_tickets = null;
      if (!tmp.isBlank()) {
        num_tickets = Integer.parseInt(tmp);
      }

      System.out.print("Enter type of section (reserved/general/press enter if you want to skip): ");
      String sec_type = scanner.nextLine();
      if (sec_type.isBlank()) {
        sec_type = null;
      }

      if (sec_type != null && !sec_type.equals("reserved") && !sec_type.equals("general")) {
        System.err.println("Invalid section type. Must be 'general' or 'reserved'.");
        return;
      }

      PreparedStatement filter_perf = conn.prepareStatement(
        "WITH cheapest_ticket AS ( " +
        "SELECT performance_id, MIN(price) AS cheapest, SUM(available) AS totalAvailable " +
        "FROM seatMap " +
        "WHERE available > 0 AND section_type = IFNULL(?, section_type) " +
        "GROUP BY performance_id " +
        "HAVING MIN(price) >= IFNULL(?, 0) AND MIN(price) <= IFNULL(?, 10000000000) AND SUM(available) >= IFNULL(?, 0)) " + 
        "SELECT p.performance_id, performance_date, performance_time, event_name, venue_name, cheapest AS cheapestTicket, totalAvailable " +
        "FROM events AS e " +
        "JOIN performance AS p ON p.event_id = e.event_id " +
        "JOIN venue AS v ON v.venue_id = p.venue_id " +
        "JOIN belongsTo AS b ON b.event_id = e.event_id " +
        "JOIN cheapest_ticket AS ct ON ct.performance_id = p.performance_id " +
        "WHERE p.performance_status = 'scheduled' AND city = IFNULL(?, city) AND seg_name = IFNULL(?, seg_name) AND genre_name = IFNULL(?, genre_name) AND p.performance_date >= IFNULL(?, p.performance_date) AND p.performance_date <= IFNULL(?, p.performance_date) "
      );

      filter_perf.setString(1, sec_type);
      filter_perf.setObject(2, start_price);
      filter_perf.setObject(3, end_price);
      filter_perf.setObject(4, num_tickets);
      filter_perf.setString(5, city);
      filter_perf.setString(6, segment);
      filter_perf.setString(7, genre);
      filter_perf.setString(8, start_date);
      filter_perf.setString(9, end_date);

      ResultSet rs = filter_perf.executeQuery();
      System.out.println("Searching for Performances by applying optional filters");
      System.out.printf("%-30s %-25s %-25s %-30s %-30s %-25s %-25s%n", "PerformanceID", "PerformanceDate", "PerformanceTime", "EventName", "VenueName", "CheapestTicket", "TotalAvailable");
      while (rs.next()) {
        System.out.printf("%-30d %-25s %-25s %-30s %-30s %-25s %-25s%n",
          rs.getInt("performance_id"),
          rs.getDate("performance_date"),
          rs.getTime("performance_time"),
          rs.getString("event_name"),
          rs.getString("venue_name"),
          rs.getDouble("cheapestTicket"),
          rs.getInt("totalAvailable")
        );
      }

    } catch (SQLException e) {
        System.err.println("Error occurred while executing filtersPerformances query: " + e.getMessage());
    }
  }
}