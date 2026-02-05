package Domain.PatternCheck;

import Domain.Interfaces.IPatternCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * This class checks for redundant interfaces in a given class node.
 */

public class RedundantInterfaceCheck implements IPatternCheck {

    @Override
    public List<LintResult> execute(ClassNode classnode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "RedundantInterfaces";
    }
}
