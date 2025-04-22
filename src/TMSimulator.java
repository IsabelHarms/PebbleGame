import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TMSimulator extends JPanel {
    private final int NUM_BANDS = 3;
    private final int TAPE_LENGTH = 100;
    private final int CELL_WIDTH = 30;
    private final int VIEW_WIDTH = 21;

    private char[][] tapes = new char[NUM_BANDS][TAPE_LENGTH];
    private int headPosition = TAPE_LENGTH / 2;
    private String currentState = "q0";
    private JLabel stateLabel;
    private TapePanel tapePanel;

    public TMSimulator() {
        super();
        setSize(800, 300);
        setLayout(new BorderLayout());

        initTapes();

        tapePanel = new TapePanel();
        tapePanel.setPreferredSize(new Dimension(CELL_WIDTH * VIEW_WIDTH + 60, CELL_WIDTH * NUM_BANDS + 20));
        add(tapePanel, BorderLayout.CENTER);

        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(e -> step());

        stateLabel = new JLabel("State: " + currentState);
        JPanel controlPanel = new JPanel();
        controlPanel.add(stepButton);
        controlPanel.add(stateLabel);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void initTapes() {
        for (int i = 0; i < NUM_BANDS; i++) {
            for (int j = 0; j < TAPE_LENGTH; j++) {
                tapes[i][j] = '#';
            }
        }
        tapes[0][headPosition] = '1';
        tapes[1][headPosition] = '0';
        tapes[2][headPosition] = '1';
    }

    private void step() {
        if (tapes[0][headPosition] == '1') {
            tapes[0][headPosition] = '0';
        } else {
            tapes[0][headPosition] = '1';
        }

        boolean moveRight = currentState.length() % 2 == 0;
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
                    headPosition = (headPosition + direction + TAPE_LENGTH) % TAPE_LENGTH;
                    currentState = moveRight ? "q1" : "q2";
                    tapePanel.setOffset(0);
                    tapePanel.repaint();
                    stateLabel.setText("State: " + currentState);
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

            for (int i = 0; i < NUM_BANDS; i++) {
                for (int j = 0; j < VIEW_WIDTH; j++) {
                    int tapeIndex = start + j;
                    int x = j * CELL_WIDTH - offsetX + 30;
                    int y = i * CELL_WIDTH + 10;

                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, CELL_WIDTH, CELL_WIDTH);

                    if (tapeIndex >= 0 && tapeIndex < TAPE_LENGTH) {
                        g.setColor(Color.BLACK);
                        g.drawString(Character.toString(tapes[i][tapeIndex]), x + CELL_WIDTH / 2 - 4, y + CELL_WIDTH / 2 + 5);
                    }

                    if (j == VIEW_WIDTH / 2) {
                        g.setColor(new Color(255, 255, 100, 150));
                        g.fillRect(x, y, CELL_WIDTH, CELL_WIDTH);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TMSimulator::new);
    }
}
