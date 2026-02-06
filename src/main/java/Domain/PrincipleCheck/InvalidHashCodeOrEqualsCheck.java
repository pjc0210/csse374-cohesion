package Domain.PrincipleCheck;

import Domain.Category;
import Domain.Interfaces.IPrincipleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class checks for invalid equals() or hashCode() overrides.
 * According to the Java contract, if you override equals(), you must also override hashCode(), and vice versa.
 */
public class InvalidHashCodeOrEqualsCheck implements IPrincipleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();

        boolean hasEqualsOverride = false;
        boolean hasHashCodeOverride = false;

        // Check all methods in the class
        List<MethodNode> methods = classNode.methods;
        for (MethodNode method : methods) {
            // Check for equals(Object) method: signature is (Ljava/lang/Object;)Z
            if (method.name.equals("equals") && method.desc.equals("(Ljava/lang/Object;)Z")) {
                hasEqualsOverride = true;
            }

            // Check for hashCode() method: signature is ()I
            if (method.name.equals("hashCode") && method.desc.equals("()I")) {
                hasHashCodeOverride = true;
            }
        }

        // XOR check: exactly one is overridden but not both
        if (hasEqualsOverride ^ hasHashCodeOverride) {
            String message;
            if (hasEqualsOverride) {
                message = "Class overrides equals() but not hashCode(). " +
                        "Both methods must be overridden together to maintain the equals-hashCode contract.";
            } else {
                message = "Class overrides hashCode() but not equals(). " +
                        "Both methods must be overridden together to maintain the equals-hashCode contract.";
            }

            lintResults.add(new LintResult(
                    getName(),
                    Category.PRINCIPLE,
                    "Invalid equals/hashCode override in class: " + classNode.name,
                    message
            ));
        }

        return lintResults;
    }

    @Override
    public String getName() {
        return "InvalidHashCodeOrEqualsCheck";
    }
}