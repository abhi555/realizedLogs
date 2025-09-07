import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class FilterSpkLines {

    private static final String INPUT_FILE = "resources/combined_logs/cleaned_combined_no_steaming_updated.log";
    private static final String OUTPUT_FILE = "resources/cleaned_no_steaming.log";

    public static void main(String[] args) {
        try {
            Path inputPath = Paths.get(INPUT_FILE);
            Path outputPath = Paths.get(OUTPUT_FILE);

            // Read all lines from input file
            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            List<String> filteredLines = new ArrayList<>();

            for (String line : lines) {
                if (line.contains("SPK")) {
                    // Extract string inside quotes
                    String quotedText = extractQuotedText(line);

                    if (quotedText != null) {
                        // Count number of words
                        int wordCount = quotedText.trim().split("\\s+").length;

                        // Skip if more than 8 words
                        if (wordCount > 8) {
                            continue;
                        }
                    }
                }
                // Copy all other lines (SPK with <=8 words or non-SPK lines)
                filteredLines.add(line);
            }

            // Write the filtered lines to output file
            Files.write(outputPath, filteredLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Processing complete!");
            System.out.println("   Output saved to: " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract text inside double quotes
    private static String extractQuotedText(String line) {
        int firstQuote = line.indexOf('"');
        int lastQuote = line.lastIndexOf('"');
        if (firstQuote != -1 && lastQuote != -1 && firstQuote < lastQuote) {
            return line.substring(firstQuote + 1, lastQuote);
        }
        return null; // No quoted text found
    }
}
