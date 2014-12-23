package hl7.v2.instance.tree;

public class Line {

    private int line;
    private String content;

    public Line(int line, String content) {
        this.line = line;
        this.content = content;
    }

    public int getLine() {
        return line;
    }

    public String getContent() {
        return content;
    }
}
