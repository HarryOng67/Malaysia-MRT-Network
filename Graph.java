package com.mrt.graph;

import java.util.List;

/*
  Generic Graph interface.
  Defines the standard operations any graph implementation must support:
  vertex/edge management and a traversal (BFS).
 
  <V> the type used to represent a vertex (here, a Station name - String)
 */
public interface Graph<V> {

    // Adds a new vertex to the graph. Returns false if it already exists. 
    boolean addVertex(V vertex);

    // Removes a vertex (and all edges connected to it). Returns false if not found. 
    boolean removeVertex(V vertex);

    // Adds an undirected edge between v1 and v2. Returns false if either vertex is missing. 
    boolean addEdge(V v1, V v2);

    // Removes the undirected edge between v1 and v2. Returns false if it didn't exist.
    boolean removeEdge(V v1, V v2);

    // Returns all vertices currently in the graph. 
    List<V> getVertices();

    // Returns the neighbors (adjacent vertices) of the given vertex. 
    List<V> getNeighbors(V vertex);

    // Returns the total number of vertices (stations) in the graph.
    int getSize();

    /*
      Performs a Breadth-First Search (BFS) traversal starting from startVertex.
      Returns the list of vertices in the order they were visited.
    */
    List<V> bfs(V startVertex);
}
