import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public final class OrganizerOperations {
    private OrganizerOperations() {
    }

    public static void createEvent(String organizerEmail, String eventName, double resaleCap) {
        String sql = """
            INSERT INTO events (organizer_email, event_name, resale_cap)
            VALUES (?, ?, ?)
            """;
        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, organizerEmail);
            statement.setString(2, eventName);
            statement.setDouble(3, resaleCap);
            int rows = statement.executeUpdate();
            if (rows == 1) {
                System.out.println("Event created successfully.");
            } else {
                System.err.println("Failed to create event.");
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while creating event: " + e.getMessage());
        }
    }

    public static void addPerformance(int eventId, int venueId, String performanceDate, String performanceTime) {
        String sql = """
            INSERT INTO performance
            (performance_date,
            performance_time,
            performance_status,
            event_id,
            venue_id)
            VALUES (?, ?, 'scheduled', ?, ?)
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, performanceDate);
            statement.setString(2, performanceTime);
            statement.setInt(3, eventId);
            statement.setInt(4, venueId);

            int rows = statement.executeUpdate();

            if (rows == 1) {
                System.out.println("Performance added successfully.");
            } else {
                System.out.println("Failed to add performance.");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Failed to add performance due to constraint violation.");
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Failed to add performance.");
            System.out.println(e.getMessage());
        }
    }

    public static void definePriceTier(int performanceId, String tierName, double price) {
        if (tierName.isBlank()) {
            System.err.println("Tier name cannot be empty.");
            return;
        }

        if (price < 0) {
            System.err.println("Price cannot be negative.");
            return;
        }
        String sql = """
            INSERT INTO priceTier
            (performance_id, tier_name, price)
            VALUES (?, ?, ?)
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, performanceId);
            statement.setString(2, tierName);
            statement.setDouble(3, price);
            statement.executeUpdate();
            System.out.println("Price tier added successfully.");

        } catch (SQLException e) {
            System.out.println("Failed to add price tier. Check that performance ID exists and that the tier name is unique for this performance.");
            System.out.println(e.getMessage());
        }
    }

    public static void assignSectionsToTier(int assignPerformanceId, int assignVenueId, String sectionName, String assignTierName) {
        String validateSql = """
            SELECT COUNT(*)
            FROM performance p
            JOIN section s
            ON s.venue_id = p.venue_id
            JOIN priceTier pt
            ON pt.performance_id = p.performance_id
            WHERE p.performance_id = ? AND p.venue_id = ? AND s.section_name = ? AND pt.tier_name = ?
            """;
        String insertSql = """
            INSERT INTO assignedToTier
            (performance_id, venue_id, section_name, tier_name)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = Database.connect();
             PreparedStatement validateStatement = connection.prepareStatement(validateSql);
             PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            validateStatement.setInt(1, assignPerformanceId);
            validateStatement.setInt(2, assignVenueId);
            validateStatement.setString(3, sectionName);
            validateStatement.setString(4, assignTierName);

            try (var result = validateStatement.executeQuery()) {
                result.next();

                if (result.getInt(1) == 0) {
                    System.err.println(
                        "Assignment failed. Check that the performance, venue, section, and tier are valid and belong together."
                    );
                    return;
                }
            }

            insertStatement.setInt(1, assignPerformanceId);
            insertStatement.setInt(2, assignVenueId);
            insertStatement.setString(3, sectionName);
            insertStatement.setString(4, assignTierName);
            insertStatement.executeUpdate();
                
            System.out.println("Section assigned to tier successfully.");
            
        } catch (SQLException e) {
            System.out.println("Failed to assign section to tier.");
            System.out.println(e.getMessage());
        }
    }

    public static void updateTierPrice(int updatePerformanceId, String updateTierName, double newPrice) {
        if (newPrice < 0) {
            System.err.println("Price cannot be negative.");
            return;
        }

        String checkSql = """
            SELECT pt.tier_name, p.performance_date, p.performance_time,
            COUNT(t.ticket_id) AS tickets_sold
            FROM priceTier pt
            JOIN performance p
            ON p.performance_id = pt.performance_id
            LEFT JOIN assignedToTier att
            ON att.performance_id = pt.performance_id
            AND att.tier_name = pt.tier_name
            LEFT JOIN ticket t
            ON t.venue_id = att.venue_id
            AND t.section_name = att.section_name
            LEFT JOIN customerOrder co
            ON co.order_id = t.order_id
            AND co.performance_id = pt.performance_id
            WHERE pt.performance_id = ?
            AND pt.tier_name = ?
            GROUP BY pt.tier_name, p.performance_date, p.performance_time
            """;

        String updateSql = """
            UPDATE priceTier
            SET price = ?
            WHERE performance_id = ?
            AND tier_name = ?
            """;

        try (Connection connection = Database.connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement checkStatement =
                        connection.prepareStatement(checkSql)) {

                checkStatement.setInt(1, updatePerformanceId);
                checkStatement.setString(2, updateTierName);

                try (ResultSet result = checkStatement.executeQuery()) {
                    if (!result.next()) {
                        System.err.println("Performance or tier does not exist.");
                        connection.rollback();
                        return;
                    }

                    java.time.LocalDateTime performanceDateTime =
                        java.time.LocalDateTime.of(
                            result.getDate("performance_date").toLocalDate(),
                            result.getTime("performance_time").toLocalTime()
                        );

                    if (!performanceDateTime.isAfter(
                            java.time.LocalDateTime.now())) {
                        System.err.println(
                            "Only tiers for future performances can be updated."
                        );
                        connection.rollback();
                        return;
                    }

                    if (result.getInt("tickets_sold") > 0) {
                        System.err.println(
                            "Price cannot be changed because tickets have already "
                            + "been sold in this tier."
                        );
                        connection.rollback();
                        return;
                    }
                }
            }

            try (PreparedStatement updateStatement =
                        connection.prepareStatement(updateSql)) {

                updateStatement.setDouble(1, newPrice);
                updateStatement.setInt(2, updatePerformanceId);
                updateStatement.setString(3, updateTierName);
                updateStatement.executeUpdate();
            }

            connection.commit();
            System.out.println("Tier price updated successfully.");

        } catch (SQLException e) {
            System.err.println(
                "Error updating tier price: " + e.getMessage()
            );
        }
    }

    public static void blockSeat(String organizerEmail, int performanceId, int venueId,
                        String sectionName, String rowName, int seatNum, String reason) {
        if (reason.isBlank()) {
            System.err.println("A reason must be provided.");
            return;
        }

        String validateSql = """
            SELECT COUNT(*)
            FROM performance p
            JOIN events e
            ON e.event_id = p.event_id
            JOIN seat s
            ON s.venue_id = p.venue_id
            JOIN assignedToTier att
            ON att.performance_id = p.performance_id
            AND att.venue_id = s.venue_id
            AND att.section_name = s.section_name
            WHERE p.performance_id = ?
            AND p.venue_id = ?
            AND e.organizer_email = ?
            AND p.performance_status = 'scheduled'
            AND s.section_name = ?
            AND s.row_name = ?
            AND s.seat_num = ?
            """;

        String soldSql = """
            SELECT COUNT(*)
            FROM reserveSeat rs
            JOIN ticket t
            ON t.ticket_id = rs.ticket_id
            JOIN customerOrder co
            ON co.order_id = t.order_id
            WHERE co.performance_id = ?
            AND rs.venue_id = ?
            AND rs.section_name = ?
            AND rs.row_name = ?
            AND rs.seat_num = ?
            AND t.ticket_status = 'active'
            """;

        String insertSql = """
            INSERT INTO blocks
            (performance_id, section_name, venue_id,
            row_name, seat_num, reason)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = Database.connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement validateStatement = connection.prepareStatement(validateSql)) {
                validateStatement.setInt(1, performanceId);
                validateStatement.setInt(2, venueId);
                validateStatement.setString(3, organizerEmail);
                validateStatement.setString(4, sectionName);
                validateStatement.setString(5, rowName);
                validateStatement.setInt(6, seatNum);

                try (ResultSet result = validateStatement.executeQuery()) {
                    result.next();

                    if (result.getInt(1) == 0) {
                        System.err.println(
                            "The performance, organizer, venue, section, or seat is invalid."
                        );
                        connection.rollback();
                        return;
                    }
                }
            }

            try (PreparedStatement soldStatement = connection.prepareStatement(soldSql)) {
                soldStatement.setInt(1, performanceId);
                soldStatement.setInt(2, venueId);
                soldStatement.setString(3, sectionName);
                soldStatement.setString(4, rowName);
                soldStatement.setInt(5, seatNum);

                try (ResultSet result = soldStatement.executeQuery()) {
                    result.next();

                    if (result.getInt(1) > 0) {
                        System.err.println(
                            "The seat cannot be blocked because it has already been sold."
                        );
                        connection.rollback();
                        return;
                    }
                }
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStatement.setInt(1, performanceId);
                insertStatement.setString(2, sectionName);
                insertStatement.setInt(3, venueId);
                insertStatement.setString(4, rowName);
                insertStatement.setInt(5, seatNum);
                insertStatement.setString(6, reason);

                insertStatement.executeUpdate();
            }

            connection.commit();
            System.out.println("Seat blocked successfully.");

        } catch (SQLException e) {
            System.err.println(
                "Could not block the seat. It may already be blocked: "
                + e.getMessage()
            );
        }
    }

    public static void unblockSeat(String organizerEmail, int performanceId, int venueId, String sectionName, String rowName, int seatNum) {
        String deleteSql = """
            DELETE b
            FROM blocks b
            JOIN performance p
            ON p.performance_id = b.performance_id
            JOIN events e
            ON e.event_id = p.event_id
            WHERE b.performance_id = ?
            AND b.venue_id = ?
            AND b.section_name = ?
            AND b.row_name = ?
            AND b.seat_num = ?
            AND e.organizer_email = ?
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement =
                connection.prepareStatement(deleteSql)) {

            statement.setInt(1, performanceId);
            statement.setInt(2, venueId);
            statement.setString(3, sectionName);
            statement.setString(4, rowName);
            statement.setInt(5, seatNum);
            statement.setString(6, organizerEmail);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted == 1) {
                System.out.println("Seat unblocked successfully.");
            } else {
                System.err.println(
                    "Seat could not be unblocked. Check that it is currently blocked and that the organizer manages this performance."
                );
            }

        } catch (SQLException e) {
            System.err.println(
                "Error unblocking seat: " + e.getMessage()
            );
        }
    }

    public static void cancelPerformance(String organizerEmail, int performanceId, String reason) {
        Connection connection = null;
        try {
            connection = Database.connect();
            connection.setAutoCommit(false);

            // Verify organizer owns the performance
            String verifySql = """
                SELECT COUNT(*)
                FROM performance p
                JOIN events e
                ON p.event_id = e.event_id
                WHERE p.performance_id = ?
                AND e.organizer_email = ?
                AND p.performance_status = 'scheduled'
                """;

            try (PreparedStatement statement =
                        connection.prepareStatement(verifySql)) {

                statement.setInt(1, performanceId);
                statement.setString(2, organizerEmail);

                ResultSet rs = statement.executeQuery();
                rs.next();

                if (rs.getInt(1) == 0) {
                    System.err.println(
                            "Invalid organizer or performance is already cancelled."
                    );
                    connection.rollback();
                    return;
                }
            }

            // Cancel performance
            String updatePerformance = """
                UPDATE performance
                SET performance_status = 'cancelled'
                WHERE performance_id = ?
                """;

            try (PreparedStatement statement =
                        connection.prepareStatement(updatePerformance)) {

                statement.setInt(1, performanceId);
                statement.executeUpdate();
            }

            // Record performance cancellation
            String insertCancellation = """
                INSERT INTO cancelsPerformance
                (performance_id, organizer_email, reason)
                VALUES (?, ?, ?)
                """;

            try (PreparedStatement statement = connection.prepareStatement(insertCancellation)) {
                statement.setInt(1, performanceId);
                statement.setString(2, organizerEmail);
                statement.setString(3, reason);

                statement.executeUpdate();
            }

            // Get all active tickets for this performance
            String ticketSql = """
                SELECT t.ticket_id,
                    t.face_value,
                    co.customer_email
                FROM ticket t
                JOIN customerOrder co
                ON t.order_id = co.order_id
                WHERE co.performance_id = ?
                AND t.ticket_status = 'active'
                """;

            try (PreparedStatement ticketStatement = connection.prepareStatement(ticketSql)) {
                ticketStatement.setInt(1, performanceId);
                ResultSet tickets = ticketStatement.executeQuery();
                while (tickets.next()) {
                    int ticketId = tickets.getInt("ticket_id");
                    double refund = tickets.getDouble("face_value");
                    String customerEmail =
                            tickets.getString("customer_email");

                    // Update ticket status
                    try (PreparedStatement updateTicket = connection.prepareStatement("""
                                    UPDATE ticket
                                    SET ticket_status = 'refunded'
                                    WHERE ticket_id = ?
                                    """)) {

                        updateTicket.setInt(1, ticketId);
                        updateTicket.executeUpdate();
                    }

                    // Withdraw active resale listing if it exists
                    try (PreparedStatement withdrawListing = connection.prepareStatement("""
                            UPDATE resaleListing
                            SET listing_status = 'withdrawn'
                            WHERE ticket_id = ?
                            AND listing_status = 'active'
                            """)) {
                        withdrawListing.setInt(1, ticketId);
                        withdrawListing.executeUpdate();
                    }

                    // Record refund
                    try (PreparedStatement insertRefund = connection.prepareStatement("""
                                    INSERT INTO cancelsTicket
                                    (ticket_id,
                                    customer_email,
                                    refund_amount,
                                    reason)
                                    VALUES (?, ?, ?, ?)
                                    """)) {

                        insertRefund.setInt(1, ticketId);
                        insertRefund.setString(2, customerEmail);
                        insertRefund.setDouble(3, refund);
                        insertRefund.setString(4, "Performance cancelled: " + reason);

                        insertRefund.executeUpdate();
                    }
                }
            }

            connection.commit();

            System.out.println(
                    "Performance cancelled successfully."
            );

        } catch (SQLException e) {

            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ignored) {
            }
            System.err.println("Failed to cancel performance: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}