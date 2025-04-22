import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;

class PanelPebbleGame extends Panel implements MouseListener, MouseMotionListener {
    JButton startMinimizingButton = new JButton("Start Minimizing");
    public PanelPebbleGame(Graph graph) {
        super(graph);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.add(bottomPanel, BorderLayout.SOUTH);

        addMouseListener(this);
        addMouseMotionListener(this);
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
