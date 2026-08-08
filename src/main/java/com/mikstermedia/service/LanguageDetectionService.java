package com.mikstermedia.service;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import com.google.common.base.Optional;

@Slf4j
@Service
public class LanguageDetectionService {

    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;

    @PostConstruct
    public void init() {
        log.info("Initializing LanguageDetectionService (loading language profiles)...");
        try {
            // Load all built-in profiles (around 70 languages)
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
            
            // Build the language detector
            this.languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(languageProfiles)
                    .build();
            
            // Factory for processing short strings
            this.textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
            
            log.info("LanguageDetectionService initialized successfully.");
        } catch (IOException e) {
            log.error("Failed to load language profiles. Language detection will fall back to returning true.", e);
        }
    }

    /**
     * Determines if a given text (like a song title) is likely English.
     * <p>
     * Note: Song titles are often very short (1-3 words) which makes language detection ambiguous.
     * To prevent false positives (blocking valid instrumental tracks like "Neon Drive"),
     * this method returns {@code true} if the text is English OR if the language detector
     * cannot confidently identify a non-English language. It only returns {@code false} if
     * it definitively detects a foreign language (e.g. Spanish, Japanese, Russian).
     *
     * @param text The song title or artist name to check.
     * @return true if English or ambiguous, false if confidently detected as non-English.
     */
    private static final List<String> FOREIGN_LANGUAGE_KEYWORDS = List.of(
        "hindi", "punjabi", "tamil", "telugu", "malayalam", "kannada", 
        "bengali", "gujarati", "marathi", "odia", "oriya", "santhali", 
        "bhojpuri", "urdu", "nepali", "sinhala", "spanish", "kpop", 
        "jpop", "cpop", "bollywood", "tollywood", "kollywood"
    );

    public boolean isLikelyEnglish(String text) {
        if (languageDetector == null || text == null || text.trim().isEmpty()) {
            return true; // Pass through if service failed to init or string is empty
        }

        String textLower = text.toLowerCase();

        // 1. Explicit keyword rejection for transliterated foreign songs 
        // (e.g. "Bas Tu Hi | Hindi AI Song" is written in Latin chars but is Hindi)
        for (String kw : FOREIGN_LANGUAGE_KEYWORDS) {
            if (textLower.contains(kw)) {
                log.debug("Filtered out non-English track via explicit keyword '{}'. Text: '{}'", kw, text);
                return false;
            }
        }

        // 2. Fast-path rejection for common non-Latin scripts
        // Expanded to include Indic scripts (Tamil, Odia, Bengali, etc.), Greek, Hebrew, etc.
        if (text.matches(".*[\\p{IsArabic}\\p{IsCyrillic}\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}\\p{IsHangul}\\p{IsThai}\\p{IsDevanagari}\\p{IsBengali}\\p{IsGurmukhi}\\p{IsGujarati}\\p{IsOriya}\\p{IsTamil}\\p{IsTelugu}\\p{IsKannada}\\p{IsMalayalam}\\p{IsSinhala}\\p{IsGreek}\\p{IsHebrew}].*")) {
            log.debug("Filtered out non-English track via script regex. Text: '{}'", text);
            return false;
        }
        
        TextObject textObject = textObjectFactory.forText(text);
        Optional<LdLocale> lang = languageDetector.detect(textObject);
        
        if (lang.isPresent()) {
            String detectedLanguage = lang.get().getLanguage();
            // If it confidently detects a language that is NOT English, return false.
            if (!"en".equalsIgnoreCase(detectedLanguage)) {
                log.debug("Filtered out non-English track. Detected language '{}' for text: '{}'", detectedLanguage, text);
                return false;
            }
        }
        
        // Return true if detected as English OR if it couldn't confidently detect any language (short title)
        return true;
    }
}
