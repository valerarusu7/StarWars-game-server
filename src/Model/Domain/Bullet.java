package Model.Domain;

import java.io.Serializable;

public class Bullet implements Serializable {
    private double speed;
    private int size;
    private String color;

    private double[] coordinates;
    private int[] velocity;
    private String type;

    public Bullet(double[] coordinates, int[] velocity, String type) {
        this.coordinates = coordinates;
        this.velocity = velocity;
        this.type = type;
    }

    public Bullet(int speed, int size, String color) {
        this.speed = speed;
        this.size = size;
        this.color = color;
    }

    public double getSpeed() {
        return speed;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public int[] getVelocity() {
        return velocity;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public void setVelocity(int[] velocity) {
        this.velocity = velocity;
    }

    public int getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }
}
