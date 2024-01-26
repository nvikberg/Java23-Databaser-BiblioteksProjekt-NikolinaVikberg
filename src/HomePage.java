import java.sql.Connection;
import java.util.Scanner;

public class HomePage {

    private static Scanner scanner;
    private static Connection connection;

    //log in sidan
    static void firstPage() {
        scanner = new Scanner(System.in);
        String input = "";
        do {
            System.out.println("Välkommen till Biblioteket!\n1. Logga in\n2. Ny användare\n3. Sök utan att logga in");
            input = scanner.nextLine();

            switch (input) {
                case "1": Login.loginMethod(); break;
                case "2": Login.newUser(); break;
                case "3": mainLibrary(); break;
                default: System.out.println("Ogiltigt val, prova igen");
            }

        } while (!input.equals("99"));
    }

    //meny sidan
    public static void mainLibrary() {
        scanner = new Scanner(System.in);
        String input;
        do {
            System.out.println("Välkommen till Biblioteket! Välj vad du vill göra");
            System.out.println("1. Sök Böcker\n2. Sök Tidningar\n3. Returnera\n4. Mina lån \n5. Ändra min användare \n6. Visa mina aktiviter\n8. Tillbaka till login/skapa ny\n9. Logga ut"); //sök på författare, sök på titel, se alla böcker
            input = scanner.nextLine();
            switch (input) {
                case "1": Books.booksMethod(); break;
                case "2": Magazines.magazinesMethod(); break;
                case "3": returns(); break;
                case "4": Users.showLoans(); break;
                case "5": Users.userChanges(); break;
                case "6": Users.showUserActivity(); break;
                case "8": firstPage(); break;
                case "9": Login.logOutMethod(); break;
                default: System.out.println("Ogiltigt val, prova igen");
            }
        } while (!input.equals("99"));
    }

    //returnera valen
    private static void returns() {
        System.out.println("Returnera\n1. Böcker\n2. Tidningar");
        String input = scanner.nextLine();
        switch (input) {
            case "1": Books.returnBooks(); break;
            case "2": Magazines.returnMagazines(); break;
            default: System.out.println("Ogiltigt val, prova igen");

        }
    }
}
