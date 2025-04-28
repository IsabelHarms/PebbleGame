import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pebble Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.setLayout(new BorderLayout());

            CardLayout cardLayout = new CardLayout();
            JPanel mainPanel = new JPanel(cardLayout);

            TmBuilder builderPanel = new TmBuilder(turingMachine -> {
                TmSimulator simulator = new TmSimulator(turingMachine, 3, 100);
                Graph graph = new Graph(); //todo converted graph
                PanelPebbleGame pebblePanel = new PanelPebbleGame(graph);
                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, simulator, pebblePanel);
                splitPane.setDividerLocation(600);

                mainPanel.add(splitPane, "simulator");
                cardLayout.show(mainPanel, "simulator");
            });

            mainPanel.add(builderPanel, "builder");

            frame.add(mainPanel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
