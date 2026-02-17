package SpellCheckExamples;

import Domain.Category;

public class NoErrorsClass {
    public final String checkName;
    public static Category category;
    private final String location;
    private final String message; // might not be needed

    public NoErrorsClass(String checkName, Category category, String location, String message) {
        this.checkName = checkName;
        this.category = category;
        this.location = location;
        this.message = message;
    }
}
