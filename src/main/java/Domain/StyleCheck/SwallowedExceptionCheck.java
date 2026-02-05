package Domain.StyleCheck;

import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;
import java.util.List;

/**
 * A style check that identifies swallowed exceptions in the class.
 */

public class SwallowedExceptionCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classnode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "SwallowedExceptions";
    }
}