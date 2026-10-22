package com.mrt.model;

import java.util.List;

public class Station {

    private String name;
    private List<String> connectedStations;

    // Constructor
    public Station(String name, List<String> connectedStations) {
        this.name = name;
        this.connectedStations = connectedStations;
    }

    // Getter
    public String getName() {
        return name;
    }

    public List<String> getConnectedStations() {
        return connectedStations;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setConnectedStations(List<String> connectedStations) {
        this.connectedStations = connectedStations;
    }

    @Override
    public String toString() {
        return name;
    }
}
