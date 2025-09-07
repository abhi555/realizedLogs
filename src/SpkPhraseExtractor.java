import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class SpkPhraseExtractor {

    private static final String INPUT_FILE = "resources/combined_logs/cleaned_combined_no_steaming.log";
    private static final String OUTPUT_FILE = "resources/phrases.txt";

    public static void main(String[] args) {
        Path inputPath = Paths.get(INPUT_FILE);
        Path outputPath = Paths.get(OUTPUT_FILE);

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            String currentLine = reader.readLine();
            String nextLine;

            while (currentLine != null) {
                nextLine = reader.readLine();

                if (nextLine == null) break; // reached end of file

                // Check if both current and next lines contain SPK
                if (currentLine.contains("SPK") && nextLine.contains("SPK")) {

                    // Extract text after SPK for both lines
                    String currentSpkText = extractTextAfterSpk(currentLine);
                    String nextSpkText = extractTextAfterSpk(nextLine);

                    // Count words in the first SPK text
                    int wordCount = currentSpkText.trim().isEmpty() ? 0 : currentSpkText.trim().split("\\s+").length;

                    // If word count > 1, append it to phrases.txt
                    if (wordCount > 1) {
                        writer.write(currentSpkText);
                        writer.newLine();
                    }

                    // Compare texts after removing spaces
                    String noSpaceCurrent = currentSpkText.replaceAll("\\s+", "");
                    String noSpaceNext = nextSpkText.replaceAll("\\s+", "");

                    if (noSpaceCurrent.equalsIgnoreCase(noSpaceNext)) {
                        System.out.println("Matching SPK lines found:");
                        System.out.println(currentLine);
                        System.out.println(nextLine);
                        System.out.println("--------------------------");
                    }
                }

                currentLine = nextLine;
            }

            System.out.println("✅ Processing complete. Extracted phrases written to: " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts the part of the line after "SPK" and removes quotes.
     */
    private static String extractTextAfterSpk(String line) {
        int spkIndex = line.indexOf("SPK");
        if (spkIndex == -1) return "";
        String text = line.substring(spkIndex + 3).trim();
        return text.replace("\"", "");
    }
}
