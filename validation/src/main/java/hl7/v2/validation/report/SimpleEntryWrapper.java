package hl7.v2.validation.report;

public class SimpleEntryWrapper {
    private String category;
    private String classification;
    private String template;

    public SimpleEntryWrapper(String category, String classification, String template) {
        this.category = category;
        this.classification = classification;
        this.template = template;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
