import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
public final class CustomerOperations {
    private CustomerOperations() {
    }

    public static void addPaymentInformation(String cardNum, String cardholderName, String expirationDate, String cvv) {
        if (cardNum.isBlank() || cardholderName.isBlank() || expirationDate.isBlank() || cvv.isBlank()) {
            System.err.println("All payment information fields are required.");
            return;
        }

        if (!cardNum.matches("\\d{16}")) {
            System.err.println("Card number must be 16 digits.");
            return;
        }

        if (!expirationDate.matches("\\d{2}/\\d{2}")) {
            System.err.println("Expiration date must be in MM/YY format.");
            return;
        }

        String sql = """
            INSERT INTO paymentInformation
            (card_num, cardholder_name, card_expiry, cvv)
            VALUES (?, ?, ?, ?)
            """;

        String[] expiryParts = expirationDate.split("/");
        String expiryDateFormatted = "20" + expiryParts[1] + "-" + expiryParts[0] + "-01";

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cardNum);
            statement.setString(2, cardholderName);
            statement.setString(3, expiryDateFormatted);
            statement.setString(4, cvv);
            statement.executeUpdate();

            System.out.println("Payment information added successfully.");

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to add payment information: " + exception.getMessage()
            );
        }
    }

    public static void purchaseTickets(String customerEmail, String cardNum, int performanceId, int venueId,
        String sectionName, int quantity, Scanner scanner) {
        if (quantity <= 0) {
            System.err.println("Quantity must be greater than zero.");
            return;
        }
        Connection connection = null;
        try {
            connection = Database.connect();
            connection.setAutoCommit(false);

            String sectionSql = """
                SELECT s.section_type, pt.price
                FROM performance p
                JOIN assignedToTier att
                ON att.performance_id = p.performance_id
                AND att.venue_id = p.venue_id
                JOIN section s
                ON s.venue_id = att.venue_id
                AND s.section_name = att.section_name
                JOIN priceTier pt
                ON pt.performance_id = att.performance_id
                AND pt.tier_name = att.tier_name
                JOIN customer c
                ON c.email = ?
                JOIN paymentInformation pi
                ON pi.card_num = ?
                WHERE p.performance_id = ?
                AND p.venue_id = ?
                AND p.performance_status = 'scheduled'
                AND s.section_name = ?
                """;

            String sectionType;
            double price;
            try (PreparedStatement statement = connection.prepareStatement(sectionSql)) {
                statement.setString(1, customerEmail);
                statement.setString(2, cardNum);
                statement.setInt(3, performanceId);
                statement.setInt(4, venueId);
                statement.setString(5, sectionName);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        System.err.println(
                                "Invalid customer, card, performance, venue, section, or tier assignment."
                        );
                        connection.rollback();
                        return;
                    }

                    sectionType = result.getString("section_type");
                    price = result.getDouble("price");
                }
            }

            String orderSql = """
                INSERT INTO customerOrder
                (card_num, performance_id, customer_email)
                VALUES (?, ?, ?)
                """;

            int orderId;

            try (PreparedStatement statement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, cardNum);
                statement.setInt(2, performanceId);
                statement.setString(3, customerEmail);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException(
                                "Order ID could not be generated."
                        );
                    }

                    orderId = keys.getInt(1);
                }
            }

            if (sectionType.equals("reserved")) {
                purchaseReservedTickets(connection, scanner, customerEmail, performanceId,
                        venueId, sectionName, quantity, orderId, price);
            } else {
                purchaseGeneralTickets(connection, customerEmail, performanceId, venueId,
                        sectionName, quantity, orderId, price);
            }

            connection.commit();
            System.out.println(
                    quantity + " ticket(s) purchased successfully. Order ID: " + orderId
            );

        } catch (SQLException exception) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println(
                    "Purchase failed: " + exception.getMessage()
            );

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private static void purchaseReservedTickets(Connection connection, Scanner scanner, String customerEmail,
        int performanceId, int venueId, String sectionName, int quantity, int orderId, double price) throws SQLException {
        String availabilitySql = """
            SELECT COUNT(*)
            FROM seat s
            WHERE s.venue_id = ?
            AND s.section_name = ?
            AND s.row_name = ?
            AND s.seat_num = ?
            AND NOT EXISTS (
                SELECT 1
                FROM blocks b
                WHERE b.performance_id = ?
                    AND b.venue_id = s.venue_id
                    AND b.section_name = s.section_name
                    AND b.row_name = s.row_name
                    AND b.seat_num = s.seat_num
            )
            AND NOT EXISTS (
                SELECT 1
                FROM reserveSeat rs
                JOIN ticket t
                    ON t.ticket_id = rs.ticket_id
                JOIN customerOrder co
                    ON co.order_id = t.order_id
                WHERE co.performance_id = ?
                    AND rs.venue_id = s.venue_id
                    AND rs.section_name = s.section_name
                    AND rs.row_name = s.row_name
                    AND rs.seat_num = s.seat_num
                    AND t.ticket_status = 'active'
            )
            """;

        for (int i = 1; i <= quantity; i++) {
            System.out.println("Seat " + i + ":");

            System.out.print("Row name: ");
            String rowName = scanner.nextLine().trim();

            System.out.print("Seat number: ");
            int seatNum = Integer.parseInt(
                    scanner.nextLine()
            );

            try (PreparedStatement statement = connection.prepareStatement(availabilitySql)) {
                statement.setInt(1, venueId);
                statement.setString(2, sectionName);
                statement.setString(3, rowName);
                statement.setInt(4, seatNum);
                statement.setInt(5, performanceId);
                statement.setInt(6, performanceId);

                try (ResultSet result = statement.executeQuery()) {
                    result.next();

                    if (result.getInt(1) == 0) {
                        throw new SQLException(
                                "Seat " + rowName + "-" + seatNum
                                + " does not exist, is blocked, or is sold."
                        );
                    }
                }
            }

            int ticketId = insertTicket(connection, orderId, venueId, sectionName, price);
            String reserveSql = """
                INSERT INTO reserveSeat
                (ticket_id, section_name, venue_id, row_name, seat_num)
                VALUES (?, ?, ?, ?, ?)
                """;

            try (PreparedStatement statement = connection.prepareStatement(reserveSql)) {
                statement.setInt(1, ticketId);
                statement.setString(2, sectionName);
                statement.setInt(3, venueId);
                statement.setString(4, rowName);
                statement.setInt(5, seatNum);
                statement.executeUpdate();
            }

            insertOwnership(connection, ticketId, customerEmail);
        }
    }

    private static void purchaseGeneralTickets(
        Connection connection,
        String customerEmail,
        int performanceId,
        int venueId,
        String sectionName,
        int quantity,
        int orderId,
        double price) throws SQLException {

        String capacitySql = 
            """
            SELECT ga.capacity - COUNT(t.ticket_id) AS remaining
            FROM generalAdmissionSection ga
            LEFT JOIN customerOrder co
            ON co.performance_id = ?
            LEFT JOIN ticket t
            ON t.order_id = co.order_id
            AND t.venue_id = ga.venue_id
            AND t.section_name = ga.section_name
            AND t.ticket_status = 'active'
            WHERE ga.venue_id = ?
            AND ga.section_name = ?
            GROUP BY ga.capacity
            """;

        try (PreparedStatement statement = connection.prepareStatement(capacitySql)) {
            statement.setInt(1, performanceId);
            statement.setInt(2, venueId);
            statement.setString(3, sectionName);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()
                        || result.getInt("remaining") < quantity) {

                    throw new SQLException(
                            "Not enough general-admission capacity."
                    );
                }
            }
        }

        for (int i = 0; i < quantity; i++) {
            int ticketId = insertTicket(connection, orderId, venueId, sectionName, price);
            insertOwnership(connection, ticketId, customerEmail);
        }
    }

    private static int insertTicket(Connection connection, int orderId, int venueId, String sectionName, double price) throws SQLException {
        String sql = """
            INSERT INTO ticket
            (face_value, ticket_status, order_id, venue_id, section_name)
            VALUES (?, 'active', ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setDouble(1, price);
            statement.setInt(2, orderId);
            statement.setInt(3, venueId);
            statement.setString(4, sectionName);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "Ticket ID could not be generated."
                    );
                }

                return keys.getInt(1);
            }
        }
    }

    private static void insertOwnership(Connection connection, int ticketId, String customerEmail) throws SQLException {
        String sql = """
            INSERT INTO ownsTicket
            (ticket_id, email)
            VALUES (?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ticketId);
            statement.setString(2, customerEmail);
            statement.executeUpdate();
        }
    }

    public static void cancelTicket(String customerEmail, int ticketId, String reason) {
        if (reason.isBlank()) {
            System.err.println("A cancellation reason is required.");
            return;
        }

        Connection connection = null;
        String verifySql = """
            SELECT t.face_value
            FROM ticket t
            JOIN customerOrder co
            ON co.order_id = t.order_id
            JOIN performance p
            ON p.performance_id = co.performance_id
            WHERE t.ticket_id = ?
            AND co.customer_email = ?
            AND t.ticket_status = 'active'
            AND TIMESTAMP(p.performance_date, p.performance_time)
                >= NOW() + INTERVAL 7 DAY
            AND p.performance_status = 'scheduled'
            """;

        try {
            connection = Database.connect();
            connection.setAutoCommit(false);

            double refundAmount;

            try (PreparedStatement verifyStatement = connection.prepareStatement(verifySql)) {
                verifyStatement.setInt(1, ticketId);
                verifyStatement.setString(2, customerEmail);

                try (ResultSet result = verifyStatement.executeQuery()) {
                    if (!result.next()) {
                        connection.rollback();
                        System.err.println(
                                "Cancellation failed. Check that you purchased the "
                                + "ticket, it is active, and the performance is at "
                                + "least seven days away."
                        );
                        return;
                    }

                    refundAmount = result.getDouble("face_value");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            UPDATE ticket
                            SET ticket_status = 'refunded'
                            WHERE ticket_id = ?
                            """)) {
                statement.setInt(1, ticketId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            UPDATE resaleListing
                            SET listing_status = 'withdrawn'
                            WHERE ticket_id = ?
                            AND listing_status = 'active'
                            """)) {

                statement.setInt(1, ticketId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                        """
                            UPDATE ownsTicket
                            SET ended_at = NOW()
                            WHERE ticket_id = ?
                            AND ended_at IS NULL
                            """)) {
                statement.setInt(1, ticketId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            INSERT INTO cancelsTicket
                            (ticket_id, customer_email, refund_amount, reason)
                            VALUES (?, ?, ?, ?)
                            """)) {
                statement.setInt(1, ticketId);
                statement.setString(2, customerEmail);
                statement.setDouble(3, refundAmount);
                statement.setString(4, reason);
                statement.executeUpdate();
            }

            connection.commit();
            System.out.println(
                    "Ticket cancelled successfully. Full refund: $" + String.format("%.2f", refundAmount)
            );

        } catch (SQLException exception) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }

            System.err.println(
                    "Ticket cancellation failed: " + exception.getMessage()
            );

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public static void createResaleListing(String sellerEmail, int ticketId, double listingPrice) {
        if (listingPrice < 0) {
            System.err.println("Listing price cannot be negative.");
            return;
        }

        String verifySql = """
            SELECT t.face_value,
                e.resale_cap
            FROM ticket t
            JOIN customerOrder co
            ON co.order_id = t.order_id
            JOIN performance p
            ON p.performance_id = co.performance_id
            JOIN events e
            ON e.event_id = p.event_id
            JOIN ownsTicket ot
            ON ot.ticket_id = t.ticket_id
            AND ot.ended_at IS NULL
            WHERE t.ticket_id = ?
            AND ot.email = ?
            AND t.ticket_status = 'active'
            AND p.performance_status = 'scheduled'
            AND TIMESTAMP(p.performance_date, p.performance_time) > NOW()
            AND NOT EXISTS (
                SELECT 1
                FROM resaleListing rl
                WHERE rl.ticket_id = t.ticket_id
                    AND rl.listing_status = 'active'
            )
            """;

        String insertSql = """
            INSERT INTO resaleListing
            (listing_price, listing_status, ticket_id, seller_email)
            VALUES (?, 'active', ?, ?)
            """;

        try (Connection connection = Database.connect();
            PreparedStatement verifyStatement = connection.prepareStatement(verifySql);
            PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            verifyStatement.setInt(1, ticketId);
            verifyStatement.setString(2, sellerEmail);

            double maximumPrice;
            try (ResultSet result = verifyStatement.executeQuery()) {
                if (!result.next()) {
                    System.err.println(
                            "Listing failed. Check that you currently own an active ticket for a future performance and that no active listing already exists."
                    );
                    return;
                }

                double faceValue = result.getDouble("face_value");
                double resaleCap = result.getDouble("resale_cap");
                maximumPrice = faceValue * resaleCap;
            }

            if (listingPrice > maximumPrice) {
                System.err.printf(
                        "Listing price exceeds the resale cap. Maximum allowed: $%.2f%n",
                        maximumPrice
                );
                return;
            }

            insertStatement.setDouble(1, listingPrice);
            insertStatement.setInt(2, ticketId);
            insertStatement.setString(3, sellerEmail);
            insertStatement.executeUpdate();

            System.out.println("Resale listing created successfully.");

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to create resale listing: " + exception.getMessage()
            );
        }
    }

    public static void withdrawListing(String sellerEmail, int listingId) {
        String sql = """
            UPDATE resaleListing
            SET listing_status = 'withdrawn'
            WHERE listing_id = ?
            AND seller_email = ?
            AND listing_status = 'active'
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, listingId);
            statement.setString(2, sellerEmail);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 1) {
                System.out.println("Resale listing withdrawn successfully.");
            } else {
                System.err.println(
                        "Listing could not be withdrawn. Check that it exists, belongs to this seller, and is still active."
                );
            }
        } catch (SQLException exception) {
            System.err.println(
                    "Failed to withdraw listing: " + exception.getMessage()
            );
        }
    }
    
    public static void buyResaleTicket(String buyerEmail, int listingId) {
        Connection connection = null;
        String verifySql = """
            SELECT rl.ticket_id,
                rl.listing_price,
                rl.seller_email
            FROM resaleListing rl
            JOIN ticket t
            ON t.ticket_id = rl.ticket_id
            JOIN customerOrder co
            ON co.order_id = t.order_id
            JOIN performance p
            ON p.performance_id = co.performance_id
            WHERE rl.listing_id = ?
            AND rl.listing_status = 'active'
            AND t.ticket_status = 'active'
            AND p.performance_status = 'scheduled'
            AND TIMESTAMP(p.performance_date, p.performance_time) > NOW()
            """;

        try {
            connection = Database.connect();
            connection.setAutoCommit(false);

            int ticketId;
            double salePrice;
            String sellerEmail;

            try (PreparedStatement verifyStatement = connection.prepareStatement(verifySql)) {
                verifyStatement.setInt(1, listingId);

                try (ResultSet result = verifyStatement.executeQuery()) {
                    if (!result.next()) {
                        connection.rollback();
                        System.err.println(
                                "Purchase failed. The listing is unavailable."
                        );
                        return;
                    }

                    ticketId = result.getInt("ticket_id");
                    salePrice = result.getDouble("listing_price");
                    sellerEmail = result.getString("seller_email");
                }
            }

            if (buyerEmail.equalsIgnoreCase(sellerEmail)) {
                connection.rollback();
                System.err.println("You cannot buy your own listing.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            SELECT email
                            FROM customer
                            WHERE email = ?
                            """)) {

                statement.setString(1, buyerEmail);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        connection.rollback();
                        System.err.println("Buyer account does not exist.");
                        return;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            UPDATE resaleListing
                            SET listing_status = 'sold'
                            WHERE listing_id = ?
                            AND listing_status = 'active'
                            """)) {

                statement.setInt(1, listingId);

                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    System.err.println("The listing is no longer available.");
                    return;
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            INSERT INTO purchaseListing
                            (listing_id, buyer_email, sale_price)
                            VALUES (?, ?, ?)
                            """)) {
                statement.setInt(1, listingId);
                statement.setString(2, buyerEmail);
                statement.setDouble(3, salePrice);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                        """
                            UPDATE ownsTicket
                            SET ended_at = NOW()
                            WHERE ticket_id = ?
                            AND ended_at IS NULL
                            """)) {

                statement.setInt(1, ticketId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            INSERT INTO ownsTicket
                            (ticket_id, email)
                            VALUES (?, ?)
                            """)) {

                statement.setInt(1, ticketId);
                statement.setString(2, buyerEmail);
                statement.executeUpdate();
            }

            connection.commit();
            System.out.printf(
                    "Resale ticket purchased successfully for $%.2f.%n", salePrice
            );

        } catch (SQLException exception) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println(
                    "Resale purchase failed: " + exception.getMessage()
            );

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public static void addReview(String email, int performanceId, int eventRating, int venueRating, String comment) {
        if (eventRating < 1 || eventRating > 5
                || venueRating < 1 || venueRating > 5) {
            System.err.println("Ratings must be between 1 and 5.");
            return;
        }

        if (comment.isBlank()) {
            System.err.println("Comment cannot be empty.");
            return;
        }

        String verifySql = """
            SELECT COUNT(*)
            FROM ownsTicket ot
            JOIN ticket t
            ON t.ticket_id = ot.ticket_id
            JOIN customerOrder co
            ON co.order_id = t.order_id
            JOIN performance p
            ON p.performance_id = co.performance_id
            WHERE ot.email = ?
            AND co.performance_id = ?
            AND TIMESTAMP(p.performance_date, p.performance_time) < NOW()
            """;

        String insertSql = """
            INSERT INTO reviews
            (email, performance_id, event_rating, venue_rating, comment)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = Database.connect();
            PreparedStatement verifyStatement = connection.prepareStatement(verifySql);
            PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            verifyStatement.setString(1, email);
            verifyStatement.setInt(2, performanceId);

            try (ResultSet result = verifyStatement.executeQuery()) {
                result.next();
                if (result.getInt(1) == 0) {
                    System.err.println(
                            "Review failed. The customer must have attended this past performance."
                    );
                    return;
                }
            }

            insertStatement.setString(1, email);
            insertStatement.setInt(2, performanceId);
            insertStatement.setInt(3, eventRating);
            insertStatement.setInt(4, venueRating);
            insertStatement.setString(5, comment);
            insertStatement.executeUpdate();

            System.out.println("Review added successfully.");

        } catch (SQLException exception) {
            System.err.println(
                    "Failed to add review. The customer may have already "
                    + "reviewed this performance."
            );
        }
    }

    public static void viewOwnershipHistory(int ticketId) {
        String sql = """
            SELECT email, acquired_at, ended_at
            FROM ownsTicket
            WHERE ticket_id = ?
            ORDER BY acquired_at
            """;

        try (Connection connection = Database.connect();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, ticketId);

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;

                System.out.println("Ownership history for ticket " + ticketId + ":");

                while (result.next()) {
                    found = true;

                    String email = result.getString("email");
                    String acquiredAt = result.getString("acquired_at");
                    String endedAt = result.getString("ended_at");

                    System.out.println(
                        "Owner: " + email
                        + " | Acquired: " + acquiredAt
                        + " | Ended: " + (endedAt == null ? "Current owner" : endedAt)
                    );
                }

                if (!found) {
                    System.err.println("No ownership history found for this ticket.");
                }
            }

        } catch (SQLException exception) {
            System.err.println("Failed to retrieve ownership history: " + exception.getMessage());
        }
    }
}