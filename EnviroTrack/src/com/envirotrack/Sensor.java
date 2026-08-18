package com.envirotrack;
public class Sensor {
    private int id;
    private String name;
    private String value;

    // Default constructor
    public Sensor() {}

    // Constructor without ID (for new sensors to insert into DB)
    public Sensor(String name, String value) {
        this.name = name;
        this.value = value;
    }

    // Constructor with ID (for existing sensors fetched from DB)
    public Sensor(int id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + value;
    }
}
