public class TestRegex {
    public static void main(String[] args) {
        String text = "20 عاما";
        boolean matches = text.matches(".*[\\p{IsArabic}\\p{IsCyrillic}\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}\\p{IsHangul}\\p{IsThai}\\p{IsDevanagari}].*");
        System.out.println("Matches: " + matches);
    }
}
