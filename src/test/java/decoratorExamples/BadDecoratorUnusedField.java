package decoratorExamples;

interface BadComponent {
    void doSomething();
}

/**
 * Bad decorator - has component field but never uses it
 */
public class BadDecoratorUnusedField implements BadComponent {
    private BadComponent component; // NEVER USED!

    public BadDecoratorUnusedField(BadComponent component) {
        this.component = component;
    }

    @Override
    public void doSomething() {
        // Doesn't delegate - just does its own thing
        System.out.println("Doing something without delegation");
    }
}