import java.sql.*;
import java.util.Scanner;

public class Users {

    private static Scanner scanner;
    private static Connection connection;

    static void userChanges() {
        scanner = new Scanner(System.in);
        System.out.println("Vill du \n1. Ändra namn\n2. Ändra e-post\n3. Ändra lösenord\n4. Radera användare\n9. Gå tillbaka");
        String input = scanner.nextLine();

        do {
            switch (input) {
                case "1": updateName(); break;
                case "2": updateEmail(); break;
                case "3": updatePassword(); break;
                case "4": deleteUsers(); break;
                case "9": HomePage.mainLibrary();
                default: System.out.println("Oops ogiltig input");
            }
        } while (!input.equalsIgnoreCase("99"));

    }

    //metod att ändra namn
    private static void updateName() {
        connection = Database.getConnection();
        scanner = new Scanner(System.in);

        System.out.print("Ange epost-adressen för användaren som skall ändras: ");
        String email = scanner.nextLine();
        System.out.print("Ange nytt namn på användaren: ");
        String namn = scanner.nextLine();

        String sql = "UPDATE Users SET name = ? WHERE email = ?";

        try {

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, namn);
            ps.setString(2, email);

            int changedRows = ps.executeUpdate();

            if (changedRows > 0) {
                int usersid = ActivityLog.getUsersIdFromEmail(connection, email); //ropar in metoden för att ta ut id från email
                ActivityLog.log(connection, " " + usersid, "Användaren " + email + " uppdaterade sitt namn till " + namn); //sparar ner i loggen
                System.out.println("Användaren är uppdaterad");
            } else {
                System.out.println("Kunde inte hitta en användare med " + email);
            }

            System.out.println("Vill du ändra fler? J/N ");
            String continueInput = scanner.nextLine();
            if ("n".equalsIgnoreCase(continueInput)) {
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

    //metod att ändra email
    private static void updateEmail() {
        connection = Database.getConnection();
        scanner = new Scanner(System.in);

        try {
            System.out.print("Ange epost-adressen för användaren som skall ändras: ");
            String previousEmail = scanner.nextLine();
            System.out.print("Ange ny email: ");
            String newEmail = scanner.nextLine();


            String sql = "UPDATE Users SET email = ? WHERE email = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, newEmail);
            ps.setString(2, previousEmail);

            int changedRows = ps.executeUpdate();

            if (changedRows > 0) {
                int usersid = ActivityLog.getUsersIdFromEmail(connection, newEmail); //ropar in metoden för att ta ut id från email
                ActivityLog.log(connection, " " + usersid, "Användaren " + newEmail + " bytte email från " + previousEmail + " till " + newEmail); //sparar ner i loggen
                System.out.println("Användaren är uppdaterad");
            } else {
                System.out.println("Kunde inte hitta en användare med " + previousEmail);
            }

            System.out.println("Vill du ändra fler? J/N ");
            String continueInput = scanner.nextLine();
            if ("n".equalsIgnoreCase(continueInput)) {
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

    //metod att ändra lösen
    private static void updatePassword() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();

        try {
            System.out.print("Ange epost-adressen för användaren som skall ändras: ");
            String email = scanner.nextLine();
            System.out.print("Ange nytt lösenord: ");
            String newPassword = scanner.nextLine();

            String hashedPassword = Hashing.Encrypt(newPassword);

            String sql = "UPDATE Users SET hashed_password = ? WHERE email = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, hashedPassword);
            ps.setString(2, email);

            int changedRows = ps.executeUpdate();

            if (changedRows > 0) {
                int usersid = ActivityLog.getUsersIdFromEmail(connection, email); //ropar in metoden för att ta ut id från email
                ActivityLog.log(connection, " " + usersid, "Användaren " + email + " bytte lösenord"); //sparar ner i loggen
                System.out.println("Användaren är uppdaterad");
            } else {
                System.out.println("Kunde inte hitta en användare med email " + email);
            }

            System.out.println("Vill du ändra fler? J/N ");
            String continueInput = scanner.nextLine();
            if ("n".equalsIgnoreCase(continueInput)) {
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

    //metod att radera användare.. den raderar faktiskt! men den buggar :P
    private static void deleteUsers() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();
        String email = Main.loggedInEmail;

        boolean booksReturned = false;

        String checkReturnedSql = "SELECT COUNT(*) FROM loanlogg WHERE usersid = ? AND returned IS NULL";

        try {
            PreparedStatement checkBooksReturnedStmt = connection.prepareStatement(checkReturnedSql);
            checkBooksReturnedStmt.setString(1, email);
            ResultSet result = checkBooksReturnedStmt.executeQuery();
            if (result.next()) {
                int hasBorrowed = result.getInt(1);
                if (hasBorrowed == 0) {
                    booksReturned = true;
                }
            }
        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);
        }

        if (!booksReturned) {
            System.out.println("Du kan inte radera ditt konto förrän alla lån är återlämnade.");
            return;
        }


        System.out.println("delete " + email);
        String deleteSql = "DELETE FROM users WHERE email = ?";

            try {
                PreparedStatement ps = connection.prepareStatement(deleteSql);
                ps.setString(1, email);
                int changedRows = ps.executeUpdate();

                if (changedRows > 0) {
                    System.out.println("Användaren " + email + " raderades");

                } else {
                    System.out.println("Något blev fel, fast om du hade raderat under tiden du var inloggad så blev det raderat ändå. Men om du raderade efter du skapat en ny användare så raderades det inte :P ");
                    return;
                }

            } catch (SQLException ex) {
                Database.SQLExceptionPrint(ex);
                System.out.println("Det gick inte att radera användare");
            }
    }


    //metod att visa lån
    static void showLoans() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();
        String email = Main.loggedInEmail;

        String sql = "SELECT l.startDate, l.endDate, b.title AS item, 'book' AS type " +
                "FROM loanlogg l " +
                "JOIN users u ON l.usersid = u.id " +
                "JOIN book_author ba ON l.book_authorid = ba.id " +
                "JOIN books b ON ba.booksid = b.id " +
                "WHERE u.email = ? AND l.returned IS NULL " +
                "UNION " +
                "SELECT l.startDate, l.endDate, m.name AS item, 'magazine' AS type " +
                "FROM loanlogg l " +
                "JOIN users u ON l.usersid = u.id " +
                "JOIN magazines m ON l.magazinesId = m.id " +
                "WHERE u.email = ? AND l.returned IS NULL ORDER BY startDate DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, email);
            ResultSet result = ps.executeQuery();

            boolean hasLoan = false;

            while (result.next()) {
                hasLoan = true;
                System.out.println(result.getString("item") + " - " + (result.getString("type") + " Lånad: " + result.getString("startDate") + " Ska vara returnerad: " + result.getString("endDate")));
            }

            if(!hasLoan){
                System.out.println("Du har inga lån");
                HomePage.mainLibrary();
            }


        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);
            System.out.println("Du hade nog inga lån ");
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //metod att visa aktivitet
    static void showUserActivity(){
        scanner = new Scanner(System.in);
        connection = Database.getConnection();
        String email = Main.loggedInEmail;

        String sql = "SELECT a.message, a.activitydate FROM activitylog a " +
                "JOIN users u ON a.usersid = u.id " +
                "WHERE u.email = ? ORDER BY a.activitydate DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet result = ps.executeQuery();

            while (result.next()) {
                System.out.println(result.getString("message") + "  " + result.getString("activitydate"));
            }

            System.out.println("9. Gå tillbaka");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("9")) {
                HomePage.mainLibrary();
            } else {
                HomePage.mainLibrary();
            }


        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);
            System.out.println("Du hade nog inga aktiviteter ");
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}