package hashCodeExamples;

/**
 * Invalid class with only hashCode() overridden
 */
public class InvalidOnlyHashCode {
    private int id;

    @Override
    public int hashCode() {
        return id;
    }
}