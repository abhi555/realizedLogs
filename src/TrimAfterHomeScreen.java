import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class TrimAfterHomeScreen {

    private static final String CLEANED_LOGS_DIR = "resources/non_absence_data";
    private static final String TARGET = " HOME_SCREEN";

    public static void main(String[] args) {
        try {
            // Process all .txt files in the directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(CLEANED_LOGS_DIR), "*.txt")) {
                for (Path filePath : stream) {
                    trimLinesAfterHomeScreen(filePath);
                }
            }

            System.out.println("✅ Trimming after HOME_SCREEN completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void trimLinesAfterHomeScreen(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<String> modifiedLines = new ArrayList<>();

        for (String line : lines) {
            int index = line.indexOf(TARGET);
            if (index != -1) {
                // Keep everything up to HOME_SCREEN and trim after it
                int endIndex = index + TARGET.trim().length();
                modifiedLines.add(line.substring(0, endIndex+1));
            } else {
                // Keep the line unchanged if it doesn't contain HOME_SCREEN
                modifiedLines.add(line);
            }
        }

        // Write modified content back to the same file
        Files.write(filePath, modifiedLines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Updated file: " + filePath.getFileName());
    }
}
