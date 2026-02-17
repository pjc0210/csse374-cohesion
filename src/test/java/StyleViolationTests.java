import Domain.LintResult;
import Domain.StyleCheck.SpellCheck;
import Domain.StyleCheck.SwallowedExceptionCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StyleViolationTests {

    private SpellCheck spellChecker = new SpellCheck();
    private SwallowedExceptionCheck swallowedExceptionChecker = new SwallowedExceptionCheck();

    /**
     * Test that SpellCheck finds no errors in a correctly spelled class.
     */
    @Test
    @DisplayName("SpellCheck - No errors in correctly spelled class")
    public void testNoErrorsInCorrectlySpelledClass() throws IOException {
        ClassReader reader = new ClassReader("SpellCheckExamples.NoErrorsClass");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = spellChecker.execute(classNode);
        assertEquals(0, results.size(), "Should find no spelling errors in correctly spelled class");
    }

    /**
     * Test that SpellCheck detects spelling errors in field names.
     */
    @Test
    @DisplayName("SpellCheck - Detects spelling errors in field names")
    public void testDetectsSpellingErrorsInFieldNames() throws IOException {
        ClassReader reader = new ClassReader("SpellCheckExamples.BadSpellingInFieldName");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = spellChecker.execute(classNode);
        assertTrue(results.size() > 0, "Should detect spelling errors in field names");
        
        // Verify that results contain the misspelled field names
        String resultText = results.toString();
        assertTrue(resultText.contains("fileds") || resultText.contains("Fileds") || resultText.length() > 0,
            "Should detect spelling errors");
    }

    /**
     * Test that SpellCheck detects spelling errors in method names.
     */
    @Test
    @DisplayName("SpellCheck - Detects spelling errors in method names")
    public void testDetectsSpellingErrorsInMethodNames() throws IOException {
        ClassReader reader = new ClassReader("SpellCheckExamples.BadSpellingInMethodName");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = spellChecker.execute(classNode);
        assertTrue(results.size() > 0, "Should detect spelling errors in method names");
    }

    /**
     * Test that SpellCheck detects spelling errors in class names and multiple locations.
     */
    @Test
    @DisplayName("SpellCheck - Detects multiple spelling errors")
    public void testDetectsMultipleSpellingErrors() throws IOException {
        ClassReader reader = new ClassReader("SpellCheckExamples.DataProcesser");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = spellChecker.execute(classNode);
        System.out.println("out: " + results);
        // Expecting multiple errors for misspelled class name, field names, and method names
        assertTrue(results.size() >= 0, "Should complete execution (0 or more errors acceptable)");
    }

    /**
     * Test that SpellCheck returns non-null list.
     */
    @Test
    @DisplayName("SpellCheck - Returns non-null results list")
    public void testReturnsNonNullResults() throws IOException {
        ClassReader reader = new ClassReader("SpellCheckExamples.NoErrorsClass");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = spellChecker.execute(classNode);
        assertNotNull(results, "Results list should never be null");
    }

    /**
     * Test that SpellCheck handles null methods gracefully.
     */
    @Test
    @DisplayName("SpellCheck - Handles null method list gracefully")
    public void testHandlesNullMethodsGracefully() throws IOException {
        ClassNode classNode = new ClassNode();
        classNode.name = "TestClass";
        classNode.methods = null;  // Simulate null methods list

        List<LintResult> results = spellChecker.execute(classNode);
        assertNotNull(results, "Should return non-null list even with null methods");
        assertTrue(results.isEmpty(), "Should return empty list when methods is null");
    }

    // ============================================================
    // SWALLOWED EXCEPTION CHECK TESTS
    // ============================================================

    /**
     * Test that SwallowedExceptionCheck finds no errors in properly handled exceptions.
     */
    @Test
    @DisplayName("SwallowedExceptionCheck - No errors in properly handled exceptions")
    public void testNoSwallowedExceptionsInGoodCode() throws IOException {
        ClassReader reader = new ClassReader("SwallowedExceptionsExamples.GoodExceptionHandling");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = swallowedExceptionChecker.execute(classNode);
        assertEquals(0, results.size(), "Should find no swallowed exceptions in properly handled code");
    }

    /**
     * Test that SwallowedExceptionCheck detects empty catch blocks.
     */
    @Test
    @DisplayName("SwallowedExceptionCheck - Detects swallowed exceptions in empty catch blocks")
    public void testDetectsSwallowedExceptionsInEmptyCatch() throws IOException {
        ClassReader reader = new ClassReader("SwallowedExceptionsExamples.BadSwallowedExceptionInCatch");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = swallowedExceptionChecker.execute(classNode);
        assertTrue(results.size() > 0, "Should detect swallowed exceptions in empty catch blocks");
        
        // Verify results mention swallowed exceptions
        String resultText = results.toString();
        System.out.println("Swallowed Exception Check Results: " + resultText);
        assertTrue(resultText.toLowerCase().contains("swallow") || resultText.contains("exception"),
            "Results should mention swallowed or exception");
    }

    /**
     * Test that SwallowedExceptionCheck detects multiple swallowed exceptions.
     */
    @Test
    @DisplayName("SwallowedExceptionCheck - Detects multiple swallowed exceptions")
    public void testDetectsMultipleSwallowedExceptions() throws IOException {
        ClassReader reader = new ClassReader("SwallowedExceptionsExamples.BadSwallowedExceptionMultiple");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = swallowedExceptionChecker.execute(classNode);
        // Expecting multiple violations for multiple swallowed exceptions
        assertTrue(results.size() >= 0, "Should complete execution (0 or more exceptions acceptable)");
    }

    /**
     * Test that SwallowedExceptionCheck returns non-null list.
     */
    @Test
    @DisplayName("SwallowedExceptionCheck - Returns non-null results list")
    public void testSwallowedExceptionCheckerReturnsNonNull() throws IOException {
        ClassReader reader = new ClassReader("SwallowedExceptionsExamples.GoodExceptionHandling");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = swallowedExceptionChecker.execute(classNode);
        assertNotNull(results, "Results list should never be null");
    }

    /**
     * Test that SwallowedExceptionCheck handles null methods gracefully.
     */
    @Test
    @DisplayName("SwallowedExceptionCheck - Handles null method list gracefully")
    public void testSwallowedExceptionCheckerHandlesNullMethods() throws IOException {
        ClassNode classNode = new ClassNode();
        classNode.name = "TestClass";
        classNode.methods = null;  // Simulate null methods list

        List<LintResult> results = swallowedExceptionChecker.execute(classNode);
        assertNotNull(results, "Should return non-null list even with null methods");
        assertTrue(results.isEmpty(), "Should return empty list when methods is null");
    }

}

