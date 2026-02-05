package Domain.PrincipleCheck;

import Domain.Interfaces.IPrincipleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/*
 * This class checks for adherence to the Hollywood Principle.
 */

public class HollywoodPrincipleCheck implements IPrincipleCheck {
    @Override
    public List<LintResult> execute(ClassNode classnode) {
        return List.of();
    }

    @Override
    public String getName() {
        return "Hollywood";
    }
}
