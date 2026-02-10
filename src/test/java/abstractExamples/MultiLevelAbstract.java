package abstractExamples;

abstract class Level1 {
    public abstract void level1Method();
}

abstract class Level2 extends Level1 {
    public abstract void level2Method();

    @Override
    public void level1Method() {
        System.out.println("Level1 method implemented in Level2");
    }
}

/**
 * Should implement level2Method (level1Method already implemented in Level2)
 */
public class MultiLevelAbstract extends Level2 {

    @Override
    public void level2Method() {
        System.out.println("Level2 method implemented");
    }
}