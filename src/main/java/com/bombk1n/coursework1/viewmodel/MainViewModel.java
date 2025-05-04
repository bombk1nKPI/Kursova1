package com.bombk1n.coursework1.viewmodel;

import com.bombk1n.coursework1.model.Graph;
import com.bombk1n.coursework1.util.VertexCoverAlgorithms;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MainViewModel {
    private ObjectProperty<Graph> graph = new SimpleObjectProperty<>(new Graph());
    private ListProperty<Integer> resultCover = new SimpleListProperty<>(FXCollections.observableArrayList());
    private StringProperty executionTime = new SimpleStringProperty();

    public void generateRandomGraph(int vertices, int edges) {
        graph.set(Graph.generateRandomGraph(vertices, edges));
    }

    public void addEdge(int from, int to) {
        graph.get().addEdge(from, to);
    }

    public void solve(String method) {
    long start = System.nanoTime(); 
    Set<Integer> cover;
    switch (method) {
        case "Greedy":
            cover = VertexCoverAlgorithms.greedy(graph.get());
            break;
        case "Approx":
            cover = VertexCoverAlgorithms.approx(graph.get());
            break;
        case "Backtracking":
            cover = VertexCoverAlgorithms.backtracking(graph.get());
            break;
        default:
            throw new IllegalArgumentException("Unknown method");
    }
    long end = System.nanoTime();
    resultCover.setAll(cover);
    executionTime.set(((end - start) / 1_000_000) + " ms"); 
}

    public void saveToFile(String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        for (Integer v : resultCover) {
            writer.write(v + "\n");
        }
        writer.close();
    }
    
    public void clearGraph() {
        graph.set(new Graph());
        resultCover.clear();
        executionTime.set("");
    }

    public ListProperty<Integer> resultCoverProperty() { return resultCover; }
    public StringProperty executionTimeProperty() { return executionTime; }
    public ObjectProperty<Graph> graphProperty() { return graph; }
}

