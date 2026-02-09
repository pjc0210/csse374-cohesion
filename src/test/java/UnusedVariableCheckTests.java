import Domain.Category;
import Domain.LintResult;
import Domain.StyleCheck.UnusedVariablesCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnusedVariablesCheckTest {

    private UnusedVariablesCheck check;

    @BeforeEach
    void setUp() {
        check = new UnusedVariablesCheck();
    }

    @Test
    void testGetName() {
        assertEquals("UnusedVariables", check.getName());
    }

    @Test
    void testNoUnusedFields() throws IOException {
        ClassNode classNode = getClassNode(TestClassAllFieldsUsed.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Should find no unused fields");
    }

    @Test
    void testOneUnusedField() throws IOException {
        ClassNode classNode = getClassNode(TestClassOneUnusedField.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(1, results.size(), "Should find exactly one unused field");
        assertTrue(results.get(0).getMessage().contains("unusedField"));
    }

    @Test
    void testMultipleUnusedFields() throws IOException {
        ClassNode classNode = getClassNode(TestClassMultipleUnusedFields.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(3, results.size(), "Should find three unused fields");
    }

    @Test
    void testFieldUsedInConstructor() throws IOException {
        ClassNode classNode = getClassNode(TestClassFieldUsedInConstructor.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Field used in constructor should not be flagged");
    }

    @Test
    void testFieldUsedInMethod() throws IOException {
        ClassNode classNode = getClassNode(TestClassFieldUsedInMethod.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Field used in method should not be flagged");
    }

    @Test
    void testStaticFieldUsed() throws IOException {
        ClassNode classNode = getClassNode(TestClassStaticFieldUsed.class);
        List<LintResult> results = check.execute(classNode);

        // NOTE: Static fields that are actually accessed should not be flagged
        assertTrue(results.isEmpty(), "Static field that is used should not be flagged");
    }

    @Test
    void testStaticFieldUnused() throws IOException {
        ClassNode classNode = getClassNode(TestClassStaticFieldUnused.class);
        List<LintResult> results = check.execute(classNode);

        // Should find UNUSED_COUNTER (not the inlined String constant)
        assertEquals(1, results.size(), "Unused static field should be flagged");
        assertTrue(results.get(0).getMessage().contains("UNUSED_COUNTER"));
    }

    @Test
    void testMixedUsedAndUnused() throws IOException {
        ClassNode classNode = getClassNode(TestClassMixed.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(2, results.size(), "Should find exactly two unused fields");

        boolean foundUnused1 = results.stream().anyMatch(r -> r.getMessage().contains("'unusedField1'"));
        boolean foundUnused2 = results.stream().anyMatch(r -> r.getMessage().contains("'unusedField2'"));

        assertTrue(foundUnused1 && foundUnused2, "Should report both unused fields");

        // Use exact field names with quotes to avoid substring matching
        boolean foundUsed = results.stream().anyMatch(r ->
                r.getMessage().contains("'usedField'") || r.getMessage().contains("'alsoUsed'"));

        assertFalse(foundUsed, "Should not report used fields");
    }

    @Test
    void testFieldReadAndWritten() throws IOException {
        ClassNode classNode = getClassNode(TestClassFieldReadAndWritten.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Field that is both read and written should not be flagged");
    }

    @Test
    void testClassWithNoFields() throws IOException {
        ClassNode classNode = getClassNode(TestClassNoFields.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Class with no fields should produce no results");
    }

    private ClassNode getClassNode(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        ClassReader reader = new ClassReader(className);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    // ===== Test Classes =====

    public static class TestClassAllFieldsUsed {
        private int field1;
        private String field2;

        public void useFields() {
            field1 = 42;
            System.out.println(field2);
        }
    }

    public static class TestClassOneUnusedField {
        private int usedField;
        private int unusedField;

        public void method() {
            usedField = 10;
        }
    }

    public static class TestClassMultipleUnusedFields {
        private int unused1;
        private String unused2;
        private double unused3;
        private int used;

        public void method() {
            used = 5;
        }
    }

    public static class TestClassFieldUsedInConstructor {
        private String name;

        public TestClassFieldUsedInConstructor() {
            this.name = "default";
        }
    }

    public static class TestClassFieldUsedInMethod {
        private int counter;

        public int getCounter() {
            return counter;
        }

        public void increment() {
            counter++;
        }
    }

    // FIXED: Use non-final or non-primitive to avoid inlining
    public static class TestClassStaticFieldUsed {
        private static int COUNTER = 0;  // Changed from final String

        public static int getCounter() {
            return COUNTER;  // Actually accessed, not inlined
        }
    }

    // FIXED: Use actual unused field
    public static class TestClassStaticFieldUnused {
        private static int USED_COUNTER = 0;
        private static int UNUSED_COUNTER;

        public static int getCounter() {
            return USED_COUNTER;
        }
    }

    public static class TestClassMixed {
        private int usedField;
        private String unusedField1;
        private double alsoUsed;
        private boolean unusedField2;

        public void doSomething() {
            usedField = 10;
            alsoUsed = 3.14;
        }
    }

    public static class TestClassFieldReadAndWritten {
        private int value;

        public void setValue(int val) {
            this.value = val;
        }

        public int getValue() {
            return value;
        }
    }

    public static class TestClassNoFields {
        public void method() {
            System.out.println("No fields here!");
        }
    }
}