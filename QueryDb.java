import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class QueryDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/mmai?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "mmai";
        String pass = "T@ylor55";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("=== ARTISTS (first 5) ===");
            try (ResultSet rs = stmt.executeQuery("SELECT id, name, email FROM artists LIMIT 5")) {
                while (rs.next()) {
                    System.out.printf("- ID: %d | Name: %s | Email: %s%n", rs.getLong("id"), rs.getString("name"), rs.getString("email"));
                }
            }
            
            System.out.println("\n=== TRACKS (first 5) ===");
            try (ResultSet rs = stmt.executeQuery("SELECT id, title, creator, creator_email FROM tracks LIMIT 5")) {
                while (rs.next()) {
                    System.out.printf("- ID: %d | Title: %s | Creator: %s | Email: %s%n", rs.getLong("id"), rs.getString("title"), rs.getString("creator"), rs.getString("creator_email"));
                }
            }
        }
    }
}
