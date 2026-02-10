package decoratorExamples;

/**
 * Regular class - not a decorator, should not be checked
 */
public class NotADecorator {
    private String name;
    private int value;

    public NotADecorator(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public void doWork() {
        System.out.println("Working");
    }
}