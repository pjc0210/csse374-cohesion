package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class CamelCaseCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> results = new ArrayList<>();

        String className = simpleName(classNode.name);

        // ---- Class name: UpperCamelCase ----
        if (!isUpperCamelCase(className)) {
            results.add(new LintResult(
                    getName(),
                    Category.STYLE,
                    classNode.name.replace('/', '.'),
                    "Class name '" + className + "' should use UpperCamelCase"
            ));
        }

        // ---- Field names: lowerCamelCase ----
        if (classNode.fields != null) {
            for (FieldNode field : classNode.fields) {
                if ((field.access & Opcodes.ACC_SYNTHETIC) != 0) continue;

                if (!isLowerCamelCase(field.name)) {
                    results.add(new LintResult(
                            getName(),
                            Category.STYLE,
                            classNode.name.replace('/', '.'),
                            "Field '" + field.name + "' should use lowerCamelCase"
                    ));
                }
            }
        }

        // ---- Method names: lowerCamelCase ----
        if (classNode.methods != null) {
            for (MethodNode method : classNode.methods) {
                if (method.name.equals("<init>") || method.name.equals("<clinit>")) continue;
                if ((method.access & Opcodes.ACC_SYNTHETIC) != 0) continue;

                if (!isLowerCamelCase(method.name)) {
                    results.add(new LintResult(
                            getName(),
                            Category.STYLE,
                            classNode.name.replace('/', '.'),
                            "Method '" + method.name + "' should use lowerCamelCase"
                    ));
                }
            }
        }

        return results;
    }

    @Override
    public String getName() {
        return "CamelCase";
    }

    // ==================== Helpers ====================

    private boolean isUpperCamelCase(String name) {
        if (name == null || name.isEmpty()) return false;
        if (!Character.isUpperCase(name.charAt(0))) return false;
        return !name.contains("_");
    }

    private boolean isLowerCamelCase(String name) {
        if (name == null || name.isEmpty()) return false;
        if (!Character.isLowerCase(name.charAt(0))) return false;
        return !name.contains("_");
    }

    private String simpleName(String internalName) {
        int idx = internalName.lastIndexOf('/');
        return idx >= 0 ? internalName.substring(idx + 1) : internalName;
    }
}
