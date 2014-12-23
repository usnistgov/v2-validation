package hl7.v2.instance.tree;

public class SimpleNode extends Node {

    private String value;

    public SimpleNode (
            NodeType nodeType,
            String name,
            String usage,
            int min,
            String max,
            String path,
            int line,
            int column,
            String value
    ) {
        this.nodeType = nodeType;
        this.name = name;
        this.usage = usage;
        this.min = min;
        this.max = max;
        this.path = path;
        this.line = line;
        this.column = column;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "\t"+path + ": "+name+" "+usage+" ["+min+".."+max+"] -> " + value + "\n";
    }
}
