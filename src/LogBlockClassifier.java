import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.deleteIfExists;

public class LogBlockClassifier {

    private static final String INPUT_FILE = "resources/no_home_screen_no_steaming_no_phrases.txt";
    private static final String ALS_FILE = "resources/ALS.txt";
    private static final String NON_ALS_FILE = "resources/nonALS.txt";

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\[\\d{2}-\\d{2}-\\d{2}.*\\]$");
    private static final Pattern SPK_PATTERN = Pattern.compile("SPK\\s+\"([^\"]*)\"");
    private static final Pattern SPE_PATTERN = Pattern.compile("SPE\\s+\"([^\"]*)\"");

    public static void main(String[] args) {
        try {
            deleteIfExists(Paths.get(ALS_FILE));
            deleteIfExists(Paths.get(NON_ALS_FILE));
            List<String> allLines = Files.readAllLines(Paths.get(INPUT_FILE), StandardCharsets.UTF_8);

            List<String> alsOutput = new ArrayList<>();
            List<String> nonAlsOutput = new ArrayList<>();

            Pattern datePattern = Pattern.compile("\\*\\[YY-MM-DD=(\\d{2}-\\d{2}-\\d{2})\\]\\*");
            List<List<String>> dateBlocks = new ArrayList<>();
            List<String> currentBlock = new ArrayList<>();

            for (String line : allLines) {
                if (datePattern.matcher(line).matches()) {
                    if (!currentBlock.isEmpty()) {
                        dateBlocks.add(new ArrayList<>(currentBlock));
                        currentBlock.clear();
                    }
                }
                currentBlock.add(line);
            }
            if (!currentBlock.isEmpty()) {
                dateBlocks.add(currentBlock); // Add last block
            }
            System.out.println("Total blocks found=" + dateBlocks.size());
            // Print each block to console
            for (List<String> block : dateBlocks) {
                System.out.println("=== Date Block === date is = " + block.get(0));
//                for (String l : block) {
//                    System.out.println(l);
//                }
                System.out.println();
                processDateBlock(block, block.get(0), alsOutput, nonAlsOutput);
            }

           //  Write final results
            Files.write(Paths.get(ALS_FILE), alsOutput, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(Paths.get(NON_ALS_FILE), nonAlsOutput, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("\n✅ Processing complete!");
            System.out.println("ALS output saved to: " + ALS_FILE);
            System.out.println("Non-ALS output saved to: " + NON_ALS_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processDateBlock(List<String> dateBlock, String dateLine,
                                         List<String> alsOutput, List<String> nonAlsOutput) {

        if (dateBlock.isEmpty()) return;

        // Always write the date line to both files
        alsOutput.add(dateLine);
        nonAlsOutput.add(dateLine);

        dateBlock.remove(0);
        System.out.println("\n=== Processing Date Block: " + dateLine + " ===");

        // Traverse the date block from bottom to top
        List<String> reversed = new ArrayList<>(dateBlock);
        Collections.reverse(reversed);

        List<List<String>> innerBlocks = new ArrayList<>();
        List<String> currentInnerBlock = new ArrayList<>();

        for (String line : reversed) {
            if (line.contains("DEL DISPLAY") || line.contains("DEL WORD") ) {
                // Close current inner block if exists
                if (!currentInnerBlock.isEmpty()) {
                    Collections.reverse(currentInnerBlock);
                    innerBlocks.add(currentInnerBlock);
                    currentInnerBlock = new ArrayList<>();
                }
                //currentInnerBlock.add(line);  // Start a new block
            } else {
                currentInnerBlock.add(line);
            }
        }

        // Add last inner block if any
        if (!currentInnerBlock.isEmpty()) {
            Collections.reverse(currentInnerBlock);
            innerBlocks.add(currentInnerBlock);
        }

        // Process each inner block
        for (List<String> block : innerBlocks) {
            if (block.size() <= 2) {
                continue; // Ignore empty or trivial blocks
            }

            if (isALSBlock(block)) {
                System.out.println("🔹 ALS Block Detected");
                alsOutput.addAll(block);
            } else {
                nonAlsOutput.addAll(block);
            }
        }
    }

    private static boolean isALSBlock(List<String> block) {
        for (int i = 0; i < block.size() - 1; i++) {
            String line = block.get(i);
            String nextLine = block.get(i + 1);

            // Check SPK patterns
            Matcher spkMatcher1 = SPK_PATTERN.matcher(line);
            Matcher spkMatcher2 = SPK_PATTERN.matcher(nextLine);

//            if (spkMatcher1.find() && spkMatcher2.find()) {
//                String phrase1 = spkMatcher1.group(1).trim();
//                String phrase2 = spkMatcher2.group(1).trim();
//
//                // Case 1: Check if phrase1 exists in phrase2 with a space before the quote
//                if (phrase2.contains(phrase1 + " ")) {
//                    System.out.println("✅ ALS Match (SPK phrase subset): " + phrase1 + " ⟶ " + phrase2);
//                    return true;
//                }
//            }


            String quoted1 = extractQuoted(line);
            String quoted2 = extractQuoted(nextLine);

            if (quoted1 == null || quoted2 == null) return false;

            // Check if quoted2 contains quoted1 followed by space, or dot and space
            if (quoted2.contains(quoted1 + " ") || quoted2.contains(quoted1 + ". ")) {
                System.out.println("✅ ALS Match (SPK phrase subset): " + quoted1 + " ⟶ " + quoted2);
                System.out.println("Moving block to ALS file ********start*******");
                for (String l : block) System.out.println(l);
                System.out.println("Moving block to ALS file ********END*******\n");
                return true;
            }


            // Case 2: Check SPE followed by SPK
            Matcher speMatcher = SPE_PATTERN.matcher(line);
            if (speMatcher.find() && spkMatcher2.find()) {
                System.out.println("✅ ALS Match (SPE followed by SPK): " + line + " | " + nextLine);
                return true;
            }
        }
        return false;
    }

    public static String extractQuoted(String line) {
        int start = line.indexOf('"');
        int end = line.lastIndexOf('"');
        if (start != -1 && end != -1 && end > start)
            return line.substring(start + 1, end);
        return null;
    }


}
