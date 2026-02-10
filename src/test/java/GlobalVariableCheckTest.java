import Domain.LintResult;
import Domain.StyleCheck.GlobalVariableCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Global Variable Style Checking
 *
 * Global variable conventions:
 * 1. Constants (static final) should be UPPER_SNAKE_CASE
 * 2. Static non-final variables should be camelCase
 * 3. Public mutable static variables are code smells
 * 4. Instance variables are not checked (only static fields)
 */
public class GlobalVariableCheckTest {

    // ==================== CONSTANT NAMING TESTS ====================

    @Test
    @DisplayName("Valid constants in UPPER_SNAKE_CASE - should not flag naming")
    public void testValidConstants() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.ValidConstants");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        // May have warnings about visibility, but no naming violations
        assertTrue(results.stream().noneMatch(r -> r.getMessage().contains("does not follow UPPER_SNAKE_CASE")),
                "Should not flag properly named constants");
    }

    @Test
    @DisplayName("Invalid constant naming - should flag")
    public void testInvalidConstantNaming() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.InvalidConstantNaming");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.size() > 0, "Should flag constants not in UPPER_SNAKE_CASE");
        assertTrue(results.toString().contains("maxValue"),
                "Should flag 'maxValue' constant");
    }

    @Test
    @DisplayName("Constants with numbers - should allow")
    public void testNumericConstants() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.NumericConstants");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.stream().noneMatch(r -> r.getMessage().contains("MAX_SIZE_1024")),
                "Should allow numbers in constant names");
        assertTrue(results.stream().noneMatch(r -> r.getMessage().contains("HTTP_200_OK")),
                "Should allow numbers in constant names");
    }

    // ==================== STATIC VARIABLE NAMING TESTS ====================

    @Test
    @DisplayName("Valid static variables in camelCase - should not flag naming")
    public void testValidStaticVariables() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.ValidStaticVariables");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.stream().noneMatch(r -> r.getMessage().contains("does not follow camelCase")),
                "Should not flag properly named static variables");
    }

    @Test
    @DisplayName("Invalid static variable naming - should flag")
    public void testInvalidStaticVariableNaming() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.InvalidStaticVariableNaming");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.size() > 0, "Should flag static variables not in camelCase");
        assertTrue(results.toString().contains("InstanceCount"),
                "Should flag 'InstanceCount' (starts with uppercase)");
        assertTrue(results.toString().contains("shared_buffer"),
                "Should flag 'shared_buffer' (snake_case)");
    }

    // ==================== CODE SMELL DETECTION ====================

    @Test
    @DisplayName("Public mutable static fields - should flag as code smell")
    public void testPublicMutableStatic() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.PublicMutableStatic");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.stream().anyMatch(r -> r.getMessage().contains("Public mutable static variable")),
                "Should warn about public mutable static variables");
    }

    @Test
    @DisplayName("Private mutable static fields - should not flag as public mutable")
    public void testPrivateMutableStatic() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.PrivateMutableStatic");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.stream().noneMatch(r -> r.getMessage().contains("Public mutable")),
                "Should not warn about private mutable static variables");
    }

    // ==================== SPECIAL CASES ====================

    @Test
    @DisplayName("Only instance fields - should not check any")
    public void testOnlyInstanceFields() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.OnlyInstanceFields");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "Should ignore all instance (non-static) variables");
    }

    @Test
    @DisplayName("serialVersionUID - should allow as special case")
    public void testSerialVersionUID() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.SerializableClass");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.stream().noneMatch(r -> r.getMessage().contains("serialVersionUID")),
                "Should allow serialVersionUID as special case");
    }

    @Test
    @DisplayName("Empty class - should not flag anything")
    public void testEmptyClass() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.EmptyClass");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "Should not flag empty class");
    }

    // ==================== MIXED SCENARIOS ====================

    @Test
    @DisplayName("Mixed valid and invalid global variables")
    public void testMixedGlobalVariables() throws IOException {
        GlobalVariableCheck checker = new GlobalVariableCheck();
        ClassReader reader = new ClassReader("GlobalVariableExamples.MixedGlobalVariables");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();
        assertTrue(allResults.contains("badConstant"),
                "Should flag incorrectly named constant");
        assertFalse(allResults.contains("GOOD_CONSTANT"),
                "Should not flag correctly named constant");
        assertTrue(allResults.contains("Bad_Variable"),
                "Should flag 'Bad_Variable' (snake_case)");
    }
}