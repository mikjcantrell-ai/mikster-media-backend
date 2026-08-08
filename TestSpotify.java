import java.net.*;
import java.io.*;
import java.util.*;

public class TestSpotify {
    public static void main(String[] args) throws Exception {
        String token = "";
        File f = new File("src/main/resources/application.properties");
        Scanner sc = new Scanner(f);
        String clientId = null, clientSecret = null;
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.contains("spotify.client-id=")) clientId = line.split("=")[1].trim();
            if (line.contains("spotify.client-secret=")) clientSecret = line.split("=")[1].trim();
        }
        
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        URL tokenUrl = new URL("https://accounts.spotify.com/api/token");
        HttpURLConnection con = (HttpURLConnection) tokenUrl.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Basic " + auth);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setDoOutput(true);
        con.getOutputStream().write("grant_type=client_credentials".getBytes());
        
        InputStream is = con.getInputStream();
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String resp = s.hasNext() ? s.next() : "";
        token = resp.split("\"access_token\":\"")[1].split("\"")[0];
        
        int[] limits = {50, 49, 40, 30, 20};
        for(int limit : limits) {
            try {
                URL searchUrl = new URL("https://api.spotify.com/v1/search?type=track&limit=" + limit + "&market=US&q=artist:beyonce");
                HttpURLConnection scon = (HttpURLConnection) searchUrl.openConnection();
                scon.setRequestProperty("Authorization", "Bearer " + token);
                InputStream sis = scon.getInputStream();
                System.out.println("Limit " + limit + " returned HTTP 200");
            } catch (Exception e) {
                System.out.println("Limit " + limit + " failed: " + e.getMessage());
            }
        }
    }
}
