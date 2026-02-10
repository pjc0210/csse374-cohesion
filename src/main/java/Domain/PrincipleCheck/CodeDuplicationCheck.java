package Domain.PrincipleCheck;

import Domain.Category;
import Domain.Interfaces.IPrincipleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class checks for code duplication within a class.
 */
public class CodeDuplicationCheck implements IPrincipleCheck {

    private static final double SIMILARITY_THRESHOLD = 0.8; // 80% similarity

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();
        List<MethodNode> methods = classNode.methods;

        // Compare each pair of methods
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                MethodNode method1 = methods.get(i);
                MethodNode method2 = methods.get(j);

                // Skip constructors and small methods
                if (isConstructor(method1) || isConstructor(method2)) continue;
                if (isMethodTooSmall(method1) || isMethodTooSmall(method2)) continue;

                double similarity = calculateSimilarity(method1, method2);

                if (similarity > SIMILARITY_THRESHOLD) {
                    lintResults.add(new LintResult(
                            getName(),
                            Category.PRINCIPLE,
                            String.valueOf(getLineNumber(method1)),
                            "Methods '" + method1.name + "' and '" + method2.name +
                                    "' have high code duplication (" +
                                    String.format("%.0f", similarity * 100) + "% similar)"
                    ));
                }
            }
        }

        return lintResults;
    }

    private double calculateSimilarity(MethodNode method1, MethodNode method2) {
        if (method1.instructions == null || method2.instructions == null) {
            return 0.0;
        }

        List<Integer> opcodes1 = getOpcodeSequence(method1);
        List<Integer> opcodes2 = getOpcodeSequence(method2);

        if (opcodes1.isEmpty() || opcodes2.isEmpty()) {
            return 0.0;
        }

        // Calculate similarity using Longest Common Subsequence (LCS)
        int lcsLength = longestCommonSubsequence(opcodes1, opcodes2);
        int maxLength = Math.max(opcodes1.size(), opcodes2.size());

        return (double) lcsLength / maxLength;
    }

    private List<Integer> getOpcodeSequence(MethodNode method) {
        List<Integer> opcodes = new ArrayList<>();

        for (AbstractInsnNode insn : method.instructions) {
            int opcode = insn.getOpcode();
            if (opcode != -1) { // Skip pseudo-instructions (labels, line numbers, etc.)
                opcodes.add(opcode);
            }
        }

        return opcodes;
    }

    private int longestCommonSubsequence(List<Integer> seq1, List<Integer> seq2) {
        int m = seq1.size();
        int n = seq2.size();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (seq1.get(i - 1).equals(seq2.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        return dp[m][n];
    }

    private boolean isConstructor(MethodNode method) {
        return method.name.equals("<init>") || method.name.equals("<clinit>");
    }

    private boolean isMethodTooSmall(MethodNode method) {
        if (method.instructions == null) return true;

        int instructionCount = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() != -1) {
                instructionCount++;
            }
        }

        return instructionCount < 5; // Skip methods with fewer than 5 instructions
    }

    private int getLineNumber(MethodNode method) {
        if (method.instructions == null) return -1;

        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof org.objectweb.asm.tree.LineNumberNode) {
                return ((org.objectweb.asm.tree.LineNumberNode) insn).line;
            }
        }

        return -1;
    }

    @Override
    public String getName() {
        return "CodeDuplication";
    }
}