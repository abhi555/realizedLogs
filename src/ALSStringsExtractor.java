import java.io.*;

public class ALSStringsExtractor {
    public static void main(String[] args) {
        // Input and output file paths
        String inputFile = "resources/ALS.txt";
        String outputFile = "resources/ALS-strings.txt";

        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Trim trailing spaces for safety
                String trimmed = line.trim();

                // Check conditions:
                // 1. Line ends with: space + double quote
                // 2. OR line starts with "*[YY-MM-DD"
                if (trimmed.endsWith(" \"") || trimmed.startsWith("*[YY-MM-DD")) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Filtered lines have been written to: " + outputFile);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }
}
