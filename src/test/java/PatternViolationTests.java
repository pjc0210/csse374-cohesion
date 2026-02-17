import Domain.LintResult;
import Domain.PatternCheck.RedundantInterfaceCheck;
import Domain.PatternCheck.ThreeLayerPatternCheck;
import Domain.PrincipleCheck.EncapsulationCheck;
import Domain.PatternCheck.DecoratorPatternCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



public class PatternViolationTests {

    @Test
    @DisplayName("[Redundant Interface] No interfaces - should not flag")
    public void testNoInterfaces() throws IOException {
        RedundantInterfaceCheck ric = new RedundantInterfaceCheck();
        ClassReader reader = new ClassReader("redundantInterfaceExamples.RI_NoInterfaces");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = ric.execute(classNode);
        assertEquals(0, results.size());
    }

    @Test
    @DisplayName("[Redundant Interface] Single interface - should not flag")
    public void testSingleInterface() throws IOException {
        RedundantInterfaceCheck ric = new RedundantInterfaceCheck();
        ClassReader reader = new ClassReader("redundantInterfaceExamples.RI_JustOneInterface");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = ric.execute(classNode);
        assertEquals(0, results.size());
    }

    @Test
    @DisplayName("[Redundant Interface] Extends ArrayList and also implements List - should flag")
    public void testRedundantList() throws IOException {
        RedundantInterfaceCheck ric = new RedundantInterfaceCheck();
        ClassReader reader = new ClassReader("redundantInterfaceExamples.RI_OnlyExtendsList");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = ric.execute(classNode);
        assertTrue(results.size() >= 1, "Should flag at least one redundant interface");

        String msg = results.get(0).getMessage();
        assertTrue(msg.toLowerCase().contains("redundant") || msg.toLowerCase().contains("interface"));
    }

    @Test
    @DisplayName("[Redundant Interface] Multiple redundant interfaces - should flag")
    public void testMultipleRedundantInterfaces() throws IOException {
        RedundantInterfaceCheck ric = new RedundantInterfaceCheck();
        ClassReader reader = new ClassReader("redundantInterfaceExamples.RI_RedundantInterfaces");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = ric.execute(classNode);
        assertTrue(results.size() >= 1, "Should flag redundant interfaces");

        boolean mentionsList = results.stream()
                .anyMatch(r -> r.getMessage().contains("List") || r.getMessage().toLowerCase().contains("list"));
        assertTrue(mentionsList, "At least one result should mention List being redundant (common case)");
    }

    @Test
    @DisplayName("Good decorator - should not flag")
    public void testGoodDecorator() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.GoodDecorator");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Good decorator with proper delegation should not be flagged");
    }

    @Test
    @DisplayName("Bad decorator with unused field - should flag")
    public void testBadDecoratorUnusedField() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.BadDecoratorUnusedField");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        assertTrue(results.size() > 0,
                "Should flag decorator with unused component field");
        assertTrue(allResults.contains("never used") || allResults.contains("component"),
                "Should mention the unused field");
    }

    @Test
    @DisplayName("Bad decorator without delegation - should flag")
    public void testBadDecoratorNoDelegation() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.BadDecoratorNoDelegation");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        assertTrue(results.size() > 0,
                "Should flag decorator that doesn't delegate");
        assertTrue(allResults.contains("delegate") || allResults.contains("component"),
                "Should mention delegation issue");
    }

    @Test
    @DisplayName("Bad decorator with unused constructor parameter - should flag")
    public void testBadDecoratorUnusedConstructorParam() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.BadDecoratorUnusedConstructorParam");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        String allResults = results.toString();

        assertTrue(!results.isEmpty(),
                "Should flag decorator with unused constructor parameter");
        assertTrue(allResults.contains("parameter") || allResults.contains("Constructor"),
                "Should mention constructor parameter issue");
    }

    @Test
    @DisplayName("Not a decorator - should not flag")
    public void testNotADecorator() throws IOException {
        DecoratorPatternCheck checker = new DecoratorPatternCheck();
        ClassReader reader = new ClassReader("decoratorExamples.NotADecorator");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);

        assertEquals(0, results.size(),
                "Regular class should not be identified as decorator");
    }

    @Test
    @DisplayName("Not a decorator - should not flag")
    public void testpath() throws IOException {
        ThreeLayerPatternCheck checker = new ThreeLayerPatternCheck();
        ClassReader reader = new ClassReader("SpellCheckExamples.NoErrorsClass");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        // placeholder removed; replaced by focused three-layer tests below
    }

    @Test
    @DisplayName("[ThreeLayer] Presentation -> Data should flag")
    public void testPresentationToData() throws IOException {
        ThreeLayerPatternCheck checker = new ThreeLayerPatternCheck();
        ClassReader reader = new ClassReader("threeelayerpatterntexamples.presentation.PresentationController");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.size() >= 1, "Presentation referencing Data should be flagged");
        boolean mentions = results.stream().anyMatch(r -> r.getMessage().toLowerCase().contains("presentation") || r.getMessage().toLowerCase().contains("data"));
        assertTrue(mentions, "Result should mention presentation/data relationship");
    }

    @Test
    @DisplayName("[ThreeLayer] Data -> Presentation should flag")
    public void testDataToPresentation() throws IOException {
        ThreeLayerPatternCheck checker = new ThreeLayerPatternCheck();
        ClassReader reader = new ClassReader("threeelayerpatterntexamples.persistence.RepoWithPresentationRef");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.size() >= 1, "Data referencing Presentation should be flagged");
        boolean mentions = results.stream().anyMatch(r -> r.getMessage().toLowerCase().contains("data") || r.getMessage().toLowerCase().contains("presentation"));
        assertTrue(mentions, "Result should mention data/presentation relationship");
    }

    @Test
    @DisplayName("[ThreeLayer] Domain -> Presentation should flag")
    public void testDomainToPresentation() throws IOException {
        ThreeLayerPatternCheck checker = new ThreeLayerPatternCheck();
        ClassReader reader = new ClassReader("threeelayerpatterntexamples.domain.DomainService");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertTrue(results.size() >= 1, "Domain referencing Presentation should be flagged");
        boolean mentions = results.stream().anyMatch(r -> r.getMessage().toLowerCase().contains("domain") || r.getMessage().toLowerCase().contains("presentation"));
        assertTrue(mentions, "Result should mention domain/presentation relationship");
    }

    @Test
    @DisplayName("[ThreeLayer] Presentation -> Domain should NOT flag")
    public void testPresentationToDomainNoFlag() throws IOException {
        ThreeLayerPatternCheck checker = new ThreeLayerPatternCheck();
        ClassReader reader = new ClassReader("threeelayerpatterntexamples.presentation.GoodController");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = checker.execute(classNode);
        assertEquals(0, results.size(), "Presentation referencing Domain should NOT be flagged");
    }
}

