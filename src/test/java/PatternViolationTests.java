import Domain.LintResult;
import Domain.PrincipleCheck.EncapsulationCheck;
import Domain.PatternCheck.DecoratorPatternCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



public class PatternViolationTests {

    @Test
    @DisplayName("Good decorator - should not flag")
    public void testGoodDecorator() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.GoodDecorator");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Good decorator with proper delegation should not be flagged");
    }

    @Test
    @DisplayName("Bad decorator with unused field - should flag")
    public void testBadDecoratorUnusedField() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.BadDecoratorUnusedField");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        assertTrue(results.size() > 0,
                "Should flag decorator with unused component field");
        assertTrue(allResults.contains("never used") || allResults.contains("component"),
                "Should mention the unused field");
    }

    @Test
    @DisplayName("Bad decorator without delegation - should flag")
    public void testBadDecoratorNoDelegation() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.BadDecoratorNoDelegation");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        assertTrue(results.size() > 0,
                "Should flag decorator that doesn't delegate");
        assertTrue(allResults.contains("delegate") || allResults.contains("component"),
                "Should mention delegation issue");
    }

    @Test
    @DisplayName("Bad decorator with unused constructor parameter - should flag")
    public void testBadDecoratorUnusedConstructorParam() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.BadDecoratorUnusedConstructorParam");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        assertTrue(!results.isEmpty(),
                "Should flag decorator with unused constructor parameter");
        assertTrue(allResults.contains("parameter") || allResults.contains("Constructor"),
                "Should mention constructor parameter issue");
    }

    @Test
    @DisplayName("Not a decorator - should not flag")
    public void testNotADecorator() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.NotADecorator");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Regular class should not be identified as decorator");
    }
}
