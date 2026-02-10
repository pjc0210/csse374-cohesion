package hashCodeExamples;

/**
 * Class with hashCode() but wrong signature (not an override)
 */
public class ValidWrongHashCodeSignature {
    private int id;

    // Wrong signature: takes parameter
    public int hashCode(int value) {
        return value;
    }
}