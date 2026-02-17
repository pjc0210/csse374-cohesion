package SpellCheckExamples;

/**
 * Example class with misspelled field names.
 * Should trigger spelling errors.
 */
public class BadSpellingInFieldName {
    // Misspellings: "fiels" instead of "field", "descritpion" instead of "description"
    private String fileds;
    private String descritpion;
    private int cunt;
    
    public BadSpellingInFieldName(String fileds, String descritpion, int cunt) {
        this.fileds = fileds;
        this.descritpion = descritpion;
        this.cunt = cunt;
    }
    
    public String getFileds() {
        return fileds;
    }
    
    public void setFileds(String fileds) {
        this.fileds = fileds;
    }
}
