package Domain.PrincipleCheck;

import Domain.Category;
import Domain.Interfaces.IPrincipleCheck;
import Domain.LintResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.List;

public class EncapsulationCheck implements IPrincipleCheck {

    public EncapsulationCheck(){}

    @Override
    public List<LintResult> execute(ClassNode classNode){

        List<LintResult> lintResults = new ArrayList<>();

        List<FieldNode> fields = classNode.fields;

        for(FieldNode field : fields){
            boolean isPrivate = (field.access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
            boolean isStatic = (field.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
            if(!isPrivate && !isStatic){
//                System.out.println(field.name + " " + field.access);
                ;lintResults.add(new LintResult(getName(), Category.PRINCIPLE, classNode.name, "field " + field.name + " should be private or statix."));
            }
        }
        return lintResults;
    }

    @Override
    public String getName(){
        return "EncapsulationCheck";
    }
}
