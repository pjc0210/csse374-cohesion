package Domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EncapsulationCheck implements IPrincipleCheck{

    public EncapsulationCheck(){}

    @Override
    public boolean execute(ClassNode classNode){
        List<FieldNode> fields = classNode.fields;

        for(FieldNode field : fields){
            if(field.access != Opcodes.ACC_STATIC && field.access != Opcodes.ACC_PRIVATE){
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName(){
        return "EncapsulationCheck";
    }
}
