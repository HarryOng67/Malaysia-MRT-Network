package com.mrt;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.mrt.graph.GraphView;
import com.mrt.graph.MRTGraph;
import com.mrt.util.JsonLoader;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class App {

    private static final Pattern VALID_STATION_NAME = Pattern.compile("^[a-zA-Z0-9\\s]+$");

    private static final MRTGraph graph = new MRTGraph();
    private static final Scanner scanner = new Scanner(System.in);

    // JavaFX main network window
    private static Stage graphStage;
    private static Pane graphPane;
    private static boolean javafxStarted = false;

    public static void main(String[] args) {

        // exposes loadFromStations(...) which adds every vertex, then every edge.
        graph.loadFromStations(JsonLoader.loadStations());

        boolean running = true;

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createGraphMenu();
                    break;
                case "2":
                    traverseNetwork();
                    break;
                case "3":
                    viewStationNetwork();
                    break;
                case "0":
                    running = false;
                    System.out.printf("%n%84s","Exiting... Goodbye!");
                    break;
                default:
                    System.out.printf("%n%88s%n%n%n","Invalid selection. Please try again.");
            }
        }

        if (javafxStarted) {
            Platform.exit();
        }

        System.exit(0);
    }

    private static void printMainMenu() {
        System.out.printf("%110s%n", "-".repeat(70));
        System.out.printf("%87s%n","Welcome to Malaysia MRT Line!");
        System.out.printf("%110s%n", "-".repeat(70));
        System.out.printf("%100s%n","Main Menu (Press '0' to back to exit or main menu)");
        System.out.printf("%110s%n", "-".repeat(70));
        System.out.printf("%99s%n", "Menu: Enter \"1\" to \"3\" for computation option.\n");
        System.out.printf("%67s%n","1. Create Graph");
        System.out.printf("%85s%n","2. Traverse Station Network (BFS)");
        System.out.printf("%83s%n","3. View the MRT Station Network");
        System.out.printf("%109s%n","-".repeat(31) + "0. Exit" + "-".repeat(31));
        System.out.printf("%n%63s","Selection: ");
    }

    // ---------------- Create Graph submenu ----------------

    private static void createGraphMenu() {

        boolean back = false;

        while (!back) {
            System.out.println("\n" + " ".repeat(48) + "Create Graph: Enter \"1\" to \"5\" for updating the graph.");
            System.out.println(" ".repeat(48) + "1. Add a station");
            System.out.println(" ".repeat(48) + "2. Remove a station");
            System.out.println(" ".repeat(48) + "3. Add a connection");
            System.out.println(" ".repeat(48) + "4. Remove a connection");
            System.out.println(" ".repeat(48) + "5. Return to the main menu");
            System.out.println(" ".repeat(48) + "------------------------------");
            System.out.print(" ".repeat(48) + "Selection: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addVertexFlow();
                    break;
                case "2":
                    removeVertexFlow();
                    break;
                case "3":
                    addEdgeFlow();
                    break;
                case "4":
                    removeEdgeFlow();
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.printf("%n%84s%n%n","Invalid selection. Please try again.");
            }
        }
    }

    private static void addVertexFlow() {
        boolean continueLoop = true;
        while (continueLoop) {
            System.out.printf("%n%101s","#(1) Add a Station (Press \"0\" to return to main menu)");
            System.out.printf("%n%79s","Enter the name of the station: ");
            String name = scanner.nextLine().trim();

            if (name.equals("0")) {
                return; 
            }

            if (name.isEmpty()) {
                System.out.println(" ".repeat(48) + "Station name cannot be empty.");
            }
            else if (!VALID_STATION_NAME.matcher(name).matches()) {
                System.out.println(" ".repeat(48) + "Invalid station name: only letters, numbers, and spaces are allowed (no symbols).");
            }
            else if (graph.resolveStationName(name) != null) {
                System.out.println(" ".repeat(48) + "Station \"" + name + "\" already exists in the network.");
            } 
            else {
                graph.addVertex(name);
                graph.save();
                System.out.println(" ".repeat(48) + "Station \"" + name + "\" has been added.");
            }

            System.out.print("\n" + " ".repeat(48) + "Continue? Y/N: ");
            continueLoop = scanner.nextLine().trim().equalsIgnoreCase("Y");
        }
    }

    private static void removeVertexFlow() {
        boolean continueLoop = true;
        while (continueLoop) {
            System.out.println("\n" + " ".repeat(48) + "#(2) Remove a station (Press \"0\" to return to main menu)\"");
            System.out.print(" ".repeat(48) + "Enter the name of the station: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return;
            }

            String name = graph.resolveStationName(input);

            if (name != null && graph.removeVertex(name)) {
                System.out.println(" ".repeat(48) + "Station \"" + name + "\" has been removed.");
                graph.save();
            } else {
                System.out.println(" ".repeat(48) + "Station \"" + input + "\" was not found.");
            }

            System.out.print("\n" + " ".repeat(48) + "Continue? Y/N: ");
            continueLoop = scanner.nextLine().trim().equalsIgnoreCase("Y");
        }
    }

    private static void addEdgeFlow() {
        printStationList();

        boolean continueLoop = true;
        while (continueLoop) {
            System.out.println("\n" + " ".repeat(48) +"#(3) Add an Connection (Press \"0\" to return to main menu)");
            System.out.print(" ".repeat(48) + "Enter the 1st station: ");

            String fromInput = scanner.nextLine().trim();
            if (fromInput.equals("0")) {
                return;
            }

            System.out.print(" ".repeat(48) + "Enter the 2nd station: ");
            String toInput = scanner.nextLine().trim();
            if (toInput.equals("0")) {
                return;
            }

            

            String from = graph.resolveStationName(fromInput);
            String to = graph.resolveStationName(toInput);

            if (from == null || to == null) {
                System.out.println("\n" + " ".repeat(48) + "One or both station do not exist. Please add them first.");
            } else if (graph.addEdge(from, to)) {
                System.out.println("\n" + " ".repeat(48) + "There is now a station path between " + from + " and " + to + ".");
                graph.save();
            } else {
                System.out.println("\n" + " ".repeat(48) + "This connection already exists.");
            }

            System.out.print("\n" + " ".repeat(48) + "Continue? Y/N: ");
            continueLoop = scanner.nextLine().trim().equalsIgnoreCase("Y");
            System.out.print("\n" );
        }
    }

    private static void removeEdgeFlow() {
        printStationList();

        boolean continueLoop = true;
        while (continueLoop) {
            System.out.println("\n" + " ".repeat(48) + "#(4) Remove an Connection (Press \"0\" to return to main menu)");

            System.out.print("\n" + " ".repeat(48) + "Enter the 1st station: ");
            String fromInput = scanner.nextLine().trim();
            if (fromInput.equals("0")) {
                return;
            }
            
            System.out.print(" ".repeat(48) + "Enter the 2nd station: ");
            String toInput = scanner.nextLine().trim();
            if (toInput.equals("0")) {
                return;
            }

            String from = graph.resolveStationName(fromInput);
            String to = graph.resolveStationName(toInput);

            if (from != null && to != null && graph.removeEdge(from, to)) {
                System.out.println("\n" + " ".repeat(48) + "Station path between " + from + " and " + to + " has been removed.");
                graph.save();
            } else {
                System.out.println("\n" + " ".repeat(48) + "Could not remove connection. Check station names.");
            }

            System.out.print("\n" + " ".repeat(48) + "Continue? Y/N: ");
            continueLoop = scanner.nextLine().trim().equalsIgnoreCase("Y");
        }
    }


    // ---------------- BFS Traversal (CLI text + JavaFX visualization) ----------------

    private static void traverseNetwork() {
        printStationList();

        System.out.print("\n" + " ".repeat(48) + "(Press \"0\" to return to main menu)");
        System.out.print("\n" + " ".repeat(48) + "Enter the starting station for BFS traversal: ");
        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            return;
        }

        String start = graph.resolveStationName(input);
        if (start == null) {
            System.out.println("\n" + " ".repeat(48) + "Station \"" + input + "\" was not found in the network.");
            return;
        }

        List<String> order = graph.bfsTraverse(start);

        if (order.isEmpty()) {
            System.out.println("\n" + " ".repeat(48) + "Station \"" + start + "\" was not found in the network.");
            return;
        }

        System.out.println("\n" + " ".repeat(48) + "BFS Traversal Order starting from \"" + start + "\":\n");
        System.out.println(String.join(" -> ", order));

        showTraversalWindow(order);
    }

    // Display all available stations neatly in a multi-column grid
    private static void printStationList() {

        List<String> stations = new java.util.ArrayList<>(graph.getVertices());

        System.out.println("\n" + " ".repeat(35) + "Available stations:");
        System.out.println(" ".repeat(35) + "-".repeat(87));

        int columns = 3;
        int colWidth = 30;

        for (int i = 0; i < stations.size(); i += columns) {
            StringBuilder row = new StringBuilder(" ".repeat(35));
            for (int j = i; j < Math.min(i + columns, stations.size()); j++) {
                String numbered = (j + 1) + ". " + stations.get(j);
                row.append(String.format("%-" + colWidth + "s", numbered));
            }
            System.out.println(row.toString());
        }

        System.out.println(" ".repeat(35) + "-".repeat(87));
    }

    private static void showTraversalWindow(List<String> visitOrder) {

        System.out.println("\n" + " ".repeat(48) + "Opening BFS traversal visualization...\n\n");

        if (!javafxStarted) {
            javafxStarted = true;
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                openTraversalStage(visitOrder);
            });
        } else {
            Platform.runLater(() -> openTraversalStage(visitOrder));
        }
    }

    private static void openTraversalStage(List<String> visitOrder) {

        Stage traversalStage = new Stage();
        Pane traversalPane = new Pane();
        traversalPane.setPrefSize(1000, 700);

        Scene scene = new Scene(traversalPane, 1000, 700);
        traversalStage.setScene(scene);
        traversalStage.setTitle("BFS Traversal Result (" + visitOrder.size() + " of " + graph.getSize() + " stations reached)");

        traversalPane.widthProperty().addListener((obs, o, n) ->
                GraphView.renderWithTraversal(traversalPane, graph, visitOrder));
        traversalPane.heightProperty().addListener((obs, o, n) ->
                GraphView.renderWithTraversal(traversalPane, graph, visitOrder));

        GraphView.renderWithTraversal(traversalPane, graph, visitOrder);

        traversalStage.show();
    }

    // ---------------- View full MRT Station Network (no highlight) ----------------

    private static void viewStationNetwork() {

        System.out.println("\n" + " ".repeat(48) + "Opening MRT Station network visualization...\n\n");

        if (!javafxStarted) {
            javafxStarted = true;
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                showGraphWindow();
            });
        } else {
            Platform.runLater(App::showGraphWindow);
        }
    }

    private static void showGraphWindow() {

        if (graphStage == null) {
            graphStage = new Stage();
            graphPane = new Pane();
            graphPane.setPrefSize(1000, 700);
            Scene scene = new Scene(graphPane, 1000, 700);
            graphStage.setScene(scene);
            graphStage.setTitle("Malaysia MRT Station Network");

            graphPane.widthProperty().addListener((obs, o, n) -> GraphView.render(graphPane, graph));
            graphPane.heightProperty().addListener((obs, o, n) -> GraphView.render(graphPane, graph));
        }

         // the latest station/connection counts after any Create Graph edits.
            graphStage.setTitle("Malaysia MRT Station Network (" + graph.getSize() + " stations, " + graph.getConnectionCount() + " connections)");

        GraphView.render(graphPane, graph);

        if (!graphStage.isShowing()) {
            graphStage.show();
        } else {
            graphStage.toFront();
        }
    }
}
