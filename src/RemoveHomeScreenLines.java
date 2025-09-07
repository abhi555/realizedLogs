import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class RemoveHomeScreenLines {

    private static final String INPUT_FILE = "resources/cleaned_no_steaming_no_phrases.log";
    private static final String OUTPUT_FILE = "resources/no_home_screen_no_steaming_no_phrases.txt";

    public static void main(String[] args) {
        try {
            Path inputPath = Paths.get(INPUT_FILE);
            Path outputPath = Paths.get(OUTPUT_FILE);

            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            List<String> filteredLines = new ArrayList<>();

            for (String line : lines) {
                if (!line.contains("HOME_SCREEN")) {
                    filteredLines.add(line);
                }
            }

            // Write the filtered lines to output file
            Files.write(outputPath, filteredLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Lines without HOME_SCREEN saved to: " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
