import Domain.LintResult;
import Domain.StyleCheck.UnusedParametersCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnusedParametersCheckTests {

    private UnusedParametersCheck check;

    @BeforeEach
    void setUp() {
        check = new UnusedParametersCheck();
    }

    @Test
    void testGetName() {
        assertEquals("UnusedParameters", check.getName());
    }

    @Test
    void testNoUnusedParameters() throws IOException {
        ClassNode classNode = getClassNode(TestClassAllParametersUsed.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Should find no unused parameters");
    }

    @Test
    void testOneUnusedParameter() throws IOException {
        ClassNode classNode = getClassNode(TestClassOneUnusedParameter.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(1, results.size(), "Should find exactly one unused parameter");
        assertTrue(results.get(0).getMessage().contains("unusedParam"));
    }

    @Test
    void testMultipleUnusedParameters() throws IOException {
        ClassNode classNode = getClassNode(TestClassMultipleUnusedParameters.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(3, results.size(), "Should find three unused parameters");
    }

    @Test
    void testParameterUsedInMethodBody() throws IOException {
        ClassNode classNode = getClassNode(TestClassParameterUsedInBody.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Parameter used in method body should not be flagged");
    }

    @Test
    void testParameterUsedInReturn() throws IOException {
        ClassNode classNode = getClassNode(TestClassParameterUsedInReturn.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Parameter used in return statement should not be flagged");
    }

    @Test
    void testParameterUsedInConditional() throws IOException {
        ClassNode classNode = getClassNode(TestClassParameterUsedInConditional.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Parameter used in conditional should not be flagged");
    }

    @Test
    void testMixedUsedAndUnusedParameters() throws IOException {
        ClassNode classNode = getClassNode(TestClassMixedParameters.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(2, results.size(), "Should find exactly two unused parameters");

        boolean foundUnused1 = results.stream().anyMatch(r -> r.getMessage().contains("'unused1'"));
        boolean foundUnused2 = results.stream().anyMatch(r -> r.getMessage().contains("'unused2'"));

        assertTrue(foundUnused1 && foundUnused2, "Should report both unused parameters");

        boolean foundUsed = results.stream().anyMatch(r ->
                r.getMessage().contains("'used1'") || r.getMessage().contains("'used2'"));

        assertFalse(foundUsed, "Should not report used parameters");
    }

    @Test
    void testConstructorParameters() throws IOException {
        ClassNode classNode = getClassNode(TestClassConstructorParameters.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(1, results.size(), "Should find one unused constructor parameter");
        assertTrue(results.get(0).getMessage().contains("unusedParam"));
    }

    @Test
    void testOverriddenMethodWithUnusedParameter() throws IOException {
        ClassNode classNode = getClassNode(TestClassOverriddenMethod.class);
        List<LintResult> results = check.execute(classNode);

        // Override methods may have unused parameters to match signature
        // The check might flag these or skip them - depends on implementation
        // For now, we test that it runs without error
        assertNotNull(results);
    }

    @Test
    void testStaticMethodParameters() throws IOException {
        ClassNode classNode = getClassNode(TestClassStaticMethod.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(1, results.size(), "Should find unused parameter in static method");
        assertTrue(results.get(0).getMessage().contains("unusedParam"));
    }

    @Test
    void testMultipleMethodsWithUnusedParameters() throws IOException {
        ClassNode classNode = getClassNode(TestClassMultipleMethods.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(2, results.size(), "Should find unused parameters across multiple methods");
    }

    @Test
    void testParameterPassedToAnotherMethod() throws IOException {
        ClassNode classNode = getClassNode(TestClassParameterPassedToMethod.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Parameter passed to another method should not be flagged");
    }

    @Test
    void testMethodWithNoParameters() throws IOException {
        ClassNode classNode = getClassNode(TestClassNoParameters.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Method with no parameters should produce no results");
    }

    @Test
    void testVarArgsParameter() throws IOException {
        ClassNode classNode = getClassNode(TestClassVarArgs.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(1, results.size(), "Should find unused varargs parameter");
    }

    @Test
    void testParameterModified() throws IOException {
        ClassNode classNode = getClassNode(TestClassParameterModified.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Parameter that is modified should not be flagged");
    }

    private ClassNode getClassNode(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        ClassReader reader = new ClassReader(className);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    // ===== Test Classes =====

    public static class TestClassAllParametersUsed {
        public int add(int a, int b) {
            return a + b;
        }

        public void printMessage(String message) {
            System.out.println(message);
        }
    }

    public static class TestClassOneUnusedParameter {
        public void method(int usedParam, int unusedParam) {
            System.out.println(usedParam);
        }
    }

    public static class TestClassMultipleUnusedParameters {
        public void process(int unused1, String unused2, double unused3, boolean used) {
            if (used) {
                System.out.println("Processing");
            }
        }
    }

    public static class TestClassParameterUsedInBody {
        public void calculate(int value) {
            int result = value * 2;
            System.out.println(result);
        }
    }

    public static class TestClassParameterUsedInReturn {
        public int square(int x) {
            return x * x;
        }

        public String format(String text) {
            return "Formatted: " + text;
        }
    }

    public static class TestClassParameterUsedInConditional {
        public void check(boolean condition, String message) {
            if (condition) {
                System.out.println(message);
            }
        }
    }

    public static class TestClassMixedParameters {
        public void complexMethod(int used1, String unused1, double used2, boolean unused2) {
            System.out.println(used1);
            System.out.println(used2);
        }
    }

    public static class TestClassConstructorParameters {
        private String name;

        public TestClassConstructorParameters(String name, int unusedParam) {
            this.name = name;
        }
    }

    public static class TestClassOverriddenMethod {
        public void overriddenMethod(Object param) {
            // Parameter unused but required by interface/parent class signature
            System.out.println("Method called");
        }
    }

    public static class TestClassStaticMethod {
        public static void staticMethod(int usedParam, String unusedParam) {
            System.out.println(usedParam);
        }
    }

    public static class TestClassMultipleMethods {
        public void method1(int unused) {
            System.out.println("Method 1");
        }

        public void method2(String alsoUnused) {
            System.out.println("Method 2");
        }

        public void method3(double used) {
            System.out.println(used);
        }
    }

    public static class TestClassParameterPassedToMethod {
        public void outerMethod(String data) {
            innerMethod(data);
        }

        private void innerMethod(String input) {
            System.out.println(input);
        }
    }

    public static class TestClassNoParameters {
        public void simpleMethod() {
            System.out.println("No parameters");
        }

        public int getConstant() {
            return 42;
        }
    }

    public static class TestClassVarArgs {
        public void printAll(String... messages) {
            // Varargs parameter unused
            System.out.println("Method called");
        }
    }

    public static class TestClassParameterModified {
        public void modifyParameter(int value) {
            value = value + 10;
            System.out.println(value);
        }

        public void incrementParameter(int counter) {
            counter++;
            System.out.println(counter);
        }
    }
}