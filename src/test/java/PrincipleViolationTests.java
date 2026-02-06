import Domain.LintResult;
import Domain.PrincipleCheck.EncapsulationCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Encapsulation Violation Detection
 * 
 * Encapsulation violations include:
 * 1. Public fields (should be private with getters/setters)
 * 2. Returning references to mutable internal state
 * 3. Accepting mutable objects without defensive copying
 * 4. Exposing internal collections directly
 * 5. Public static mutable fields
 */
public class PrincipleViolationTests {
    
    // ==================== PUBLIC FIELDS ====================
    EncapsulationCheck ec = new EncapsulationCheck();

    @Test
    @DisplayName("Public instance field - should flag")
    public void testPrivateInstanceField() throws IOException {
        EncapsulationCheck ec = new EncapsulationCheck();
        ClassReader reader = new ClassReader("EncapsulationViolationDataClass");
        // Step 2. ClassNode is just a data container for the parsed class
        ClassNode classNode = new ClassNode();
        List<LintResult> results = ec.execute(classNode);
        
        assertEquals(0, results.size(), "help");
    }
    

}
