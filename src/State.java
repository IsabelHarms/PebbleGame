import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class State {
    int x, y;

    int radius;
    int number;

    int algorithmArrayIndex;

    List<Edge> incomingEdges;
    List<Edge> outgoingEdges;

    Color color;

    boolean currentlyPebbled = false;
    boolean hasBeenPebbled = false;

    public State(int x, int y, int number, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.number = number;
        this.color = Color.white;
        incomingEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
    }

    public int getNumber() {
        return number;
    }


    public int getIndex() {
        return algorithmArrayIndex;
    }

    public void setIndex(int algorithmArrayIndex) {
        this.algorithmArrayIndex = algorithmArrayIndex;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean hasPredecessor() {
        for(Edge edge: incomingEdges) {
            if (edge.startState != this) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSuccessor() {
        for(Edge edge: outgoingEdges) {
            if (edge.endState != this) {
                return true;
            }
        }
        return false;
    }

    public Edge connected(State node) {
        for (Edge edge: outgoingEdges) {
            if (edge.endState == node) {
                return edge;
            }
        }
        return null;
    }

    public void nextNodesRecursive(Set<State> reachedNodes) {
        for (Edge outgoingEdge: outgoingEdges) {
            State node = outgoingEdge.endState;
            if (!reachedNodes.contains(node)) {
                reachedNodes.add(node);
                node.nextNodesRecursive(reachedNodes);
            }
        }
    }

    public void previousNodesRecursive(Set<State> reachedNodes) {
        for (Edge incomingEdge: incomingEdges) {
            State node = incomingEdge.startState;
            if (!reachedNodes.contains(node)) {
                reachedNodes.add(node);
                node.previousNodesRecursive(reachedNodes);
            }
        }
    }
    public void draw(Graphics g) {
        g.setColor(Color.white);
        if(this.hasBeenPebbled) {
            g.setColor(Color.green);
        }
        if(this.currentlyPebbled) {
            g.setColor(Color.blue);
        }
        g.fillOval(x - radius, y - radius, radius*2, radius*2);
        g.setColor(Color.BLACK);

        g.drawOval(x - radius, y - radius, radius*2, radius*2);

        Font font = new Font("Arial", Font.BOLD, 16);
        g.setFont(font);

        FontMetrics fm = g.getFontMetrics();
        int labelWidth = fm.stringWidth(this.getNumber() + ""); //todo
        int labelHeight = fm.getAscent();
        g.drawString(this.getNumber() + "", x - labelWidth / 2, y + labelHeight / 4);
    }

    public boolean contains(int px, int py) {
        return Math.abs(x - px) <= radius && Math.abs(y - py) <= radius;
    }

    public boolean canBePebbled() {
        if (hasPredecessor()) {
            for(Edge incomingEdge : this.incomingEdges) {
                if (!incomingEdge.startState.currentlyPebbled) {
                    return false;
                }
            }
        }
        return true;
    }
}
