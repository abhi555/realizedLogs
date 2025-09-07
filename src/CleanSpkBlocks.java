import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class CleanSpkBlocks {

    private static final String INPUT_FILE = "resources/combined_logs/cleaned_combined.log";
    private static final String OUTPUT_FILE = "resources/combined_logs/cleaned_combined_no_steaming.log";

    public static void main(String[] args) {
        try {
            Path inputPath = Paths.get(INPUT_FILE);
            Path outputPath = Paths.get(OUTPUT_FILE);

            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            List<String> cleanedLines = new ArrayList<>();

            Set<String> seenSpkMessages = new HashSet<>();
            boolean insideSpkBlock = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                // Detect start of a new day or block end due to DEL DISPLAY
                if (line.startsWith("*[YY-MM-DD") || line.contains("DEL DISPLAY")) {
                    // Reset block data when day ends or DEL DISPLAY is hit
                    insideSpkBlock = false;
                    seenSpkMessages.clear();
                    cleanedLines.add(line);
                    continue;
                }

                // If the line contains SPK, we are inside a SPK block
                if (line.contains("SPK")) {
                    insideSpkBlock = true;

                    // Extract the message part after "SPK"
                    int spkIndex = line.indexOf("SPK");
                    String spkMessage = line.substring(spkIndex + 3).trim();

                    // Keep only the first occurrence of the same SPK message
                    if (!seenSpkMessages.contains(spkMessage)) {
                        seenSpkMessages.add(spkMessage);
                        cleanedLines.add(line);
                    } else {
                        System.out.println("delete line: " + spkMessage);
                    }

                } else {
                    // If not inside a SPK block, just add the line
                    if (!insideSpkBlock) {
                        cleanedLines.add(line);
                    }
                    // If inside SPK block but the line doesn't contain SPK,
                    // add it since it's part of the block but not duplicate check
                    else {
                        cleanedLines.add(line);
                    }
                }
            }

            // Write cleaned data to output file
            Files.write(outputPath, cleanedLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Cleaned SPK blocks written to: " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
