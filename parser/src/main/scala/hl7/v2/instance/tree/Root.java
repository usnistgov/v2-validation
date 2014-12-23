package hl7.v2.instance.tree;

import java.util.List;

public class Root extends ComplexNode {

    private List<Line> invalidLines;
    private List<Line> unexpectedLines;

    public Root (
            String name,
            List<Node> children,
            List<Line> invalidLines,
            List<Line> unexpectedLines
    ) {
        super(NodeType.MESSAGE, name, "R", 1, "1", "Message", 1, 1, children);
        this.invalidLines = invalidLines;
        this.unexpectedLines = unexpectedLines;
    }

    public List<Line> getInvalidLines() {
        return invalidLines;
    }

    public List<Line> getUnexpectedLines() {
        return unexpectedLines;
    }
}
