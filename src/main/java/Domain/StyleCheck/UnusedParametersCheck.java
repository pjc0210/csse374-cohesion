package Domain.StyleCheck;

import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;
import java.util.List;

/**
 * A style check that identifies unused parameters in methods.
 */

public class UnusedParametersCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classnode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "UnusedParams";
    }
}
