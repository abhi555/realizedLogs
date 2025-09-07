import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ExtractSpkPhrases {

    private static final String INPUT_FILE = "resources/combined_logs/cleaned_combined_no_steaming.log";
    private static final String OUTPUT_FILE = "resources/combined_logs/cleaned_combined_no_steaming_updated.log";
    private static final String PHRASES_FILE = "resources/phrases.txt";

    public static void main(String[] args) {
        try {
            Path inputPath = Paths.get(INPUT_FILE);
            Path outputPath = Paths.get(OUTPUT_FILE);
            Path phrasesPath = Paths.get(PHRASES_FILE);

            // Read all lines from input file
            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            List<String> updatedLines = new ArrayList<>();

            // Create or clear phrases.txt at the start
            Files.write(phrasesPath, new ArrayList<>(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            for (int i = 0; i < lines.size(); i++) {
                String currentLine = lines.get(i);

                // Check if current line has SPK and next line exists and has SPK too
                if (currentLine.contains("SPK") && i + 1 < lines.size()) {
                    String nextLine = lines.get(i + 1);

                    if (nextLine.contains("SPK")) {
                        // Extract text after SPK for both lines
                        String currentSpkText = extractSpkText(currentLine);
                        String nextSpkText = extractSpkText(nextLine);

                        // Compare after removing spaces
                        String normalizedCurrent = currentSpkText.replaceAll("\\s+", "");
                        String normalizedNext = nextSpkText.replaceAll("\\s+", "");

                        if (normalizedCurrent.equalsIgnoreCase(normalizedNext)) {
                            // Count number of words in the phrase
                            int wordCount = currentSpkText.trim().split("\\s+").length;

                            if (wordCount > 1) {
                                // Append phrase to phrases.txt
                                Files.write(phrasesPath,
                                        (currentSpkText + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                                        StandardOpenOption.APPEND);
                            }

                            if (wordCount == 1) {
                                updatedLines.add(currentLine);
                            }

                            // Print both lines on console
                            System.out.println("Matching SPK lines found:");
                            System.out.println(currentLine);
                            System.out.println(nextLine);
                            System.out.println("----");

                            // Skip both SPK lines (remove them from output)
                            i++; // skip next line as well
                            continue;
                        }
                    }
                }

                // If not a matching SPK pair, keep the line
                updatedLines.add(currentLine);
            }

            // Write the cleaned log file without removed SPK lines
            Files.write(outputPath, updatedLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Process complete!");
            System.out.println("   Updated log saved to: " + OUTPUT_FILE);
            System.out.println("   Phrases saved to: " + PHRASES_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract text after "SPK" and remove quotes
    private static String extractSpkText(String line) {
        int spkIndex = line.indexOf("SPK");
        if (spkIndex == -1) return "";
        String afterSpk = line.substring(spkIndex + 3).trim();
        return afterSpk.replace("\"", "");
    }
}
