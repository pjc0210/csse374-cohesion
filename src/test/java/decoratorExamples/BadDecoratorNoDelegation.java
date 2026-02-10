package decoratorExamples;

interface AnotherComponent {
    String getValue();
}

/**
 * Bad decorator - stores component but doesn't delegate to it
 */
public class BadDecoratorNoDelegation implements AnotherComponent {
    private AnotherComponent component;

    public BadDecoratorNoDelegation(AnotherComponent component) {
        this.component = component;
        // Field is assigned but never used to call methods
    }

    @Override
    public String getValue() {
        // Returns own value instead of delegating
        return "my own value";
    }
}