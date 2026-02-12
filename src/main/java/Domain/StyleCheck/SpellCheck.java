package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.objectweb.asm.tree.*;

import java.util.*;



import java.io.IOException;
import java.util.List;

/**
 * A style check that verifies spelling in the code.
 */

public class SpellCheck implements IStyleCheck {
    JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();
//
//        try {
//            lintResults.addAll(checkWord(classNode.name, "class name"));
//
//            for (FieldNode field : classNode.fields) {
//                lintResults.addAll(checkWord(field.name, "field name"));
//            }
//            for (MethodNode method : classNode.methods) {
//                lintResults.addAll(checkWord(method.name, "method name"));
//                for (AbstractInsnNode insn : method.instructions) {
//                    if (insn instanceof FieldInsnNode) {
//                        lintResults.addAll(checkWord(((FieldInsnNode) insn).name, insn));;
//                    }
//                }
//                for (ParameterNode p : method.parameters) {
//                    lintResults.addAll(checkWord(p.name, "parameter name in method" + method.name));
//                }
//                for(LocalVariableNode var: method.localVariables){
//                    lintResults.addAll(checkWord(var.name, "local variable name in method" + method.name));
//                }
//            }
//
//        }   catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return lintResults;
    }

    protected List<LintResult> checkWord(String str, AbstractInsnNode insn) throws IOException {
        List<String> indWords = getWords(str);
        List<LintResult> lintResults = new ArrayList<>();
//        for(String word : indWords){
//            List<RuleMatch> matches = langTool.check(word);
//            if (!matches.isEmpty()) {
//                String message = "✗ '" + word + "' has spelling errors:";
//                lintResults.add(new LintResult(getName(), Category.STYLE, "On line " + getLineNumber(insn), message));
//            }
//        }
        return lintResults;
    }

    protected List<LintResult> checkWord(String str, String type) throws IOException {
        List<String> indWords = getWords(str);
        List<LintResult> lintResults = new ArrayList<>();
//        for(String word : indWords){
//            List<RuleMatch> matches = langTool.check(word);
//            if (!matches.isEmpty()) {
//                String message = "✗ '" + word + "' has spelling errors:";
//                lintResults.add(new LintResult(getName(), Category.STYLE, "Error in " + type, message));
//            }
//        }
        return lintResults;
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
    protected List<String> getWords(String str) {
        String[] parts = str.split("(?=\\p{Upper})");
//        for (String part : parts) {
//            System.out.println(part);
//        }
        return Arrays.asList(parts);
    }

    @Override
    public String getName() {
        return "Spelling";
    }
}
