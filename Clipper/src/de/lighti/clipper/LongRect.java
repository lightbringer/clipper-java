package de.lighti.clipper;

public class LongRect {
    public double left;
    public double top;
    public double right;
    public double bottom;

    public LongRect() {

    }

    public LongRect( double l, double t, double r, double b ) {
        left = l;
        top = t;
        right = r;
        bottom = b;
    }

    public LongRect( LongRect ir ) {
        left = ir.left;
        top = ir.top;
        right = ir.right;
        bottom = ir.bottom;
    }
}
