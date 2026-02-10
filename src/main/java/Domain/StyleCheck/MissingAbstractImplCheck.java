package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;

/**
 * A style check that verifies that classes extending abstract classes implement all abstract methods.
 *
 * This check detects:
 * - Concrete classes that extend abstract classes but don't implement all abstract methods
 * - Missing method implementations from the superclass hierarchy
 *
 * Note: This is primarily a compile-time error in Java, but can occur in bytecode analysis
 * when dealing with incomplete class hierarchies or during development.
 */
public class MissingAbstractImplCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();

        // Only check concrete classes (not abstract or interfaces)
        if (isAbstract(classNode) || isInterface(classNode)) {
            return lintResults;
        }

        // Check if this class extends an abstract class
        if (classNode.superName == null || classNode.superName.equals("java/lang/Object")) {
            return lintResults; // No abstract superclass
        }

        // Try to load the superclass and check for abstract methods
        try {
            ClassNode superClass = loadClass(classNode.superName);
            if (superClass != null && isAbstract(superClass)) {
                checkAbstractMethodImplementations(classNode, superClass, lintResults);
            }
        } catch (IOException e) {
            // Superclass not available for analysis - skip this check
            // This is common when analyzing only part of a codebase
        }

        return lintResults;
    }

    /**
     * Check if a concrete class implements all abstract methods from its superclass
     */
    private void checkAbstractMethodImplementations(ClassNode classNode, ClassNode superClass,
                                                    List<LintResult> lintResults) {
        // Get all abstract methods from the superclass hierarchy
        Set<MethodSignature> abstractMethods = collectAbstractMethods(superClass);

        if (abstractMethods.isEmpty()) {
            return; // No abstract methods to implement
        }

        // Get all methods implemented in this class
        Set<MethodSignature> implementedMethods = collectImplementedMethods(classNode);

        // Find missing implementations
        for (MethodSignature abstractMethod : abstractMethods) {
            if (!implementedMethods.contains(abstractMethod)) {
                lintResults.add(new LintResult(
                        getName(),
                        Category.STYLE,
                        "Class " + getSimpleClassName(classNode.name) + " does not implement abstract method '" +
                                abstractMethod.name + abstractMethod.descriptor + "' from " + getSimpleClassName(superClass.name),
                        "Concrete classes must implement all abstract methods from their superclass hierarchy. " +
                                "The method '" + abstractMethod.name + "' is declared abstract in " +
                                getSimpleClassName(superClass.name) + " but is not implemented in " +
                                getSimpleClassName(classNode.name) + ". " +
                                "Either implement this method or declare the class as abstract."
                ));
            }
        }
    }

    /**
     * Collect all abstract methods from a class and its superclass hierarchy
     */
    private Set<MethodSignature> collectAbstractMethods(ClassNode classNode) {
        Set<MethodSignature> abstractMethods = new HashSet<>();

        // Collect abstract methods from this class
        for (MethodNode method : classNode.methods) {
            if (isAbstractMethod(method)) {
                abstractMethods.add(new MethodSignature(method.name, method.desc));
            }
        }

        // Recursively collect from superclass
        if (classNode.superName != null && !classNode.superName.equals("java/lang/Object")) {
            try {
                ClassNode superClass = loadClass(classNode.superName);
                if (superClass != null) {
                    abstractMethods.addAll(collectAbstractMethods(superClass));
                }
            } catch (IOException e) {
                // Superclass not available - can't analyze further up the hierarchy
            }
        }

        return abstractMethods;
    }

    /**
     * Collect all methods implemented in a class (including inherited non-abstract methods)
     */
    private Set<MethodSignature> collectImplementedMethods(ClassNode classNode) {
        Set<MethodSignature> implementedMethods = new HashSet<>();

        // Collect concrete methods from this class
        for (MethodNode method : classNode.methods) {
            if (!isAbstractMethod(method)) {
                implementedMethods.add(new MethodSignature(method.name, method.desc));
            }
        }

        // Collect concrete methods from superclass
        if (classNode.superName != null && !classNode.superName.equals("java/lang/Object")) {
            try {
                ClassNode superClass = loadClass(classNode.superName);
                if (superClass != null) {
                    for (MethodNode method : superClass.methods) {
                        if (!isAbstractMethod(method) && !isPrivate(method)) {
                            // Inherited non-abstract, non-private methods count as implemented
                            implementedMethods.add(new MethodSignature(method.name, method.desc));
                        }
                    }

                    // Recursively collect from superclass's superclass
                    if (superClass.superName != null && !superClass.superName.equals("java/lang/Object")) {
                        implementedMethods.addAll(collectInheritedConcreteMethods(superClass.superName));
                    }
                }
            } catch (IOException e) {
                // Superclass not available
            }
        }

        return implementedMethods;
    }

    /**
     * Collect inherited concrete methods from the class hierarchy
     */
    private Set<MethodSignature> collectInheritedConcreteMethods(String className) {
        Set<MethodSignature> methods = new HashSet<>();

        try {
            ClassNode classNode = loadClass(className);
            if (classNode != null) {
                for (MethodNode method : classNode.methods) {
                    if (!isAbstractMethod(method) && !isPrivate(method)) {
                        methods.add(new MethodSignature(method.name, method.desc));
                    }
                }

                // Recursively collect from superclass
                if (classNode.superName != null && !classNode.superName.equals("java/lang/Object")) {
                    methods.addAll(collectInheritedConcreteMethods(classNode.superName));
                }
            }
        } catch (IOException e) {
            // Class not available
        }

        return methods;
    }

    /**
     * Load a class by name for analysis
     */
    private ClassNode loadClass(String className) throws IOException {
        // Try to load from classpath
        String resourceName = className + ".class";
        InputStream classStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);

        if (classStream == null) {
            // Try loading as if it's in the current package being analyzed
            throw new IOException("Class not found: " + className);
        }

        try {
            ClassReader classReader = new ClassReader(classStream);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        } finally {
            classStream.close();
        }
    }

    /**
     * Check if a class is abstract
     */
    private boolean isAbstract(ClassNode classNode) {
        return (classNode.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    /**
     * Check if a class is an interface
     */
    private boolean isInterface(ClassNode classNode) {
        return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    }

    /**
     * Check if a method is abstract
     */
    private boolean isAbstractMethod(MethodNode method) {
        return (method.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    /**
     * Check if a method is private
     */
    private boolean isPrivate(MethodNode method) {
        return (method.access & Opcodes.ACC_PRIVATE) != 0;
    }

    /**
     * Get simple class name from full path
     */
    private String getSimpleClassName(String fullClassName) {
        int lastSlash = fullClassName.lastIndexOf('/');
        if (lastSlash >= 0) {
            return fullClassName.substring(lastSlash + 1);
        }
        return fullClassName;
    }

    @Override
    public String getName() {
        return "MissingAbstractImplCheck";
    }

    /**
     * Helper class to represent a method signature (name + descriptor)
     */
    private static class MethodSignature {
        final String name;
        final String descriptor;

        MethodSignature(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MethodSignature that = (MethodSignature) obj;
            return name.equals(that.name) && descriptor.equals(that.descriptor);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + descriptor.hashCode();
        }

        @Override
        public String toString() {
            return name + descriptor;
        }
    }
}