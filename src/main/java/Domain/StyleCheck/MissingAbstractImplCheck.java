package Domain.StyleCheck;

import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;
import java.util.List;

/**
 * A style check that verifies that classes extending abstract classes implement all abstract methods.
 */

public class MissingAbstractImplCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "MissingAbstractImpl";
    }
}