package com.bombk1n.coursework1.view;

import com.bombk1n.coursework1.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;

public class MainViewController {
    @FXML private TextField vertexField, edgeField, edgeFrom, edgeTo;
    @FXML private ComboBox<String> methodBox;
    @FXML private Label timeLabel;
    @FXML private ListView<Integer> resultList;
    @FXML private Canvas graphCanvas;

    private final MainViewModel viewModel = new MainViewModel();

    @FXML
    public void initialize() {
        resultList.itemsProperty().bind(viewModel.resultCoverProperty());
        timeLabel.textProperty().bind(viewModel.executionTimeProperty().concat(" ms"));

        setNumericInputRestriction(vertexField);
        setNumericInputRestriction(edgeField);
        setNumericInputRestriction(edgeFrom);
        setNumericInputRestriction(edgeTo);
    }

    private void setNumericInputRestriction(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }

    @FXML
    public void onGenerateGraph() {
        if (vertexField.getText().isEmpty() || edgeField.getText().isEmpty()) {
            showError("Both 'Vertices' and 'Edges' fields must be filled.");
            return;
        }

        try {
            viewModel.clearGraph();
            int numVertices = Integer.parseInt(vertexField.getText());
            int numEdges = Integer.parseInt(edgeField.getText());

            if (numVertices < 1) {
                showError("Number of vertices must be at least 1.");
                return;
            }
            if (numVertices > 40) {
                showError("Number of vertices cannot exceed 40.");
                return;
            }

            int maxEdges = (numVertices * (numVertices - 1)) / 2;
            if (numEdges < 0 || numEdges > maxEdges) {
                showError("Number of edges must be between 0 and " + maxEdges + " for " + numVertices + " vertices.");
                return;
            }

            viewModel.generateRandomGraph(numVertices, numEdges);
            drawGraph();
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for vertices and edges.");
        }
    }

    @FXML
public void onAddEdge() {
    if (edgeFrom.getText().isEmpty() || edgeTo.getText().isEmpty()) {
        showError("Both 'From' and 'To' fields must be filled.");
        return;
    }

    try {
        int from = Integer.parseInt(edgeFrom.getText());
        int to = Integer.parseInt(edgeTo.getText());

        if (from == to) {
            showError("The 'From' and 'To' vertices cannot be the same.");
            return;
        }

        viewModel.addEdge(from, to);
        drawGraph();
    } catch (NumberFormatException e) {
        showError("Please enter valid numbers for 'From' and 'To' fields.");
    }
}

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void onSolve() {
        if (viewModel.graphProperty().get().vertices.isEmpty()) {
            showError("The graph is empty. Please generate or add edges to the graph before solving.");
            return;
        }
    
        String method = methodBox.getValue();
        int vertexCount = viewModel.graphProperty().get().vertices.size();
    
        if ("Backtracking".equals(method) && vertexCount > 20) { 
            showError("The backtracking method is not suitable for graphs with more than 20 vertices due to high computational complexity.");
            return;
        }
    
        viewModel.solve(method);
        drawGraph();
    }

    @FXML
public void onSaveResult() {
    try {
        Path desktopPath = Paths.get(System.getProperty("user.home"),"OneDrive", "Рабочий стол", "vertex_cover.txt");
        System.out.println(desktopPath);
        viewModel.saveToFile(desktopPath.toString());
    } catch (IOException e) {
        new Alert(Alert.AlertType.ERROR, "Failed to save file").showAndWait();
    }
}

    @FXML
    public void onClearGraph() {
        viewModel.clearGraph();
        drawGraph();
    }

    private void drawGraph() {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());

        var graph = viewModel.graphProperty().get();
        var cover = viewModel.resultCoverProperty().get();

        if (graph.vertices.isEmpty()) {
            return;
        }

        int size = graph.vertices.size();
        int radius = 200;
        int centerX = (int) graphCanvas.getWidth() / 2;
        int centerY = (int) graphCanvas.getHeight() / 2;

        var positions = new HashMap<Integer, double[]>();
        int i = 0;

        for (int v : graph.vertices) {
            double angle = size > 1 ? 2 * Math.PI * i / size : 0;
            double x = centerX + (size > 1 ? radius * Math.cos(angle) : 0);
            double y = centerY + (size > 1 ? radius * Math.sin(angle) : 0);
            positions.put(v, new double[]{x, y});
            i++;
        }

        gc.setLineWidth(2);

        for (var e : graph.edges) {
            double[] p1 = positions.get(e.getFrom());
            double[] p2 = positions.get(e.getTo());
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
        }

        for (int v : graph.vertices) {
            double[] pos = positions.get(v);
            if (cover.contains(v)) {
                gc.setFill(javafx.scene.paint.Color.RED);
            } else {
                gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            }
            gc.fillOval(pos[0] - 10, pos[1] - 10, 20, 20);
            gc.strokeText(String.valueOf(v), pos[0] - 4, pos[1] + 4);
        }
    }
    @FXML
    public void onDeleteEdge() {
        if (edgeFrom.getText().isEmpty() || edgeTo.getText().isEmpty()) {
            showError("Both 'From' and 'To' fields must be filled.");
            return;
        }
    
        try {
            int from = Integer.parseInt(edgeFrom.getText());
            int to = Integer.parseInt(edgeTo.getText());
    
            if (from == to) {
                showError("The 'From' and 'To' vertices cannot be the same.");
                return;
            }
    
            var graph = viewModel.graphProperty().get();
            graph.edges.removeIf(edge -> 
                (edge.getFrom() == from && edge.getTo() == to) || 
                (edge.getFrom() == to && edge.getTo() == from)
            );
            drawGraph(); 
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for 'From' and 'To' fields.");
        }
    }
}