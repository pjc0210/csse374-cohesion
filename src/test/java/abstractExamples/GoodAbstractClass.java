package abstractExamples;

abstract class AnotherBase {
    public abstract void abstractMethod();
}

/**
 * Abstract class extending abstract class - this is OK
 */
public abstract class GoodAbstractClass extends AnotherBase {

    public void concreteMethod() {
        System.out.println("Concrete method in abstract class");
    }

    // Don't need to implement abstractMethod because this class is also abstract
}