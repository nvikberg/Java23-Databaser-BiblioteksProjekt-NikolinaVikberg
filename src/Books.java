import java.sql.*;
import java.util.Scanner;

public class Books {
    private static Scanner scanner;
    private static Connection connection;

    //metod för alternativen användaren kan välja för böcker
    static void booksMethod() {
        scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Välj vad du vill göra\n1. Sök titlar\n2. Sök författare\n3. Visa alla böcker\n9. Tillbaka till startsidan");
            String input = scanner.nextLine();
            switch (input) {
                case "1": borrowBooks(); break;
                case "2": searchAuthors(); break;
                case "3": booksInformation(); break;
                case "9": HomePage.mainLibrary(); break;
                default: System.out.println("Ogiltigt val, prova igen");
            }
        }

    }

    //metod att låna böcker
    private static void borrowBooks() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();

        try {
            System.out.println("Skriv titeln på boken du vill låna ");
            String chosenBook = scanner.nextLine();
            String email = Main.loggedInEmail;
            //sql query hämtar in info om boken user har sökt
            String sql = "SELECT b.title, a.lastname AS authorslastname, a.firstname AS authorsfirstname, b.yearPublished " +
                    "FROM books b " +
                    "JOIN book_author ba ON b.id = ba.booksid " +
                    "JOIN authors a ON ba.authorsid = a.id " +
                    "WHERE b.title = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, chosenBook);
            ResultSet result = ps.executeQuery();

            //gettar infon från sql queryt
            while (result.next()) {
                System.out.println(result.getString("title") + " av " + result.getString("authorsfirstname") + " " + result.getString("authorslastname") + " utgiven år " + result.getDate("yearPublished"));

                //Kollar statusen om boken är utlånad eller inte
                String statusSql = "SELECT status FROM books WHERE title = ?";
                try {
                    PreparedStatement statusPs = connection.prepareStatement(statusSql);
                    statusPs.setString(1, chosenBook);
                    ResultSet statusRs = statusPs.executeQuery();

                    if (statusRs.next() && "Not Available".equals(statusRs.getString("status"))) {
                        System.out.println("Tyvärr den boken är utlånad just nu");
                        booksMethod();

                    }

                } catch (SQLException ex) {
                    Database.SQLExceptionPrint(ex);
                    System.out.println("fel meddelande");

                }

                //om boken var available fortsätter vi med låne metoden
                System.out.println("Vill du låna " + chosenBook + " J/N");
                String input = scanner.nextLine();


                if ("j".equalsIgnoreCase(input)) {
                    //uppdaterar statusen om boken blir utlånad
                    String updateStatusSql = "UPDATE books SET status = 'Not Available' WHERE title = ?";
                    try {
                        PreparedStatement updateStatusPs = connection.prepareStatement(updateStatusSql);
                        updateStatusPs.setString(1, chosenBook);
                        // ResultSet updateRs = updateStatusPs.executeUpdate();
                        int updateStatus = updateStatusPs.executeUpdate();

                        if (updateStatus > 0) {
                            System.out.println("Bokens status är uppdaterad till 'Not available'");
                        } else {
                            System.out.println("Bokens status är inte uppdaterad");
                        }
                    } catch (SQLException ex) {
                        Database.SQLExceptionPrint(ex);
                        System.out.println("Bokens status är inte uppdaterad");
                    }

                    //sql till låneloggen, insertar användareninfon, bokens info och start och end date med 30 dagar tills den ska vara returnerad
                    String sql2 = "INSERT INTO loanlogg (usersid, book_authorid, startDate, endDate) " +
                            "VALUES ((SELECT id FROM users WHERE email = ?), " +
                            "(SELECT ba.id FROM book_author ba " +
                            "JOIN books b ON ba.booksid = b.id " +
                            "WHERE b.title = ?), " +
                            "CURRENT_TIMESTAMP, " +
                            "DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY))"; //lägger på 30 dagar

                    try {
                        PreparedStatement ps2 = connection.prepareStatement(sql2);
                        ps2.setString(1, email);
                        ps2.setString(2, chosenBook);
                        //datumet läggs till automatiskt

                        int changedRows = ps2.executeUpdate();

                        if (changedRows > 0) {
                            int usersid = ActivityLog.getUsersIdFromEmail(connection, email); //ropar in metoden för att ta ut id från email
                            ActivityLog.log(connection, " " + usersid, "Användaren " + email + " lånade " + chosenBook); //sparar ner i loggen
                            System.out.println("Du har nu lånat " + chosenBook);
                        } else {
                            System.out.println("Du har inte lånat: " + chosenBook);
                        }

                        System.out.println("Vill du fortsätta söka? J/N ");
                        String continueInput = scanner.nextLine();
                        if ("n".equalsIgnoreCase(continueInput)) {
                            HomePage.mainLibrary();
                        }

                    } catch (SQLException x) {
                        Database.SQLExceptionPrint(x);
                        System.out.println("Du behöver vara inloggad för att låna, gå tillbaka till startsidan för att logga in");
                    }
                }
            }
               // System.out.println("Det finns ingen bok med den titeln ");
               // booksMethod();
        } catch (SQLException x) {
            Database.SQLExceptionPrint(x);
        }
    }

    //metod att återlämna böcker
    static void returnBooks() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();
        String email = Main.loggedInEmail;

        while (true) {
            System.out.println("Skriv titeln på boken du vill returnera ");
            String returnBook = scanner.nextLine();

            //kollar om användaren har lånat boken
            String loanCheckSql = "SELECT * FROM loanlogg WHERE usersid = (SELECT id FROM users WHERE email = ?) " +
                    "AND book_authorid = (SELECT ba.id FROM book_author ba " +
                    "JOIN books b ON ba.booksid = b.id WHERE b.title = ?) AND returned IS NULL";

            try {
                PreparedStatement loanCheckPs = connection.prepareStatement(loanCheckSql);
                loanCheckPs.setString(1, email);
                loanCheckPs.setString(2, returnBook);
                ResultSet loanCheckResult = loanCheckPs.executeQuery();

                if (loanCheckResult.next()) {
                    System.out.println("Boken hittades"); //fortsätter sedan med resten av återlämingsmetoden

                } else {
                    System.out.println("Du har inte lånat " + returnBook + " eller så är den redan returnerad.");
                    HomePage.mainLibrary();
                }

                //sql query för informationen om bok titeln
                String sql = "SELECT b.title, a.lastname AS authorslastname, a.firstname AS authorsfirstname, b.yearPublished " +
                        "FROM books b " +
                        "JOIN book_author ba ON b.id = ba.booksid " +
                        "JOIN authors a ON ba.authorsid = a.id " +
                        "WHERE b.title = ?";
                try {
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setString(1, returnBook);
                    ResultSet result = ps.executeQuery();

                    if (result.next()) {
                        //skriver ut bok infon
                        System.out.println(result.getString("title") + " av " + result.getString("authorsfirstname") + " " + result.getString("authorslastname") + " utgiven år " + result.getDate("yearPublished"));
                        System.out.println("Vill du returnera " + returnBook + " J/N");
                        String input = scanner.nextLine();

                        //om user vill återlämnna så uppdateras bokens status till available
                        if ("j".equalsIgnoreCase(input)) {
                            String updateStatusSql = "UPDATE books SET status = 'Available' WHERE title = ?";
                            try {
                                PreparedStatement updateStatusPs = connection.prepareStatement(updateStatusSql);
                                updateStatusPs.setString(1, returnBook);
                                // ResultSet updateRs = updateStatusPs.executeUpdate();
                                int updateStatus = updateStatusPs.executeUpdate();

                                if (updateStatus > 0) {
                                    System.out.println("Bokens status är uppdaterad till 'Available'");
                                } else {
                                    System.out.println("Bokens status är inte uppdaterad");
                                }
                            } catch (SQLException ex) {
                                Database.SQLExceptionPrint(ex);
                                System.out.println("Bokens status är inte uppdaterad");
                            }

                            //Uppdaterar loanlogg till att den boken är "returned" tillsammans med korrekt user
                            String sql2 = "UPDATE loanlogg SET returned = true " +
                                    "WHERE usersid = (SELECT id FROM users WHERE email = ?) " +
                                    "AND book_authorid = (SELECT ba.id FROM book_author ba " +
                                    "JOIN books b ON ba.booksid = b.id " +
                                    "WHERE b.title = ?)";

                            try {
                                PreparedStatement ps2 = connection.prepareStatement(sql2);
                                ps2.setString(1, email);
                                ps2.setString(2, returnBook);

                                int changedRows = ps2.executeUpdate();

                                if (changedRows > 0) {
                                    int usersid = ActivityLog.getUsersIdFromEmail(connection, email); //ropar in metoden för att ta ut id från email
                                    ActivityLog.log(connection, " " + usersid, "Användaren " + email + " returnerade " + returnBook);
                                    System.out.println("Du har nu returnerat " + returnBook);
                                } else {
                                    System.out.println("Du har inte lånat " + returnBook + " eller så är den redan returnerad.");
                                }

                                System.out.println("Vill du fortsätta returnera böcker? J/N ");
                                String continueInput = scanner.nextLine();

                                if ("j".equalsIgnoreCase(continueInput)) {
                                    returnBooks();
                                } else {
                                    HomePage.mainLibrary();
                                }

                            } catch (SQLException ex) {
                                Database.SQLExceptionPrint(ex);
                                System.out.println("Du behöver vara inloggad för att låna, gå tillbaka till startsidan för att logga in");

                            }
                        }
                    } else {
                        System.out.println("Ingen bok hittades med den titeln, kolla stavningen och prova igen");
                        return;


                    }

                } catch (SQLException ex) {
                    Database.SQLExceptionPrint(ex);
                }

            } catch (SQLException ex) {
                Database.SQLExceptionPrint(ex);


            }
        }
    }
//metod att söka författare
    private static void searchAuthors() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();

        while (true) {

            try {
                System.out.println("Vilken författare vill du söka på? Skriv efternamnet ");
                String chosenAuthor = scanner.nextLine();

                String sql = "SELECT a.lastname AS authorslastname, a.firstname AS authorsfirstname, b.title, b.yearPublished " +
                        "FROM book_author ba " +
                        "JOIN authors a ON a.id = ba.authorsId " +
                        "JOIN books b ON ba.booksId = b.id " +
                        "WHERE a.lastname = ?";

                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, chosenAuthor);
                ResultSet result = ps.executeQuery();

                while (result.next()) {
                    // System.out.println("Bok skriven av " + chosenAuthor);
                    System.out.println(result.getString("authorslastname") + " " + result.getString("authorsfirstname") + " " + result.getString("title") + " Utgiven år " + result.getString("yearPublished"));
                }

                System.out.println("Vill du fortsätta söka? J/N");
                String input = scanner.nextLine();
                if ("n".equalsIgnoreCase(input)) {
                    booksMethod();
                }
            } catch (SQLException ex) {
                Database.SQLExceptionPrint(ex);
            }

            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

    }

    //Metod att visa alla böcker i biblioteket
    private static void booksInformation() {
        connection = Database.getConnection();
        scanner = new Scanner(System.in);

        try {
            String sql = "SELECT id, (SELECT title FROM books WHERE id = booksid) AS books, " +
                    "(SELECT lastname FROM authors WHERE id = authorsid) AS authorslastname, " +
                    "(SELECT firstname FROM authors WHERE id = authorsid) AS authorsfirstname, " +
                    "(SELECT yearPublished FROM books WHERE id = booksid) AS yearPublished FROM book_author ORDER BY authorslastname DESC";

            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            System.out.println(" ");
            while (result.next()) {
                System.out.println(result.getString("books") + " av " +
                        result.getString("authorsfirstname") + " " + result.getString("authorslastname")
                        + " " + result.getDate("yearPublished"));
            }

            System.out.println("9. Gå tillbaka");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("9")) {
                booksMethod();
            } else {
                HomePage.mainLibrary();
            }

        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);

        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}


