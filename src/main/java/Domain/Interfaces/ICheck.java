package Domain.Interfaces;

import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public interface ICheck {
    List<LintResult> execute(ClassNode classNode);
    String getName();
}
