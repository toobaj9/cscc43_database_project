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
                    UserOperations.createUser();
                    break;
                case 2:
                    UserOperations.deleteUser();
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
            System.out.println("6. Block seat");
            System.out.println("7. Unblock seat");
            System.out.println("8. Back to Main Menu");

            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    OrganizerOperations.createEvent();
                    break;
                case 2:
                    OrganizerOperations.addPerformance();
                    break;
                case 3:
                    OrganizerOperations.definePriceTier();
                    break;
                case 4:
                    OrganizerOperations.assignSectionsToTier();
                    break;
                case 5:
                    OrganizerOperations.updateTierPrice();
                    break;
                case 6:
                    OrganizerOperations.blockSeat();
                    break;
                case 7:
                    OrganizerOperations.unblockSeat();
                    break;
                case 8:
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
                    CustomerOperations.purchaseTickets();
                    break;
                case 2:
                    CustomerOperations.cancelTicket();
                    break;
                case 3:
                    CustomerOperations.createResaleListing();
                    break;
                case 4:
                    CustomerOperations.withdrawListing();
                    break;
                case 5:
                    CustomerOperations.buyResaleTicket();
                    break;
                case 6:
                    CustomerOperations.addReview();
                    break;
                case 7:
                    CustomerOperations.viewOwnershipHistory();
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
            System.out.println("Searchs & Queries Menu:");
            System.out.println("1. Query 1");
            System.out.println("2. Query 2");
            System.out.println("3. Query 3");
            System.out.println("4. Query 4");
            System.out.println("5. Query 5");
            System.out.println("6. Query 6");
            System.out.println("7. Query 7");
            System.out.println("8. Back to Main Menu");

            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    QueryOperations.query1();
                    break;
                case 2:
                    QueryOperations.query2();
                    break;
                case 3:
                    QueryOperations.query3();
                    break;
                case 4:
                    QueryOperations.query4();
                    break;
                case 5:
                    QueryOperations.query5();
                    break;
                case 6:
                    QueryOperations.query6();
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
            System.out.println("1. Report 1");
            System.out.println("2. Report 2");
            System.out.println("3. Report 3");
            System.out.println("4. Report 4");
            System.out.println("5. Report 5");
            System.out.println("6. Report 6");
            System.out.println("7. Report 7");
            System.out.println("8. Report 8");
            System.out.println("9. Report 9");
            System.out.println("10. Back to Main Menu");

            int choice = readInt("Please select an option: ");

            switch (choice) {
                case 1:
                    ReportOperations.report1();
                    break;
                case 2:
                    ReportOperations.report2();
                    break;
                case 3:
                    ReportOperations.report3();
                    break;
                case 4:
                    ReportOperations.report4();
                    break;
                case 5:
                    ReportOperations.report5();
                    break;
                case 6:
                    ReportOperations.report6();
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