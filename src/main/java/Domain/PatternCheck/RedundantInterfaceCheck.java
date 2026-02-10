package Domain.PatternCheck;

import Domain.Category;
import Domain.LintResult;
import Domain.Interfaces.IPatternCheck;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.*;

public class RedundantInterfaceCheck implements IPatternCheck {

    private final Map<String, ClassNode> nodeCache = new HashMap<>();
    private final Map<String, Set<String>> superInterfaceCache = new HashMap<>();
    private final Map<String, Set<String>> allInterfacesCache = new HashMap<>();

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> results = new ArrayList<>();

        if (classNode == null || classNode.interfaces == null || classNode.interfaces.isEmpty()) {
            return results;
        }

        String className = toDot(classNode.name);

        List<String> direct = new ArrayList<>();
        for (Object o : classNode.interfaces) {
            if (o != null) direct.add(o.toString());
        }

        Set<String> redundant = new LinkedHashSet<>();

        // 1) duplicates in the implements list
        Set<String> seen = new HashSet<>();
        for (String itf : direct) {
            if (!seen.add(itf)) redundant.add(itf);
        }

        // 2) interface is redundant because another implemented interface already extends it
        for (int i = 0; i < direct.size(); i++) {
            for (int j = 0; j < direct.size(); j++) {
                if (i == j) continue;
                String a = direct.get(i);
                String b = direct.get(j);
                if (a.equals(b)) continue;

                // if b extends a, then a is redundant when both are implemented
                if (interfaceExtends(b, a)) {
                    redundant.add(a);
                }
            }
        }

        // 3) interface is redundant because superclass already provides it (directly or indirectly)
        String superName = classNode.superName;
        if (superName != null && !"java/lang/Object".equals(superName)) {
            Set<String> superAll = allInterfacesOfType(superName);
            for (String itf : direct) {
                if (superAll.contains(itf)) redundant.add(itf);
            }
        }

        for (String itf : redundant) {
            String msg =
                    "Redundant interface: '" + simple(itf) + "' is explicitly implemented by '" + simple(classNode.name) +
                            "' even though it is already implied (via another interface or superclass).";

            // If your Category enum doesn't have PATTERN, change this to whatever you use (DESIGN, CODE_SMELL, etc.)
            results.add(new LintResult(getName(), Category.PATTERN, className, msg));
        }

        return results;
    }

    @Override
    public String getName() {
        return "Redundant Interface";
    }

    private boolean interfaceExtends(String childInternalName, String ancestorInternalName) {
        if (childInternalName == null || ancestorInternalName == null) return false;
        if (childInternalName.equals(ancestorInternalName)) return true;

        Set<String> supers = allSuperInterfaces(childInternalName);
        return supers.contains(ancestorInternalName);
    }

    private Set<String> allSuperInterfaces(String interfaceInternalName) {
        Set<String> cached = superInterfaceCache.get(interfaceInternalName);
        if (cached != null) return cached;

        Set<String> out = new HashSet<>();
        ClassNode n = loadNode(interfaceInternalName);
        if (n == null || n.interfaces == null) {
            superInterfaceCache.put(interfaceInternalName, out);
            return out;
        }

        for (Object o : n.interfaces) {
            if (o == null) continue;
            String sup = o.toString();
            if (out.add(sup)) {
                out.addAll(allSuperInterfaces(sup));
            }
        }

        superInterfaceCache.put(interfaceInternalName, out);
        return out;
    }

    private Set<String> allInterfacesOfType(String internalName) {
        Set<String> cached = allInterfacesCache.get(internalName);
        if (cached != null) return cached;

        Set<String> out = new HashSet<>();
        ClassNode n = loadNode(internalName);
        if (n == null) {
            allInterfacesCache.put(internalName, out);
            return out;
        }

        if (n.interfaces != null) {
            for (Object o : n.interfaces) {
                if (o == null) continue;
                String itf = o.toString();
                out.add(itf);
                out.addAll(allSuperInterfaces(itf));
            }
        }

        String sup = n.superName;
        if (sup != null && !"java/lang/Object".equals(sup)) {
            out.addAll(allInterfacesOfType(sup));
        }

        allInterfacesCache.put(internalName, out);
        return out;
    }

    private ClassNode loadNode(String internalName) {
        if (internalName == null) return null;

        ClassNode cached = nodeCache.get(internalName);
        if (cached != null) return cached;

        try {
            ClassReader reader = new ClassReader(internalName);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);
            nodeCache.put(internalName, node);
            return node;
        } catch (IOException e) {
            nodeCache.put(internalName, null);
            return null;
        }
    }

    private String toDot(String internal) {
        return internal == null ? "" : internal.replace('/', '.');
    }

    private String simple(String internalOrDot) {
        if (internalOrDot == null) return "";
        String x = internalOrDot.replace('/', '.');
        int k = x.lastIndexOf('.');
        return k >= 0 ? x.substring(k + 1) : x;
    }
}
