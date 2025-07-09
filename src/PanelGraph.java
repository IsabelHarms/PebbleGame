import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class PanelGraph extends Panel implements MouseListener, MouseMotionListener {
    private State draggedNode;
    private State edgeStartNode = null;
    private int tempX, tempY;

    private State prevState;

    JButton exportButton = new JButton("Export Graph");
    JButton importButton = new JButton("Import Graph");
    JButton startPebbleGameButton = new JButton("Start Pebble Game");

    List<State> lastSegment;
    public PanelGraph(Graph graph, JSplitPane splitPane) {
        super(graph);
        JPanel topButtonPanel = new JPanel();
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exportButton.setFont(new Font("Arial", Font.BOLD, 20));
        exportButton.setPreferredSize(new Dimension(200, 50));
        importButton.setFont(new Font("Arial", Font.BOLD, 20));
        importButton.setPreferredSize(new Dimension(200, 50));
        startPebbleGameButton.setFont(new Font("Arial", Font.BOLD, 20));
        startPebbleGameButton.setPreferredSize(new Dimension(400, 50));
        topButtonPanel.add(exportButton);
        topButtonPanel.add(importButton);
        this.add(topButtonPanel, BorderLayout.NORTH);
        bottomPanel.add(startPebbleGameButton);
        this.add(bottomPanel, BorderLayout.SOUTH);

        addMouseListener(this);
        addMouseMotionListener(this);
        exportButton.addActionListener(e -> exportGraph());
        importButton.addActionListener(e -> importGraph());

        startPebbleGameButton.addActionListener(e -> {
            if(!graph.isValid) {
                return;
            }
            splitPane.setRightComponent(new PanelPebbleGame(graph));
        });
    }

    public void reset() {
        this.graph = new Graph();
        this.prevState = null;
        this.lastSegment = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (edgeStartNode != null) {
            g.setColor(Color.GRAY);
            g.drawLine(edgeStartNode.x, edgeStartNode.y, tempX, tempY);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
    //left mouse clicked = new node
            graph.addState(new State(e.getX(), e.getY(), graph.currentNodeNumber++, 30));
            repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
    //right mouse clicked = edit node
            boolean nodeClicked = false;
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY())) {
                    nodeClicked = true;
                    for (Edge edge: new ArrayList<>(node.incomingEdges)) {
                        graph.removeEdge(edge);
                    }
                    for (Edge edge: new ArrayList<>(node.outgoingEdges)) {
                        graph.removeEdge(edge);
                    }
                    graph.removeNode(node);
                    repaint();
                    break;
                }
            }
            if (!nodeClicked) {
                for (Edge edge : graph.getEdges()) {
                    if (isClickOnEdge(e.getX(), e.getY(), edge)) {
                        graph.removeEdge(edge);
                        repaint();
                        break;
                    }
                }
            }
        }
        updateGraphState();
    }



    @Override
    public void mousePressed(MouseEvent e) {
    //mouse right pressed on node = start edge
        if (SwingUtilities.isRightMouseButton(e)) {
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY())) {
                    edgeStartNode = node;
                    tempX = e.getX();
                    tempY = e.getY();
                    break;
                }
            }
        } else {
    //mouse left pressed on node = move node
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY())) {
                    draggedNode = node;
                    break;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    //mouse drag = dynamic draw edge
        if (draggedNode != null) {
            draggedNode.x = e.getX();
            draggedNode.y = e.getY();
            repaint();
        } else if (edgeStartNode != null) {
            tempX = e.getX();
            tempY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    //mouse release while drawing edge + end on node = finalize edge
        if (SwingUtilities.isRightMouseButton(e)) {
            if (edgeStartNode != null) {
                for (State node : graph.getStates()) {
                    if (node.contains(e.getX(), e.getY()) && node != edgeStartNode) {
                        if (edgeStartNode.connected(node) != null) {
                            graph.removeEdge(edgeStartNode.connected(node));
                        }
                        Edge edge = new Edge(edgeStartNode, node);
                        graph.addEdge(edge);
                        if (node.connected(edgeStartNode) != null) {
                            Edge invertedEdge = node.connected(edgeStartNode);
                        }
                        break;
                    }
                }
                edgeStartNode = null;
                repaint();
            }
        } else {
            draggedNode = null;
        }
        updateGraphState();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateGraphState();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private boolean isClickOnEdge(int px, int py, Edge edge) {
        int x1 = edge.startState.x;
        int y1 = edge.startState.y;
        int x2 = edge.endState.x;
        int y2 = edge.endState.y;

        return isPointNearLine(px, py, x1, y1, x2, y2, 5);
    }

    private boolean isPointNearLine(int px, int py, int x1, int y1, int x2, int y2, double tolerance) {
        double distance = Math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1) /
                Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        return distance <= tolerance;
    }

    private void exportGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            graph.exportGraph(fileToSave);
        }
    }

    private void importGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to import");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            graph.importGraph(fileToImport);
            repaint();
            updateGraphState();
        }
    }

    public void makeFirstNode(TuringMachine tm) {

        State state = new State(500, 500, 0, 30);

        TuringMachine.Tape[] tapes = tm.getTapes();
        for (TuringMachine.Tape tape : tapes) {
            tape.addLastSegmentOfBlock(state);
        }

        prevState = state;
        graph.index++;
        graph.addState(state);
    }

    public void makeNextNode(TuringMachine tm) {
        int centerX = 500;
        int centerY = 500;
        int stepSize = 100; // Distance between nodes

        // Generate spiral coordinates based on step index
        int index = graph.index;

        int x = 0;
        int y = 0;
        int dx = 1;
        int dy = 0;
        int segmentLength = 1;
        int segmentPassed = 0;
        int directionChanges = 0;

        // Simulate spiral walk until reaching the desired index
        for (int i = 0; i < index; i++) {
            x += dx;
            y += dy;
            segmentPassed++;

            if (segmentPassed == segmentLength) {
                // Change direction: right→down→left→up→right→...
                segmentPassed = 0;
                int temp = dx;
                dx = -dy;
                dy = temp;
                directionChanges++;

                if (directionChanges % 2 == 0) {
                    segmentLength++;
                }
            }
        }

        int nodeX = centerX + x * stepSize;
        int nodeY = centerY + y * stepSize;

        State state = new State(nodeX, nodeY, index, 30);
        if (prevState != null) {
            Edge edge = new Edge(prevState, state);
            graph.addEdge(edge);

        }

        TuringMachine.Tape[] tapes = tm.getTapes();
        for (TuringMachine.Tape tape : tapes) {
            State prevSegmentOfBlock = tape.getLastSegmentOfBlock();
            if(prevSegmentOfBlock != null) {
                Edge edge = new Edge(prevSegmentOfBlock, state);
                graph.addEdge(edge);
            }
            tape.changeLastSegmentOfBlock(state);
        }

        prevState = state;
        graph.index++;
        graph.addState(state);
    }


}
