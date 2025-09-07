import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


public class CleanCombinedLog {

    private static final String INPUT_FILE = "resources/combined_logs/combined.log";
    private static final String OUTPUT_FILE = "resources/combined_logs/cleaned_combined.log";

    private static final String TARGET_STRING = "LOC Home";
    private static final String REPLACEMENT_STRING = "HOME_SCREEN";
    private static final String[] DELETE_KEYWORDS = {"LOC ", "CTL ", "PAG "};

    public static void main(String[] args) {
        try {
            Path inputPath = Paths.get(INPUT_FILE);
            Path outputPath = Paths.get(OUTPUT_FILE);

            // Read all lines from combined.log
            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            List<String> cleanedLines = new ArrayList<>();

            for (String line : lines) {

                // Keep date marker lines untouched
                if (line.startsWith("*[YY-MM-DD=")) {
                    cleanedLines.add(line);
                    continue;
                }

                String updatedLine = line;

                // If line contains "LOC Home"
                if (updatedLine.contains(TARGET_STRING)) {
                    // Replace "LOC Home" with "HOME_SCREEN"
                    int idx = updatedLine.indexOf(TARGET_STRING);
                    updatedLine = updatedLine.substring(0, idx) + REPLACEMENT_STRING;
                }

                // After replacement, delete any line that contains restricted keywords
                boolean deleteLine = false;
                for (String keyword : DELETE_KEYWORDS) {
                    if (updatedLine.contains(keyword)) {
                        deleteLine = true;
                        break;
                    }
                }

                // Keep the line if it doesn't match delete criteria
                if (!deleteLine && !updatedLine.trim().isEmpty()) {
                    cleanedLines.add(updatedLine);
                }
            }

            // Write cleaned lines to a new file
            Files.write(outputPath, cleanedLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Cleaned log file created: " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
