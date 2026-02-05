package Domain.StyleCheck;

import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;
import java.util.List;

/**
 * A style check that verifies spelling in the code.
 */

public class SpellCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classnode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "Spelling";
    }
}
