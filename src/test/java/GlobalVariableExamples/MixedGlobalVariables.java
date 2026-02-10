package GlobalVariableExamples;

/**
 * Mixed valid and invalid global variables
 */
public class MixedGlobalVariables {
    public static final String GOOD_CONSTANT = "valid";
    public static final int badConstant = 42;  // Invalid: should be UPPER_SNAKE_CASE

    private static int goodVariable = 0;  // Valid: camelCase
    private static int Bad_Variable = 0;  // Invalid: snake_case

    public static int publicMutable = 10;  // Invalid: public mutable
}