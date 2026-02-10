package hashCodeExamples;

/**
 * Invalid class with only equals() overridden
 */
public class InvalidOnlyEquals {
    private int id;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InvalidOnlyEquals that = (InvalidOnlyEquals) obj;
        return id == that.id;
    }
}