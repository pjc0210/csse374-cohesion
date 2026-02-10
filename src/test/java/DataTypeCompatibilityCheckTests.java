import Domain.Category;
import Domain.LintResult;
import Domain.StyleCheck.DataTypeCompatibilityCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataTypeCompatibilityCheckTests {

    private DataTypeCompatibilityCheck check;

    @BeforeEach
    void setUp() {
        check = new DataTypeCompatibilityCheck();
    }

    @Test
    void testCorrectFieldAssignments() throws IOException {
        ClassNode classNode = getClassNode(CorrectFieldTypes.class);
        List<LintResult> results = check.execute(classNode);
        assertTrue(results.isEmpty(), "Should have no errors for correct field assignments");
    }

    @Test
    void testIncorrectFieldAssignment() throws IOException {
        ClassNode classNode = createIncorrectFieldTypesClass();
        List<LintResult> results = check.execute(classNode);

        assertFalse(results.isEmpty(), "Should detect incompatible field assignment");
        assertTrue(results.stream().anyMatch(r ->
                        r.getMessage().contains("Incompatible type assigned to field")),
                "Should report incompatible field assignment");
    }

    @Test
    void testCorrectMethodSignatures() throws IOException {
        ClassNode classNode = getClassNode(CorrectMethodSignatures.class);
        List<LintResult> results = check.execute(classNode);
        assertTrue(results.isEmpty(), "Should have no errors for correct method signatures");
    }

    @Test
    void testIncorrectMethodSignature() throws IOException {
        ClassNode classNode = createIncorrectMethodSignaturesClass();
        List<LintResult> results = check.execute(classNode);

        assertFalse(results.isEmpty(), "Should detect incompatible method signature");
        assertTrue(results.stream().anyMatch(r ->
                        r.getMessage().contains("Incompatible method signature")),
                "Should report incompatible method signature");
    }

    @Test
    void testCorrectReturnTypes() throws IOException {
        ClassNode classNode = getClassNode(CorrectReturnTypes.class);
        List<LintResult> results = check.execute(classNode);
        assertTrue(results.isEmpty(), "Should have no errors for correct return types");
    }

    @Test
    void testIncorrectReturnType() throws IOException {
        ClassNode classNode = createIncorrectReturnTypesClass();
        List<LintResult> results = check.execute(classNode);

        assertFalse(results.isEmpty(), "Should detect incompatible return type");
        assertTrue(results.stream().anyMatch(r ->
                        r.getMessage().contains("Incompatible return type")),
                "Should report incompatible return type");
    }

    @Test
    void testGetName() {
        assertEquals("DataTypeCompatibility", check.getName());
    }

    @Test
    void testResultCategory() throws IOException {
        ClassNode classNode = createIncorrectFieldTypesClass();
        List<LintResult> results = check.execute(classNode);

        if (!results.isEmpty()) {
            assertEquals(Category.STYLE, results.get(0).getCategory());
        }
    }

    // ==================== Helper Methods ====================

    private ClassNode getClassNode(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        ClassReader classReader = new ClassReader(className);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    private ClassNode createIncorrectFieldTypesClass() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = "IncorrectFieldTypes";
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

        // Create an int field
        cw.visitField(Opcodes.ACC_PRIVATE, "number", "I", null, null).visitEnd();

        // Constructor
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Method that assigns wrong type to field
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "badAssignment", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn("not a number");
        // WRONG: Using String descriptor for int field
        mv.visitFieldInsn(Opcodes.PUTFIELD, className, "number", "Ljava/lang/String;");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();

        ClassReader classReader = new ClassReader(cw.toByteArray());
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    private ClassNode createIncorrectMethodSignaturesClass() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = "IncorrectMethodSignatures";
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

        // Constructor
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Actual method that takes an int
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "methodWithInt", "(I)V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();

        // Method that calls it with WRONG signature
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "badCaller", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn("wrong");
        // WRONG: Calling with String descriptor when actual method takes int
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "methodWithInt", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();

        ClassReader classReader = new ClassReader(cw.toByteArray());
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    private ClassNode createIncorrectReturnTypesClass() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = "IncorrectReturnTypes";
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

        // Constructor
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Method declared to return int but uses ARETURN
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getBadNumber", "()I", null, null);
        mv.visitCode();
        mv.visitLdcInsn("not a number");
        // WRONG: Using ARETURN for int return type
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        ClassReader classReader = new ClassReader(cw.toByteArray());
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    // ==================== Embedded Test Classes ====================

    static class CorrectFieldTypes {
        private int number;
        private String text;

        public void setFields() {
            number = 42;
            text = "Hello";
        }
    }

    static class CorrectMethodSignatures {
        public void methodWithInt(int value) {
            System.out.println(value);
        }

        public void caller() {
            methodWithInt(42);
        }
    }

    static class CorrectReturnTypes {
        public int getNumber() {
            return 42;
        }

        public String getText() {
            return "Hello";
        }

        public void doNothing() {
            return;
        }
    }
}