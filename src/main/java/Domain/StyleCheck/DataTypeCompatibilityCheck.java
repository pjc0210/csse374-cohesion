package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

/**
 * A style check that verifies data type compatibility within the class.
 */

public class DataTypeCompatibilityCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new java.util.ArrayList<>();

        // Check field assignments
        checkFieldAssignments(classNode, lintResults);

        // Check method signatures and return types
        checkMethods(classNode, lintResults);

        return lintResults;
    }

    private void checkFieldAssignments(ClassNode classNode, List<LintResult> lintResults) {
        for (MethodNode method : classNode.methods) {
            if (method.instructions == null) continue;

            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (fieldInsn.getOpcode() == Opcodes.PUTFIELD ||
                            fieldInsn.getOpcode() == Opcodes.PUTSTATIC) {

                        FieldNode field = findField(classNode, fieldInsn.name);
                        if (field != null && !field.desc.equals(fieldInsn.desc)) {
                            lintResults.add(new LintResult(
                                    getName(),
                                    Category.STYLE,
                                    String.valueOf(getLineNumber(insn)),
                                    "Incompatible type assigned to field '" + fieldInsn.name + "'"
                            ));
                        }
                    }
                }
            }
        }
    }

    private void checkMethods(ClassNode classNode, List<LintResult> lintResults) {
        for (MethodNode method : classNode.methods) {
            if (method.instructions == null) continue;

            Type methodType = Type.getMethodType(method.desc);
            Type returnType = methodType.getReturnType();

            for (AbstractInsnNode insn : method.instructions) {
                // Check method calls
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    MethodNode calledMethod = findMethod(classNode, methodInsn.name, methodInsn.desc);

                    if (calledMethod != null && !calledMethod.desc.equals(methodInsn.desc)) {
                        lintResults.add(new LintResult(
                                getName(),
                                Category.STYLE,
                                String.valueOf(getLineNumber(insn)),
                                "Incompatible method signature for '" + methodInsn.name + "'"
                        ));
                    }
                }

                // Check return types
                if (isReturnInstruction(insn)) {
                    if (!isCorrectReturnInstruction(insn.getOpcode(), returnType)) {
                        lintResults.add(new LintResult(
                                getName(),
                                Category.STYLE,
                                String.valueOf(getLineNumber(insn)),
                                "Incompatible return type in method '" + method.name + "'"
                        ));
                    }
                }
            }
        }
    }

    private FieldNode findField(ClassNode classNode, String name) {
        for (FieldNode field : classNode.fields) {
            if (field.name.equals(name)) {
                return field;
            }
        }
        return null;
    }

    private MethodNode findMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(name)) {
                return method;
            }
        }
        return null;
    }

    private boolean isReturnInstruction(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        return opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
                opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
                opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN;
    }

    private boolean isCorrectReturnInstruction(int opcode, Type returnType) {
        int sort = returnType.getSort();

        if (opcode == Opcodes.RETURN) return sort == Type.VOID;
        if (opcode == Opcodes.IRETURN) return sort == Type.INT || sort == Type.BOOLEAN ||
                sort == Type.BYTE || sort == Type.SHORT ||
                sort == Type.CHAR;
        if (opcode == Opcodes.LRETURN) return sort == Type.LONG;
        if (opcode == Opcodes.FRETURN) return sort == Type.FLOAT;
        if (opcode == Opcodes.DRETURN) return sort == Type.DOUBLE;
        if (opcode == Opcodes.ARETURN) return sort == Type.OBJECT || sort == Type.ARRAY;

        return false;
    }

    private int getLineNumber(AbstractInsnNode insn) {
        while (insn != null) {
            if (insn instanceof LineNumberNode) {
                return ((LineNumberNode) insn).line;
            }
            insn = insn.getPrevious();
        }
        return -1;
    }

    @Override
    public String getName() {
        return "DataTypeCompatibility";
    }
}