package Domain;

import Domain.Interfaces.ICheck;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Linter {
	
	// String[] fieldForAnalysisByThisProgram = new String[1];
    private final List<ICheck> allChecks;
    private List<ICheck> appliedChecks;
    private List<ClassNode> classNodes;
    private List<LintResult> lintResults;

    public Linter(String[] input) throws IOException {
        this.allChecks = new ArrayList<ICheck>();
        this.appliedChecks = new ArrayList<ICheck>();
        this.classNodes = new ArrayList<ClassNode>();

        this.importClassNodes(input);
        this.setupChecks();
    }

    private void setupChecks() {
        // add pattern checks
        this.allChecks.add(new Domain.PatternCheck.DecoratorPatternCheck());
        this.allChecks.add(new Domain.PatternCheck.RedundantInterfaceCheck());
        this.allChecks.add(new Domain.PatternCheck.StrategyPatternCheck());
        this.allChecks.add(new Domain.PatternCheck.ThreeLayerPatternCheck());

        // add principle checks
        this.allChecks.add(new Domain.PrincipleCheck.CodeDuplicationCheck());
        this.allChecks.add(new Domain.PrincipleCheck.EncapsulationCheck());
        this.allChecks.add(new Domain.PrincipleCheck.HollywoodPrincipleCheck());

        // add style checks
        this.allChecks.add(new Domain.StyleCheck.CamelCaseCheck());
        this.allChecks.add(new Domain.StyleCheck.DataTypeCompatibilityCheck());
        this.allChecks.add(new Domain.StyleCheck.GlobalVariableCheck());
        this.allChecks.add(new Domain.StyleCheck.MissingAbstractImplCheck());
//        this.allChecks.add(new Domain.StyleCheck.SpellCheck());
        this.allChecks.add(new Domain.StyleCheck.SwallowedExceptionCheck());
        this.allChecks.add(new Domain.StyleCheck.UnusedParametersCheck());
        this.allChecks.add(new Domain.StyleCheck.UnusedVariablesCheck());
    }

    public void importClassNodes(String[] input) throws IOException {
        for (String className : input) {
            // One way to read in a Java class with ASM:
            // Step 1. ASM's ClassReader does the heavy lifting of parsing the compiled Java class.
            ClassReader reader = new ClassReader(className);

            // Step 2. ClassNode is just a data container for the parsed class
            ClassNode classNode = new ClassNode();

            // Step 3. Tell the Reader to parse the specified class and store its data in our ClassNode.
            // EXPAND_FRAMES means: I want my code to work. (Always pass this flag.)
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);

            // store ClassNodes
            classNodes.add(classNode);
        }
    }

    public void addChecks(List<ICheck> checksToAdd) {
        for (ICheck check : checksToAdd) {
            if (!this.appliedChecks.contains(check)) {
                this.appliedChecks.add(check);
                for (ClassNode classNode : classNodes) {
                    this.lintResults.addAll(check.execute(classNode));
                }
            }
        }
    }

    private void applyInitialChecks() {
        List<LintResult> results = new ArrayList<LintResult>();
        for (ICheck check : allChecks) {
            for (ClassNode classNode : classNodes) {
                results.addAll(check.execute(classNode));
            }
        }
        this.lintResults = results;
    }
}
