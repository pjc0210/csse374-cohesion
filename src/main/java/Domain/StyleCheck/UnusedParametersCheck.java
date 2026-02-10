package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

public class UnusedParametersCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> out = new ArrayList<>();
        if (classNode.methods == null) return out;

        for (MethodNode method : classNode.methods) {
            if (shouldSkip(method)) continue;

            List<ParamInfo> params = getParams(method);
            if (params.isEmpty()) continue;

            Set<Integer> usedSlots = getUsedSlots(method);

            for (ParamInfo p : params) {
                if (!usedSlots.contains(p.slot)) {
                    String msg = "Unused parameter '" + p.name + "' in method '" + method.name + "'";
                    out.add(new LintResult(
                            getName(),
                            Category.STYLE,
                            classNode.name.replace('/', '.'),
                            msg
                    ));
                }
            }
        }

        return out;
    }

    @Override
    public String getName() {
        return "UnusedParameters";
    }

    private boolean shouldSkip(MethodNode method) {
        if ((method.access & Opcodes.ACC_ABSTRACT) != 0) return true;
        if ((method.access & Opcodes.ACC_NATIVE) != 0) return true;

        if (method.name.equals("<clinit>")) return true;

        int start = method.desc.indexOf('(') + 1;
        int end = method.desc.indexOf(')');
        return !(start < end);
    }

    private static final class ParamInfo {
        final int slot;
        final String name;

        ParamInfo(int slot, String name) {
            this.slot = slot;
            this.name = name;
        }
    }

    private List<ParamInfo> getParams(MethodNode method) {
        List<Integer> paramSlots = computeParamSlots(method);
        if (paramSlots.isEmpty()) return List.of();

        Map<Integer, String> slotToName = new HashMap<>();
        if (method.localVariables != null) {
            for (LocalVariableNode lv : method.localVariables) {
                if (lv == null) continue;
                if (paramSlots.contains(lv.index)) {
                    if (!"this".equals(lv.name)) {
                        slotToName.put(lv.index, lv.name);
                    }
                }
            }
        }

        List<ParamInfo> params = new ArrayList<>();
        int fallbackNum = 0;
        for (int slot : paramSlots) {
            String name = slotToName.get(slot);
            if (name == null || name.isBlank()) {
                name = "param" + fallbackNum;
            }
            fallbackNum++;
            params.add(new ParamInfo(slot, name));
        }
        return params;
    }

    private List<Integer> computeParamSlots(MethodNode method) {
        List<Integer> slots = new ArrayList<>();

        boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
        int slot = isStatic ? 0 : 1;

        String desc = method.desc;
        int i = desc.indexOf('(') + 1;

        while (desc.charAt(i) != ')') {
            char c = desc.charAt(i);

            slots.add(slot);

            if (c == 'J' || c == 'D') {
                slot += 2;
            } else {
                slot += 1;
            }

            if (c == 'L') {
                while (desc.charAt(i) != ';') i++;
            } else if (c == '[') {
                while (desc.charAt(i) == '[') i++;
                if (desc.charAt(i) == 'L') {
                    while (desc.charAt(i) != ';') i++;
                }
            }

            i++;
        }

        return slots;
    }

    private Set<Integer> getUsedSlots(MethodNode method) {
        Set<Integer> used = new HashSet<>();
        if (method.instructions == null) return used;

        for (AbstractInsnNode insn = method.instructions.getFirst();
             insn != null;
             insn = insn.getNext()) {

            if (insn instanceof VarInsnNode) {
                VarInsnNode v = (VarInsnNode) insn;
                if (isLoad(v.getOpcode())) {
                    used.add(v.var);
                }
            } else if (insn instanceof IincInsnNode) {
                used.add(((IincInsnNode) insn).var);
            }
        }

        return used;
    }

    private boolean isLoad(int opcode) {
        return opcode == Opcodes.ILOAD ||
                opcode == Opcodes.LLOAD ||
                opcode == Opcodes.FLOAD ||
                opcode == Opcodes.DLOAD ||
                opcode == Opcodes.ALOAD;
    }
}
