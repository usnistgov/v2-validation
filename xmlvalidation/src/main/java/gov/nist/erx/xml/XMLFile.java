package gov.nist.erx.xml;

/**
 * Created by mcl1 on 9/21/15.
 */
public class XMLFile {

    private String path;
    private String content;

    public XMLFile(String path, String content) {
        this.path = path;
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }
}
