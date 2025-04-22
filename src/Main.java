import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pebble Game");
        frame.setLayout(new BorderLayout());

        JPanel leftPanel = new TMSimulator();
        leftPanel.setBorder(BorderFactory.createTitledBorder("Turing Machine Simulator"));

        Graph graph = new Graph();
        JPanel rightPanel = new PanelGraph(graph);
        rightPanel.setBorder(BorderFactory.createTitledBorder("Graph View"));

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);

        frame.setSize(800, 400); // Made it a bit wider
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
