import javax.swing.*;
import java.util.*;

public class TuringMachine {

    public static class TmState {
        private final String name;
        private boolean isStart;
        private boolean isAccept;
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

        public void setStart(boolean isStart) {
            this.isStart = isStart;
        }
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

    class Tape {
        private List<Character> tape;

        private List<State> lastSegment;
        private int headPosition;

        private int lastMoveDirection = 0;

        public Tape() {
            this.tape = new ArrayList<Character>();
            this.lastSegment = new ArrayList<State>();
            this.headPosition = 0;
        }

        public State getLastSegmentOfBlock() {
            return lastSegment.get(headPosition);
        }

        public void changeLastSegmentOfBlock(State state) {
            lastSegment.set(headPosition, state);
        }

        public void addLastSegmentOfBlock(State state) {
            lastSegment.add(headPosition, state);
        }

        public void expandLeftLastSegmentOfBlock() {
            lastSegment.add(0, null);
        }

        public Character read() {
            if (headPosition < 0) {
                expandLeft();
            }
            if (headPosition >= tape.size()) {
                return null;
            }
            return tape.get(headPosition);
        }

        public void write(Character c) {
            if (headPosition < 0) {
                expandLeft();
            }
            while (headPosition >= tape.size()) {
                tape.add(null);
            }
            tape.set(headPosition, c);
        }

        public void move(int direction) {
            lastMoveDirection = direction;
            headPosition += direction;
            if (headPosition < 0) {
                expandLeft();
                expandLeftLastSegmentOfBlock();
            }
            while (headPosition >= tape.size()) {
                tape.add(null);
            }
            while (headPosition >= lastSegment.size()) {
                lastSegment.add(null);
            }
        }

        private void expandLeft() {
            tape.add(0, null);
            headPosition++;
        }

        public Character getAt(int index) {
            if (index >= 0 && index < tape.size()) {
                Character c = tape.get(index);
                return c != null ? c : '#';
            } else {
                return '#';
            }
        }


        public int getLastMoveDirection() {
            return lastMoveDirection;
        }

        public int getHeadPosition() {
            return headPosition;
        }

        public List<Character> getTape() {
            return tape;
        }
    }
    private String timeComplexity = "O(?)";

    final Set<TmState> states = new HashSet<>();
    final Set<Character> alphabet = new HashSet<>();
    final Map<TmState, Map<TransitionInput, TransitionOutput>> transitions = new HashMap<>();
    private TmState startState;
    private Set<TmState> acceptStates = new HashSet<>();

    private TmState currentState;

    private Tape[] tapes;

    public TuringMachine(int tapeCount, TmState startState, List<TmState> allStates, Set<TmState> acceptStates) {
        this.tapes = new Tape[tapeCount];
        for (int i = 0; i < tapeCount; i++) {
            this.tapes[i] = new Tape();
        }

        this.startState = startState;
        this.currentState = startState;

        for (TmState state : allStates) {
            this.states.add(state);
            if (acceptStates.contains(state)) {
                this.acceptStates.add(state);
            }
        }
    }

    public Set<TmState> getStates() { return states; }
    public Set<Character> getAlphabet() { return alphabet; }

    public TmState getCurrentState() { return currentState; }
    public Map<TmState, Map<TransitionInput, TransitionOutput>> getTransitions() { return transitions; }

    public Tape[] getTapes() { return tapes; }

    public void setTimeComplexity(String complexity) {
        this.timeComplexity = complexity.startsWith("O(") ? complexity : "O(" + complexity + ")";
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

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

    public void initInput(String word) {
        for (int i = 0; i < word.length(); i++) {
            tapes[0].write(word.charAt(i));
            tapes[0].move(1);
        }
        tapes[0].headPosition = 0;
    }

    public void initTapes() {
        for (int i = 0; i < this.tapes.length; i++) {
            this.tapes[i] = new Tape();
        }
    }

    public void reset() {
        initTapes();
        currentState = getStartState();
    }

    public void step(TmSimulator tmSimulator) {
        if (currentState.isAccept) {
            JOptionPane.showMessageDialog(tmSimulator, "Turing Machine accepted.");
            return;
        }

        List<Character> readSymbols = new ArrayList<>();
        for (Tape tape : tapes) {
            readSymbols.add(tape.getAt(tape.getHeadPosition()));
        }

        TransitionInput input = new TransitionInput(readSymbols);
        Map<TransitionInput, TransitionOutput> stateTransitions = transitions.get(currentState);
        if (stateTransitions == null) return;

        TransitionOutput output = stateTransitions.get(input);
        if (output == null) return;

        List<Character> writeSymbols = output.getSymbolsToWrite();
        List<Integer> moveDirections = output.getMoveDirections();

        for (int i = 0; i < tapes.length; i++) {
            tapes[i].write(writeSymbols.get(i));
            tapes[i].move(moveDirections.get(i));
        }

        currentState = output.getNextState();
        if (currentState.isAccept) {
            JOptionPane.showMessageDialog(tmSimulator, "Turing Machine accepted.");
            tmSimulator.disableStepButton();
        }
    }


    public List<Integer> getExpectedMoveDirections() {
        List<Character> symbols = new ArrayList<>();
        for (Tape t : tapes) {
            symbols.add(t.getAt(t.getHeadPosition()));
        }

        TransitionInput input = new TransitionInput(symbols);
        Map<TransitionInput, TransitionOutput> stateTransitions = transitions.get(currentState);
        if (stateTransitions == null) return Collections.nCopies(tapes.length, 0);

        TransitionOutput output = stateTransitions.get(input);
        return output != null ? output.getMoveDirections() : Collections.nCopies(tapes.length, 0);
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
