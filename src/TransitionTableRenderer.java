import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TransitionTableRenderer extends DefaultTableCellRenderer {
    private final TransitionTableModel model;

    public TransitionTableRenderer(TransitionTableModel model) {
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        TuringMachine.TmState state = model.getStateAt(row);
        if (state.getName().equals(model.currentState.getName())) {
            c.setBackground(Color.YELLOW);
        } else {
            c.setBackground(Color.WHITE);
        }

        setHorizontalAlignment(CENTER);
        return c;
    }
}
