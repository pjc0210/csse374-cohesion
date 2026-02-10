package abstractExamples;

import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility to generate bytecode for classes that don't implement abstract methods
 * This simulates scenarios that can't be created with normal Java compilation
 */
public class BadBytecodeGenerator {

    public static void main(String[] args) throws IOException {
        // Ensure output directory exists
        new File("target/test-classes/abstractExamples").mkdirs();

        generateBadMissingOneMethod();
        generateBadMissingAllMethods();
    }

    /**
     * Generate a concrete class that extends an abstract class but doesn't implement all methods
     */
    public static void generateBadMissingOneMethod() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // Create the abstract base class first
        cw.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_SUPER,
                "abstractExamples/BaseWithTwoMethods",
                null,
                "java/lang/Object",
                null);

        // Add abstract methodOne
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT,
                "methodOne",
                "()V",
                null,
                null);
        mv.visitEnd();

        // Add abstract methodTwo
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT,
                "methodTwo",
                "()V",
                null,
                null);
        mv.visitEnd();

        // Constructor
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        // Write base class
        byte[] baseBytes = cw.toByteArray();
        try (FileOutputStream fos = new FileOutputStream("target/test-classes/abstractExamples/BaseWithTwoMethods.class")) {
            fos.write(baseBytes);
        }

        // Now create the concrete class that only implements methodOne
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // NOTE: No ACC_ABSTRACT flag - this is a concrete class
        cw.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                "abstractExamples/BadMissingOneMethod",
                null,
                "abstractExamples/BaseWithTwoMethods",
                null);

        // Constructor
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "abstractExamples/BaseWithTwoMethods", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Implement only methodOne
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "methodOne", "()V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Method one implemented");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        // DON'T implement methodTwo - this creates the violation

        cw.visitEnd();

        byte[] classBytes = cw.toByteArray();
        try (FileOutputStream fos = new FileOutputStream("target/test-classes/abstractExamples/BadMissingOneMethod.class")) {
            fos.write(classBytes);
        }

        System.out.println("Generated BadMissingOneMethod.class");
    }

    /**
     * Generate a concrete class that doesn't implement any abstract methods
     */
    public static void generateBadMissingAllMethods() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // Create abstract base class
        cw.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_SUPER,
                "abstractExamples/BaseWithMethod",
                null,
                "java/lang/Object",
                null);

        // Add abstract method
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT,
                "requiredMethod",
                "()V",
                null,
                null);
        mv.visitEnd();

        // Constructor
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        // Write base class
        byte[] baseBytes = cw.toByteArray();
        try (FileOutputStream fos = new FileOutputStream("target/test-classes/abstractExamples/BaseWithMethod.class")) {
            fos.write(baseBytes);
        }

        // Create concrete class that doesn't implement the abstract method
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                "abstractExamples/BadMissingAllMethods",
                null,
                "abstractExamples/BaseWithMethod",
                null);

        // Constructor
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "abstractExamples/BaseWithMethod", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Add some other method (but not the required one)
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "someOtherMethod", "()V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Some other method");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();

        byte[] classBytes = cw.toByteArray();
        try (FileOutputStream fos = new FileOutputStream("target/test-classes/abstractExamples/BadMissingAllMethods.class")) {
            fos.write(classBytes);
        }

        System.out.println("Generated BadMissingAllMethods.class");
    }
}