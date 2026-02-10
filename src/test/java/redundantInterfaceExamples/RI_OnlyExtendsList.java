package redundantInterfaceExamples;

import java.util.ArrayList;
import java.util.List;

public class RI_OnlyExtendsList extends ArrayList<String> implements List<String> {
    public void x() {}
}
