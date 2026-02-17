package SpellCheckExamples;

/**
 * Example class with misspelled method names.
 * Should trigger spelling errors.
 */
public class BadSpellingInMethodName {
    // Misspellings in method names: "calulate" instead of "calculate", "excute" instead of "execute"
    
    public int calulate(int a, int b) {
        return a + b;
    }
    
    public void excute() {
        System.out.println("Executing");
    }
    
    public String retreive(String key) {
        return key;
    }
}
