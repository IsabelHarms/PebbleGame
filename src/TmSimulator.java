import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TmSimulator extends JPanel {
    private final int CELL_WIDTH = 30;
    private final int VIEW_WIDTH = 21;

    private final TuringMachine tm;
    private JLabel stateLabel;
    private TapePanel tapePanel;
    private JButton stepButton;
    private JButton resetButton;

    private PanelGraph panelGraph;



    public TmSimulator(TuringMachine tm, PanelGraph panelGraph) {
        super();
        this.tm = tm;
        this.panelGraph = panelGraph;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 300));
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

// Tape simulation
        tapePanel = new TapePanel();
        tapePanel.setPreferredSize(new Dimension(CELL_WIDTH * VIEW_WIDTH + 60, CELL_WIDTH * tm.getTapes().length + 20));
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

            // Insert word
            for (int i = 0; i < word.length(); i++) {
                int pos = tm.getTapes()[0].getHeadPosition() + i;
                while (pos >= tm.getTapes()[0].getTape().size()) {
                    tm.getTapes()[0].getTape().add(null);
                }
                tm.getTapes()[0].getTape().set(pos, word.charAt(i));
            }

            tapePanel.repaint();
        });

// Transition table
        TransitionTableModel transitionModel = new TransitionTableModel(tm);
        transitionModel.setCurrentState(tm.getCurrentState());
        JTable transitionTable = new JTable(transitionModel);
        transitionTable.setDefaultRenderer(Object.class, new TransitionTableRenderer(transitionModel));
        JScrollPane transitionScroll = new JScrollPane(transitionTable);
        transitionScroll.setPreferredSize(new Dimension(800, 150));
        centerPanel.add(transitionScroll);

// Machine description
        stateLabel = new JLabel("State: " + tm.getCurrentState().getName());
        JTextArea descriptionArea = new JTextArea(tm.generateDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(800, 100));
        centerPanel.add(stateLabel);
        centerPanel.add(descriptionScroll);

        add(centerPanel, BorderLayout.CENTER);

        stepButton = new JButton("Step");
        stepButton.addActionListener(e -> {
            List<Integer> moveDirs = tm.getExpectedMoveDirections();

            int delay = 5;
            AtomicInteger index = new AtomicInteger();
            int pixelSteps = CELL_WIDTH;
            int tapeCount = tm.getTapes().length;
            int[] offsets = new int[tapeCount];

            Timer animationTimer = new Timer(delay, null);

            animationTimer.addActionListener(ev -> {
                boolean allDone = true;

                for (int i = 0; i < tapeCount; i++) {
                    int visualDirection = moveDirs.get(i) * CELL_WIDTH; // simulate target shift
                    int step = Integer.signum(visualDirection); // either +1, -1, or 0
                    if (Math.abs(offsets[i]) < Math.abs(visualDirection)) {
                        offsets[i] += step;
                        tapePanel.setOffset(i, offsets[i]);
                        allDone = false;
                    }
                }

                if (allDone) {
                    animationTimer.stop();
                    if(!tm.getCurrentState().isAccept()) {
                        tm.step(this);
                        panelGraph.makeNextNode(tm);
                        panelGraph.repaint();
                        for (int i = 0; i < tapeCount; i++) {
                            tapePanel.setOffset(i, 0);
                        }
                    }

                    tapePanel.repaint();
                    stateLabel.setText("State: " + tm.getCurrentState().getName());
                    transitionModel.setCurrentState(tm.getCurrentState());
                    repaint();
                }
            });

            animationTimer.start();
        });

        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            tm.reset();
            stepButton.setEnabled(true);
            stateLabel.setText("State: " + tm.getCurrentState().getName());
            tapePanel.repaint();
            panelGraph.reset();
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(stepButton);
        controlPanel.add(resetButton);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);

        panelGraph.makeFirstNode(tm);
    }

    public void disableStepButton() {
        stepButton.setEnabled(false);
    }

    private class TapePanel extends JPanel {
        private int[] offsetsX;

        public TapePanel() {
            // Initialize offsets for each tape
            this.offsetsX = new int[tm.getTapes().length];
            Arrays.fill(offsetsX, 0);
        }
        public void setOffset(int bandIndex, int offset) {
            this.offsetsX[bandIndex] = offset;
            repaint();
        }

        public void resetOffsets() {
            this.offsetsX = new int[tm.getTapes().length];
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int i = 0; i < tm.getTapes().length; i++) {
                TuringMachine.Tape tape = tm.getTapes()[i];
                int head = tape.getHeadPosition();
                List<Character> cells = tape.getTape();

                int start = head - VIEW_WIDTH / 2;

                for (int j = 0; j < VIEW_WIDTH; j++) {
                    int tapeIndex = start + j;
                    int x = j * CELL_WIDTH - offsetsX[i] + 30;
                    int y = i * CELL_WIDTH + 10;

                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, CELL_WIDTH, CELL_WIDTH);

                    if (tapeIndex >= 0 && tapeIndex < cells.size()) {
                        Character ch = cells.get(tapeIndex);
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
