import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TmSimulator extends JPanel {
    private final int CELL_WIDTH = 30;
    private final int VIEW_WIDTH = 21;

    private final TuringMachine tm;
    private JLabel stateLabel;
    private TapePanel tapePanel;
    private JButton stepButton;
    private JButton resetButton;

    public TmSimulator(TuringMachine tm) {
        super();
        this.tm = tm;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 300));

//        tapePanel = new TapePanel();
//        tapePanel.setPreferredSize(new Dimension(CELL_WIDTH * VIEW_WIDTH + 60,
//                CELL_WIDTH * tm.getTapes().length + 20));
//        add(tapePanel, BorderLayout.CENTER);
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
        JTextArea descriptionArea = new JTextArea(tm.generateDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(800, 100));
        centerPanel.add(descriptionScroll);

        add(centerPanel, BorderLayout.CENTER);

        stepButton = new JButton("Step");
        stepButton.addActionListener(e -> {
            int direction = tm.getExpectedMoveDirection();

            int pixelSteps = CELL_WIDTH;
            int delay = 5;
            final int[] offset = {0};

            Timer animationTimer = new Timer(delay, null);
            animationTimer.addActionListener(ev -> {
                if (Math.abs(offset[0]) < pixelSteps) {
                    offset[0] += direction;
                    tapePanel.setOffset(offset[0]);
                } else {
                    animationTimer.stop();

                    tm.step();

                    tapePanel.setOffset(0);
                    tapePanel.repaint();
                    stateLabel.setText("State: " + tm.getCurrentState().getName());
                }
            });
            animationTimer.start();
        });



        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            tm.reset();
            stateLabel.setText("State: " + tm.getCurrentState().getName());
            tapePanel.repaint();
        });

        stateLabel = new JLabel("State: " + tm.getCurrentState().getName());
        JPanel controlPanel = new JPanel();
        controlPanel.add(stepButton);
        controlPanel.add(resetButton);
        controlPanel.add(stateLabel);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
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

            for (int i = 0; i < tm.getTapes().length; i++) {
                TuringMachine.Tape tape = tm.getTapes()[i];
                int head = tape.getHeadPosition();
                List<Character> cells = tape.getTape();

                int start = head - VIEW_WIDTH / 2;

                for (int j = 0; j < VIEW_WIDTH; j++) {
                    int tapeIndex = start + j;
                    int x = j * CELL_WIDTH - offsetX + 30;
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
