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
        String partA = parts[0];
        String partB = parts[1];
        startingPlayer = parts[2].charAt(0);

        
        String[] tokA = partA.isEmpty() ? new String[0] : partA.split(",");
        int[] hA = new int[tokA.length / 2];
        int[] dA = new int[tokA.length / 2];
        for (int i = 0; i < hA.length; i++) {
            hA[i] = Integer.parseInt(tokA[2 * i]);
            dA[i] = Integer.parseInt(tokA[2 * i + 1]);
        }

        
        String[] tokB = partB.isEmpty() ? new String[0] : partB.split(",");
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
            if (sumB == 0) return sumA;      // A wins
            else if (sumA == 0) return -sumB; // A loses
        } else {
            if (sumA == 0) return sumB;       // B wins
            else if (sumB == 0) return -sumA; // B loses
        }
        return 0;
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
            for (int i = 0; i < st.healthA.length; i++) {
                if (st.healthA[i] <= 0) continue;
                for (int j = 0; j < st.healthB.length; j++) {
                    if (st.healthB[j] <= 0) continue;
                    acts.add(new Action('A', i, j));
                }
            }
        } else {
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

        // don't store all children only track the best one
        Node bestChild = null;
        
        if (isMaxTurn(node.state)) {
            int best = Integer.MIN_VALUE;
            for (Action a : actions) {
                Node child = new Node(applyAction(node.state, a), node, a.toString(), node.depth + 1);
                int value = minimaxNoAB(child);
                
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

        // don't store all children only track the best one
        Node bestChild = null;

        if (isMaxTurn(node.state)) {
            int value = Integer.MIN_VALUE;
            for (Action a : actions) {
                Node child = new Node(applyAction(node.state, a), node, a.toString(), node.depth + 1);
                int childValue = minimaxAB(child, alpha, beta);
                
                if (childValue > value) {
                    value = childValue;
                    bestChild = child; 
                }
                
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }
            node.value = value;
        } else {
            int value = Integer.MAX_VALUE;
            for (Action a : actions) {
                Node child = new Node(applyAction(node.state, a), node, a.toString(), node.depth + 1);
                int childValue = minimaxAB(child, alpha, beta);
                
                if (childValue < value) {
                    value = childValue;
                    bestChild = child; 
                }
                
                beta = Math.min(beta, value);
                if (alpha >= beta) break;
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
            if (cur.children == null || cur.children.isEmpty()) break;
            
            // We stored only the best child, so take it
            Node chosen = cur.children.get(0);
            plan.add(chosen.action);
            cur = chosen;
        }
        return plan;
    }

    
    public String solve(String initialStateString, boolean ab, boolean visualize) {
        try {
            nodesExpanded = 0;

            State init = parseInitialState(initialStateString);
            Node root = new Node(init, null, null, 0);
            this.initialNode = root;

            if (ab) {
                minimaxAB(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } else {
                minimaxNoAB(root);
            }

            List<String> plan = reconstructPlan(root);

            int finalScore = root.value; 

            String planStr = String.join(",", plan);
            return planStr + ";" + finalScore + ";" + nodesExpanded + ";";

        } catch (Exception e) {
            e.printStackTrace();
            return ";;0;";
        }
    }

    
}