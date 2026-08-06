import java.util.Scanner;

public class UserInterface {
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        boolean running = true;

        while (running) {
            printMainMenu();
            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    userManagementMenu();
                    break;
                case 2:
                    organizerOperationsMenu();
                    break;
                case 3:
                    customerOperationsMenu();
                    break;
                case 4:
                    searchAndQueriesMenu();
                    break;
                case 5:
                    reportsMenu();
                    break;
                case 6:
                    running = false;
                    System.out.println("Exiting the application. Goodbye!");
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
        System.out.println("Application terminated.");
    }

    private void printMainMenu() {
        System.out.println("My Tix");
        System.out.println("Main Menu:");
        System.out.println("1. User Management");
        System.out.println("2. Organizer Operations");
        System.out.println("3. Customer Operations");
        System.out.println("4. Searchs & Queries");
        System.out.println("5. Reports");
        System.out.println("6. Exit");
    }

    private void userManagementMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("User Management Menu:");
            System.out.println("1. Create User");
            System.out.println("2. Delete User");
            System.out.println("3. Back to Main Menu");
            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    System.out.print("Email: ");
                    String email = scanner.nextLine();

                    System.out.print("Name: ");
                    String name = scanner.nextLine();

                    System.out.print("Address: ");
                    String address = scanner.nextLine();

                    System.out.print("Date of Birth (YYYY-MM-DD): ");
                    String dob = scanner.nextLine();

                    System.out.print("Type (customer/organizer): ");
                    String type = scanner.nextLine().trim().toLowerCase();

                    UserOperations.createUser(
                        email,
                        name,
                        address,
                        dob,
                        type
                    );
                    break;
                case 2:
                    System.out.print("Enter the email of the user to delete: ");
                    String deleteEmail = scanner.nextLine().trim();
                    UserOperations.deleteUser(deleteEmail);
                    break;
                case 3:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void organizerOperationsMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("Organizer Operations Menu:");
            System.out.println("1. Create Event");
            System.out.println("2. Add Performance");
            System.out.println("3. Define Price Tier");
            System.out.println("4. Assign Sections to Tier");
            System.out.println("5. Update Tier Price");
            System.out.println("6. Block Seat");
            System.out.println("7. Unblock Seat");
            System.out.println("8. Cancel Performance");
            System.out.println("9. Back to Main Menu");

            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    System.out.print("Organizer email: ");
                    String organizerEmail = scanner.nextLine().trim();

                    System.out.print("Event name: ");
                    String eventName = scanner.nextLine().trim();

                    System.out.print("Resale cap (e.g. 1.20): ");
                    double resaleCap = Double.parseDouble(scanner.nextLine());

                    OrganizerOperations.createEvent(organizerEmail, eventName, resaleCap);
                    break;
                case 2:
                    System.out.print("Event ID: ");
                    int eventId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Venue ID: ");
                    int venueId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Performance Date (YYYY-MM-DD): ");
                    String date = scanner.nextLine();

                    System.out.print("Performance Time (HH:MM:SS): ");
                    String time = scanner.nextLine();

                    OrganizerOperations.addPerformance(eventId, venueId, date, time);
                    break;
                case 3:
                    System.out.print("Performance ID: ");
                    int performanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Tier Name: ");
                    String tierName = scanner.nextLine();

                    System.out.print("Price (>= 0): ");
                    Double price = Double.parseDouble(scanner.nextLine());

                    OrganizerOperations.definePriceTier(performanceId, tierName, price);
                    break;
                case 4:
                    System.out.print("Performance ID: ");
                    int assignPerformanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Venue ID: ");
                    int assignVenueId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Section name: ");
                    String sectionName = scanner.nextLine().trim();

                    System.out.print("Tier name: ");
                    String assignTierName = scanner.nextLine().trim();

                    OrganizerOperations.assignSectionsToTier(assignPerformanceId, assignVenueId,sectionName, assignTierName);
                    break;
                case 5:
                    System.out.print("Performance ID: ");
                    int updatePerformanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Tier name: ");
                    String updateTierName = scanner.nextLine().trim();

                    System.out.print("New price: ");
                    double newPrice = Double.parseDouble(scanner.nextLine());

                    OrganizerOperations.updateTierPrice(updatePerformanceId, updateTierName, newPrice);
                    break;
                case 6:
                    System.out.print("Organizer email: ");
                    String blockOrganizerEmail = scanner.nextLine().trim();

                    System.out.print("Performance ID: ");
                    int blockPerformanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Venue ID: ");
                    int blockVenueId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Section name: ");
                    String blockSectionName = scanner.nextLine().trim();

                    System.out.print("Row name: ");
                    String blockRowName = scanner.nextLine().trim();

                    System.out.print("Seat number: ");
                    int blockSeatNum = Integer.parseInt(scanner.nextLine());

                    System.out.print("Reason: ");
                    String blockReason = scanner.nextLine().trim();

                    OrganizerOperations.blockSeat(blockOrganizerEmail, blockPerformanceId, blockVenueId,
                        blockSectionName, blockRowName, blockSeatNum, blockReason);
                    break;
                case 7:
                    System.out.print("Organizer email: ");
                    String unblockOrganizerEmail = scanner.nextLine().trim();

                    System.out.print("Performance ID: ");
                    int unblockPerformanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Venue ID: ");
                    int unblockVenueId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Section name: ");
                    String unblockSectionName = scanner.nextLine().trim();

                    System.out.print("Row name: ");
                    String unblockRowName = scanner.nextLine().trim();

                    System.out.print("Seat number: ");
                    int unblockSeatNum = Integer.parseInt(scanner.nextLine());

                    OrganizerOperations.unblockSeat(unblockOrganizerEmail, unblockPerformanceId, unblockVenueId,
                        unblockSectionName, unblockRowName, unblockSeatNum);
                    break;
                case 8:
                    System.out.print("Organizer email: ");
                    String cancelOrganizerEmail = scanner.nextLine().trim();

                    System.out.print("Performance ID: ");
                    int cancelPerformanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Reason: ");
                    String cancelReason = scanner.nextLine();

                    OrganizerOperations.cancelPerformance(cancelOrganizerEmail, cancelPerformanceId, cancelReason);
                    break;
                case 9:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void customerOperationsMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("Customer Operations Menu:");
            System.out.println("1. Purchase Tickets");
            System.out.println("2. Cancel Ticket");
            System.out.println("3. Create Resale Listing");
            System.out.println("4. Withdraw Listing");
            System.out.println("5. Buy Resale Ticket");
            System.out.println("6. Add Review");
            System.out.println("7. View Ownership History");
            System.out.println("8. Back to Main Menu");

            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    System.out.print("Customer email: ");
                    String customerEmail = scanner.nextLine().trim();

                    System.out.print("Card number: ");
                    String cardNum = scanner.nextLine().trim();

                    System.out.print("Performance ID: ");
                    int purchasePerformanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Venue ID: ");
                    int purchaseVenueId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Section name: ");
                    String purchaseSection = scanner.nextLine().trim();

                    System.out.print("Quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());

                    CustomerOperations.purchaseTickets(customerEmail, cardNum, purchasePerformanceId, purchaseVenueId, purchaseSection, quantity, scanner);
                    break;
                case 2:
                    System.out.print("Customer email: ");
                    String cancelCustomerEmail = scanner.nextLine().trim();

                    System.out.print("Ticket ID: ");
                    int cancelTicketId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Cancellation reason: ");
                    String cancelReason = scanner.nextLine().trim();

                    CustomerOperations.cancelTicket(cancelCustomerEmail, cancelTicketId, cancelReason);
                    break;
                case 3:
                    System.out.print("Seller email: ");
                    String sellerEmail = scanner.nextLine().trim();

                    System.out.print("Ticket ID: ");
                    int listingTicketId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Listing price: ");
                    double listingPrice = Double.parseDouble(scanner.nextLine());

                    CustomerOperations.createResaleListing(sellerEmail, listingTicketId,listingPrice);
                    break;
                case 4:
                    System.out.print("Seller email: ");
                    String withdrawSellerEmail = scanner.nextLine().trim();

                    System.out.print("Listing ID: ");
                    int withdrawListingId = Integer.parseInt(scanner.nextLine());

                    CustomerOperations.withdrawListing(withdrawSellerEmail, withdrawListingId);
                    break;
                case 5:
                     System.out.print("Buyer email: ");
                    String buyerEmail = scanner.nextLine().trim();

                    System.out.print("Listing ID: ");
                    int listingId = Integer.parseInt(scanner.nextLine());

                    CustomerOperations.buyResaleTicket(buyerEmail,listingId);
                    break;
                case 6:
                    System.out.print("Customer email: ");
                    String reviewEmail = scanner.nextLine().trim();

                    System.out.print("Performance ID: ");
                    int reviewPerformanceId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Event rating (1-5): ");
                    int eventRating = Integer.parseInt(scanner.nextLine());

                    System.out.print("Venue rating (1-5): ");
                    int venueRating = Integer.parseInt(scanner.nextLine());

                    System.out.print("Comment: ");
                    String comment = scanner.nextLine().trim();

                    CustomerOperations.addReview(reviewEmail, reviewPerformanceId, eventRating, venueRating, comment);
                    break;
                case 7:
                    System.out.print("Ticket ID: ");
                    int historyTicketId = Integer.parseInt(scanner.nextLine());

                    CustomerOperations.viewOwnershipHistory(historyTicketId);
                    break;
                case 8:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void searchAndQueriesMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("Searches & Queries Menu:");
            System.out.println("1. Query 1: Search by Distance");
            System.out.println("2. Query 2: Search by Postal Code");
            System.out.println("3. Query 3: Search by Address");
            System.out.println("4. Query 4: Search by Date and Ticket Availability");
            System.out.println("5. Query 5: Search with Filters");
            System.out.println("6. Query 6: View Seat Map Summary");
            System.out.println("7. Query 7: Find Best Available Seats");
            System.out.println("8. Back to Main Menu");

            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    // query 1
                    QueryOperations.searchByDistance();
                    break;
                case 2:
                    // query 2
                    QueryOperations.searchByPostalCode();
                    break;
                case 3:
                    // query 3
                    QueryOperations.upcomingPerformancesAtAddress();
                    break;
                case 4:
                    // query 4
                    QueryOperations.performancesInRange();
                    break;
                case 5:
                    // query 5
                    QueryOperations.filtersPerformances();
                    break;
                case 6:
                    QueryOperations.seatMapSummary();
                    break;
                case 7:
                    QueryOperations.query7();
                    break;
                case 8:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void reportsMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("Reports Menu:");
            System.out.println("1. Report 1: Total Tickets Sold & Gross Revenue");
            System.out.println("2. Report 2: Total Events & Performances");
            System.out.println("3. Report 3: Rank Organizers by Gross Revenue");
            System.out.println("4. Report 4");
            System.out.println("5. Report 5: Rank Customers by Orders");
            System.out.println("6. Report 6: Cancelled Tickets & Performances Report");
            System.out.println("7. Report 7");
            System.out.println("8. Report 8");
            System.out.println("9. Report 9");
            System.out.println("10. Back to Main Menu");

            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    ReportOperations.report1(scanner);
                    break;
                case 2:
                    ReportOperations.report2(scanner);
                    break;
                case 3:
                    ReportOperations.report3(scanner);
                    break;
                case 4:
                    ReportOperations.report4();
                    break;
                case 5:
                    ReportOperations.report5(scanner);
                    break;
                case 6:
                    ReportOperations.report6(scanner);
                    break;
                case 7:
                    ReportOperations.report7();
                    break;
                case 8:
                    ReportOperations.report8();
                    break;
                case 9:
                    ReportOperations.report9();
                    break;
                case 10:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }
}