package abstractExamples;

abstract class AbstractBase {
    public abstract void doSomething();
    public abstract int calculate(int x);

    // Concrete method
    public void concreteMethod() {
        System.out.println("Concrete");
    }
}

/**
 * Good implementation - implements all abstract methods
 */
public class GoodAbstractImpl extends AbstractBase {

    @Override
    public void doSomething() {
        System.out.println("Doing something");
    }

    @Override
    public int calculate(int x) {
        return x * 2;
    }
}