package Domain.StyleCheck;

import Domain.Category;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.io.IOException;

/**
 * A style check that verifies spelling in the code.
 */

public class SpellCheck implements IStyleCheck {
    // Lazily initialize language tool to avoid hard dependency at classload time
    private JLanguageTool langTool = null;

    @Override
    public List<LintResult> execute(ClassNode classNode) {
        List<LintResult> lintResults = new ArrayList<>();

       try {
           lintResults.addAll(checkWord(classNode.name, "class name"));

           if (classNode.fields != null) {
               for (FieldNode field : classNode.fields) {
                   lintResults.addAll(checkWord(field.name, "field name"));
               }
           }
           if (classNode.methods != null) {
               for (MethodNode method : classNode.methods) {
                   lintResults.addAll(checkWord(method.name, "method name"));

                   if (method.instructions != null) {
                       for (AbstractInsnNode insn : method.instructions) {
                           if (insn instanceof FieldInsnNode) {
                               lintResults.addAll(checkWord(((FieldInsnNode) insn).name, insn));
                           }
                       }
                   }

                   if (method.parameters != null) {
                       for (ParameterNode p : method.parameters) {
                           lintResults.addAll(checkWord(p.name, "parameter name in method " + method.name));
                       }
                   }

                   if (method.localVariables != null) {
                       for (LocalVariableNode var : method.localVariables) {
                           lintResults.addAll(checkWord(var.name, "local variable name in method " + method.name));
                       }
                   }
               }
           }

       }   catch (IOException e) {
           throw new RuntimeException(e);
       }
        return lintResults;
    }

    protected List<LintResult> checkWord(String str, AbstractInsnNode insn) throws IOException {
        if (str == null || str.isEmpty()) return Collections.emptyList();
        List<String> indWords = getWords(str);
        List<LintResult> lintResults = new ArrayList<>();
       for(String word : indWords){
           List<RuleMatch> matches = ensureLangTool() ? langTool.check(word) : Collections.emptyList();
           if (!matches.isEmpty()) {
               String message = "✗ '" + word + "' has spelling errors:";
               lintResults.add(new LintResult(getName(), Category.STYLE, "On line " + getLineNumber(insn), message));
           }
       }
        return lintResults;
    }

    protected List<LintResult> checkWord(String str, String type) throws IOException {
        if (str == null || str.isEmpty()) return Collections.emptyList();
        List<String> indWords = getWords(str);
        List<LintResult> lintResults = new ArrayList<>();
       for(String word : indWords){
           List<RuleMatch> matches = ensureLangTool() ? langTool.check(word) : Collections.emptyList();
           if (!matches.isEmpty()) {
               String message = "✗ '" + word + "' has spelling errors:";
               lintResults.add(new LintResult(getName(), Category.STYLE, "Error in " + type, message));
           }
       }
        return lintResults;
    }

    /**
     * Lazily attempts to initialize the LanguageTool instance.
     * Returns true if the tool is available, false otherwise.
     */
    private boolean ensureLangTool() {
        if (langTool != null) return true;
        try {
            langTool = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));
            return true;
        } catch (Throwable t) {
            // LanguageTool not available on classpath; skip spelling checks
            langTool = null;
            return false;
        }
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
        if (str == null || str.isEmpty()) return Collections.emptyList();
        String[] parts = str.split("(?=\\p{Upper})");
    //    for (String part : parts) {
    //        System.out.println(part);
    //    }
        return Arrays.asList(parts);
    }

    @Override
    public String getName() {
        return "Spelling";
    }
}
