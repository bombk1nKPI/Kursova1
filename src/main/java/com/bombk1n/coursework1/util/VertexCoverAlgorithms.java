package com.bombk1n.coursework1.util;

import com.bombk1n.coursework1.model.Edge;
import com.bombk1n.coursework1.model.Graph;

import java.util.*;

public class VertexCoverAlgorithms {

    public static Set<Integer> greedy(Graph graph) {
        Set<Integer> vertexCover = new HashSet<>();
        Set<Edge> uncoveredEdges = new HashSet<>(graph.edges);

        while (!uncoveredEdges.isEmpty()) {
            Map<Integer, Integer> degreeMap = new HashMap<>();
            for (Edge edge : uncoveredEdges) {
                degreeMap.put(edge.getFrom(), degreeMap.getOrDefault(edge.getFrom(), 0) + 1);
                degreeMap.put(edge.getTo(), degreeMap.getOrDefault(edge.getTo(), 0) + 1);
            }

            int vertex = degreeMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey();

            vertexCover.add(vertex);

            uncoveredEdges.removeIf(edge -> edge.getFrom() == vertex || edge.getTo() == vertex);
        }

        return vertexCover;
    }

    public static Set<Integer> approx(Graph graph) {
        Set<Integer> vertexCover = new HashSet<>();
        Set<Edge> uncoveredEdges = new HashSet<>(graph.edges);

        while (!uncoveredEdges.isEmpty()) {
            Edge edge = uncoveredEdges.iterator().next();
            int u = edge.getFrom();
            int v = edge.getTo();

            vertexCover.add(u);
            vertexCover.add(v);

            uncoveredEdges.removeIf(e -> e.getFrom() == u || e.getFrom() == v || e.getTo() == u || e.getTo() == v);
        }

        return vertexCover;
    }

    public static Set<Integer> backtracking(Graph graph) {
        List<Integer> vertexList = new ArrayList<>(graph.vertices);
        Set<Edge> uncoveredEdges = new HashSet<>(graph.edges);
        Set<Integer> bestVertexCover = new HashSet<>(graph.vertices);

        backtrack(new HashSet<>(), vertexList, uncoveredEdges, bestVertexCover);
        return bestVertexCover;
    }

    private static void backtrack(Set<Integer> currentVertexCover, List<Integer> remainingVertices, Set<Edge> uncoveredEdges, Set<Integer> bestVertexCover) {
        if (isCover(uncoveredEdges, currentVertexCover)) {
            if (currentVertexCover.size() < bestVertexCover.size()) {
                bestVertexCover.clear();
                bestVertexCover.addAll(currentVertexCover);
            }
            return;
        }

        if (remainingVertices.isEmpty()) return;

        int vertex = remainingVertices.get(0);
        List<Integer> nextVertices = new ArrayList<>(remainingVertices);
        nextVertices.remove(0);

        Set<Integer> withVertex = new HashSet<>(currentVertexCover);
        withVertex.add(vertex);
        backtrack(withVertex, nextVertices, uncoveredEdges, bestVertexCover);

        backtrack(currentVertexCover, nextVertices, uncoveredEdges, bestVertexCover);
    }

    private static boolean isCover(Set<Edge> edges, Set<Integer> vertexCover) {
        for (Edge edge : edges) {
            if (!vertexCover.contains(edge.getFrom()) && !vertexCover.contains(edge.getTo())) {
                return false;
            }
        }
        return true;
    }
}
