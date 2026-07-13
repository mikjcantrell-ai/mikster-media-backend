import java.util.Map;
import java.util.List;

public class TestResendPayload {
    public static void main(String[] args) throws Exception {
        String artistName = "Test Artist";
        String artistUrl = "https://mikstermedia.com/artists"; 
        String shareText = java.net.URLEncoder.encode("My artist profile is blowing up and just got its feature extended on the front page of @MiksterMedia! Let's keep it going, check out my tracks here: " + artistUrl, "UTF-8");
        String twitterUrl = "https://twitter.com/intent/tweet?text=" + shareText;
        String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + java.net.URLEncoder.encode(artistUrl, "UTF-8") + "&quote=" + shareText;

        String htmlContent = "<h2>Hi " + artistName + ",</h2>" +
                "<p>Your profile has been performing incredibly well with the community! Because of its popularity, we have decided to <strong>extend your feature</strong> on the front page of Mikster Media.</p>" +
                "<p>Keep the momentum going! We’d love for you to share the good news with your fans so they can continue to follow your work.</p>" +
                "<div style=\"background: #f4f4f4; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                "  <p style=\"margin-top:0;\"><strong>Share this on social media:</strong></p>" +
                "  <p style=\"font-style: italic;\">\"My artist profile is blowing up and just got its feature extended on the front page of @MiksterMedia! Let's keep it going, check out my tracks here: " + artistUrl + "\"</p>" +
                "  <div style=\"margin-top: 15px;\">" +
                "    <a href=\"" + twitterUrl + "\" style=\"display:inline-block; background:#1DA1F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px; margin-right:10px;\">Share on X (Twitter)</a>" +
                "    <a href=\"" + facebookUrl + "\" style=\"display:inline-block; background:#1877F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px;\">Share on Facebook</a>" +
                "  </div>" +
                "</div>" +
                "<p>Congratulations again!</p>" +
                "<p>— The Mikster Media Team</p>";
        
        System.out.println(htmlContent);
    }
}
