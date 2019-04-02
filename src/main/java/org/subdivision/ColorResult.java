package org.subdivision;

/**
 * Value object to store the components of a color
 */
class ColorResult {
    private int b;
    private int g;
    private int r;

    public ColorResult(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public double squared() {
        return r * r + g * g + b * b;
    }
}