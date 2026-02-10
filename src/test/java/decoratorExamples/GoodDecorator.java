package decoratorExamples;

interface Component {
    void operation();
}

class ConcreteComponent implements Component {
    @Override
    public void operation() {
        System.out.println("ConcreteComponent operation");
    }
}

/**
 * Good decorator - properly wraps and delegates
 */
public class GoodDecorator implements Component {
    private Component wrappedComponent;

    public GoodDecorator(Component component) {
        this.wrappedComponent = component;
    }

    @Override
    public void operation() {
        // Delegates to wrapped component
        wrappedComponent.operation();
        // Can add extra behavior
        System.out.println("Added behavior");
    }
}