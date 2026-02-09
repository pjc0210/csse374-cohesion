package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A style check that identifies unused variables in the class.
 */

public class UnusedVariablesCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();
        List<FieldNode> fields = classNode.fields;

        for (FieldNode field : fields) {
            if (!isFieldUsed(field, classNode)) {
                lintResults.add(new LintResult(
                        getName(),
                        Category.STYLE,
                        classNode.name,
                        "Field '" + field.name + "' is declared but never used."));
            }
        }
        return lintResults;
    }

    private boolean isFieldUsed(FieldNode field, ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.instructions == null) continue;

            for (AbstractInsnNode insn : method.instructions) {
                if (insn.getType() == AbstractInsnNode.FIELD_INSN) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;

                    // Check both the field name AND owner class
                    if (fieldInsn.name.equals(field.name) &&
                            fieldInsn.owner.equals(classNode.name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    @Override
    public String getName() {
        return "UnusedVariables";
    }
}
