import javax.security.auth.kerberos.EncryptionKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Hashing {

    private static final int SALT_LENGTH = 16;

    public static String Encrypt(String password){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        System.out.println("Salt1: " + byteArrayToHexString(salt));

        try{
            //skapar en instance av sha-256 hash funktionen
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Lägg till vårt salt
            digest.update(salt);

            //gör hash för lösen

            byte[] hashedPassword = digest.digest(password.getBytes());

            return byteArrayToHexString(hashedPassword) + byteArrayToHexString(salt);

        } catch (NoSuchAlgorithmException ex) {
            return null;
        }

    }

    public static boolean Verify (String password, String hashedPassword){
        String passwordHash = hashedPassword.substring(0, 64);
        String saltHex = hashedPassword.substring(64);
       // System.out.println("Salt1.5:" + saltHex);
        byte[] salt = hexStringToByteArray(saltHex);
       // System.out.println("Salt2: " + byteArrayToHexString(salt));

        try{
            //skapar en instance av sha-256 hash funktionen
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Lägg till vårt salt
            digest.update(salt);

            //gör hash för lösen
            byte[] hashedInputPassword = digest.digest(password.getBytes());
            //System.out.println("hashedInput: " + byteArrayToHexString(hashedInputPassword));
            String hashedInputPasswordHex = byteArrayToHexString(hashedInputPassword);
            return passwordHash.equals(hashedInputPasswordHex);
        } catch (NoSuchAlgorithmException ex) {
            return false;
        }

    }

    //omvandla till en läsbar sträng
    //binär byte array till sträng med hexdecimal
    private static String byteArrayToHexString(byte[] array){
        StringBuilder sb = new StringBuilder();
        for(byte b : array ) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    //hexa decimal sträng binär byte array
    private static byte[] hexStringToByteArray(String s){
        int length = s.length();
        byte[] data = new byte[length/2];
        for(int i =0; i < length; i += 2) { //
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16 )); // 1 x 16
        }
        return data;
    }
}
