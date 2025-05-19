import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class TmBuilder extends JPanel {
    private final DefaultListModel<TuringMachine.TmState> stateListModel = new DefaultListModel<>();
    private final DefaultListModel<Character> alphabetListModel = new DefaultListModel<>();
    private final DefaultTableModel transitionsModel = new DefaultTableModel(
            new Object[]{"From State", "Symbols Read", "Symbols Write", "Move Dir (-1/0/1)", "To State"}, 0);

    private final JList<TuringMachine.TmState> stateList = new JList<>(stateListModel);
    private final JList<Character> alphabetList = new JList<>(alphabetListModel);
    private final JTable transitionsTable = new JTable(transitionsModel);
    private final JSpinner tapeCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private boolean tapeCountLocked = false;

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
        stateList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TuringMachine.TmState s) {
                    String baseName = s.getName().split(" \\(")[0];
                    List<String> tags = new ArrayList<>();
                    if (s.isStart()) tags.add("start");
                    if (s.isAccept()) tags.add("accept");
                    label.setText(tags.isEmpty() ? baseName : baseName + " (" + String.join(", ", tags) + ")");
                }
                return label;
            }
        });
        stateList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;

                int index = stateList.locationToIndex(e.getPoint());
                if (index == -1) return;

                stateList.setSelectedIndex(index);
                TuringMachine.TmState selectedState = stateListModel.get(index);

                int confirm = JOptionPane.showConfirmDialog(
                        TmBuilder.this,
                        "Delete state '" + selectedState.getName() + "' and all related transitions?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    stateListModel.remove(index);

                    for (int i = transitionsModel.getRowCount() - 1; i >= 0; i--) {
                        String from = (String) transitionsModel.getValueAt(i, 0);
                        String to = (String) transitionsModel.getValueAt(i, 4);
                        if (from.equals(selectedState.getName()) || to.equals(selectedState.getName())) {
                            transitionsModel.removeRow(i);
                            SwingUtilities.invokeLater(() -> {
                                if (transitionsModel.getRowCount() <= 0) {
                                    tapeCountSpinner.setEnabled(true);
                                    tapeCountLocked = false;
                                }
                            });
                        }
                    }
                }
            }
        });

        transitionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;

                int row = transitionsTable.rowAtPoint(e.getPoint());
                if (row == -1) return;

                transitionsTable.setRowSelectionInterval(row, row);

                String from = (String) transitionsModel.getValueAt(row, 0);
                String to = (String) transitionsModel.getValueAt(row, 4);

                int confirm = JOptionPane.showConfirmDialog(
                        TmBuilder.this,
                        "Delete transition from '" + from + "' to '" + to + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    if (transitionsTable.isEditing()) {
                        transitionsTable.getCellEditor().stopCellEditing();
                    }

                    transitionsModel.removeRow(row);

                    if (transitionsModel.getRowCount() == 0) {
                        tapeCountSpinner.setEnabled(true);
                        tapeCountLocked = false;
                    }
                }
            }
        });

        JLabel tapeLabel = new JLabel("Tape Count: ", SwingConstants.LEFT);
        tapeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        tapeCountSpinner.setFont(new Font("SansSerif", Font.BOLD, 16));
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeft.add(tapeLabel);
        topLeft.add(tapeCountSpinner);
        leftPanel.add(topLeft, BorderLayout.NORTH);


        JButton addStateButton = new JButton("Add State");
        addStateButton.addActionListener(e -> addState());
        statePanel.add(addStateButton, BorderLayout.SOUTH);

        leftPanel.add(statePanel);

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
    }

    private void addState() {
        String name = JOptionPane.showInputDialog(this, "Enter state name:");
        if (name == null || name.isBlank()) return;
        boolean isStart = JOptionPane.showConfirmDialog(this, "Is this a start state?", "Start State", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        if (isStart) {
            for (int i = 0; i < stateListModel.size(); i++) {
                stateListModel.get(i).setStart(false);
            }
        }
        boolean isAccept = JOptionPane.showConfirmDialog(this, "Is this an accept state?", "Accept State", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        int index = stateListModel.size();

        TuringMachine.TmState state = new TuringMachine.TmState(name, isStart, isAccept, index);
        stateListModel.addElement(state);
    }

    private void addSymbol() {
        String input = JOptionPane.showInputDialog(this, "Enter symbol (single character):");
        if (input == null || input.length() != 1) return;
        alphabetListModel.addElement(input.charAt(0));
    }

    private void addTransition() {
        int tapeCount = (Integer) tapeCountSpinner.getValue();

        JPanel panel = new JPanel(new BorderLayout());
        JPanel rowsPanel = new JPanel(new GridLayout(tapeCount, 3, 5, 5));

        JComboBox<String> fromCombo = new JComboBox<>();
        JComboBox<String> toCombo = new JComboBox<>();

        for (int i = 0; i < stateListModel.size(); i++) {
            fromCombo.addItem(stateListModel.get(i).getName());
            toCombo.addItem(stateListModel.get(i).getName());
        }

        List<JTextField> readFields = new ArrayList<>();
        List<JTextField> writeFields = new ArrayList<>();
        List<JTextField> moveFields = new ArrayList<>();

        for (int i = 0; i < tapeCount; i++) {
            JTextField read = new JTextField(1);
            JTextField write = new JTextField(1);
            JTextField move = new JTextField(2);
            readFields.add(read);
            writeFields.add(write);
            moveFields.add(move);

            rowsPanel.add(read);
            rowsPanel.add(write);
            rowsPanel.add(move);
        }

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        JPanel stateFields = new JPanel(new GridLayout(2, 2));
        fieldsPanel.add(new JLabel("From State:"));
        fieldsPanel.add(fromCombo);
        fieldsPanel.add(new JLabel("To State:"));
        fieldsPanel.add(toCombo);


        fieldsPanel.add(stateFields);
        fieldsPanel.add(new JLabel("Per Tape (Read | Write | Move):"));
        fieldsPanel.add(rowsPanel);


        panel.add(fieldsPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Transition", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        StringBuilder reads = new StringBuilder();
        StringBuilder writes = new StringBuilder();
        StringBuilder moves = new StringBuilder();

        for (int i = 0; i < tapeCount; i++) {
            String r = readFields.get(i).getText().trim();
            String w = writeFields.get(i).getText().trim();
            String m = moveFields.get(i).getText().trim();

            if (r.length() != 1 || w.length() != 1 || (!m.equals("-1") && !m.equals("0") && !m.equals("1"))) {
                JOptionPane.showMessageDialog(this, "Invalid input on tape " + (i + 1));
                return;
            }

            reads.append(r);
            writes.append(w);
            if (i < tapeCount - 1) {
                reads.append(",");
                writes.append(",");
            }
            moves.append(m);
            if (i < tapeCount - 1) moves.append(",");
        }

        transitionsModel.addRow(new Object[]{
                fromCombo.getSelectedItem(),
                reads.toString(),
                writes.toString(),
                moves.toString(),
                toCombo.getSelectedItem(),
                "X"
        });

        if (!tapeCountLocked) {
            tapeCountSpinner.setEnabled(false);
            tapeCountLocked = true;
        }
    }

    private void finalizeTM() {
        TuringMachine tm = buildTM();
        String complexity = promptComplexityInput();
        tm.setTimeComplexity(complexity);
        onFinalize.accept(tm);
    }

    private TuringMachine buildTM() {
        TuringMachine.TmState startState = null;
        Set<TuringMachine.TmState> acceptStates = new HashSet<>();
        List<TuringMachine.TmState> allStates = new ArrayList<>();

        for (int i = 0; i < stateListModel.size(); i++) {
            TuringMachine.TmState s = stateListModel.get(i);
            allStates.add(s);
            if (s.isStart()) startState = s;
            if (s.isAccept()) acceptStates.add(s);
        }

        if (startState == null) throw new IllegalStateException("No start state defined");

        int tapeCount = (Integer) tapeCountSpinner.getValue();
        TuringMachine tm = new TuringMachine(tapeCount, startState, allStates, acceptStates);

        Set<Character> alphabet = new HashSet<>();
        for (int i = 0; i < transitionsModel.getRowCount(); i++) {
            String read = (String) transitionsModel.getValueAt(i, 1);
            String write = (String) transitionsModel.getValueAt(i, 2);
            for (char c : read.replace(",", "").toCharArray()) alphabet.add(c);
            for (char c : write.replace(",", "").toCharArray()) alphabet.add(c);
        }
        alphabet.forEach(tm::addSymbol);


        for (int i = 0; i < transitionsModel.getRowCount(); i++) {
            String fromName = (String) transitionsModel.getValueAt(i, 0);
            String readStr = (String) transitionsModel.getValueAt(i, 1);
            String writeStr = (String) transitionsModel.getValueAt(i, 2);
            String moveStr = (String) transitionsModel.getValueAt(i, 3);
            String toName = (String) transitionsModel.getValueAt(i, 4);

            List<Character> readSymbols = toCharList(readStr);
            List<Character> writeSymbols = toCharList(writeStr);
            List<Integer> moveDirs = toIntList(moveStr);

            TuringMachine.TmState from = findStateByName(fromName);
            TuringMachine.TmState to = findStateByName(toName);

            if (from != null && to != null) {
                tm.addTransition(from,
                        new TuringMachine.TransitionInput(readSymbols),
                        new TuringMachine.TransitionOutput(to, writeSymbols, moveDirs));
            }
        }

        return tm;
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

    private TuringMachine.TmState findStateByName(String name) {
        for (int i = 0; i < stateListModel.size(); i++) {
            if (stateListModel.get(i).getName().equals(name)) return stateListModel.get(i);
        }
        return null;
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

    private String promptComplexityInput() {
        JTextField expressionField = new JTextField();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(expressionField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 5, 5, 5));
        String[] buttons = {"n", "log", "^", "*", "+", "(", ")", "CLR", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "←"};

        for (String label : buttons) {
            JButton btn = new JButton(label);
            btn.addActionListener(e -> {
                if (label.equals("CLR")) expressionField.setText("");
                else if (label.equals("←")) {
                    String text = expressionField.getText();
                    if (!text.isEmpty()) expressionField.setText(text.substring(0, text.length() - 1));
                } else {
                    expressionField.setText(expressionField.getText() + label);
                }
            });
            buttonPanel.add(btn);
        }

        panel.add(buttonPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Time Complexity", JOptionPane.OK_CANCEL_OPTION);
        return result == JOptionPane.OK_OPTION ? expressionField.getText().trim() : "O(?)";
    }


    private void importTM() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                stateListModel.clear();
                transitionsModel.setRowCount(0);
                tapeCountSpinner.setEnabled(true);
                tapeCountLocked = false;

                String line;
                String section = "";
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    switch (line) {
                        case "TAPES:" -> { section = "tapes"; continue; }
                        case "STATES:" -> { section = "states"; continue; }
                        case "TRANSITIONS:" -> { section = "transitions"; continue; }
                    }

                    switch (section) {
                        case "tapes" -> tapeCountSpinner.setValue(Integer.parseInt(line));
                        case "states" -> {
                            String[] parts = line.split(" ");
                            String name = parts[0];
                            boolean isStart = Arrays.asList(parts).contains("start");
                            boolean isAccept = Arrays.asList(parts).contains("accept");
                            stateListModel.addElement(new TuringMachine.TmState(name, isStart, isAccept, stateListModel.size()));
                        }
                        case "transitions" -> {
                            String[] sides = line.split("->");
                            String[] left = sides[0].trim().split(" ");
                            String[] right = sides[1].trim().split(" ");
                            transitionsModel.addRow(new Object[]{
                                    left[0],           // from
                                    left[1],           // read
                                    right[1],          // write
                                    right[2],          // move
                                    right[0],          // to
                                    "X"
                            });
                            tapeCountSpinner.setEnabled(false);
                            tapeCountLocked = true;
                        }
                    }
                }
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
                writer.println("TAPES: " + tapeCountSpinner.getValue());
                writer.println();

                writer.println("STATES:");
                for (int i = 0; i < stateListModel.size(); i++) {
                    TuringMachine.TmState s = stateListModel.get(i);
                    writer.print(s.getName());
                    if (s.isStart()) writer.print(" start");
                    if (s.isAccept()) writer.print(" accept");
                    writer.println();
                }

                writer.println();
                writer.println("TRANSITIONS:");
                for (int i = 0; i < transitionsModel.getRowCount(); i++) {
                    writer.println(
                            transitionsModel.getValueAt(i, 0) + " " +         // from
                                    transitionsModel.getValueAt(i, 1) + " -> " +      // read
                                    transitionsModel.getValueAt(i, 4) + " " +         // to
                                    transitionsModel.getValueAt(i, 2) + " " +         // write
                                    transitionsModel.getValueAt(i, 3));               // move
                }

                JOptionPane.showMessageDialog(this, "Exported successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export.");
            }
        }
    }


    private class ButtonRenderer extends JButton implements TableCellRenderer {
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
                SwingUtilities.invokeLater(() -> {
                   if (transitionsModel.getRowCount() <= 0) {
                       tapeCountSpinner.setEnabled(true);
                       tapeCountLocked = false;
                   }
                });
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
}
