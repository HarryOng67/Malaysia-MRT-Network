package com.mrt.graph;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mrt.model.Station;
import com.mrt.util.JsonLoader;

/*
  MRTGraph is the concrete graph implementation for this assignment.
  It represents the MRT (rail) station network, where:
  - each vertex is a station name (String)
  - each edge is a direct rail connection between two stations
  
  It inherits all data-structure operations (add/remove vertex & edge) and
  the BFS traversal algorithm from AbstractGraph which stores the
  graph internally as an ADJACENCY LIST.
  
  This class also provides small bridging helpers so App.java can work with
  Station objects and the JSON persistence layer without needing to
  know about the adjacency-list internals.
*/
public class MRTGraph extends AbstractGraph<String> {

    public MRTGraph() {
        super();
    }

    // Convenience helper: total number of direct connections (edges) in the network. 
    public int getConnectionCount() {
        int total = 0;
        for (String v : getVertices()) {
            total += getNeighbors(v).size();
        }
        // Each undirected edge is counted twice (once from each endpoint)
        return total / 2;
    }

    /**
     * Finds the actual stored station name that matches the given input,
     * ignoring case differences (e.g. "kajang" matches "Kajang").
     * Returns the actual stored name if found, or null if no match exists.
     */
    public String resolveStationName(String input) {
        if (input == null) return null;
        for (String vertex : getVertices()) {
            if (vertex.equalsIgnoreCase(input)) {
                return vertex;
            }
        }
        return null;
    }

    /*
      Populates this graph from a list of {@link Station} objects (as loaded
      from mrt.json). Vertices are added first, then edges, so it doesn't
      matter which order the stations appear in the JSON file.
    */
    public void loadFromStations(List<Station> stations) {
        for (Station s : stations) {
            addVertex(s.getName());
        }
        for (Station s : stations) {
            for (String neighbor : s.getConnectedStations()) {
                addVertex(neighbor); // in case a neighbor wasn't declared as its own entry
                addEdge(s.getName(), neighbor);
            }
        }
    }

    /*
      Persists the current graph state back to data/mrt.json by converting
      the adjacency list into a Map&lt;String, Station&gt; that JsonLoader
      knows how to write out.
    */
    public void save() {
        Map<String, Station> stationMap = new LinkedHashMap<>();
        for (String vertex : getVertices()) {
            stationMap.put(vertex, new Station(vertex, getNeighbors(vertex)));
        }
        JsonLoader.saveStations(stationMap);
    }

    /*
      Runs BFS starting at the given station name. Returns an empty list if
      the station doesn't exist. (Thin wrapper kept for readability in App.java.)
    */
    public List<String> bfsTraverse(String startVertex) {
        return bfs(startVertex);
    }
}
