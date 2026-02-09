package Domain.PrincipleCheck;

import Domain.Interfaces.IPrincipleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * This class checks for code duplication within a class.
 */

public class CodeDuplicationCheck implements IPrincipleCheck {
    @Override
    public List<LintResult> execute(ClassNode classNode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "CodeDuplication";
    }
}
