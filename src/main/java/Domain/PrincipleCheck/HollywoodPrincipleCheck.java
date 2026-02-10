package Domain.PrincipleCheck;

import Domain.Category;
import Domain.Interfaces.IPrincipleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This class checks for adherence to the Hollywood Principle.
 *
 * Hollywood Principle: "Don't call us, we'll call you"
 *
 * Violations detected:
 * 1. Concrete classes directly calling framework/abstract base class methods
 *    (instead of letting the framework call them)
 * 2. Low-level components calling high-level components directly
 * 3. Subclasses calling parent class methods other than super() constructors
 *    when they should be overriding and being called by the parent
 *
 * Key indicators:
 * - Classes with names suggesting they are implementations/concrete types
 *   calling abstract/base/framework classes
 * - Use of INVOKESTATIC or INVOKEVIRTUAL on parent/framework classes
 *   outside of constructors
 */
public class HollywoodPrincipleCheck implements IPrincipleCheck {

    // Framework and high-level class indicators
    private static final Set<String> FRAMEWORK_KEYWORDS = new HashSet<>(Arrays.asList(
            "Abstract", "Base", "Framework", "Manager", "Controller", "Handler", "Template"
    ));

    // Low-level implementation indicators
    private static final Set<String> IMPLEMENTATION_KEYWORDS = new HashSet<>(Arrays.asList(
            "Impl", "Concrete", "Default", "Simple", "Basic"
    ));

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> results = new ArrayList<>();

        // Skip interfaces and abstract classes
        if ((classNode.access & Opcodes.ACC_INTERFACE) != 0 ||
                (classNode.access & Opcodes.ACC_ABSTRACT) != 0) {
            return results;
        }

        String className = classNode.name.replace('/', '.');
        boolean isLowLevelClass = isLowLevelImplementation(className);

        // Check each method for Hollywood Principle violations
        for (MethodNode method : classNode.methods) {
            if (method.instructions == null) {
                continue;
            }

            // Analyze method calls
            method.instructions.forEach(instruction -> {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode methodCall = (MethodInsnNode) instruction;
                    checkMethodCall(classNode, method, methodCall, isLowLevelClass, results);
                }
            });
        }

        // Check for inappropriate calls to superclass methods
        checkSuperclassCalls(classNode, results);

        return results;
    }

    private void checkMethodCall(ClassNode classNode,
                                 MethodNode method,
                                 MethodInsnNode methodCall,
                                 boolean isLowLevelClass,
                                 List<LintResult> results) {

        String calledClass = methodCall.owner.replace('/', '.');
        String calledMethod = methodCall.name;

        // Skip constructor calls - they're expected
        if (calledMethod.equals("<init>") || method.name.equals("<init>")) {
            return;
        }

        // Skip calls to standard library and common utilities
        if (isStandardLibrary(calledClass)) {
            return;
        }

        // Check if calling a framework/high-level class
        if (isFrameworkClass(calledClass)) {
            String message = String.format(
                    "Hollywood Principle violation: Low-level class '%s' is calling framework/high-level class '%s.%s()' directly. " +
                            "The framework should call you, not the other way around.",
                    getSimpleName(classNode.name),
                    getSimpleName(calledClass),
                    calledMethod
            );

            results.add(new LintResult(
                    getName(),
                    Category.STYLE,
                    classNode.name.replace('/', '.'),
                    message
            ));
        }

        // Check for low-level calling high-level based on naming
        if (isLowLevelClass && isHighLevelClass(calledClass)) {
            String message = String.format(
                    "Hollywood Principle violation in method '%s': Implementation class '%s' is calling high-level class '%s.%s()'. " +
                            "Consider using dependency injection or callbacks instead.",
                    method.name,
                    getSimpleName(classNode.name),
                    getSimpleName(calledClass),
                    calledMethod
            );

            results.add(new LintResult(
                    getName(),
                    Category.STYLE,
                    classNode.name.replace('/', '.'),
                    message
            ));
        }
    }

    private void checkSuperclassCalls(ClassNode classNode, List<LintResult> results) {
        String superName = classNode.superName;

        // Skip if no superclass or only extends Object
        if (superName == null || superName.equals("java/lang/Object")) {
            return;
        }

        // If parent is abstract/template, check for template method pattern violations
        if (isTemplateClass(superName)) {
            for (MethodNode method : classNode.methods) {
                if (method.instructions == null) {
                    continue;
                }

                final boolean[] callsSuperNonConstructor = {false};

                method.instructions.forEach(instruction -> {
                    if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode methodCall = (MethodInsnNode) instruction;

                        // Check if calling super class method (not constructor)
                        if (methodCall.owner.equals(superName) &&
                                !methodCall.name.equals("<init>") &&
                                methodCall.getOpcode() == Opcodes.INVOKESPECIAL) {
                            callsSuperNonConstructor[0] = true;
                        }
                    }
                });

                if (callsSuperNonConstructor[0] && !method.name.equals("<init>")) {
                    String message = String.format(
                            "Possible Hollywood Principle violation: Method '%s' in class '%s' calls parent template class '%s' directly. " +
                                    "In Template Method pattern, the parent should call your overridden methods.",
                            method.name,
                            getSimpleName(classNode.name),
                            getSimpleName(superName)
                    );

                    results.add(new LintResult(
                            getName(),
                            Category.STYLE,
                            classNode.name.replace('/', '.'),
                            message
                    ));
                }
            }
        }
    }

    private boolean isLowLevelImplementation(String className) {
        String simpleName = getSimpleName(className);
        for (String kw : IMPLEMENTATION_KEYWORDS) {
            if (simpleName.contains(kw)) return true;
        }
        return false;
    }

    private boolean isFrameworkClass(String className) {
        String simpleName = getSimpleName(className);
        for (String kw : FRAMEWORK_KEYWORDS) {
            if (simpleName.contains(kw)) return true;
        }
        return false;
    }

    private boolean isHighLevelClass(String className) {
        String simpleName = getSimpleName(className);
        return simpleName.endsWith("Manager") ||
                simpleName.endsWith("Controller") ||
                simpleName.endsWith("Service") ||
                simpleName.endsWith("Facade") ||
                simpleName.endsWith("Coordinator");
    }

    private boolean isTemplateClass(String className) {
        String simpleName = getSimpleName(className);
        return simpleName.contains("Template") ||
                simpleName.startsWith("Abstract") ||
                simpleName.startsWith("Base");
    }

    private boolean isStandardLibrary(String className) {
        return className.startsWith("java.") ||
                className.startsWith("javax.") ||
                className.startsWith("sun.") ||
                className.startsWith("com.sun.");
    }

    private String getSimpleName(String fullName) {
        String cleaned = fullName.replace('/', '.');
        int lastDot = cleaned.lastIndexOf('.');
        return lastDot >= 0 ? cleaned.substring(lastDot + 1) : cleaned;
    }

    @Override
    public String getName() {
        return "HollywoodPrinciple";
    }
}
