package battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BattleSolver {

    public Node initialNode; 
    private int nodesExpanded;
    private char startingPlayer; 

    public static class State {
        public int[] healthA;
        public int[] damageA;
        public int[] healthB;
        public int[] damageB;
        public char turn; 

        public State(int[] hA, int[] dA, int[] hB, int[] dB, char t) {
            healthA = hA;
            damageA = dA;
            healthB = hB;
            damageB = dB;
            turn = t;
        }

        public State copy() {
            return new State(
                    Arrays.copyOf(healthA, healthA.length),
                    Arrays.copyOf(damageA, damageA.length),
                    Arrays.copyOf(healthB, healthB.length),
                    Arrays.copyOf(damageB, damageB.length),
                    turn
            );
        }

        public int sumHealthA() {
            int s = 0;
            for (int h : healthA) s += h;
            return s;
        }

        public int sumHealthB() {
            int s = 0;
            for (int h : healthB) s += h;
            return s;
        }
    }

    private State parseInitialState(String s) {
        String t = s;
        if (t.endsWith(";")) t = t.substring(0, t.length() - 1);
        String[] parts = t.split(";");
        
        // Handle edge case: missing parts
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid initial state format");
        }
        
        String partA = parts[0];
        String partB = parts[1];
        startingPlayer = parts[2].charAt(0);

        // Parse A - handle empty army
        String[] tokA = (partA == null || partA.isEmpty()) ? new String[0] : partA.split(",");
        int[] hA = new int[tokA.length / 2];
        int[] dA = new int[tokA.length / 2];
        for (int i = 0; i < hA.length; i++) {
            hA[i] = Integer.parseInt(tokA[2 * i]);
            dA[i] = Integer.parseInt(tokA[2 * i + 1]);
        }

        // Parse B - handle empty army
        String[] tokB = (partB == null || partB.isEmpty()) ? new String[0] : partB.split(",");
        int[] hB = new int[tokB.length / 2];
        int[] dB = new int[tokB.length / 2];
        for (int i = 0; i < hB.length; i++) {
            hB[i] = Integer.parseInt(tokB[2 * i]);
            dB[i] = Integer.parseInt(tokB[2 * i + 1]);
        }
        
        return new State(hA, dA, hB, dB, startingPlayer);
    }

    private boolean isTerminal(State st) {
        return st.sumHealthA() == 0 || st.sumHealthB() == 0;
    }

    private int utility(State st) {
        int sumA = st.sumHealthA();
        int sumB = st.sumHealthB();

        if (startingPlayer == 'A') {
            if (sumB == 0) {
                // A wins
                return sumA;
            } else if (sumA == 0) {
                // A loses
                return -sumB;
            }
        } else {
            if (sumA == 0) {
                // B wins
                return sumB;
            } else if (sumB == 0) {
                // B loses
                return -sumA;
            }
        }
        return 0; // Non-terminal state (shouldn't happen in terminal check)
    }

    private static class Action {
        char player;
        int attacker;
        int target;

        Action(char p, int a, int t) {
            player = p;
            attacker = a;
            target = t;
        }

        public String toString() {
            return player + "(" + attacker + "," + target + ")";
        }
    }

    private List<Action> generateActions(State st) {
        List<Action> acts = new ArrayList<>();
        if (st.turn == 'A') {
            // A attacks B
            for (int i = 0; i < st.healthA.length; i++) {
                if (st.healthA[i] <= 0) continue;
                for (int j = 0; j < st.healthB.length; j++) {
                    if (st.healthB[j] <= 0) continue;
                    acts.add(new Action('A', i, j));
                }
            }
        } else {
            // B attacks A
            for (int i = 0; i < st.healthB.length; i++) {
                if (st.healthB[i] <= 0) continue;
                for (int j = 0; j < st.healthA.length; j++) {
                    if (st.healthA[j] <= 0) continue;
                    acts.add(new Action('B', i, j));
                }
            }
        }
        return acts;
    }

    private State applyAction(State st, Action a) {
        State ns = st.copy();
        if (a.player == 'A') {
            int dmg = ns.damageA[a.attacker];
            ns.healthB[a.target] = Math.max(0, ns.healthB[a.target] - dmg);
            ns.turn = 'B';
        } else {
            int dmg = ns.damageB[a.attacker];
            ns.healthA[a.target] = Math.max(0, ns.healthA[a.target] - dmg);
            ns.turn = 'A';
        }
        return ns;
    }

    private boolean isMaxTurn(State st) {
        return st.turn == startingPlayer;
    }

    private int minimaxNoAB(Node node) {
        if (isTerminal(node.state)) {
            node.value = utility(node.state);
            return node.value;
        }

        List<Action> actions = generateActions(node.state);
        nodesExpanded++;

        // Edge case: No valid actions (shouldn't happen if terminal check is correct)
        if (actions.isEmpty()) {
            node.value = utility(node.state);
            return node.value;
        }

        Node bestChild = null;
        
        if (isMaxTurn(node.state)) {
            int best = Integer.MIN_VALUE;
            for (Action a : actions) {
                Node child = new Node(applyAction(node.state, a), node, a.toString(), node.depth + 1);
                int value = minimaxNoAB(child);
                
                // Use > for first improvement (deterministic tie-breaking)
                if (value > best) {
                    best = value;
                    bestChild = child;
                }
            }
            node.value = best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Action a : actions) {
                Node child = new Node(applyAction(node.state, a), node, a.toString(), node.depth + 1);
                int value = minimaxNoAB(child);
                
                // Use < for first improvement (deterministic tie-breaking)
                if (value < best) {
                    best = value;
                    bestChild = child;
                }
            }
            node.value = best;
        }
        
        // Store only the best child
        if (bestChild != null) {
            node.children = new ArrayList<>();
            node.children.add(bestChild);
        }
        
        return node.value;
    }

    private int minimaxAB(Node node, int alpha, int beta) {
        if (isTerminal(node.state)) {
            node.value = utility(node.state);
            return node.value;
        }

        List<Action> actions = generateActions(node.state);
        nodesExpanded++;

        // Edge case: No valid actions
        if (actions.isEmpty()) {
            node.value = utility(node.state);
            return node.value;
        }

        Node bestChild = null;

        if (isMaxTurn(node.state)) {
            int value = Integer.MIN_VALUE;
            for (Action a : actions) {
                Node child = new Node(applyAction(node.state, a), node, a.toString(), node.depth + 1);
                int childValue = minimaxAB(child, alpha, beta);
                
                // Update best child on improvement
                if (childValue > value) {
                    value = childValue;
                    bestChild = child;
                }
                
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break; // Beta cutoff
                }
            }
            node.value = value;
        } else {
            int value = Integer.MAX_VALUE;
            for (Action a : actions) {
                Node child = new Node(applyAction(node.state, a), node, a.toString(), node.depth + 1);
                int childValue = minimaxAB(child, alpha, beta);
                
                // Update best child on improvement
                if (childValue < value) {
                    value = childValue;
                    bestChild = child;
                }
                
                beta = Math.min(beta, value);
                if (alpha >= beta) {
                    break; // Alpha cutoff
                }
            }
            node.value = value;
        }
        
        // Store only the best child
        if (bestChild != null) {
            node.children = new ArrayList<>();
            node.children.add(bestChild);
        }

        return node.value;
    }

    private List<String> reconstructPlan(Node root) {
        List<String> plan = new ArrayList<>();
        Node cur = root;

        while (!isTerminal(cur.state)) {
            if (cur.children == null || cur.children.isEmpty()) {
                break;
            }
            
            Node chosen = cur.children.get(0);
            if (chosen.action != null) {
                plan.add(chosen.action);
            }
            cur = chosen;
        }
        return plan;
    }

    public String solve(String initialStateString, boolean ab, boolean visualize) {
        try {
            nodesExpanded = 0;

            State init = parseInitialState(initialStateString);
            
            // Edge case: Game already over
            if (isTerminal(init)) {
                Node root = new Node(init, null, null, 0);
                root.value = utility(init);
                this.initialNode = root;
                return ";" + root.value + ";0;";
            }
            
            Node root = new Node(init, null, null, 0);
            this.initialNode = root;

            if (ab) {
                minimaxAB(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } else {
                minimaxNoAB(root);
            }

            List<String> plan = reconstructPlan(root);
            int finalScore = root.value;

            String planStr = plan.isEmpty() ? "" : String.join(",", plan);
            return planStr + ";" + finalScore + ";" + nodesExpanded + ";";

        } catch (Exception e) {
            e.printStackTrace();
            return ";;0;";
        }
    }
}