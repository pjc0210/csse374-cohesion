package Domain;

public class LintResult {
    private final String checkName;
    private final Category category;
    private final String location;
    private final String message; // might not be needed

    public LintResult(String checkName, Category category, String location, String message) {
        this.checkName = checkName;
        this.category = category;
        this.location = location;
        this.message = message;
    }

    public String toString() {
        return String.format("[%s] %s at %s: %s", category.name(), checkName, location, message);
    }

    public String getMessage(){
        return message;
    }

    public String getCheckName() {
        return this.checkName;
    }

    public Category getCategory(){
        return this.category;
    }
}
