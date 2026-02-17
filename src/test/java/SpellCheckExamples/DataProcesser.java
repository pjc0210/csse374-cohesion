package SpellCheckExamples;

/**
 * Example class with multiple spelling errors.
 * Misspelled class name and methods/fields.
 */
public class DataProcesser {  // "Processer" instead of "Processor"
    
    private String inputDatta;  // "Datta" instead of "Data"
    private String ouputPath;   // "ouput" instead of "output"
    
    public void procesInformation() {  // "proces" instead of "process"
        System.out.println("Processing");
    }
    
    public String getInputData() {
        return inputDatta;
    }
    
    public String getOutputPath() {
        return ouputPath;
    }
}
