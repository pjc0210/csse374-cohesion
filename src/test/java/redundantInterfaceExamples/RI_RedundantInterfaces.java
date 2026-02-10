package redundantInterfaceExamples;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

public class RI_RedundantInterfaces extends ArrayList<String> implements List<String>, RandomAccess {
    public void x() {}
}
