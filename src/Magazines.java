import java.sql.*;
import java.util.Scanner;

public class Magazines {
    private static Scanner scanner;
    private static Connection connection;

    static void magazinesMethod() {
        scanner = new Scanner(System.in);


        System.out.println("1. Sök tidning\n2. Visa alla tidningar\n9. Tillbaka till startsidan");
        String input = scanner.nextLine();

        do {
            switch (input) {
                case "1": borrowMagazines(); break;
                case "2": magazineInformation(); break;
                case "9": HomePage.mainLibrary(); break;
                default:
                    System.out.println("Ogiltigt val, prova igen");
            }

        } while (!input.equalsIgnoreCase("9"));


        scanner.close();
    }


    //metod att låna tidningar
    static void borrowMagazines() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();

        try {
            System.out.println("Skriv namnet på tidningen du söker ");
            String chosenMagazine = scanner.nextLine();
            String sql = "SELECT name, datePublished FROM magazines WHERE name = ?";
            PreparedStatement ps = Magazines.connection.prepareStatement(sql);
            ps.setString(1, chosenMagazine);
            ResultSet result = ps.executeQuery();

            while (result.next()) {
                System.out.println(result.getString("name") + " " + result.getString("datePublished"));


                //Kollar statusen om boken är utlånad eller inte
                String statusSql = "SELECT status FROM magazines WHERE name = ?";
                try {
                    PreparedStatement statusPs = connection.prepareStatement(statusSql);
                    statusPs.setString(1, chosenMagazine);
                    ResultSet statusRs = statusPs.executeQuery();

                    if (statusRs.next() && "Not Available".equals(statusRs.getString("status"))) {
                        System.out.println("Tyvärr den tidningen är utlånad just nu");
                        magazinesMethod();

                    }

                } catch (SQLException ex) {
                    Database.SQLExceptionPrint(ex);
                    System.out.println("fel meddelande");

                }

                System.out.println("Vill du låna " + chosenMagazine + "? J/N");
                String input = scanner.nextLine();

                if ("j".equalsIgnoreCase(input)) {
                    //uppdaterar statusen om tidningen blir utlånad
                    String updateStatusSql = "UPDATE magazines SET status = 'Not Available' WHERE name = ?";
                    try {
                        PreparedStatement updateStatusPs = connection.prepareStatement(updateStatusSql);
                        updateStatusPs.setString(1, chosenMagazine);
                        // ResultSet updateRs = updateStatusPs.executeUpdate();
                        int updateStatus = updateStatusPs.executeUpdate();

                        if (updateStatus > 0) {
                            System.out.println("Tidningens status är uppdaterad till 'Not available'");
                        } else {
                            System.out.println("Tidningens status är inte uppdaterad");
                        }
                    } catch (SQLException ex) {
                        Database.SQLExceptionPrint(ex);
                        System.out.println("Tidningens status är inte uppdaterad");
                    }

                    String email = Main.loggedInEmail;

                    String sql2 = "INSERT INTO loanlogg (usersid, magazinesid, startDate, endDate) " +
                            "VALUES ((SELECT id FROM users WHERE email = ?), " +
                            "(SELECT id FROM magazines WHERE name = ?), " +
                            "CURRENT_TIMESTAMP, " +
                            "DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 10 DAY))"; //
                    try {
                        PreparedStatement ps2 = Magazines.connection.prepareStatement(sql2);
                        ps2.setString(1, email);
                        ps2.setString(2, chosenMagazine);
                        //datumet läggs till automatiskt

                        int changedRows = ps2.executeUpdate();

                        if (changedRows > 0) {
                            System.out.println("Du har nu lånat " + chosenMagazine);
                            int usersid = ActivityLog.getUsersIdFromEmail(connection, email); //ropar in metoden för att ta ut id från email
                            ActivityLog.log(connection, " " + usersid, "Användaren " + email + " lånade " + chosenMagazine); //

                        } else {
                            System.out.println("Du har inte lånat: " + chosenMagazine);
                        }

                        System.out.println("Vill du göra en ny sökning? J/N ");
                        String continueInput = scanner.nextLine();

                        if ("n".equalsIgnoreCase(continueInput)) {
                            HomePage.mainLibrary();
                        }

                    } catch (SQLException ex) {
                        Database.SQLExceptionPrint(ex);
                        System.out.println("Du behöver vara inloggad för att låna, gå tillbaka till startsidan för att logga in");

                    }
                } else {
                    System.out.println("Du har inte lånat: " + chosenMagazine);
                    magazinesMethod();
                }
            }
        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);
        }
    }


    //metod att återlämna tidningar
    static void returnMagazines() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();
        String email = Main.loggedInEmail;


        try {
            System.out.println("Skriv namnet på tidningen du vill returnera ");
            String returnMagazine = scanner.nextLine();


            //kollar om användaren har lånat tidningen
            String loanCheckSql = "SELECT * FROM loanlogg WHERE usersid = (SELECT id FROM users WHERE email = ?) " +
                    "AND magazinesid = (SELECT id FROM magazines WHERE name = ?) AND returned IS NULL";

                PreparedStatement loanCheckPs = connection.prepareStatement(loanCheckSql);
                loanCheckPs.setString(1, email);
                loanCheckPs.setString(2, returnMagazine);
                ResultSet loanCheckResult = loanCheckPs.executeQuery();

                if (loanCheckResult.next()) {
                    System.out.println("Tidningen hittades"); //fortsätter sedan med resten av återlämingsmetoden

                    String sql = "SELECT name, datePublished FROM magazines WHERE name = ?";
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setString(1, returnMagazine);
                    ResultSet result = ps.executeQuery();

                    while (result.next()) {
                        System.out.println(result.getString("name") + " " + result.getString("datePublished"));

                        System.out.println("Vill du returnera " + returnMagazine + "? J/N");
                        String input = scanner.nextLine();


                        if ("j".equalsIgnoreCase(input)) {

                            String updateStatusSql = "UPDATE magazines SET status = 'Available' WHERE name = ?";
                            try {
                                PreparedStatement updateStatusPs = connection.prepareStatement(updateStatusSql);
                                updateStatusPs.setString(1, returnMagazine);
                                // ResultSet updateRs = updateStatusPs.executeUpdate();
                                int updateStatus = updateStatusPs.executeUpdate();

                                if (updateStatus > 0) {
                                    System.out.println("Tidningens status är uppdaterad till 'Available'");
                                } else {
                                    System.out.println("Tidningens status är inte uppdaterad");
                                }
                            } catch (SQLException ex) {
                                Database.SQLExceptionPrint(ex);
                                System.out.println("Tidningens status är inte uppdaterad");
                            }

                            String sql2 = "UPDATE loanlogg SET returned = true " +
                                    "WHERE usersid = (SELECT id FROM users WHERE email = ?) " +
                                    "AND magazinesid = (SELECT id FROM magazines WHERE name = ?)";


                            try {
                                PreparedStatement ps2 = connection.prepareStatement(sql2);
                                ps2.setString(1, email);
                                ps2.setString(2, returnMagazine);
                                //datumet läggs till automatiskt

                                int changedRows = ps2.executeUpdate();

                                if (changedRows > 0) {
                                    System.out.println("Du har nu returnerat " + returnMagazine);
                                    int usersid = ActivityLog.getUsersIdFromEmail(connection, email); //ropar in metoden för att ta ut id från email
                                    ActivityLog.log(connection, " " + usersid, "Användaren " + email + " returnerade " + returnMagazine); //sparar ner i loggen


                                } else {
                                    System.out.println("Du har inte lånat: " + returnMagazine);

                                }
                                System.out.println("Vill du göra returnera mer? J/N ");
                                String continueInput = scanner.nextLine();

                                if ("n".equalsIgnoreCase(continueInput)) {
                                    HomePage.mainLibrary();
                                }
                            } catch (SQLException ex) {
                                Database.SQLExceptionPrint(ex);
                                System.out.println("Du behöver vara inloggad för att returnera, gå tillbaka till startsidan för att logga in");

                            }
                        } else {
                            System.out.println("vad händer här 1");
                        }
                    }
                } else {
                    System.out.println("Du har inte lånat: " + returnMagazine + " eller så är den redan returnerad");
                }

            } catch (SQLException ex) {
            System.out.println("vad händer här 3");
                Database.SQLExceptionPrint(ex);
            }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //metod att visa alla tidningar
    private static void magazineInformation() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();
        String input = scanner.nextLine();

        do {
            try {
                String sql = "SELECT name, datePublished FROM magazines";
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet result = ps.executeQuery();
                System.out.println(" ");
                while (result.next()) {
                    System.out.println(result.getString("name") + " " + result.getDate("datePublished"));
                }

                System.out.println("9. Gå tillbaka");
                input = scanner.nextLine();

            } catch (SQLException ex) {
                Database.SQLExceptionPrint(ex);
            }
        } while (!input.equalsIgnoreCase("9"));
        magazinesMethod();


}
}