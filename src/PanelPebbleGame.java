import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;

class PanelPebbleGame extends Panel implements MouseListener, MouseMotionListener {
    JButton startTimePebblingButton = new JButton("Pebble with minimum moves");
    JButton startSpacePebblingButton = new JButton("Pebble with minimum pebbles");

    JButton nextButton = new JButton("Next");
    JButton backButton = new JButton("Back");

    Pebbler pebbler;
    public PanelPebbleGame(Graph graph) {
        super(graph);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.add(bottomPanel, BorderLayout.SOUTH);

        startTimePebblingButton.setFont(new Font("Arial", Font.BOLD, 20));
        startTimePebblingButton.setPreferredSize(new Dimension(400, 50));
        startSpacePebblingButton.setFont(new Font("Arial", Font.BOLD, 20));
        startSpacePebblingButton.setPreferredSize(new Dimension(400, 50));
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.setPreferredSize(new Dimension(200, 50));
        nextButton.setFont(new Font("Arial", Font.BOLD, 20));
        nextButton.setPreferredSize(new Dimension(200, 50));
        backButton.setVisible(false);
        nextButton.setVisible(false);
        bottomPanel.add(startTimePebblingButton);
        bottomPanel.add(startSpacePebblingButton);
        bottomPanel.add(backButton);
        bottomPanel.add(nextButton);

        addMouseListener(this);
        addMouseMotionListener(this);
        startTimePebblingButton.addActionListener(e -> startTimePebbling());
        startSpacePebblingButton.addActionListener(e -> startSpacePebbling());
        pebbler = new Pebbler(graph);
    }

    private void startTimePebbling() {
        switchButtons();
        pebbler.pebbleTime();
    }

    private void startSpacePebbling() {
        switchButtons();
        pebbler.pebbleSpace();
    }

    private void switchButtons() {
        startTimePebblingButton.setVisible(false);
        startSpacePebblingButton.setVisible(false);
        backButton.setVisible(true);
        nextButton.setVisible(true);


    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY())) {
                    node.currentlyPebbled = false;
                    repaint();
                    break;
                }
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY()) && node.canBePebbled()) {
                    node.hasBeenPebbled = true;
                    node.currentlyPebbled = true;
                    if (graph.fullyPebbled()) {
                        graphStateTextArea.setText("won");
                    }
                    repaint();
                    break;
                }
            }
        }
        updateGraphState();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
