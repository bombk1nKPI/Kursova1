package com.bombk1n.coursework1.model;

import java.util.*;

public class Graph {
    public List<Edge> edges = new ArrayList<>();
    public Set<Integer> vertices = new HashSet<>();

    public void addEdge(int from, int to) {
        edges.add(new Edge(from, to));
        vertices.add(from);
        vertices.add(to);
    }

    public static Graph generateRandomGraph(int numVertices, int numEdges) {
        Graph g = new Graph();
    
        for (int i = 0; i < numVertices; i++) {
            g.vertices.add(i);
        }
    
        Random rand = new Random(100);
        while (g.edges.size() < numEdges) {
            int from = rand.nextInt(numVertices);
            int to = rand.nextInt(numVertices);
            if (from != to && g.edges
                    .stream()
                    .noneMatch(e -> (e.getFrom() == from && e.getTo() == to) || (e.getFrom() == to && e.getTo() == from))) {
                g.addEdge(from, to);
            }
        }
    
        return g;
    }
}
