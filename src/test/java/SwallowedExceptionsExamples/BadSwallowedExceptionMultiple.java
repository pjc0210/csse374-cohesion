package SwallowedExceptionsExamples;

/**
 * Example class with multiple swallowed exceptions.
 * Should trigger multiple swallowed exception warnings.
 */
public class BadSwallowedExceptionMultiple {
    
    public void operationOne() {
        try {
            throw new IllegalArgumentException("Invalid argument");
        } catch (IllegalArgumentException e) {
            // Swallowed
        }
    }
    
    public void operationTwo() {
        try {
            throw new NullPointerException("Null reference");
        } catch (NullPointerException e) {
            // Swallowed
        }
    }
    
    public void operationThree() {
        try {
            int[] arr = new int[5];
            arr[10] = 5;  // ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {
            // Swallowed
        }
    }
    
    public void nestedTryCatch() {
        try {
            try {
                throw new Exception("Nested exception");
            } catch (Exception e) {
                // Inner catch block is empty - swallowed
            }
        } catch (Exception e) {
            // Outer catch is also empty - swallowed
        }
    }
}
