package SwallowedExceptionsExamples;

import java.io.IOException;

/**
 * Example class with good exception handling.
 * Should NOT trigger swallowed exception warnings.
 */
public class GoodExceptionHandling {
    
    public void readFileWithProperHandling(String filename) {
        try {
            // Do something that might throw IOException
            throw new IOException("File not found");
        } catch (IOException e) {
            // Proper handling: log or rethrow
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void parseNumberWithProperHandling(String input) {
        try {
            int value = Integer.parseInt(input);
            System.out.println("Parsed: " + value);
        } catch (NumberFormatException e) {
            // Proper handling: provide feedback
            System.out.println("Invalid number format: " + input);
            throw new IllegalArgumentException("Cannot parse: " + input, e);
        }
    }
}
