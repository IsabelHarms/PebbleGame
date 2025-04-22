import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Graph {
    static final int NODE_RADIUS = 30;
    private Set<State> states;
    private Set<Edge> edges;

    public boolean isValid;

    int currentNodeNumber = 0;

    public Graph() {
        states = new HashSet<>();
        edges = new HashSet<>();
        isValid = true;
    }

    public Set<State> getStates() {
        return this.states;
    }
    public void addState(State state) {
        this.states.add(state);
    }

    public void removeNode(State state) {
        this.states.remove(state);
    }

    public Set<Edge> getEdges() {
        return this.edges;
    }
    public void addEdge(Edge edge) {
        edge.startState.outgoingEdges.add(edge);
        edge.endState.incomingEdges.add(edge);
        this.edges.add(edge);
    }
    public void removeEdge(Edge edge) {
        edge.startState.outgoingEdges.remove(edge);
        edge.endState.incomingEdges.remove(edge);
        this.edges.remove(edge);
    }

    public void initializeStateIndices() {
        int i = 0;
        for (State state : getStates()) {
            state.setIndex(i);
            i++;
        }
    }

    public String validate() {
        isValid = false;
        if (states.size()==0) {
            isValid = true;
            return "start building or import your graph.";
        }
        if (states.size()==1) {
            return "valid";
        }

        for (State state: states) {
            if (!state.hasPredecessor() && !state.hasSuccessor()) {
                return "state " + state.getNumber() + " is not connected "; //not connected
            }
        }
        isValid = true;
        return "flawless";
    }

    public String getGraphState() {
        StringBuilder sb = new StringBuilder();
        sb.append(isValid? "valid":"invalid").append("\n");
        sb.append(this.validate()).append("\n");

        sb.append("States: \n");
        for (State node : states) {
            sb.append("  ").append(node.getNumber()).append(" at (").append(node.x).append(", ").append(node.y).append(")\n");
        }

        sb.append("\nEdges: \n");
        for (Edge edge : edges) {
            sb.append("  ").append(edge.startState.getNumber()).append(" -> ").append(edge.endState.getNumber())
                    .append("\n");
        }
        return sb.toString();
    }

    public boolean fullyPebbled() {
        for (State state : this.states) {
            if(!state.hasBeenPebbled) {
                return false;
            }
        }
        return true;
    }

    public List<State> getTopologicalOrder() {
        Map<State, Integer> inDegree = new HashMap<>();
        for (State state : states) {
            inDegree.put(state, 0);
        }

        for (State state : states) {
            for (State succ : state.getSuccessors()) {
                inDegree.put(succ, inDegree.get(succ) + 1);
            }
        }

        Queue<State> queue = new LinkedList<>();
        for (State state : states) {
            if (inDegree.get(state) == 0) {
                queue.add(state);
            }
        }

        List<State> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            State current = queue.poll();
            topoOrder.add(current);

            for (State successor : current.getSuccessors()) {
                inDegree.put(successor, inDegree.get(successor) - 1);
                if (inDegree.get(successor) == 0) {
                    queue.add(successor);
                }
            }
        }

        // Check for cycles
        if (topoOrder.size() != states.size()) {
            throw new IllegalStateException("Graph has at least one cycle.");
        }

        return topoOrder;
    }

    public void exportGraph(File filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Knoten schreiben
            for (State state : states) {
                writer.write("NODE," + state.number + "," + state.x + "," + state.y);
                writer.newLine();
            }

            // Kanten schreiben
            /*for (Edge edge : edges) {
                // Zeichen korrekt als Komma-separierte Liste speichern
                String characters = edge.characters.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(""));  // Zeichen ohne zusätzliche Kommas kombinieren

                writer.write("EDGE," + edge.startState.number + "," + edge.endState.number + ","
                        + characters + "," + edge.arrowType);
                writer.newLine();
            }*/

            System.out.println("Graph erfolgreich exportiert!");
        } catch (IOException e) {
            System.err.println("Fehler beim Exportieren des Graphen: " + e.getMessage());
        }
    }


    public void importGraph(File filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            states.clear();
            edges.clear();

            String line;
            Map<Integer, State> stateMap = new HashMap<>();
            this.currentNodeNumber = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals("NODE")) {
                    int number = Integer.parseInt(parts[1]);
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    State state = new State(x, y, number, NODE_RADIUS);
                    this.currentNodeNumber++;

                    states.add(state);
                    stateMap.put(number, state);
                } else if (parts[0].equals("EDGE")) {
                    int startNumber = Integer.parseInt(parts[1]);
                    int endNumber = Integer.parseInt(parts[2]);

                    Edge edge = new Edge(stateMap.get(startNumber), stateMap.get(endNumber));
                    edges.add(edge);

                    // Verknüpfungen zwischen Knoten aktualisieren
                    stateMap.get(startNumber).outgoingEdges.add(edge);
                    stateMap.get(endNumber).incomingEdges.add(edge);
                }
            }
            System.out.println("Graph erfolgreich importiert!");
        } catch (IOException e) {
            System.err.println("Fehler beim Importieren des Graphen: " + e.getMessage());
        }
    }

}
