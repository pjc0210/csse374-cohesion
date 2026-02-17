package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A style check that identifies swallowed exceptions in the class.
 */

public class SwallowedExceptionCheck implements IStyleCheck {

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> results = new ArrayList<>();
        
        if (classNode.methods == null) {
            return results;
        }

        for (MethodNode method : classNode.methods) {
            results.addAll(checkMethodForSwallowedExceptions(classNode.name, method));
        }
        
        return results;
    }
    
    /**
     * Checks a single method for swallowed exceptions.
     * Returns a list of LintResults for each swallowed exception found.
     */
    private List<LintResult> checkMethodForSwallowedExceptions(String className, MethodNode method) {
        List<LintResult> results = new ArrayList<>();
        
        if (method.tryCatchBlocks == null || method.tryCatchBlocks.isEmpty()) {
            return results;
        }
        
        if (method.instructions == null || method.instructions.size() == 0) {
            return results;
        }
        
        // Check each try-catch block
        for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
            // Get the start and end instructions of the catch handler
            int startIndex = method.instructions.indexOf(tcb.handler);
            if (startIndex == -1) {
                continue;
            }
            
            // Find where the catch block ends (next label or end of method)
            int endIndex = method.instructions.size();
            
            // Count meaningful instructions in the catch block
            int meaningfulInstructions = countMeaningfulInstructions(method.instructions, startIndex, endIndex);
            System.out.println(meaningfulInstructions);
            // If catch block has no meaningful instructions, it's swallowing the exception
            if (meaningfulInstructions == 0) {
                String line= String.valueOf(startIndex);
                String message = "Caught exception type '" + tcb.type + "' is swallowed without handling";
                LintResult result = new LintResult(
                    getName(),
                    Category.STYLE,
                    className + "." + method.name + " at line " + line,
                    message
                );
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * Counts meaningful instructions (excludes labels, line numbers, frame info, NOPs, and comments).
     */
    private int countMeaningfulInstructions(InsnList instructions, int start, int end) {
        int count = 0;
        for (int i = start; i < end && i < instructions.size(); i++) {
            AbstractInsnNode insn = instructions.get(i);
            // Skip labels, line numbers, frame info, NOPs, and comments
            if (!(insn instanceof LabelNode) && 
                !(insn instanceof LineNumberNode) && 
                !(insn instanceof FrameNode) &&
                !isNopOrComment(insn)) {
                count++;
            }
        }
        return (count - 2); //need to remove instantiation of exception from catch
    }

    /**
     * Checks if an instruction is a NOP or comment instruction.
     */
    private boolean isNopOrComment(AbstractInsnNode insn) {
        // Check for NOP instruction
        if (insn instanceof InsnNode) {
            InsnNode insnNode = (InsnNode) insn;
            if (insnNode.getOpcode() == Opcodes.NOP || insnNode.getOpcode() == -1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extracts line number from an instruction or returns -1 if not found.
     */
    private int getLineNumber(AbstractInsnNode insn) {
        // Search backward for LineNumberNode
        AbstractInsnNode current = insn;
        while (current != null) {
            if (current instanceof LineNumberNode) {
                return ((LineNumberNode) current).line;
            }
            current = current.getPrevious();
        }
        return -1;
    }

    @Override
    public String getName() {
        return "SwallowedExceptions";
    }
}