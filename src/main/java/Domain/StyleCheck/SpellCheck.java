package Domain.StyleCheck;

import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

import com.jspell.domain.*;
import com.jspell.gui.*;

import javax.swing.*;

/**
 * A style check that verifies spelling in the code.
 */

public class SpellCheck implements IStyleCheck {
    JSpellDictionaryLocal jdLocal;
    JSpellParser parser;
    JSpellChecker dialog;
    JSpellDictionaryManager manager;
    JTextArea text = new JTextArea();

    @Override
    public List<LintResult> execute(ClassNode classnode) {
        List<LintResult> lintResults = new ArrayList<>();

        manager = JSpellDictionaryManager.getJSpellDictionaryManager();
        manager.setDictionaryDirectory("./lexicons/");
        parser = new JSpellParser(manager.getJSpellDictionaryLocal("English (US)"));
        dialog = new JSpellChecker();
        dialog.setParser(parser);
        dialog.addComponent(text);
        dialog.check();

        return lintResults;
    }

    @Override
    public String getName() {
        return "Spelling";
    }
}
