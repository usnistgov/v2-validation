package hl7.v2.instance.tree;

public abstract class Node {

    protected NodeType nodeType;
    protected String name;
    protected String usage;
    protected int min;
    protected String max;
    protected String path;
    protected int line;
    protected int column;

    public NodeType getNodeType() {
        return nodeType;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public int getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public String getPath() {
        return path;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}
