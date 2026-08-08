package com.mikstermedia.controller;

import com.mikstermedia.model.Artist;
import com.mikstermedia.model.BlogPost;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final ArtistRepository artistRepository;
    private final BlogPostRepository blogPostRepository;

    private static final String BASE_URL = "https://mikstermedia.com";

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateSitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Static Routes
        addUrl(xml, "/", today, "daily", "1.0");
        addUrl(xml, "/charts", today, "weekly", "0.9");
        addUrl(xml, "/new-releases", today, "daily", "0.8");
        addUrl(xml, "/artists", today, "weekly", "0.8");
        addUrl(xml, "/songs", today, "weekly", "0.8");
        addUrl(xml, "/genres", today, "monthly", "0.6");
        addUrl(xml, "/blog", today, "daily", "0.9");
        addUrl(xml, "/join", today, "monthly", "0.7");
        addUrl(xml, "/about", today, "monthly", "0.5");

        // Dynamic Routes: Artists
        List<Artist> artists = artistRepository.findAll();
        for (Artist artist : artists) {
            String encodedName = URLEncoder.encode(artist.getName(), StandardCharsets.UTF_8).replace("+", "%20");
            addUrl(xml, "/creator/" + encodedName, today, "weekly", "0.8");
        }

        // Dynamic Routes: Blog Posts
        List<BlogPost> posts = blogPostRepository.findAllByStatusOrderByPublishedDateDesc("PUBLISHED");
        for (BlogPost post : posts) {
            addUrl(xml, "/blog/" + post.getSlug(), today, "monthly", "0.9");
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    private void addUrl(StringBuilder xml, String path, String lastMod, String changeFreq, String priority) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(BASE_URL).append(path).append("</loc>\n");
        xml.append("    <lastmod>").append(lastMod).append("</lastmod>\n");
        xml.append("    <changefreq>").append(changeFreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }
}
