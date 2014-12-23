package hl7.v2.instance.tree;

import java.util.List;

public class ComplexNode extends Node {

    private List<Node> children;

    public ComplexNode (
            NodeType nodeType,
            String name,
            String usage,
            int min,
            String max,
            String path,
            int line,
            int column,
            List<Node> children
    ) {
        this.nodeType = nodeType;
        this.name = name;
        this.usage = usage;
        this.min = min;
        this.max = max;
        this.path = path;
        this.line = line;
        this.column = column;
        this.children = children;
    }

    public List<Node> getChildren() {
        return children;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( path + ": "+name+" "+usage+" ["+min+".."+max+"]\n" );
        for(Node c : getChildren())
            sb.append( c.toString() );
        return sb.toString();
    }
}
