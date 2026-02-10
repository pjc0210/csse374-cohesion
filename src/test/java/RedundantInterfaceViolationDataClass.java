import Domain.Category;


public class RedundantInterfaceViolationDataClass {

    public final String checkName;
    private final Category category;
    private final String location;
    private final String message; // might not be needed

    public RedundantInterfaceViolationDataClass(String checkName, Category category, String location, String message) {
        this.checkName = checkName;
        this.category = category;
        this.location = location;
        this.message = message;
    }
}
