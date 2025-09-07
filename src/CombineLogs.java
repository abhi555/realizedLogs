import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombineLogs {

    private static final String INPUT_DIR = "resources/non_absence_data";
    private static final String OUTPUT_DIR = "resources/combined_logs";
    private static final String OUTPUT_FILE = "combined.log";

    private static final Pattern DATE_PATTERN = Pattern.compile("\\*\\[YY-MM-DD=(\\d{2}-\\d{2}-\\d{2})\\]\\*");
    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yy-MM-dd");
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

    // Predefined US National Holidays (fixed dates)
    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),    // New Year's Day
            MonthDay.of(7, 4),    // Independence Day
            MonthDay.of(12, 25)   // Christmas
    );

    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));

            TreeMap<LocalDate, TreeMap<LocalTime, String>> combinedMap = new TreeMap<>();

            // Process all .txt files
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(INPUT_DIR), "*.txt")) {
                for (Path filePath : stream) {
                    processFile(filePath, combinedMap);
                }
            }

            // Write combined logs
            writeCombinedLogs(combinedMap);

            System.out.println("✅ Combined log file created successfully at: " + OUTPUT_DIR + "/" + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(Path filePath, TreeMap<LocalDate, TreeMap<LocalTime, String>> combinedMap) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        LocalDate currentDate = null;
        boolean skipBlock = false;

        for (String line : lines) {
            Matcher matcher = DATE_PATTERN.matcher(line);

            if (matcher.find()) {
                // Found a date line
                currentDate = LocalDate.parse(matcher.group(1), LOG_DATE_FORMAT);
                skipBlock = isWeekend(currentDate) || isNationalHoliday(currentDate);
                continue;
            }

            if (currentDate != null && !skipBlock && !line.trim().isEmpty()) {
                // Extract time and message
                String[] parts = line.split(" ", 2);
                if (parts.length < 2) continue;

                String timeStr = parts[0];
                String message = parts[1];

                try {
                    LocalTime time = LocalTime.parse(timeStr, TIME_FORMAT);

                    // Insert into outer map
                    combinedMap
                            .computeIfAbsent(currentDate, k -> new TreeMap<>())
                            .put(time, message);

                } catch (Exception e) {
                    // Skip malformed time lines
                }
            }
        }
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private static boolean isNationalHoliday(LocalDate date) {
        MonthDay md = MonthDay.from(date);
        return FIXED_HOLIDAYS.contains(md);
    }

    private static void writeCombinedLogs(TreeMap<LocalDate, TreeMap<LocalTime, String>> combinedMap) throws IOException {
        Path outputFilePath = Paths.get(OUTPUT_DIR, OUTPUT_FILE);
        List<String> outputLines = new ArrayList<>();
        System.out.println("size of the map is =" + combinedMap.size());
        for (Map.Entry<LocalDate, TreeMap<LocalTime, String>> dateEntry : combinedMap.entrySet()) {
            LocalDate date = dateEntry.getKey();
            TreeMap<LocalTime, String> timeMap = dateEntry.getValue();

            // Write date header
            outputLines.add("*[YY-MM-DD=" + OUTPUT_DATE_FORMAT.format(date) + "]*");

            // Write time-sorted messages
            for (Map.Entry<LocalTime, String> timeEntry : timeMap.entrySet()) {
                String timeStr = TIME_FORMAT.format(timeEntry.getKey());
                outputLines.add(timeStr + " " + timeEntry.getValue());
            }
        }

        Files.write(outputFilePath, outputLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
