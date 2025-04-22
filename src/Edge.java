import java.util.HashSet;
import java.util.Set;

class Edge {
    State startState, endState;

    public Edge(State startState, State endState) {
        this.startState = startState;
        this.endState = endState;
    }
}
