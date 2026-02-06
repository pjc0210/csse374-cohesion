package Domain.PatternCheck;

import Domain.Interfaces.IPatternCheck;
import Domain.LintResult;
import Domain.Category;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.util.*;

/**
 * This class checks for redundant interfaces in a given class node.
 */
public class RedundantInterfaceCheck implements IPatternCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> results = new ArrayList<>();

        if (classNode == null || classNode.interfaces == null) {
            return results;
        }

        boolean ownerIsInterface =
                (classNode.access & Opcodes.ACC_INTERFACE) != 0;

        List<String> interfaces = classNode.interfaces;
        if (interfaces.size() < 2) {
            return results;
        }

        for (int i = 0; i < interfaces.size(); i++) {
            String ifaceA = interfaces.get(i);
            for (int j = 0; j < interfaces.size(); j++) {
                if (i == j) continue;

                String ifaceB = interfaces.get(j);

                if (isSuperInterfaceOf(ifaceA, ifaceB)) {
                    String location = classNode.name.replace('/', '.');

                    String message =
                            (ownerIsInterface
                                    ? "Redundant extended interface: "
                                    : "Redundant implemented interface: ")
                                    + ifaceA.replace('/', '.')
                                    + " is already inherited through "
                                    + ifaceB.replace('/', '.');

                    results.add(
                            new LintResult(
                                    getName(),
                                    Category.PATTERN,
                                    location,
                                    message
                            )
                    );
                    break;
                }
            }
        }

        return dedupe(results);
    }

    @Override
    public String getName() {
        return "RedundantInterfaces";
    }

    private boolean isSuperInterfaceOf(String possibleSuper, String possibleSub) {
        Set<String> visited = new HashSet<>();
        Deque<String> worklist = new ArrayDeque<>();
        worklist.add(possibleSub);

        while (!worklist.isEmpty()) {
            String current = worklist.removeFirst();
            if (!visited.add(current)) continue;

            ClassNode node = loadClassNode(current);
            if (node == null || node.interfaces == null) continue;

            for (String parent : node.interfaces) {
                if (parent.equals(possibleSuper)) {
                    return true;
                }
                worklist.add(parent);
            }
        }
        return false;
    }

    private ClassNode loadClassNode(String internalName) {
        String resource = internalName + ".class";
        try (InputStream in =
                     Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(resource)) {

            if (in == null) return null;

            ClassReader reader = new ClassReader(in);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;

        } catch (Exception e) {
            return null;
        }
    }

    private List<LintResult> dedupe(List<LintResult> input) {
        LinkedHashMap<String, LintResult> map = new LinkedHashMap<>();
        for (LintResult r : input) {
            map.putIfAbsent(r.toString(), r);
        }
        return new ArrayList<>(map.values());
    }
}
