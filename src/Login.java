import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {
    private static Scanner scanner;
    private static Connection connection;


    //skapa ny användare
    static void newUser() {
        scanner = new Scanner(System.in);
        connection = Database.getConnection();

        System.out.println("Ange ditt namn:");
        String newName = scanner.nextLine();
        System.out.println("Ange e-post:");
        String newEmail = scanner.nextLine();
        System.out.println("Välj lösenord:");
        String newPassword = scanner.nextLine();

        String hashedPassword = Hashing.Encrypt(newPassword);
        String sql = "INSERT INTO users (name, email, hashed_password) VALUES (?, ?, ?)";

        try{
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,newName);
            ps.setString(2,newEmail);
            ps.setString(3, hashedPassword);
            ps.executeUpdate();
            System.out.println("Användaren är tillagd");

            int usersid = ActivityLog.getUsersIdFromEmail(connection, newEmail); //ropar in metoden för att ta ut id från email
            ActivityLog.log(connection, " " + usersid, "Användaren " + newEmail + " lades till"); //sparar ner i loggen

            HomePage.mainLibrary();


        }catch (SQLException ex){
            Database.SQLExceptionPrint(ex);
        }
    }


    //logga in metod
    static void loginMethod(){
        scanner = new Scanner(System.in);
        connection = Database.getConnection();

        System.out.println("Logga in ");
        System.out.println("Email: ");
        String email = scanner.nextLine();

        System.out.println("Lösenord: ");
        String password = scanner.nextLine();


        if (isValid(connection, email, password)) {
            lastLogin(connection, email); //updatera sista log in i loanlogg
            System.out.println("Du är nu inloggad");
            int usersid; //ropar in metoden för att ta ut id från email
            try {
                usersid = ActivityLog.getUsersIdFromEmail(connection, email);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            ActivityLog.log(connection, " " + usersid, "Användaren " + email + " loggade in"); //sparar ner i loggen

            System.out.println("Logged in as: " + Main.loggedInEmail);

            HomePage.mainLibrary();
        } else {
            System.out.println("Du blev inte inloggad, prova igen ");
            HomePage.firstPage();
        }
    }


    //logga ut metod
    static void logOutMethod(){
        scanner = new Scanner(System.in);
        connection = Database.getConnection();

        Main.loggedInEmail = "";
        Main.loggedInUserId = 0;

        System.out.println("Du är nu utloggad");
        HomePage.firstPage();

    }


//metod kollar så lösenord och email matchar
    private static boolean isValid(Connection connection, String email, String password) {
        String sql = "SELECT id, email, hashed_password FROM users WHERE email = ?";

        try {
            PreparedStatement ps = Login.connection.prepareStatement(sql);
            ps.setString(1, email);
            //   ps.setString(2, Hashing.Encrypt(password));


            ResultSet result = ps.executeQuery();

            if (result.next() && Hashing.Verify(password, result.getString("hashed_password"))) ;
            Main.loggedInUserId = (result.getInt("id"));
            Main.loggedInEmail = (result.getString("email"));

            return true;


        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);
            return false;
        }
    }

    //sparar senaste inloggning på användaren
    private static void lastLogin(Connection connection, String email) {
        String sql = "UPDATE Users SET lastLogin = CURRENT_TIMESTAMP WHERE email = ?";

        try {
            PreparedStatement ps = Login.connection.prepareStatement(sql);
                ps.setString(1, email);

                int rowsChanged = ps.executeUpdate();

                if (rowsChanged > 0) {
                    System.out.println("Användarens senaste log in har uppdaterats ");

                } else {
                    System.out.println("Ingen användare uppdaterades ");
                }

        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);
        }

    }
}


/*
System.out.println("hashat lösen " + Hashing.Encrypt("Nikolina"));
    String passwordDB ="BCC2D8F570CE6712FC3B14A13898E94E0F9A08F28C40CCA8916EDCD244FFA75C8B4EBFFE87D4C37BA13EC6CEFCDE463D";
    passwordDB = "a89e2811bb3490f3f4e1feadd1859ee137b545f93db6b8ca298599c7c4216207f7709edb5b70db8c8d05486aaca54a87";
     System.out.println(passwordDB);
        System.out.printf("Inloggningsstatus = %b\n", Hashing.Verify("Nikolina",passwordDB));
    //System.out.println("användarnamnet är : " + username);
 */