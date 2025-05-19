import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransitionTableModel extends AbstractTableModel {
    private final List<TuringMachine.TmState> states;
    private final List<Character> alphabet;
    private final TuringMachine tm;
    TuringMachine.TmState currentState;

    public TransitionTableModel(TuringMachine tm) {
        this.tm = tm;
        this.states = new ArrayList<>(tm.getStates());
        this.alphabet = new ArrayList<>(tm.getAlphabet());
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
        return alphabet.size() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) return "State";
        return alphabet.get(column - 1).toString();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TuringMachine.TmState state = states.get(rowIndex);
        if (columnIndex == 0) {
            return state.getName();
        }
        Character symbol = alphabet.get(columnIndex - 1);

        Map<TuringMachine.TransitionInput, TuringMachine.TransitionOutput> transitions = tm.getTransitions().get(state);
        if (transitions != null) {
            for (Map.Entry<TuringMachine.TransitionInput, TuringMachine.TransitionOutput> entry : transitions.entrySet()) {
                List<Character> readSymbols = entry.getKey().getSymbolsRead();
                if (readSymbols.size() > 0 && readSymbols.get(0) == symbol) {
                    TuringMachine.TransitionOutput output = entry.getValue();
                    List<Character> writeSymbols = output.getSymbolsToWrite();
                    return output.getNextState().getName() + "," + writeSymbols.get(0);
                }
            }
        }
        return "";
    }

    public TuringMachine.TmState getStateAt(int rowIndex) {
        return states.get(rowIndex);
    }
}
