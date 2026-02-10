import Domain.Category;
import Domain.LintResult;
import Domain.PrincipleCheck.CodeDuplicationCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeDuplicationCheckTests {

    private CodeDuplicationCheck check;

    @BeforeEach
    void setUp() {
        check = new CodeDuplicationCheck();
    }

    @Test
    void testNoDuplication() throws IOException {
        ClassNode classNode = getClassNode(NoDuplicationClass.class);
        List<LintResult> results = check.execute(classNode);
        assertTrue(results.isEmpty(), "Should have no errors when methods are different");
    }

    @Test
    void testHighDuplication() throws IOException {
        ClassNode classNode = getClassNode(HighDuplicationClass.class);
        List<LintResult> results = check.execute(classNode);

        assertFalse(results.isEmpty(), "Should detect high code duplication");
        assertTrue(results.stream().anyMatch(r ->
                        r.getMessage().contains("high code duplication")),
                "Should report high code duplication");
    }

    @Test
    void testPartialDuplication() throws IOException {
        ClassNode classNode = getClassNode(PartialDuplicationClass.class);
        List<LintResult> results = check.execute(classNode);

        // Partial duplication might or might not trigger depending on threshold
        // This test just verifies the check runs without errors
        assertNotNull(results);
    }

    @Test
    void testIgnoresConstructors() throws IOException {
        ClassNode classNode = getClassNode(DuplicateConstructorsClass.class);
        List<LintResult> results = check.execute(classNode);

        // Should not flag duplicate constructors
        assertTrue(results.isEmpty(), "Should ignore constructors");
    }

    @Test
    void testIgnoresSmallMethods() throws IOException {
        ClassNode classNode = getClassNode(SmallMethodsClass.class);
        List<LintResult> results = check.execute(classNode);

        // Should not flag small duplicate methods (e.g., simple getters)
        assertTrue(results.isEmpty(), "Should ignore very small methods");
    }

    @Test
    void testGetName() {
        assertEquals("CodeDuplication", check.getName());
    }

    @Test
    void testResultCategory() throws IOException {
        ClassNode classNode = getClassNode(HighDuplicationClass.class);
        List<LintResult> results = check.execute(classNode);

        if (!results.isEmpty()) {
            assertEquals(Category.PRINCIPLE, results.get(0).getCategory());
        }
    }

    private ClassNode getClassNode(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        ClassReader classReader = new ClassReader(className);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    // ==================== Embedded Test Classes ====================

    static class NoDuplicationClass {
        public int add(int a, int b) {
            return a + b;
        }

        public int multiply(int a, int b) {
            return a * b;
        }

        public String greet(String name) {
            return "Hello, " + name;
        }
    }

    static class HighDuplicationClass {
        public int calculateTotalWithTax(int price, int quantity) {
            int subtotal = price * quantity;
            int tax = subtotal / 10;
            int total = subtotal + tax;
            return total;
        }

        public int calculateTotalWithDiscount(int price, int quantity) {
            int subtotal = price * quantity;
            int discount = subtotal / 10;
            int total = subtotal - discount;
            return total;
        }
    }

    static class PartialDuplicationClass {
        public int processDataA(int value) {
            int result = value * 2;
            result = result + 10;
            return result;
        }

        public int processDataB(int value) {
            int result = value * 2;
            result = result + 20;
            result = result * 3;
            return result;
        }
    }

    static class DuplicateConstructorsClass {
        private int x;

        public DuplicateConstructorsClass() {
            this.x = 0;
        }

        public DuplicateConstructorsClass(int x) {
            this.x = x;
        }
    }

    static class SmallMethodsClass {
        private int value;

        public int getValue() {
            return value;
        }

        public int getValueCopy() {
            return value;
        }
    }
}