import java.sql.*;

public class Main {

    static String loggedInEmail = "";
    static int loggedInUserId;

    public static void main(String[] args) {
      //  GUI gui = new GUI();
        new Main();

        String username = (System.getenv("DBUSER") != null ? System.getenv("DBUSER") : "root");
        String password = (System.getenv("DBPASS") != null ? System.getenv("DBPASS") : "robert");

        //configurerar databasen
        Database.username = username;
        Database.password = password;
        Database.database = "library";

        Connection connection = Database.getConnection();
        if (connection == null) {
            System.out.println("Kunde inte ansluta till databasen");
            System.exit(-1);
            return;
        }
        HomePage.firstPage();

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
