package hashCodeExamples;

/**
 * Class with equals() but wrong signature (not an override)
 */
public class ValidWrongEqualsSignature {
    private int id;

    public boolean equals(String other) {
        return false;
    }
}