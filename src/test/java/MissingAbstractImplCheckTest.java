import Domain.LintResult;
import Domain.StyleCheck.MissingAbstractImplCheck;
import abstractExamples.BadBytecodeGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Missing Abstract Implementation Check
 */
public class MissingAbstractImplCheckTest {

    @BeforeAll
    public static void generateBadBytecode() throws IOException {
        // Generate the bad bytecode before running tests
        System.out.println("Generating bad bytecode for testing...");
        BadBytecodeGenerator.main(new String[]{});
    }

    @Test
    @DisplayName("Good implementation - all methods implemented")
    public void testGoodAbstractImpl() throws IOException {
        MissingAbstractImplCheck checker = new MissingAbstractImplCheck();
        ClassReader reader = new ClassReader("abstractExamples.GoodAbstractImpl");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Class implementing all abstract methods should not be flagged");
    }

    @Test
    @DisplayName("Bad - missing one method implementation")
    public void testBadMissingOneMethod() throws IOException {
        MissingAbstractImplCheck checker = new MissingAbstractImplCheck();
        ClassReader reader = new ClassReader("abstractExamples.BadMissingOneMethod");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        System.out.println("BadMissingOneMethod results: " + allResults);

        assertTrue(results.size() > 0,
                "Should flag class missing methodTwo implementation");
        assertTrue(allResults.contains("methodTwo") || allResults.contains("method"),
                "Should mention the missing method");
    }

    @Test
    @DisplayName("Bad - missing all method implementations")
    public void testBadMissingAllMethods() throws IOException {
        MissingAbstractImplCheck checker = new MissingAbstractImplCheck();
        ClassReader reader = new ClassReader("abstractExamples.BadMissingAllMethods");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        System.out.println("BadMissingAllMethods results: " + allResults);

        assertTrue(results.size() > 0,
                "Should flag class missing requiredMethod implementation");
        assertTrue(allResults.contains("requiredMethod") || allResults.contains("method"),
                "Should mention the missing method");
    }

    @Test
    @DisplayName("No abstract parent - should not check")
    public void testNoAbstractParent() throws IOException {
        MissingAbstractImplCheck checker = new MissingAbstractImplCheck();
        ClassReader reader = new ClassReader("abstractExamples.NoAbstractParent");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Class with no abstract parent should not be checked");
    }

    @Test
    @DisplayName("Abstract class extending abstract - should not flag")
    public void testGoodAbstractClass() throws IOException {
        MissingAbstractImplCheck checker = new MissingAbstractImplCheck();
        ClassReader reader = new ClassReader("abstractExamples.GoodAbstractClass");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Abstract class doesn't need to implement abstract methods");
    }

    @Test
    @DisplayName("Multi-level inheritance - all implemented")
    public void testMultiLevelAbstract() throws IOException {
        MissingAbstractImplCheck checker = new MissingAbstractImplCheck();
        ClassReader reader = new ClassReader("abstractExamples.MultiLevelAbstract");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Class implementing all methods through inheritance should not be flagged");
    }

    @Test
    @DisplayName("Checker name")
    public void testCheckerName() {
        MissingAbstractImplCheck checker = new MissingAbstractImplCheck();
        assertEquals("MissingAbstractImplCheck", checker.getName());
    }
}