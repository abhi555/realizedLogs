import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class ReplaceStringsInLogs {

    private static final String CLEANED_LOGS_DIR = "resources/non_absence_data";
    private static final String TARGET_STRING = "LOC Home ";
    private static final String REPLACEMENT_STRING = "HOME_SCREEN ";

    public static void main(String[] args) {
        try {
            // Process all .txt files in the cleaned logs directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(CLEANED_LOGS_DIR), "*.txt")) {
                for (Path filePath : stream) {
                    replaceStringsInFile(filePath);
                }
            }

            System.out.println("✅ Replacement completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void replaceStringsInFile(Path filePath) throws IOException {
        // Read file content as lines
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        // Replace target string in each line
        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, lines.get(i).replace(TARGET_STRING, REPLACEMENT_STRING));
        }

        // Write updated content back to the same file
        Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Updated file: " + filePath.getFileName());
    }
}
