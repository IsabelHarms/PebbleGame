import javax.swing.table.AbstractTableModel;
import java.util.*;

public class TransitionTableModel extends AbstractTableModel {
    private final List<TuringMachine.TmState> states;
    private final List<List<Character>> symbolCombinations;
    private final TuringMachine tm;
    TuringMachine.TmState currentState;

    public TransitionTableModel(TuringMachine tm) {
        this.tm = tm;
        this.states = new ArrayList<>(tm.getStates());
        List<Character> alphabet = new ArrayList<>(tm.getAlphabet());
        int tapeCount = tm.getTapes().length;

        this.symbolCombinations = generateSymbolCombinations(alphabet, tapeCount);
    }

    private List<List<Character>> generateSymbolCombinations(List<Character> alphabet, int tapeCount) {
        List<List<Character>> combinations = new ArrayList<>();
        generateCombinationsRecursive(alphabet, tapeCount, new ArrayList<>(), combinations);
        return combinations;
    }

    private void generateCombinationsRecursive(List<Character> alphabet, int depth, List<Character> current, List<List<Character>> result) {
        if (depth == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (Character c : alphabet) {
            current.add(c);
            generateCombinationsRecursive(alphabet, depth - 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    public void setCurrentState(TuringMachine.TmState state) {
        this.currentState = state;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return states.size();
    }

    @Override
    public int getColumnCount() {
        return symbolCombinations.size() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) return "State";
        List<Character> combination = symbolCombinations.get(column - 1);
        StringBuilder name = new StringBuilder();
        for (Character c : combination) name.append(c);
        return name.toString();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TuringMachine.TmState state = states.get(rowIndex);
        if (columnIndex == 0) {
            return state.getName();
        }

        List<Character> symbolCombo = symbolCombinations.get(columnIndex - 1);
        Map<TuringMachine.TransitionInput, TuringMachine.TransitionOutput> transitions = tm.getTransitions().get(state);
        if (transitions != null) {
            for (Map.Entry<TuringMachine.TransitionInput, TuringMachine.TransitionOutput> entry : transitions.entrySet()) {
                if (symbolCombo.equals(entry.getKey().getSymbolsRead())) {
                    TuringMachine.TransitionOutput output = entry.getValue();
                    StringBuilder result = new StringBuilder(output.getNextState().getName());
                    List<Character> writeSymbols = output.getSymbolsToWrite();
                    for (Character c : writeSymbols) {
                        result.append(",").append(c);
                    }
                    return result.toString();
                }
            }
        }
        return "";
    }

    public TuringMachine.TmState getStateAt(int rowIndex) {
        return states.get(rowIndex);
    }
}
