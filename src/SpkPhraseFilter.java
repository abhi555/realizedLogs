import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class SpkPhraseFilter {

    private static final String INPUT_FILE = "resources/combined_logs/cleaned_combined_no_steaming_updated.log";

    public static void main(String[] args) {
        try {
            Path inputPath = Paths.get(INPUT_FILE);

            // Read all lines from file
            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            int total_steaming = 0;

            for (String line : lines) {
                if (line.contains("SPK")) {
                    // Extract quoted string after SPK
                    String quotedText = extractQuotedTextAfterSpk(line);

                    if (!quotedText.isEmpty()) {
                        // Count number of words
                        int wordCount = quotedText.trim().split("\\s+").length;

                        if (wordCount > 8) {
                            total_steaming++;
//                            System.out.println("Line: " + line);
                            System.out.println("Extracted phrase (" + wordCount + " words): " + quotedText);
                            System.out.println("----");
                        }
                    }
                }
            }
            System.out.println("\n******************total_steaming="+ total_steaming);
            System.out.println("✅ Done! Lines with SPK phrases having more than 5 words printed above.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract the string inside quotes after "SPK"
    private static String extractQuotedTextAfterSpk(String line) {
        int spkIndex = line.indexOf("SPK");
        if (spkIndex == -1) {
            return "";
        }

        // Look for first quote after SPK
        int firstQuote = line.indexOf('"', spkIndex);
        int secondQuote = line.indexOf('"', firstQuote + 1);

        if (firstQuote != -1 && secondQuote != -1 && secondQuote > firstQuote) {
            return line.substring(firstQuote + 1, secondQuote).trim();
        }

        return "";
    }
}
