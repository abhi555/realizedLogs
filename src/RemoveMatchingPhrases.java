import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class RemoveMatchingPhrases {

    private static final String PHRASES_FILE = "resources/phrases.txt";
    private static final String INPUT_FILE = "resources/cleaned_no_steaming.log";
    private static final String OUTPUT_FILE = "resources/cleaned_no_steaming_no_phrases.log";

    public static void main(String[] args) {
        try {
            // Read all phrases from phrases.txt into HashSet (remove spaces)
            Set<String> phrasesSet = new HashSet<>();
            List<String> phrases = Files.readAllLines(Paths.get(PHRASES_FILE), StandardCharsets.UTF_8);
            for (String phrase : phrases) {
                phrasesSet.add(phrase.replaceAll("\\s+", ""));
            }

            // Read input file
            List<String> lines = Files.readAllLines(Paths.get(INPUT_FILE), StandardCharsets.UTF_8);
            List<String> outputLines = new ArrayList<>();

            for (String line : lines) {
                if (!line.contains("SPK")) {
                    // Keep non-SPK lines
                    outputLines.add(line);
                    continue;
                }

                // Extract text inside quotes after SPK
                String spkText = extractQuotedText(line);
                if (spkText == null) {
                    // If no quoted text found, keep line
                    outputLines.add(line);
                    continue;
                }

                // Remove all spaces
                String normalizedSpkText = spkText.replaceAll("\\s+", "");

                boolean skipLine = false;
                for (String phrase : phrasesSet) {
                    if (normalizedSpkText.equalsIgnoreCase(phrase) || normalizedSpkText.contains(phrase)) {
                        skipLine = true;
                        break;
                    }
                }

                if (!skipLine) {
                    outputLines.add(line);
                }
            }

            // Write remaining lines to output file
            Files.write(Paths.get(OUTPUT_FILE), outputLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Filtering complete!");
            System.out.println("   Output saved to: " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract text inside quotes after SPK
    private static String extractQuotedText(String line) {
        int spkIndex = line.indexOf("SPK");
        if (spkIndex == -1) return null;

        int firstQuote = line.indexOf('"', spkIndex);
        int lastQuote = line.indexOf('"', firstQuote + 1);

        if (firstQuote != -1 && lastQuote != -1) {
            return line.substring(firstQuote + 1, lastQuote);
        }
        return null;
    }
}
