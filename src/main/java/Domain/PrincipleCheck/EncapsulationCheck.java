package Domain.PrincipleCheck;

import Domain.Interfaces.IPrincipleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class EncapsulationCheck implements IPrincipleCheck {

    public EncapsulationCheck(){}

    @Override
    public List<LintResult> execute(ClassNode classNode){
        List<FieldNode> fields = classNode.fields;

        for(FieldNode field : fields){
            if(field.access != Opcodes.ACC_STATIC && field.access != Opcodes.ACC_PRIVATE){
                return ;
            }
        }
        return true;
    }

    @Override
    public String getName(){
        return "EncapsulationCheck";
    }
}
