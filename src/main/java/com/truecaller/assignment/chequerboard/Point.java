package com.truecaller.assignment.chequerboard;

/**
 * Basic abstraction representing a point in the board
 * Each property represents the number of squares and the direction (negative => left|down or positive => right|up)
 */
public class Point {

    private final int x;
    private final int y;

    private Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Just a regular factory method to avoid doing manual creation of Points
     * @param x value representing the x axe in the board
     * @param y value representing the y axe in the board
     * @return The Point abstraction
     */
    public static Point newPoint(int x, int y) {
        return new Point(x,y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
