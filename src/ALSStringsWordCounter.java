import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ALSStringsWordCounter {

    public static void main(String[] args) {
        String inputFile = "resources/nonALS.txt";

        // Map to store date -> list of strings after SPK
        Map<String, List<String>> dateToStringsMap = new LinkedHashMap<>();

        // Regex patterns
        Pattern datePattern = Pattern.compile("\\*\\[YY-MM-DD=(\\d{2}-\\d{2}-\\d{2})\\]\\*");
        Pattern spkPattern = Pattern.compile("SPK\\s+\"([^\"]+)\"");

        String currentDate = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {

                // Check if this line is a date line like: *[YY-MM-DD=22-03-23]*
                Matcher dateMatcher = datePattern.matcher(line);
                if (dateMatcher.find()) {
                    currentDate = dateMatcher.group(1);
                    // Initialize list for this date if not already present
                    dateToStringsMap.putIfAbsent(currentDate, new ArrayList<>());
                    continue;
                }

                // If line contains SPK with a quoted string
                Matcher spkMatcher = spkPattern.matcher(line);
                if (spkMatcher.find() && currentDate != null) {
                    String extractedString = spkMatcher.group(1).trim();
                    dateToStringsMap.computeIfAbsent(currentDate, k -> new ArrayList<>()).add(extractedString);
                }
            }

            // Now create a map for word counts
            Map<String, Integer> dateToWordCountMap = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : dateToStringsMap.entrySet()) {
                int totalWords = 0;
                for (String str : entry.getValue()) {
                    // Split on spaces to count words
                    if (!str.isEmpty()) {
                        totalWords += str.split("\\s+").length;
                    }
                }
                dateToWordCountMap.put(entry.getKey(), totalWords);
            }

            // Print the final map
            System.out.println("=== Total Word Counts by Date ===");
            for (Map.Entry<String, Integer> entry : dateToWordCountMap.entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
