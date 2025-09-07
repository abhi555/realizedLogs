import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFilter {

    private static final String RAW_LOGS_DIR = "resources/raw_logs";
    private static final String EXCLUDED_DATES_FILE = "resources/excluded_dates.txt";
    private static final String OUTPUT_DIR = "resources/non_absence_data";
    private static final Pattern DATE_PATTERN = Pattern.compile("\\*\\[YY-MM-DD=(\\d{2}-\\d{2}-\\d{2})\\]\\*");

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yy-MM-dd");
    private static final DateTimeFormatter EXCLUDED_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");

    public static void main(String[] args) {
        try {
            // Create output directory if it doesn't exist
            Files.createDirectories(Paths.get(OUTPUT_DIR));

            // Load excluded dates into a Set<LocalDate>
            Set<LocalDate> excludedDates = loadExcludedDates();

            // Process each log file
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(RAW_LOGS_DIR), "*.txt")) {
                for (Path filePath : stream) {
                    processLogFile(filePath, excludedDates);
                }
            }

            System.out.println("✅ Log processing completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<LocalDate> loadExcludedDates() throws IOException {
        Set<LocalDate> excludedDates = new HashSet<>();
        List<String> lines = Files.readAllLines(Paths.get(EXCLUDED_DATES_FILE));

        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                LocalDate date = LocalDate.parse(line, EXCLUDED_DATE_FORMAT);
                excludedDates.add(date);
            }
        }
        return excludedDates;
    }

    private static void processLogFile(Path filePath, Set<LocalDate> excludedDates) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> filteredLines = new ArrayList<>();

        LocalDate currentDate = null;
        boolean skipBlock = false;

        for (String line : lines) {
            Matcher matcher = DATE_PATTERN.matcher(line);

            if (matcher.find()) {
                // Found a new date section
                currentDate = LocalDate.parse(matcher.group(1), LOG_DATE_FORMAT);
                skipBlock = excludedDates.contains(currentDate);
            }

            // If current date is excluded, skip the lines under it
            if (!skipBlock) {
                filteredLines.add(line);
            }
        }

        // Write the filtered log to the output folder
        Path outputFilePath = Paths.get(OUTPUT_DIR, filePath.getFileName().toString());
        Files.write(outputFilePath, filteredLines);
    }
}
