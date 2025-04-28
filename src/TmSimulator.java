import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class TmSimulator extends JPanel {
    private final int CELL_WIDTH = 30;
    private final int VIEW_WIDTH = 21;

    private List<Character>[] tapes;
    private int headPosition;
    private TuringMachine tm;
    private TuringMachine.TmState currentState;
    private JLabel stateLabel;
    private TapePanel tapePanel;
    private JButton stepButton;
    private JButton resetButton;

    public TmSimulator(TuringMachine tm, int numBands, int tapeLength) {
        super();
        setSize(800, 300);
        setLayout(new BorderLayout());

        this.tm = tm;
        this.tapes = new ArrayList[numBands];
        for (int i = 0; i < numBands; i++) {
            this.tapes[i] = new ArrayList<>();
        }
        this.headPosition = 0;
        this.currentState = tm.getStartState();

        initTapes();

        tapePanel = new TapePanel();
        tapePanel.setPreferredSize(new Dimension(CELL_WIDTH * VIEW_WIDTH + 60, CELL_WIDTH * numBands + 20));
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

// Tape simulation
        tapePanel = new TapePanel();
        tapePanel.setPreferredSize(new Dimension(CELL_WIDTH * VIEW_WIDTH + 60, CELL_WIDTH * tapes.length + 20));
        centerPanel.add(tapePanel);

// Input field
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField inputField = new JTextField(20);
        JButton setWordButton = new JButton("Set Word");
        inputPanel.add(new JLabel("Input Word:"));
        inputPanel.add(inputField);
        inputPanel.add(setWordButton);
        centerPanel.add(inputPanel);
        setWordButton.addActionListener(e -> {
            String word = inputField.getText().trim();
            if (word.isEmpty()) return;

            for (char c : word.toCharArray()) {
                if (!tm.getAlphabet().contains(c)) {
                    JOptionPane.showMessageDialog(this, "Invalid symbol: " + c);
                    return;
                }
            }

            // Insert word into tape 0 at current head position
            for (int i = 0; i < word.length(); i++) {
                int pos = headPosition + i;
                while (pos >= tapes[0].size()) {
                    tapes[0].add(null);
                }
                tapes[0].set(pos, word.charAt(i));
            }

            tapePanel.repaint();
        });

// Transition table
        TransitionTableModel transitionModel = new TransitionTableModel(tm);
        transitionModel.setCurrentState(currentState);
        JTable transitionTable = new JTable(transitionModel);
        transitionTable.setDefaultRenderer(Object.class, new TransitionTableRenderer(transitionModel));
        JScrollPane transitionScroll = new JScrollPane(transitionTable);
        transitionScroll.setPreferredSize(new Dimension(800, 150));
        centerPanel.add(transitionScroll);

// Machine description
        JTextArea descriptionArea = new JTextArea(tm.generateDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(800, 100));
        centerPanel.add(descriptionScroll);

        add(centerPanel, BorderLayout.CENTER);

        stepButton = new JButton("Step");
        stepButton.addActionListener(e -> step());

        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> reset());

        stateLabel = new JLabel("State: " + currentState.getName());
        JPanel controlPanel = new JPanel();
        controlPanel.add(stepButton);
        controlPanel.add(resetButton);
        controlPanel.add(stateLabel);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void initTapes() {
        for (List<Character> tape : tapes) {
            tape.clear();
        }
        headPosition = 0;
    }

    private void reset() {
        initTapes();
        currentState = tm.getStartState();
        headPosition = tapes[0].size() / 2;
        stateLabel.setText("State: " + currentState.getName());
        tapePanel.repaint();
    }

    private void step() {
        if (tm.isAcceptState(currentState)) {
            JOptionPane.showMessageDialog(this, "Machine has accepted.");
            return;
        }

        List<Character> readSymbols = new ArrayList<>();
        if (headPosition >= 0 && headPosition < tapes[0].size() && tapes[0].get(headPosition) != null) {
            readSymbols.add(tapes[0].get(headPosition));
        } else {
            readSymbols.add('#');
        }

        TuringMachine.TransitionInput input = new TuringMachine.TransitionInput(readSymbols);
        TuringMachine.TransitionOutput output = tm.getTransition(currentState, input);

        if (output == null) {
            JOptionPane.showMessageDialog(this, "No transition found. Machine halted.");
            return;
        }

        List<Character> writeSymbols = output.getSymbolsToWrite();
        List<Integer> moveDirections = output.getMoveDirections();

        while (headPosition >= tapes[0].size()) {
            tapes[0].add(null);
        }
        tapes[0].set(headPosition, writeSymbols.get(0));

        boolean moveRight = moveDirections.get(0) > 0;
        int direction = moveRight ? 1 : -1;
        int pixelSteps = CELL_WIDTH;
        int delay = 5;

        Timer animationTimer = new Timer(delay, null);
        final int[] offset = {0};

        animationTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Math.abs(offset[0]) < pixelSteps) {
                    offset[0] += direction;
                    tapePanel.setOffset(offset[0]);
                } else {
                    animationTimer.stop();
                    headPosition += direction;
                    if (headPosition < 0) {
                        tapes[0].add(0, null);
                        headPosition = 0;
                    }
                    while (headPosition >= tapes[0].size()) {
                        tapes[0].add(null);
                    }
                    currentState = output.getNextState();
                    tapePanel.setOffset(0);
                    tapePanel.repaint();
                    stateLabel.setText("State: " + currentState.getName());
                }
            }
        });
        animationTimer.start();
    }

    private class TapePanel extends JPanel {
    private int offsetX = 0;

    public void setOffset(int offset) {
        this.offsetX = offset;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int start = headPosition - VIEW_WIDTH / 2;

        for (int i = 0; i < tapes.length; i++) {
            for (int j = 0; j < VIEW_WIDTH; j++) {
                int tapeIndex = start + j;
                int x = j * CELL_WIDTH - offsetX + 30;
                int y = i * CELL_WIDTH + 10;

                g.setColor(Color.WHITE);
                g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
                g.setColor(Color.GRAY);
                g.drawRect(x, y, CELL_WIDTH, CELL_WIDTH);

                if (tapeIndex >= 0 && tapeIndex < tapes[i].size()) {
                    Character ch = tapes[i].get(tapeIndex);
                    if (ch != null) {
                        g.setColor(Color.BLACK);
                        g.drawString(Character.toString(ch), x + CELL_WIDTH / 2 - 4, y + CELL_WIDTH / 2 + 5);
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                        g.drawString("#", x + CELL_WIDTH / 2 - 4, y + CELL_WIDTH / 2 + 5);
                    }
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawString("#", x + CELL_WIDTH / 2 - 4, y + CELL_WIDTH / 2 + 5);
                }

                if (j == VIEW_WIDTH / 2) {
                    g.setColor(new Color(255, 255, 100, 150));
                    g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
                }
            }
        }
    }
}
}
