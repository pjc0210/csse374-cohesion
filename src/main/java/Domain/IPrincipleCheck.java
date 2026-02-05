package Domain;

import org.objectweb.asm.tree.ClassNode;

public interface IPrincipleCheck {
    boolean execute(ClassNode classNode);

    String getName();
}
