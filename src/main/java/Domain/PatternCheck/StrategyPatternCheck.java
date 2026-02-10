package Domain.PatternCheck;

import Domain.Category;
import Domain.Interfaces.IPatternCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class checks for the Strategy Pattern in a given class node.
 * Detects when a class uses conditional logic to select algorithms
 * and would benefit from extracting these into separate strategy classes.
 */
public class StrategyPatternCheck implements IPatternCheck {

    private static final int MIN_CONDITIONAL_BRANCHES = 3; // At least 3 branches suggest strategy pattern

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();

        // Check if class already uses Strategy Pattern
        if (usesStrategyPattern(classNode)) {
            return lintResults;
        }

        // Find methods with multiple conditional branches that select algorithms
        for (MethodNode method : classNode.methods) {
            if (isConstructor(method) || method.instructions == null) continue;

            int conditionalBranches = countConditionalBranches(method);

            if (conditionalBranches >= MIN_CONDITIONAL_BRANCHES) {
                lintResults.add(new LintResult(
                        getName(),
                        Category.PATTERN,
                        String.valueOf(getLineNumber(method)),
                        "Method '" + method.name + "' in class '" + getSimpleClassName(classNode.name) +
                                "' has multiple conditional branches (" + conditionalBranches +
                                "). Consider using Strategy Pattern to encapsulate varying behaviors."
                ));
            }
        }

        return lintResults;
    }

    private boolean usesStrategyPattern(ClassNode classNode) {
        // Check if class has fields that are interfaces (strategy references)
        for (FieldNode field : classNode.fields) {
            String descriptor = field.desc;
            // Strategy pattern typically uses interface/abstract class fields
            if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                String fieldType = descriptor.substring(1, descriptor.length() - 1);
                // Common strategy pattern naming conventions
                if (fieldType.contains("Strategy") || fieldType.contains("Behavior") ||
                        fieldType.contains("Policy") || fieldType.contains("Algorithm")) {
                    return true;
                }
            }
        }

        // Check if class implements Strategy-like interfaces
        if (classNode.interfaces != null) {
            for (String iface : classNode.interfaces) {
                if (iface.contains("Strategy") || iface.contains("Behavior") ||
                        iface.contains("Policy")) {
                    return true;
                }
            }
        }

        return false;
    }

    private int countConditionalBranches(MethodNode method) {
        int branches = 0;
        AbstractInsnNode prev = null;

        for (AbstractInsnNode insn : method.instructions) {
            int opcode = insn.getOpcode();

            // Count switch statements (TABLESWITCH, LOOKUPSWITCH)
            if (opcode == Opcodes.TABLESWITCH) {
                TableSwitchInsnNode switchInsn = (TableSwitchInsnNode) insn;
                branches += switchInsn.labels.size();
            } else if (opcode == Opcodes.LOOKUPSWITCH) {
                LookupSwitchInsnNode switchInsn = (LookupSwitchInsnNode) insn;
                branches += switchInsn.labels.size();
            }
            // Count if-else chains (IF instructions)
            else if (isConditionalJump(opcode)) {
                branches++;
            }

            prev = insn;
        }

        return branches;
    }

    private boolean isConditionalJump(int opcode) {
        return opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE ||
                opcode == Opcodes.IFLT || opcode == Opcodes.IFGE ||
                opcode == Opcodes.IFGT || opcode == Opcodes.IFLE ||
                opcode == Opcodes.IF_ICMPEQ || opcode == Opcodes.IF_ICMPNE ||
                opcode == Opcodes.IF_ICMPLT || opcode == Opcodes.IF_ICMPGE ||
                opcode == Opcodes.IF_ICMPGT || opcode == Opcodes.IF_ICMPLE ||
                opcode == Opcodes.IF_ACMPEQ || opcode == Opcodes.IF_ACMPNE ||
                opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL;
    }

    private boolean isConstructor(MethodNode method) {
        return method.name.equals("<init>") || method.name.equals("<clinit>");
    }

    private int getLineNumber(MethodNode method) {
        if (method.instructions == null) return -1;

        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof LineNumberNode) {
                return ((LineNumberNode) insn).line;
            }
        }

        return -1;
    }

    private String getSimpleClassName(String internalName) {
        int lastSlash = internalName.lastIndexOf('/');
        if (lastSlash >= 0) {
            return internalName.substring(lastSlash + 1);
        }
        return internalName;
    }

    @Override
    public String getName() {
        return "StrategyPattern";
    }
}