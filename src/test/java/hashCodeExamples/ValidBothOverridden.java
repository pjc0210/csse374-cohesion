package hashCodeExamples;

/**
 * Valid class with both methods overridden
 */
public class ValidBothOverridden {
    private int id;
    private String name;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValidBothOverridden that = (ValidBothOverridden) obj;
        return id == that.id && (name != null ? name.equals(that.name) : that.name == null);
    }

    @Override
    public int hashCode() {
        return id * 31 + (name != null ? name.hashCode() : 0);
    }
}