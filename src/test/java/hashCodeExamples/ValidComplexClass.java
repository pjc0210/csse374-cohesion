package hashCodeExamples;

/**
 * Complex class with many methods including both equals and hashCode
 */
public class ValidComplexClass {
    private int id;
    private String name;
    private double value;

    public void doSomething() {
        // Some logic
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValidComplexClass that = (ValidComplexClass) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}