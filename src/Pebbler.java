import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pebbler {
    Graph graph;
    public Pebbler(Graph graph) {
        this.graph = graph;
    }

    public List<PebbleMove> pebbleTime() {
        List<State> topoOrder = graph.getTopologicalOrder();
        List<PebbleMove> moves = new ArrayList<>();
        Set<State> pebbled = new HashSet<>();
        int time = 0;

        for (State state : topoOrder) {
            // Ensure all predecessors are pebbled
            for (State pred : state.getPredecessors()) {
                if (!pebbled.contains(pred)) {
                    moves.add(new PebbleMove(PebbleMove.Action.PLACE, pred, time++));
                    pebbled.add(pred);
                }
            }

            // Place pebble on current state
            moves.add(new PebbleMove(PebbleMove.Action.PLACE, state, time++));
            pebbled.add(state);

            // Remove predecessors if they are no longer needed
            for (State pred : state.getPredecessors()) {
                boolean needed = false;
                for (State succ : pred.getSuccessors()) {
                    if (!pebbled.contains(succ)) {
                        needed = true;
                        break;
                    }
                }
                if (!needed) {
                    moves.add(new PebbleMove(PebbleMove.Action.REMOVE, pred, time++));
                    pebbled.remove(pred);
                }
            }

            // Remove pebble from current state if it's a sink
            if (state.getSuccessors().isEmpty()) {
                moves.add(new PebbleMove(PebbleMove.Action.REMOVE, state, time++));
                pebbled.remove(state);
            }
        }

        return moves;
    }


    public void pebbleSpace() {

    }
}
