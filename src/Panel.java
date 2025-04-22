import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;

class Panel extends JPanel{
    static final int NODE_RADIUS = 30;
    protected Graph graph;
    protected JTextPane  graphStateTextArea;
    protected JScrollPane scrollPane;  // To make the text area scrollable
    public Panel(Graph graph) {

        setLayout(new BorderLayout());
        this.graph = graph;
        graphStateTextArea = new JTextPane();
        graphStateTextArea.setPreferredSize(new Dimension(500, 1100));
        graphStateTextArea.setEditable(false);
        graphStateTextArea.setFont(new Font("Arial", Font.PLAIN, 20));

        graphStateTextArea.setText(graph.getGraphState());  // Initialize with current state
        scrollPane = new JScrollPane(graphStateTextArea);
        add(scrollPane, BorderLayout.EAST);  // Add to the right side of the panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        for (Edge edge : graph.getEdges()) {
            int startX = edge.startState.x;
            int startY = edge.startState.y;
            int endX = edge.endState.x;
            int endY = edge.endState.y;
            drawArrowLine(g,startX, startY, endX, endY);
            drawArrowHead(g,startX, startY, endX, endY);
            g.setColor(Color.BLACK);
        }
        for (State state : graph.getStates()) {
            state.draw(g);
        }
    }

    protected void updateGraphState() {
        graphStateTextArea.setText(graph.getGraphState());
    }


    protected void drawArrowLine(Graphics g, int startX, int startY, int endX, int endY) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dx = endX - startX, dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Adjust end point to stop at the edge of the node
        double adjustedX2 = endX - (dx / distance) * NODE_RADIUS;
        double adjustedY2 = endY - (dy / distance) * NODE_RADIUS;

        g2.drawLine(startX, startY, (int) adjustedX2, (int) adjustedY2);
    }

    protected void drawArrowHead(Graphics g, int startX, int startY, int endX, int endY) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dx = endX - startX, dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        int controlX = (startX + endX) / 2;
        int controlY = (startY + endY) / 2;
        int offset = 50;

        double t = 0.85;
        double curveX = 0, curveY = 0;
        double tangentX = 0, tangentY = 0;

        curveX = endX - (dx / distance) * NODE_RADIUS;
        curveY = endY - (dy / distance) * NODE_RADIUS;
        tangentX = dx;
        tangentY = dy;

        // Berechnung des Pfeilkopfes
        double arrowAngle = Math.atan2(tangentY, tangentX);
        int arrowLength = 10;
        int arrowWidth = 6;

        int arrowX1 = (int) (curveX - arrowLength * Math.cos(arrowAngle - Math.PI / 6));
        int arrowY1 = (int) (curveY - arrowLength * Math.sin(arrowAngle - Math.PI / 6));
        int arrowX2 = (int) (curveX - arrowLength * Math.cos(arrowAngle + Math.PI / 6));
        int arrowY2 = (int) (curveY - arrowLength * Math.sin(arrowAngle + Math.PI / 6));

        // Pfeilkopf zeichnen
        g2.drawLine((int) curveX, (int) curveY, arrowX1, arrowY1);
        g2.drawLine((int) curveX, (int) curveY, arrowX2, arrowY2);
    }


}
