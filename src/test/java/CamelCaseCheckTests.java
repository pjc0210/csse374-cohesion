import Domain.LintResult;
import Domain.StyleCheck.CamelCaseCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CamelCaseCheckTests {

    private CamelCaseCheck check;

    @BeforeEach
    void setUp() {
        check = new CamelCaseCheck();
    }

    @Test
    void testGetName() {
        assertEquals("CamelCase", check.getName());
    }

    @Test
    void testAllCamelCaseValid() throws IOException {
        ClassNode classNode = getClassNode(GoodCamelCase.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Should find no camel case violations");
    }

    @Test
    void testBadFieldNameFlags() throws IOException {
        ClassNode classNode = getClassNode(BadFieldName.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(1, results.size(), "Should flag exactly one field violation");
        assertTrue(results.get(0).getMessage().contains("Field"));
        assertTrue(results.get(0).getMessage().contains("bad_field"));
    }

    @Test
    void testBadMethodNameFlags() throws IOException {
        ClassNode classNode = getClassNode(BadMethodName.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(1, results.size(), "Should flag exactly one method violation");
        assertTrue(results.get(0).getMessage().contains("Method"));
        assertTrue(results.get(0).getMessage().contains("Bad_Method"));
    }

    @Test
    void testMultipleViolations() throws IOException {
        ClassNode classNode = getClassNode(multipleViolations.class);
        List<LintResult> results = check.execute(classNode);

        assertEquals(2, results.size(), "Should flag field + method violations");

        boolean hasField = results.stream().anyMatch(r -> r.getMessage().contains("Field"));
        boolean hasMethod = results.stream().anyMatch(r -> r.getMessage().contains("Method"));

        assertTrue(hasField, "Should include a field violation");
        assertTrue(hasMethod, "Should include a method violation");
    }

    @Test
    void testIgnoresConstructors() throws IOException {
        ClassNode classNode = getClassNode(ConstructorOnly.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Constructor-only class should not be flagged for method naming");
    }

    private ClassNode getClassNode(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        ClassReader reader = new ClassReader(className);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    public static class GoodCamelCase {
        private int goodField;
        private String anotherGoodField;

        public GoodCamelCase() { }

        public void doThing() { }

        public int addTwo(int value) {
            return value + 2;
        }
    }

    public static class BadFieldName {
        public int bad_field;

        public void okMethod() { }
    }

    public static class BadMethodName {
        private int okField;

        public void Bad_Method() { }
    }

    public static class multipleViolations {
        public int bad_field;

        public void Bad_Method() { }
    }

    public static class ConstructorOnly {
        public ConstructorOnly(int someValue) { }
    }
}
