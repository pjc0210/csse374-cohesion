import Domain.LintResult;
import Domain.StyleCheck.SpellCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StyleViolationTests {

    @Test
    @DisplayName("Public instance field - should flag")
    public void testPrivateInstanceField() throws IOException {
        SpellCheck sc = new SpellCheck();
        ClassReader reader = new ClassReader("SpellCheckExamples.SpellCheckClass");
        // Step 2. ClassNode is just a data container for the parsed class
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<LintResult> results = sc.execute(classNode);
//        System.out.println(results);
        assertEquals(1, results.size(), "Only one field isn't private or static");
        assertTrue(results.get(0).getMessage().contains("checkName"), "checkName is public");
    }
}
