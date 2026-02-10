import Domain.LintResult;
import Domain.PrincipleCheck.EncapsulationCheck;
import Domain.PrincipleCheck.InvalidHashCodeOrEqualsCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import Domain.PrincipleCheck.EncapsulationCheck;
import Domain.PrincipleCheck.HollywoodPrincipleCheck;
import Domain.PrincipleCheck.InvalidHashCodeOrEqualsCheck;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Encapsulation Violation Detection
 * 
 * Encapsulation violations include:
 * 1. Public fields (should be private with getters/setters)
 * 2. Returning references to mutable internal state
 * 3. Accepting mutable objects without defensive copying
 * 4. Exposing internal collections directly
 * 5. Public static mutable fields
 */
public class PrincipleViolationTests {
    
    // ==================== PUBLIC FIELDS ====================
    EncapsulationCheck ec = new EncapsulationCheck();

    @Test
    @DisplayName("Public instance field - should flag")
    public void testPrivateInstanceField() throws IOException {
        EncapsulationCheck ec = new EncapsulationCheck();
        ClassReader reader = new ClassReader("EncapsulationViolationDataClass");
        // Step 2. ClassNode is just a data container for the parsed class
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = ec.execute(classNode);
//        System.out.println(results);
        assertEquals(1, results.size(), "Only one field isn't private or static");
        assertTrue(results.get(0).getMessage().contains("checkName"), "checkName is public");
    }

    // ==================== HASHCODE/EQUALS TESTS ====================

    @Test
    @DisplayName("[Hashcode/Equals Test] Both equals and hashCode overridden - should not flag")
    public void testBothMethodsOverridden() throws IOException {
        InvalidHashCodeOrEqualsCheck checker = new InvalidHashCodeOrEqualsCheck();
        ClassReader reader = new ClassReader("hashCodeExamples.ValidBothOverridden");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "No violation when both methods are overridden");
    }

    @Test
    @DisplayName("[Hashcode Test] Neither equals nor hashCode overridden - should not flag")
    public void testNeitherMethodOverridden() throws IOException {
        InvalidHashCodeOrEqualsCheck checker = new InvalidHashCodeOrEqualsCheck();
        ClassReader reader = new ClassReader("hashCodeExamples.ValidNeitherOverridden");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "No violation when neither method is overridden");
    }

    @Test
    @DisplayName("[Hashcode/Equals Test] Only equals overridden - should flag")
    public void testOnlyEqualsOverridden() throws IOException {
        InvalidHashCodeOrEqualsCheck checker = new InvalidHashCodeOrEqualsCheck();
        ClassReader reader = new ClassReader("hashCodeExamples.InvalidOnlyEquals");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(1, results.size(), "Should flag when only equals() is overridden");
        assertTrue(results.get(0).getMessage().contains("hashCode"),
                "Error should mention missing hashCode()");
    }

    @Test
    @DisplayName("[Hashcode/Equals Test] Only hashCode overridden - should flag")
    public void testOnlyHashCodeOverridden() throws IOException {
        InvalidHashCodeOrEqualsCheck checker = new InvalidHashCodeOrEqualsCheck();
        ClassReader reader = new ClassReader("hashCodeExamples.InvalidOnlyHashCode");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(1, results.size(), "Should flag when only hashCode() is overridden");
        assertTrue(results.get(0).getMessage().contains("equals"),
                "Error should mention missing equals()");
    }

    @Test
    @DisplayName("[Hashcode/Equals Test] Wrong equals signature - should not flag")
    public void testWrongEqualsSignature() throws IOException {
        InvalidHashCodeOrEqualsCheck checker = new InvalidHashCodeOrEqualsCheck();
        ClassReader reader = new ClassReader("hashCodeExamples.ValidWrongEqualsSignature");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(),
                "Should not flag equals() with wrong signature");
    }

    @Test
    @DisplayName("[Hashcode/Equals Test] Wrong hashCode signature - should not flag")
    public void testWrongHashCodeSignature() throws IOException {
        InvalidHashCodeOrEqualsCheck checker = new InvalidHashCodeOrEqualsCheck();
        ClassReader reader = new ClassReader("hashCodeExamples.ValidWrongHashCodeSignature");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(),
                "Should not flag hashCode() with wrong signature");
    }

    @Test
    @DisplayName("[Hashcode/Equals Test] Complex class with both methods - should not flag")
    public void testComplexClassWithBothMethods() throws IOException {
        InvalidHashCodeOrEqualsCheck checker = new InvalidHashCodeOrEqualsCheck();
        ClassReader reader = new ClassReader("hashCodeExamples.ValidComplexClass");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(),
                "Should not flag when both methods exist among many methods");
    }

    // ==================== HOLLYWOOD PRINCIPLE TESTS ====================

    @Test
    @DisplayName("[Hollywood Principle] Interface class - should not flag")
    public void testHollywoodSkipsInterface() throws IOException {
        HollywoodPrincipleCheck checker = new HollywoodPrincipleCheck();
        ClassReader reader = new ClassReader("hollywoodExamples.HP_Interface");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "Interfaces should be skipped");
    }

    @Test
    @DisplayName("[Hollywood Principle] Abstract class - should not flag")
    public void testHollywoodSkipsAbstractClass() throws IOException {
        HollywoodPrincipleCheck checker = new HollywoodPrincipleCheck();
        ClassReader reader = new ClassReader("hollywoodExamples.HP_AbstractBase");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "Abstract classes should be skipped");
    }

    @Test
    @DisplayName("[Hollywood Principle] Concrete class calling framework-like class - should flag")
    public void testHollywoodFlagsFrameworkCall() throws IOException {
        HollywoodPrincipleCheck checker = new HollywoodPrincipleCheck();
        ClassReader reader = new ClassReader("hollywoodExamples.HP_ConcreteCallsFramework");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertTrue(results.size() >= 1, "Should flag at least one violation");
        assertTrue(results.get(0).getMessage().toLowerCase().contains("hollywood principle"),
                "Message should mention Hollywood Principle");
    }

    @Test
    @DisplayName("[Hollywood Principle] Impl class calling a Manager/Controller - should flag")
    public void testHollywoodFlagsImplCallingHighLevel() throws IOException {
        HollywoodPrincipleCheck checker = new HollywoodPrincipleCheck();
        ClassReader reader = new ClassReader("hollywoodExamples.HP_ImplCallsManager");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertTrue(results.size() >= 1, "Should flag at least one violation");
        boolean mentionsHighLevel = results.stream().anyMatch(r ->
                r.getMessage().contains("high-level") || r.getMessage().contains("Manager") || r.getMessage().contains("Controller"));
        assertTrue(mentionsHighLevel, "Message should indicate high-level/framework call");
    }

    @Test
    @DisplayName("[Hollywood Principle] Only calls java.* library - should not flag")
    public void testHollywoodAllowsStandardLibraryCalls() throws IOException {
        HollywoodPrincipleCheck checker = new HollywoodPrincipleCheck();
        ClassReader reader = new ClassReader("hollywoodExamples.HP_StandardLibraryOnly");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "Calls to java.* should not be flagged");
    }

    @Test
    @DisplayName("[Hollywood Principle] Constructor calls only - should not flag")
    public void testHollywoodSkipsConstructors() throws IOException {
        HollywoodPrincipleCheck checker = new HollywoodPrincipleCheck();
        ClassReader reader = new ClassReader("hollywoodExamples.HP_ConstructorOnly");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "Constructor-only calls should be ignored");
    }

}
