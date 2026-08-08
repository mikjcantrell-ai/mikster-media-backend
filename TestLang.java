import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.google.common.base.Optional;
import com.optimaize.langdetect.i18n.LdLocale;
import java.util.List;

public class TestLang {
    public static void main(String[] args) throws Exception {
        List<LanguageProfile> profiles = new LanguageProfileReader().readAllBuiltIn();
        LanguageDetector detector = LanguageDetectorBuilder.create(NgramExtractors.standard()).withProfiles(profiles).build();
        TextObjectFactory factory = CommonTextObjectFactories.forDetectingOnLargeText();
        
        TextObject text = factory.forText("A");
        Optional<LdLocale> lang = detector.detect(text);
        System.out.println(lang.isPresent() ? lang.get().getLanguage() : "NONE");
    }
}
