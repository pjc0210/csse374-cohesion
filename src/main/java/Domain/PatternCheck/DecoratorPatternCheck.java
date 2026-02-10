package Domain.PatternCheck;

import Domain.Category;
import Domain.Interfaces.IPatternCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class checks for poor Decorator Pattern implementations.
 *
 * A good decorator should:
 * 1. Have a field that stores the wrapped object (component)
 * 2. Use that wrapped object in its methods
 * 3. Delegate calls to the wrapped object
 *
 * Poor decorators are detected when:
 * - The decorator has a wrapped component field that is never used
 * - Constructor parameters are not stored/used
 * - Methods don't delegate to the wrapped component
 */
public class DecoratorPatternCheck implements IPatternCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();

        // Check if this class looks like a decorator
        if (isLikelyDecorator(classNode)) {
            checkDecoratorQuality(classNode, lintResults);
        }

        return lintResults;
    }

    /**
     * Determine if a class is likely implementing the Decorator pattern
     * Heuristics:
     * 1. Has a field of an interface/abstract type that matches implemented interface/superclass
     * 2. Implements/extends an interface/abstract class
     * 3. Constructor takes a parameter of that type (even if not stored)
     */
    private boolean isLikelyDecorator(ClassNode classNode) {
        // Skip interfaces and abstract classes
        if ((classNode.access & Opcodes.ACC_INTERFACE) != 0 ||
                (classNode.access & Opcodes.ACC_ABSTRACT) != 0) {
            return false;
        }

        // Must implement an interface or extend a non-Object class
        boolean implementsInterface = classNode.interfaces != null && !classNode.interfaces.isEmpty();
        boolean extendsNonObject = classNode.superName != null &&
                !classNode.superName.equals("java/lang/Object");

        if (!implementsInterface && !extendsNonObject) {
            return false;
        }

        // Check if has a field OR constructor parameter of the implemented type
        // This catches decorators even if they forget to store the field

        // First check: Has field of matching type?
        for (FieldNode field : classNode.fields) {
            if (!isStatic(field) && !isPrimitive(field.desc)) {
                String fieldType = extractClassName(field.desc);

                // Check if field type matches superclass
                if (extendsNonObject && fieldType.equals(classNode.superName)) {
                    return true;
                }

                // Check if field type matches any implemented interface
                if (implementsInterface) {
                    for (Object interfaceName : classNode.interfaces) {
                        if (fieldType.equals(interfaceName)) {
                            return true;
                        }
                    }
                }
            }
        }

        // Second check: Constructor takes parameter of matching type?
        // This catches bad decorators that receive the component but don't store it
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("<init>")) {
                String descriptor = method.desc;
                List<String> paramTypes = extractParameterTypes(descriptor);

                for (String paramType : paramTypes) {
                    // Check if parameter type matches superclass
                    if (extendsNonObject && paramType.equals(classNode.superName)) {
                        return true;
                    }

                    // Check if parameter type matches any implemented interface
                    if (implementsInterface) {
                        for (Object interfaceName : classNode.interfaces) {
                            if (paramType.equals(interfaceName)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check the quality of decorator implementation
     */
    private void checkDecoratorQuality(ClassNode classNode, List<LintResult> lintResults) {
        // Find potential component fields (fields of interface/abstract types)
        List<FieldNode> componentFields = findComponentFields(classNode);

        // Get the expected component type (from interfaces or superclass)
        Set<String> expectedComponentTypes = new HashSet<>();
        if (classNode.interfaces != null) {
            for (Object interfaceName : classNode.interfaces) {
                expectedComponentTypes.add((String) interfaceName);
            }
        }
        if (classNode.superName != null && !classNode.superName.equals("java/lang/Object")) {
            expectedComponentTypes.add(classNode.superName);
        }

        // Check if constructor parameters match expected types but aren't stored
        checkUnusedConstructorParameters(classNode, expectedComponentTypes, lintResults);

        // If we have component fields, check if they're used properly
        if (componentFields.isEmpty()) {
            return; // Already flagged by checkUnusedConstructorParameters if applicable
        }

        for (FieldNode componentField : componentFields) {
            // Check if the component field is actually used
            boolean fieldUsed = isFieldUsedInMethods(classNode, componentField.name);

            if (!fieldUsed) {
                lintResults.add(new LintResult(
                        getName(),
                        Category.PATTERN,
                        "Decorator field '" + componentField.name + "' in class " + classNode.name + " is never used",
                        "The decorator pattern requires the wrapped component to be used. " +
                                "The field '" + componentField.name + "' appears to be a component field but is never referenced in any methods. " +
                                "This suggests a poor decorator implementation where the decorator doesn't delegate to the wrapped object."
                ));
            } else {
                // Check if field is used meaningfully (not just assigned)
                boolean fieldUsedMeaningfully = isFieldDelegatedTo(classNode, componentField.name);

                if (!fieldUsedMeaningfully) {
                    lintResults.add(new LintResult(
                            getName(),
                            Category.PATTERN,
                            "Decorator field '" + componentField.name + "' in class " + classNode.name + " is not properly delegated to",
                            "The decorator pattern requires delegating method calls to the wrapped component. " +
                                    "The field '" + componentField.name + "' is assigned but never has methods called on it, " +
                                    "suggesting the decorator is not properly forwarding calls to the wrapped object."
                    ));
                }
            }
        }
    }

    /**
     * Find fields that are likely component references in a decorator
     */
    private List<FieldNode> findComponentFields(ClassNode classNode) {
        List<FieldNode> componentFields = new ArrayList<>();

        for (FieldNode field : classNode.fields) {
            // Component fields should be:
            // 1. Non-static
            // 2. Non-primitive
            // 3. Preferably private or protected
            if (!isStatic(field) && !isPrimitive(field.desc)) {
                String fieldType = extractClassName(field.desc);

                // Check if field type matches superclass or interface
                if (classNode.superName != null && fieldType.equals(classNode.superName)) {
                    componentFields.add(field);
                } else if (classNode.interfaces != null) {
                    for (Object interfaceName : classNode.interfaces) {
                        if (fieldType.equals(interfaceName)) {
                            componentFields.add(field);
                            break;
                        }
                    }
                }
            }
        }

        return componentFields;
    }

    /**
     * Check if a field is used anywhere in the class methods
     */
    private boolean isFieldUsedInMethods(ClassNode classNode, String fieldName) {
        for (MethodNode method : classNode.methods) {
            if (method.instructions == null) {
                continue;
            }

            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) instruction;
                    if (fieldInsn.name.equals(fieldName) &&
                            (fieldInsn.getOpcode() == Opcodes.GETFIELD ||
                                    fieldInsn.getOpcode() == Opcodes.PUTFIELD)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if a field has method calls made on it (delegation)
     */
    private boolean isFieldDelegatedTo(ClassNode classNode, String fieldName) {
        for (MethodNode method : classNode.methods) {
            if (method.instructions == null) {
                continue;
            }

            boolean fieldLoaded = false;

            for (AbstractInsnNode instruction : method.instructions) {
                // Check if field is loaded (GETFIELD)
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) instruction;
                    if (fieldInsn.name.equals(fieldName) &&
                            fieldInsn.getOpcode() == Opcodes.GETFIELD) {
                        fieldLoaded = true;
                    }
                }

                // Check if a method is called after field is loaded
                if (fieldLoaded && instruction instanceof MethodInsnNode) {
                    return true; // Field is used to call a method (delegation)
                }

                // Reset if we hit a label or other control flow
                if (instruction instanceof LabelNode || instruction instanceof LineNumberNode) {
                    continue;
                }

                // If we store to a local variable or call a different field, reset
                if (instruction.getOpcode() == Opcodes.ASTORE) {
                    fieldLoaded = false;
                }
            }
        }

        return false;
    }

    /**
     * Check for constructor parameters that are never used
     */
    private void checkUnusedConstructorParameters(ClassNode classNode, Set<String> expectedComponentTypes, List<LintResult> lintResults) {
        for (MethodNode method : classNode.methods) {
            // Only check constructors
            if (!method.name.equals("<init>")) {
                continue;
            }

            // Parse parameter types from descriptor
            List<String> paramTypes = extractParameterTypes(method.desc);

            // Check if any parameter matches the expected component type
            for (String paramType : paramTypes) {
                if (expectedComponentTypes.contains(paramType)) {
                    // This constructor takes the component type as parameter
                    // Check if it's stored in a field
                    boolean parameterStored = isParameterStoredInField(method, classNode.fields);

                    if (!parameterStored) {
                        lintResults.add(new LintResult(
                                getName(),
                                Category.PATTERN,
                                "Constructor in decorator class " + classNode.name + " takes component parameter but doesn't store it",
                                "The decorator pattern typically stores the wrapped component passed to the constructor. " +
                                        "This constructor takes a parameter of type '" + getSimpleClassName(paramType) + "' " +
                                        "but doesn't assign it to any field, which suggests the decorator is not properly " +
                                        "storing the component to wrap."
                        ));
                        break; // Only report once per constructor
                    }
                }
            }
        }
    }

    /**
     * Check if a constructor parameter is stored in a field
     */
    private boolean isParameterStoredInField(MethodNode constructor, List<FieldNode> fields) {
        if (constructor.instructions == null) {
            return false;
        }

        // Look for PUTFIELD instructions in the constructor
        for (AbstractInsnNode instruction : constructor.instructions) {
            if (instruction instanceof FieldInsnNode) {
                FieldInsnNode fieldInsn = (FieldInsnNode) instruction;
                if (fieldInsn.getOpcode() == Opcodes.PUTFIELD) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extract parameter types from method descriptor
     */
    private List<String> extractParameterTypes(String descriptor) {
        List<String> types = new ArrayList<>();
        int i = 1; // Skip opening '('

        while (i < descriptor.length() && descriptor.charAt(i) != ')') {
            char c = descriptor.charAt(i);
            if (c == 'L') {
                // Object type
                int endIndex = descriptor.indexOf(';', i);
                String type = descriptor.substring(i + 1, endIndex);
                types.add(type);
                i = endIndex + 1;
            } else if (c == '[') {
                // Array type - skip for now
                i++;
                if (descriptor.charAt(i) == 'L') {
                    i = descriptor.indexOf(';', i) + 1;
                } else {
                    i++;
                }
            } else {
                // Primitive type - skip
                i++;
            }
        }

        return types;
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

    /**
     * Helper: Check if field is static
     */
    private boolean isStatic(FieldNode field) {
        return (field.access & Opcodes.ACC_STATIC) != 0;
    }

    /**
     * Helper: Check if descriptor is primitive type
     */
    private boolean isPrimitive(String descriptor) {
        return descriptor.length() == 1 && "ZBCSIJFD".indexOf(descriptor.charAt(0)) >= 0;
    }

    /**
     * Helper: Extract class name from field descriptor
     */
    private String extractClassName(String descriptor) {
        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
            return descriptor.substring(1, descriptor.length() - 1);
        }
        return descriptor;
    }

    /**
     * Helper: Check if two types are related (basic check)
     */
    private boolean isRelatedType(String type1, String type2) {
        // Simple heuristic: check if names are similar
        String name1 = type1.substring(type1.lastIndexOf('/') + 1);
        String name2 = type2.substring(type2.lastIndexOf('/') + 1);
        return name1.equals(name2) || name1.contains(name2) || name2.contains(name1);
    }

    @Override
    public String getName() {
        return "DecoratorPatternCheck";
    }
}