import java.util.*;

public class TuringMachine {

    public static class TmState {
        private final String name;
        private final boolean isStart;
        private final boolean isAccept;
        private final int index;

        public TmState(String name, boolean isStart, boolean isAccept, int index) {
            this.name = name;
            this.isStart = isStart;
            this.isAccept = isAccept;
            this.index = index;
        }

        public String getName() { return name; }
        public boolean isStart() { return isStart; }
        public boolean isAccept() { return isAccept; }
        public int getIndex() { return index; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TmState state)) return false;
            return name.equals(state.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class TransitionInput {
        private final List<Character> symbolsRead;

        public TransitionInput(List<Character> symbolsRead) {
            this.symbolsRead = symbolsRead;
        }

        public List<Character> getSymbolsRead() { return symbolsRead; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransitionInput that)) return false;
            return symbolsRead.equals(that.symbolsRead);
        }

        @Override
        public int hashCode() {
            return Objects.hash(symbolsRead);
        }
    }

    public static class TransitionOutput {
        private final TmState nextState;
        private final List<Character> symbolsToWrite;
        private final List<Integer> moveDirections; // -1 = Left, 0 = Stay, 1 = Right

        public TransitionOutput(TmState nextState, List<Character> symbolsToWrite, List<Integer> moveDirections) {
            this.nextState = nextState;
            this.symbolsToWrite = symbolsToWrite;
            this.moveDirections = moveDirections;
        }

        public TmState getNextState() { return nextState; }
        public List<Character> getSymbolsToWrite() { return symbolsToWrite; }
        public List<Integer> getMoveDirections() { return moveDirections; }
    }

    final Set<TmState> states = new HashSet<>();
    final Set<Character> alphabet = new HashSet<>();
    final Map<TmState, Map<TransitionInput, TransitionOutput>> transitions = new HashMap<>();
    private TmState startState;
    private Set<TmState> acceptStates = new HashSet<>();

    public Set<TmState> getStates() { return states; }
    public Set<Character> getAlphabet() { return alphabet; }
    public Map<TmState, Map<TransitionInput, TransitionOutput>> getTransitions() { return transitions; }

    public void addState(TmState state) {
        states.add(state);
        if (state.isStart()) {
            if (startState != null) throw new IllegalStateException("Start state already defined");
            startState = state;
        }
        if (state.isAccept()) acceptStates.add(state);
    }

    public void addSymbol(char symbol) {
        alphabet.add(symbol);
    }

    public void addTransition(TmState fromState, TransitionInput input, TransitionOutput output) {
        transitions.computeIfAbsent(fromState, k -> new HashMap<>()).put(input, output);
    }

    public TmState getStartState() {
        return startState;
    }

    public Set<TmState> getAcceptStates() {
        return acceptStates;
    }

    public TransitionOutput getTransition(TmState currentState, TransitionInput input) {
        Map<TransitionInput, TransitionOutput> stateTransitions = transitions.get(currentState);
        if (stateTransitions == null) return null;
        return stateTransitions.get(input);
    }

    public boolean isAcceptState(TmState state) {
        return acceptStates.contains(state);
    }

    public String generateDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("States:\n");
        for (TmState state : states) {
            sb.append("- ").append(state.getName());
            if (state.isStart()) sb.append(" (start)");
            if (state.isAccept()) sb.append(" (accept)");
            sb.append("\n");
        }
        sb.append("\nAlphabet:\n");
        for (Character c : alphabet) {
            sb.append("- ").append(c).append("\n");
        }
        sb.append("\nTransitions:\n");
        for (var entry : transitions.entrySet()) {
            TmState from = entry.getKey();
            for (var trans : entry.getValue().entrySet()) {
                sb.append(from.getName())
                        .append(" ").append(trans.getKey().getSymbolsRead())
                        .append(" -> ").append(trans.getValue().getNextState().getName())
                        .append(" write ").append(trans.getValue().getSymbolsToWrite())
                        .append(" move ").append(trans.getValue().getMoveDirections())
                        .append("\n");
            }
        }
        return sb.toString();
    }

}
