package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A style check that identifies global variable naming and usage in the class.
 *
 * Conventions checked:
 * 1. Static final fields (constants) should be UPPER_SNAKE_CASE
 * 2. Static non-final fields should be camelCase (discouraged but allowed)
 * 3. All global variables should not start with underscores (unless constant)
 * 4. Constants should be public static final
 */
public class GlobalVariableCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();

        List<FieldNode> fields = classNode.fields;

        for (FieldNode field : fields) {
            // Check if field is static (global variable)
            if (isStatic(field)) {
                checkGlobalVariableConventions(field, lintResults, classNode.name);
            }
        }

        return lintResults;
    }

    /**
     * Check if a field is static
     */
    private boolean isStatic(FieldNode field) {
        return (field.access & Opcodes.ACC_STATIC) != 0;
    }

    /**
     * Check if a field is final
     */
    private boolean isFinal(FieldNode field) {
        return (field.access & Opcodes.ACC_FINAL) != 0;
    }

    /**
     * Check if a field is public
     */
    private boolean isPublic(FieldNode field) {
        return (field.access & Opcodes.ACC_PUBLIC) != 0;
    }

    /**
     * Check if a field is private
     */
    private boolean isPrivate(FieldNode field) {
        return (field.access & Opcodes.ACC_PRIVATE) != 0;
    }

    /**
     * Check global variable naming conventions
     */
    private void checkGlobalVariableConventions(FieldNode field, List<LintResult> lintResults, String className) {
        String fieldName = field.name;
        boolean isConstant = isFinal(field);

        if (isConstant) {
            // Constants should be UPPER_SNAKE_CASE
            checkConstantNaming(field, fieldName, lintResults, className);
        } else {
            // Non-constant static fields should be camelCase
            checkStaticVariableNaming(field, fieldName, lintResults, className);
        }
    }

    /**
     * Check that constants follow UPPER_SNAKE_CASE convention
     */
    private void checkConstantNaming(FieldNode field, String fieldName, List<LintResult> lintResults, String className) {
        if (!isUpperSnakeCase(fieldName)) {
            lintResults.add(new LintResult(
                    getName(),
                    Category.STYLE,
                    "Constant '" + fieldName + "' in class " + className + " does not follow UPPER_SNAKE_CASE convention",
                    "Constants (static final fields) should be named in UPPER_SNAKE_CASE. " +
                            "Example: MAX_VALUE, DEFAULT_SIZE, CONNECTION_TIMEOUT"
            ));
        }

        // Check that constants are public (best practice)
        if (!isPublic(field) && !isPrivate(field)) {
            lintResults.add(new LintResult(
                    getName(),
                    Category.STYLE,
                    "Constant '" + fieldName + "' in class " + className + " should be either public or private",
                    "Constants should typically be declared as 'public static final' for shared constants " +
                            "or 'private static final' for class-internal constants."
            ));
        }
    }

    /**
     * Check that non-constant static variables follow camelCase convention
     */
    private void checkStaticVariableNaming(FieldNode field, String fieldName, List<LintResult> lintResults, String className) {
        if (!isCamelCase(fieldName)) {
            lintResults.add(new LintResult(
                    getName(),
                    Category.STYLE,
                    "Static variable '" + fieldName + "' in class " + className + " does not follow camelCase convention",
                    "Non-constant static variables should be named in camelCase. " +
                            "Example: instanceCount, sharedBuffer, currentState. " +
                            "Note: Mutable static variables are generally discouraged; consider using constants or instance variables instead."
            ));
        }

        // Warn about mutable global variables (design smell)
        if (isPublic(field)) {
            lintResults.add(new LintResult(
                    getName(),
                    Category.STYLE,
                    "Public mutable static variable '" + fieldName + "' in class " + className + " detected",
                    "Public mutable static variables are a code smell and can lead to unexpected behavior. " +
                            "Consider making this field private with accessor methods, or if it's truly a constant, make it final."
            ));
        }
    }

    /**
     * Check if a name follows UPPER_SNAKE_CASE convention
     * Examples: MAX_VALUE, DEFAULT_SIZE, PI
     */
    private boolean isUpperSnakeCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Should not start or end with underscore (except for special cases like serialVersionUID)
        if (name.equals("serialVersionUID")) {
            return true;
        }

        // Must match pattern: uppercase letters, digits, and underscores
        // Should not start or end with underscore
        // Should not have consecutive underscores
        return name.matches("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");
    }

    /**
     * Check if a name follows camelCase convention
     * Examples: instanceCount, myVariable, count
     */
    private boolean isCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        if (!Character.isLowerCase(name.charAt(0))) {
            return false;
        }

        if (name.contains("_")) {
            return false;
        }

        return name.matches("^[a-z][a-zA-Z0-9]*$");
    }

    @Override
    public String getName() {
        return "GlobalVariableCheck";
    }
}