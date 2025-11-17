package battle;

import java.util.ArrayList;
import java.util.List;

public class Node {
    // state for this node (populated/used by BattleSolver)
    public BattleSolver.State state;
    // parent in the search tree (null for root)
    public Node parent;
    // action that produced this node from parent, e.g. "A(0,1)" or "B(1,0)"
    public String action;
    // children of this node (filled when expanded)
    public List<Node> children = new ArrayList<>();
    // value used by minimax
    public int value;
    // depth (optional)
    public int depth;
    
    public Node() {
    }

    public Node(BattleSolver.State state, Node parent, String action, int depth) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.depth = depth;
    }

    // returns the value of this node.
    public int getValue() {
        return value;
    }
}