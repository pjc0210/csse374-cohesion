package SwallowedExceptionsExamples;

import java.io.IOException;

/**
 * Example class with swallowed exceptions (empty catch blocks).
 * Should trigger swallowed exception warnings.
 */
public class BadSwallowedExceptionInCatch {
    
    public void readFileWithSwallowedException(String filename) {
        try {
            // Do something that might throw IOException
            throw new IOException("File not found");
        } catch (IOException e) {
            // Empty catch block - swallows the exception silently!
        }
    }
    
    public void parseNumberWithSwallowedException(String input) {
        try {
            int value = Integer.parseInt(input);
            System.out.println("Parsed: " + value);
        } catch (NumberFormatException e) {
            // Empty catch block - silently swallows the error
        }
    }
    
    public void processDataWithSwallowedException() {
        try {
            throw new RuntimeException("Processing error");
        } catch (RuntimeException e) {
            // Only contains whitespace/comments, effectively empty
            // This is still a swallowed exception
        }
    }
}
