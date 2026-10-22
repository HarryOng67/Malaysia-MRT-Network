package com.mrt.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
  AbstractGraph implements the common logic for an undirected graph using an
  ADJACENCY LIST representation
  This class implements Graph.java interface and provides the core data-structure
  operations (add/remove vertex, add/remove edge) and the BFS traversal
  algorithm. Concrete graphs (MRTGraph) extend this class.
 
  <V> the type used to represent a vertex
 */

public abstract class AbstractGraph<V> implements Graph<V> {

    // Adjacency list: each vertex maps to a list of its directly connected neighbors. 
    protected Map<V, List<V>> adjacencyList;

    protected AbstractGraph() {
        // LinkedHashMap preserves insertion order, which makes the UI / visualization predictable
        this.adjacencyList = new LinkedHashMap<>();
    }

    @Override
    public boolean addVertex(V vertex) {
        if (vertex == null || adjacencyList.containsKey(vertex)) {
            return false;
        }
        adjacencyList.put(vertex, new ArrayList<>());
        return true;
    }

    @Override
    public boolean removeVertex(V vertex) {
        if (vertex == null || !adjacencyList.containsKey(vertex)) {
            return false;
        }
        adjacencyList.remove(vertex);
        // Remove this vertex from every other vertex's neighbor list
        for (List<V> neighbors : adjacencyList.values()) {
            neighbors.remove(vertex);
        }
        return true;
    }

    @Override
    public boolean addEdge(V v1, V v2) {
        if (v1 == null || v2 == null || v1.equals(v2)) {
            return false;
        }
        if (!adjacencyList.containsKey(v1) || !adjacencyList.containsKey(v2)) {
            return false;
        }
        boolean changed = false;
        List<V> n1 = adjacencyList.get(v1);
        List<V> n2 = adjacencyList.get(v2);
        if (!n1.contains(v2)) {
            n1.add(v2);
            changed = true;
        }
        if (!n2.contains(v1)) {
            n2.add(v1);
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean removeEdge(V v1, V v2) {
        if (v1 == null || v2 == null) {
            return false;
        }
        if (!adjacencyList.containsKey(v1) || !adjacencyList.containsKey(v2)) {
            return false;
        }
        boolean removed1 = adjacencyList.get(v1).remove(v2);
        boolean removed2 = adjacencyList.get(v2).remove(v1);
        return removed1 || removed2;
    }

    @Override
    public List<V> getVertices() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    @Override
    public List<V> getNeighbors(V vertex) {
        List<V> neighbors = adjacencyList.get(vertex);
        return neighbors == null ? new ArrayList<>() : new ArrayList<>(neighbors);
    }

    @Override
    public int getSize() {
        return adjacencyList.size();
    }

    /*
      Breadth-First Search (BFS).
     */
    @Override
    public List<V> bfs(V startVertex) {
        List<V> visitOrder = new ArrayList<>();
        if (startVertex == null || !adjacencyList.containsKey(startVertex)) {
            return visitOrder;
        }

        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();

        visited.add(startVertex);
        queue.offer(startVertex);

        while (!queue.isEmpty()) {
            V current = queue.poll();
            visitOrder.add(current);

            for (V neighbor : adjacencyList.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        return visitOrder;
    }

}
