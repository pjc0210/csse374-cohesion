package Domain.PatternCheck;

import Domain.Interfaces.IPatternCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * This class checks for the Three Layer Pattern in a given class node.
 */

public class ThreeLayerPatternCheck implements IPatternCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "ThreeLayerPattern";
    }
}
