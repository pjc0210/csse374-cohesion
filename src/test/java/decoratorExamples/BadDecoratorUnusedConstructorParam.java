package decoratorExamples;

interface YetAnotherComponent {
    void execute();
}

/**
 * Bad decorator - constructor parameter never stored
 */
public class BadDecoratorUnusedConstructorParam implements YetAnotherComponent {

    public BadDecoratorUnusedConstructorParam(YetAnotherComponent component) {
        // Parameter received but never stored!
    }

    @Override
    public void execute() {
        System.out.println("Execute without component");
    }
}