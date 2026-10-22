package com.mrt.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
  Renders an MRTGraph as a JavaFX Pane: stations are laid out in a grid
  (wrapped in a zig-zag pattern for readability), connections are drawn
  as lines between them. Optionally highlights a BFS
  traversal order with per-level colors and numbered labels.
 */
public class GraphView {

    private static final double NODE_RADIUS = 16;
    private static final double DEFAULT_WIDTH = 1000;
    private static final double DEFAULT_HEIGHT = 700;

    /**
     * Fixed palette used to color BFS levels. Level 1 (direct neighbors of
     * the start) gets LEVEL_COLORS[0], level 2 gets LEVEL_COLORS[1], and so
     * on, cycling back to the start of the array if there are more levels
     * than colors. The start vertex itself (level 0) is always red and is
     * NOT drawn from this palette.
     */
    private static final Color[] LEVEL_COLORS = {
            Color.web("#ffb703"), // level 1  - amber
            Color.web("#fb8500"), // level 2  - orange
            Color.web("#8ecae6"), // level 3  - light blue
            Color.web("#219ebc"), // level 4  - blue
            Color.web("#2a9d8f"), // level 5  - teal
            Color.web("#588157"), // level 6  - green
            Color.web("#6a4c93"), // level 7  - purple
            Color.web("#7fece4"), // level 8  - cyan
            Color.web("#61f472"), // level 9  - lime green
            Color.web("#90be6d"), // level 10 - light green
            Color.web("#577590"), // level 11 - slate blue
            Color.web("#f9c74f"), // level 12 - yellow
            Color.web("#277da1"), // level 13 - deep blue
            Color.web("#9d4edd"), // level 14 - violet
            Color.web("#ff70a6"), // level 15 - pink
            Color.web("#43aa8b"), // level 16 - sea green
    };

    // Renders the plain network with no BFS highlighting.
    public static Pane render(MRTGraph graph, double width, double height) {
        return render(graph, width, height, null, null);
    }

    /*
      Draws the plain station network directly into an existing Pane (clearing
      whatever was there before). This is the overload App.java uses when it
      wants to redraw the same window, e.g. on resize or after an edit.
     */
    public static void render(Pane pane, MRTGraph graph) {
        double width = pane.getWidth() > 0 ? pane.getWidth() : DEFAULT_WIDTH;
        double height = pane.getHeight() > 0 ? pane.getHeight() : DEFAULT_HEIGHT;
        Pane rendered = render(graph, width, height);
        pane.getChildren().setAll(rendered.getChildren());
    }

    /*
      Draws the network into an existing Pane with the BFS visit order
      highlighted. The first station in bfsOrder is treated as the start
      vertex.
     */
    public static void renderWithTraversal(Pane pane, MRTGraph graph, List<String> bfsOrder) {
        double width = pane.getWidth() > 0 ? pane.getWidth() : DEFAULT_WIDTH;
        double height = pane.getHeight() > 0 ? pane.getHeight() : DEFAULT_HEIGHT;
        String startVertex = (bfsOrder == null || bfsOrder.isEmpty()) ? null : bfsOrder.get(0);
        Pane rendered = render(graph, width, height, bfsOrder, startVertex);
        pane.getChildren().setAll(rendered.getChildren());
    }

    /*
      Renders the network. If bfsOrder is provided, visited stations are
      colored by BFS LEVEL (distance in hops from startVertex) using a fixed
      palette — every station at the same level gets the exact same color —
      and labeled with their visit number. Unvisited stations (unreachable
      from start) are shown in gray.
     */
    public static Pane render(MRTGraph graph, double width, double height,
                               List<String> bfsOrder, String startVertex) {
        Pane pane = new Pane();
        pane.setPrefSize(width, height);

        List<String> vertices = graph.getVertices();
        int n = vertices.size();
        Map<String, double[]> positions = new HashMap<>();

        int columns = 8;

        // Distace between stations (nodes)
        double gapX = 110;
        double gapY = 80;

        // Calculate the total rows and boundaries of the grid for centering
        int totalRows = (int) Math.ceil((double) n / columns);
        double gridWidth = (columns - 1) * gapX;
        double gridHeight = (totalRows - 1) * gapY;

        // Dynamically compute the starting coordinates to perfectly center the graph
        double startX = (width - gridWidth) / 2;
        double startY = (height - gridHeight) / 2;

        for (int i = 0; i < n; i++) {

            int row = i / columns;
            int col = i % columns;

            if (row % 2 == 1) {
                col = columns - 1 - col;
            }

            double x = startX + col * gapX;
            double y = startY + row * gapY;

            positions.put(vertices.get(i), new double[]{x, y});
        }

        // Draw edges first so nodes appear on top of the lines
        Set<String> drawnEdges = new HashSet<>();
        for (String v : vertices) {
            double[] p1 = positions.get(v);
            for (String neighbor : graph.getNeighbors(v)) {
                String key = v.compareTo(neighbor) < 0 ? v + "|" + neighbor : neighbor + "|" + v;
                if (drawnEdges.contains(key)) continue;
                drawnEdges.add(key);

                double[] p2 = positions.get(neighbor);
                if (p2 == null) continue;

                Line line = new Line(p1[0], p1[1], p2[0], p2[1]);
                line.setStroke(Color.web("#0303034d"));
                line.setStrokeWidth(3);
                pane.getChildren().add(line);
            }
        }

        Map<String, Integer> orderIndex = new HashMap<>();
        if (bfsOrder != null) {
            for (int i = 0; i < bfsOrder.size(); i++) {
                orderIndex.put(bfsOrder.get(i), i);
            }
        }

        // Compute each station's BFS LEVEL (hop-distance from startVertex),
        // independent of the visit-order index, so that "same level" can be
        // colored identically regardless of the order neighbors happened to
        // be enqueued in.
        Map<String, Integer> levelOf = new HashMap<>();
        if (bfsOrder != null && startVertex != null && vertices.contains(startVertex)) {
            Set<String> visited = new HashSet<>();
            Queue<String> queue = new LinkedList<>();
            levelOf.put(startVertex, 0);
            visited.add(startVertex);
            queue.offer(startVertex);
            while (!queue.isEmpty()) {
                String current = queue.poll();
                int level = levelOf.get(current);
                for (String neighbor : graph.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        levelOf.put(neighbor, level + 1);
                        queue.offer(neighbor);
                    }
                }
            }
        }

        // Draw nodes on top of edges
        for (String v : vertices) {
            double[] p = positions.get(v);
            Circle circle = new Circle(p[0], p[1], NODE_RADIUS);

            if (bfsOrder != null && orderIndex.containsKey(v)) {
                if (v.equals(startVertex)) {
                    circle.setFill(Color.web("#e63946")); // start vertex: red
                } else {
                    int level = levelOf.getOrDefault(v, 1); // level 1+ for anything not the start
                    Color levelColor = LEVEL_COLORS[(level - 1) % LEVEL_COLORS.length];
                    circle.setFill(levelColor); // same BFS level -> same color
                }
            } else if (bfsOrder != null) {
                circle.setFill(Color.web("#d3d3d3")); // unreachable from start
            } else {
                circle.setFill(Color.web("#4361ee")); // default (no BFS running)
            }
            circle.setStroke(Color.web("#1d3557"));
            circle.setStrokeWidth(1.5);

            Text label = new Text(v);
            label.setFont(Font.font(11));
            label.setX(p[0] - label.getLayoutBounds().getWidth() / 2);
            label.setY(p[1] - NODE_RADIUS - 10);
            label.setFill(Color.BLACK);

            Group nodeGroup = new Group(circle, label);

            if (bfsOrder != null && orderIndex.containsKey(v)) {
                // Visit-order number stays as-is: exact sequence, separate
                // from the (now per-level) color.
                Text orderLabel = new Text(String.valueOf(orderIndex.get(v) + 1));
                orderLabel.setFont(Font.font(11));
                orderLabel.setFill(Color.WHITE);
                orderLabel.setX(p[0] - orderLabel.getLayoutBounds().getWidth() / 2);
                orderLabel.setY(p[1] + 4);
                nodeGroup.getChildren().add(orderLabel);
            }

            pane.getChildren().add(nodeGroup);
        }

        // ---- Bottom-left legend: which color = which BFS level ----
        if (bfsOrder != null) {
            int maxLevel = 0;
            for (int lvl : levelOf.values()) {
                maxLevel = Math.max(maxLevel, lvl);
            }
            boolean hasUnreached = orderIndex.size() < vertices.size();

            // Build one flat, ordered list: Start, Level 1..N, then Unreached.
            // Laying these out as a continuous sequence (rather than giving
            // Unreached its own reserved column) means it simply fills the
            // next open slot wherever the level colors left off.
            List<String> legendLabels = new ArrayList<>();
            List<Color> legendItemColors = new ArrayList<>();
            legendLabels.add("Start");
            legendItemColors.add(Color.web("#e63946"));
            for (int lvl = 1; lvl <= maxLevel; lvl++) {
                legendLabels.add("Level " + lvl);
                legendItemColors.add(LEVEL_COLORS[(lvl - 1) % LEVEL_COLORS.length]);
            }
            if (hasUnreached) {
                legendLabels.add("Unreached");
                legendItemColors.add(Color.web("#d3d3d3"));
            }

            double swatchRadius = 6;
            double rowHeight = 18;
            double columnWidth = 130;
            double padding = 10;
            double titleHeight = 22;
            int rowsPerColumn = 5; // 4 level colors + the Start/continuation slot above them

            int totalItems = legendLabels.size();
            int totalColumns = (int) Math.ceil((double) totalItems / rowsPerColumn);
            int legendRows = Math.min(totalItems, rowsPerColumn);

            double legendWidth = totalColumns * columnWidth + padding * 2;
            double legendHeight = legendRows * rowHeight + padding * 2 + titleHeight;

            double legendX = 20;
            double legendY = height - legendHeight - 20;

            Rectangle legendBg = new Rectangle(legendX, legendY, legendWidth, legendHeight);
            legendBg.setFill(Color.color(1, 1, 1, 0.9));
            legendBg.setStroke(Color.web("#999999"));
            legendBg.setStrokeWidth(1);
            legendBg.setArcWidth(10);
            legendBg.setArcHeight(10);
            pane.getChildren().add(legendBg);

            Text legendTitle = new Text(legendX + padding, legendY + padding + 10, "BFS Level Legend");
            legendTitle.setFont(Font.font("System", FontWeight.BOLD, 11));
            legendTitle.setFill(Color.web("#1d3557"));
            pane.getChildren().add(legendTitle);

            for (int i = 0; i < totalItems; i++) {
                int col = i / rowsPerColumn;
                int row = i % rowsPerColumn;
                addLegendItem(pane, legendX, legendY, padding, titleHeight, columnWidth, rowHeight, swatchRadius,
                        col, row, legendLabels.get(i), legendItemColors.get(i));
            }
        }

        return pane;
    }

    /** Draws one legend row (colored swatch + label) at the given column/row cell. */
    private static void addLegendItem(Pane pane, double legendX, double legendY, double padding, double titleHeight,
                                       double columnWidth, double rowHeight, double swatchRadius,
                                       int col, int row, String label, Color color) {
        double cx = legendX + padding + col * columnWidth + swatchRadius;
        double cy = legendY + padding + titleHeight + row * rowHeight;

        Circle swatch = new Circle(cx, cy, swatchRadius);
        swatch.setFill(color);
        swatch.setStroke(Color.web("#1d3557"));
        swatch.setStrokeWidth(1);
        pane.getChildren().add(swatch);

        Text itemLabel = new Text(cx + swatchRadius + 6, cy + 4, label);
        itemLabel.setFont(Font.font(10));
        itemLabel.setFill(Color.web("#333333"));
        pane.getChildren().add(itemLabel);
    }
}