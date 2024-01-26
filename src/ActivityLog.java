import java.sql.*;


public class ActivityLog {

    //metod till skriva in aktiviteter från användarna
    static void log(Connection conn, String usersId, String message) {
        try {
            String user = (usersId == null || usersId.isEmpty()) ? null : usersId;
            String sql = "INSERT INTO ActivityLog SET usersId = ?, message = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, message);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            Database.SQLExceptionPrint(ex);
        }
    }

    //metod att ta ut userid från email
    static int getUsersIdFromEmail(Connection connection, String email) throws SQLException {

        String sql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id");
                } else {
                    throw new SQLException("Ingen användare hittad med: " + email);

                }

            }
        }
    }

}
