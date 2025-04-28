import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class TmBuilder extends JPanel {
    private final DefaultListModel<TuringMachine.TmState> stateListModel = new DefaultListModel<>();
    private final DefaultListModel<Character> alphabetListModel = new DefaultListModel<>();
    private final DefaultTableModel transitionsModel = new DefaultTableModel(
            new Object[]{"From State", "Symbols Read", "Symbols Write", "Move Dir (-1/0/1)", "To State", "Delete"}, 0);

    private final JList<TuringMachine.TmState> stateList = new JList<>(stateListModel);
    private final JList<Character> alphabetList = new JList<>(alphabetListModel);
    private final JTable transitionsTable = new JTable(transitionsModel);

    private JButton finalizeButton;
    private Consumer<TuringMachine> onFinalize;

    public TmBuilder(Consumer<TuringMachine> onFinalize) {
        super(new BorderLayout());
        this.onFinalize = onFinalize;
        buildUI();
    }

    private void buildUI() {
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));

        JPanel statePanel = new JPanel(new BorderLayout());
        statePanel.setBorder(BorderFactory.createTitledBorder("States"));
        statePanel.add(new JScrollPane(stateList), BorderLayout.CENTER);
        JButton addStateButton = new JButton("Add State");
        addStateButton.addActionListener(e -> addState());
        statePanel.add(addStateButton, BorderLayout.SOUTH);

        JPanel alphabetPanel = new JPanel(new BorderLayout());
        alphabetPanel.setBorder(BorderFactory.createTitledBorder("Alphabet"));
        alphabetPanel.add(new JScrollPane(alphabetList), BorderLayout.CENTER);
        JButton addSymbolButton = new JButton("Add Symbol");
        addSymbolButton.addActionListener(e -> addSymbol());
        alphabetPanel.add(addSymbolButton, BorderLayout.SOUTH);

        leftPanel.add(statePanel);
        leftPanel.add(alphabetPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Transitions"));
        JButton addTransitionButton = new JButton("Add Transition");
        addTransitionButton.addActionListener(e -> addTransition());
        JPanel transitionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        transitionButtonPanel.add(addTransitionButton);
        centerPanel.add(transitionButtonPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(transitionsTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton importButton = new JButton("Import TM");
        importButton.addActionListener(e -> importTM());
        JButton exportButton = new JButton("Export TM");
        exportButton.addActionListener(e -> exportTM());
        JButton validateButton = new JButton("Validate");
        validateButton.addActionListener(e -> validateTM());
        finalizeButton = new JButton("Finalize");
        finalizeButton.setEnabled(false);
        finalizeButton.addActionListener(e -> finalizeTM());

        bottomPanel.add(importButton);
        bottomPanel.add(exportButton);
        bottomPanel.add(validateButton);
        bottomPanel.add(finalizeButton);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        transitionsTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        transitionsTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    private void addState() {
        String name = JOptionPane.showInputDialog(this, "Enter state name:");
        if (name == null || name.isBlank()) return;
        boolean isStart = JOptionPane.showConfirmDialog(this, "Is this a start state?", "Start State", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        boolean isAccept = JOptionPane.showConfirmDialog(this, "Is this an accept state?", "Accept State", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        int index = stateListModel.size();
        stateListModel.addElement(new TuringMachine.TmState(name, isStart, isAccept, index));
    }

    private void addSymbol() {
        String input = JOptionPane.showInputDialog(this, "Enter symbol (single character):");
        if (input == null || input.length() != 1) return;
        alphabetListModel.addElement(input.charAt(0));
    }

    private void importTM() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                importTm(reader);
                JOptionPane.showMessageDialog(this, "Imported successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to import.");
            }
        }
    }

    private void exportTM() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
                exportTm(writer);
                JOptionPane.showMessageDialog(this, "Exported successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export.");
            }
        }
    }

    private void validateTM() {
        TuringMachine tm = buildTM();
        if (tm.getStartState() == null) {
            JOptionPane.showMessageDialog(this, "No start state defined.");
            finalizeButton.setEnabled(false);
            return;
        }
        if (tm.getAcceptStates().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No accept state defined.");
            finalizeButton.setEnabled(false);
            return;
        }
        finalizeButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, "Turing Machine is valid.");
    }

    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("X");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("X");
            button.addActionListener(e -> {
                transitionsModel.removeRow(selectedRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "X";
        }
    }

    private void addTransition() {
        JTextField fromStateField = new JTextField();
        JTextField symbolsReadField = new JTextField();
        JTextField symbolsWriteField = new JTextField();
        JTextField moveDirField = new JTextField();
        JTextField toStateField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("From State:"));
        panel.add(fromStateField);
        panel.add(new JLabel("Symbols Read:"));
        panel.add(symbolsReadField);
        panel.add(new JLabel("Symbols Write:"));
        panel.add(symbolsWriteField);
        panel.add(new JLabel("Move Direction (-1/0/1):"));
        panel.add(moveDirField);
        panel.add(new JLabel("To State:"));
        panel.add(toStateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Transition", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            transitionsModel.addRow(new Object[]{
                    fromStateField.getText().trim(),
                    symbolsReadField.getText().trim(),
                    symbolsWriteField.getText().trim(),
                    moveDirField.getText().trim(),
                    toStateField.getText().trim(),
                    "X"
            });
        }
    }


    private void finalizeTM() {
        TuringMachine tm = buildTM();
        onFinalize.accept(tm);
    }

    private TuringMachine buildTM() {
        TuringMachine tm = new TuringMachine();
        for (int i = 0; i < stateListModel.size(); i++) tm.addState(stateListModel.get(i));
        for (int i = 0; i < alphabetListModel.size(); i++) tm.addSymbol(alphabetListModel.get(i));
        for (int i = 0; i < transitionsModel.getRowCount(); i++) {
            String fromName = (String) transitionsModel.getValueAt(i, 0);
            List<Character> symbolsRead = toCharList((String) transitionsModel.getValueAt(i, 1));
            List<Character> symbolsWrite = toCharList((String) transitionsModel.getValueAt(i, 2));
            List<Integer> moveDir = toIntList((String) transitionsModel.getValueAt(i, 3));
            String toName = (String) transitionsModel.getValueAt(i, 4);

            TuringMachine.TmState from = findStateByName(fromName);
            TuringMachine.TmState to = findStateByName(toName);

            if (from != null && to != null) {
                tm.addTransition(from, new TuringMachine.TransitionInput(symbolsRead), new TuringMachine.TransitionOutput(to, symbolsWrite, moveDir));
            }
        }
        return tm;
    }

    private void importTm(BufferedReader reader) throws IOException {
        stateListModel.clear();
        alphabetListModel.clear();
        transitionsModel.setRowCount(0);

        String line;
        String section = "";
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.equals("STATES:")) { section = "states"; continue; }
            if (line.equals("ALPHABET:")) { section = "alphabet"; continue; }
            if (line.equals("TRANSITIONS:")) { section = "transitions"; continue; }

            switch (section) {
                case "states" -> {
                    String[] parts = line.split(" ");
                    String name = parts[0];
                    boolean isStart = Arrays.asList(parts).contains("start");
                    boolean isAccept = Arrays.asList(parts).contains("accept");
                    stateListModel.addElement(new TuringMachine.TmState(name, isStart, isAccept, stateListModel.size()));
                }
                case "alphabet" -> alphabetListModel.addElement(line.charAt(0));
                case "transitions" -> {
                    String[] parts = line.split("->");
                    String[] left = parts[0].trim().split(" ");
                    String[] right = parts[1].trim().split(" ");
                    transitionsModel.addRow(new Object[]{
                            left[0],
                            left[1],
                            right[1],
                            right[2],
                            right[0],
                            "X"
                    });
                }
            }
        }
    }

    private void exportTm(PrintWriter writer) {
        writer.println("STATES:");
        for (int i = 0; i < stateListModel.size(); i++) {
            TuringMachine.TmState s = stateListModel.get(i);
            writer.print(s.getName());
            if (s.isStart()) writer.print(" start");
            if (s.isAccept()) writer.print(" accept");
            writer.println();
        }
        writer.println();
        writer.println("ALPHABET:");
        for (int i = 0; i < alphabetListModel.size(); i++) {
            writer.println(alphabetListModel.get(i));
        }
        writer.println();
        writer.println("TRANSITIONS:");
        for (int i = 0; i < transitionsModel.getRowCount(); i++) {
            writer.println(transitionsModel.getValueAt(i, 0) + " " + transitionsModel.getValueAt(i, 1) +
                    " -> " + transitionsModel.getValueAt(i, 4) + " " + transitionsModel.getValueAt(i, 2) + " " + transitionsModel.getValueAt(i, 3));
        }
    }

    private TuringMachine.TmState findStateByName(String name) {
        for (int i = 0; i < stateListModel.size(); i++) {
            if (stateListModel.get(i).getName().equals(name)) return stateListModel.get(i);
        }
        return null;
    }

    private List<Character> toCharList(String s) {
        List<Character> list = new ArrayList<>();
        for (char c : s.toCharArray()) list.add(c);
        return list;
    }

    private List<Integer> toIntList(String s) {
        List<Integer> list = new ArrayList<>();
        for (String part : s.split(",")) list.add(Integer.parseInt(part.trim()));
        return list;
    }
}
