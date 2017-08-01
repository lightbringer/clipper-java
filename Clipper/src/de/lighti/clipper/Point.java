package de.lighti.clipper;

import java.util.Comparator;

public class Point {

    public static double getDeltaX(Point pt1, Point pt2 ) {
        if (pt1.getY() == pt2.getY()) {
            return Edge.HORIZONTAL;
        }
        else {
            return (pt2.getX() - pt1.getX()) / (pt2.getY() - pt1.getY());
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    private static class NumberComparator<T extends Number & Comparable<T>> implements Comparator<T> {

        @Override
        public int compare( T a, T b ) throws ClassCastException {
            return a.compareTo( b );
        }
    }

    static boolean arePointsClose(Point pt1, Point pt2, double distSqrd ) {
        final double dx = pt1.x - pt2.x;
        final double dy = pt1.y - pt2.y;
        return dx * dx + dy * dy <= distSqrd;
    }

    static double distanceFromLineSqrd(Point pt, Point ln1, Point ln2 ) {
        //The equation of a line in general form (Ax + By + C = 0)
        //given 2 points (x¹,y¹) & (x²,y²) is ...
        //(y¹ - y²)x + (x² - x¹)y + (y² - y¹)x¹ - (x² - x¹)y¹ = 0
        //A = (y¹ - y²); B = (x² - x¹); C = (y² - y¹)x¹ - (x² - x¹)y¹
        //perpendicular distance of point (x³,y³) = (Ax³ + By³ + C)/Sqrt(A² + B²)
        //see http://en.wikipedia.org/wiki/Perpendicular_distance
        final double A = ln1.y - ln2.y;
        final double B = ln2.x - ln1.x;
        double C = A * ln1.x + B * ln1.y;
        C = A * pt.x + B * pt.y - C;
        return C * C / (A * A + B * B);
    }

    static Point getUnitNormal(Point pt1, Point pt2 ) {
        double dx = pt2.x - pt1.x;
        double dy = pt2.y - pt1.y;
        if (dx == 0 && dy == 0) {
            return new Point();
        }

        final double f = 1 * 1.0 / Math.sqrt( dx * dx + dy * dy );
        dx *= f;
        dy *= f;

        return new Point( dy, -dx);
    }

    protected static boolean isPt2BetweenPt1AndPt3(Point pt1, Point pt2, Point pt3 ) {
        if (pt1.equals( pt3 ) || pt1.equals( pt2 ) || pt3.equals( pt2 )) {
            return false;
        }
        else if (pt1.x != pt3.x) {
            return pt2.x > pt1.x == pt2.x < pt3.x;
        }
        else {
            return pt2.y > pt1.y == pt2.y < pt3.y;
        }
    }

    protected static boolean slopesEqual(Point pt1, Point pt2, Point pt3 ) {
        return (pt1.y - pt2.y) * (pt2.x - pt3.x) - (pt1.x - pt2.x) * (pt2.y - pt3.y) == 0;
    }

    protected static boolean slopesEqual(Point pt1, Point pt2, Point pt3, Point pt4 ) {
        return (pt1.y - pt2.y) * (pt3.x - pt4.x) - (pt1.x - pt2.x) * (pt3.y - pt4.y) == 0;
    }

    static boolean slopesNearCollinear(Point pt1, Point pt2, Point pt3, double distSqrd ) {
        //this function is more accurate when the point that's GEOMETRICALLY
        //between the other 2 points is the one that's tested for distance.
        //nb: with 'spikes', either pt1 or pt3 is geometrically between the other pts
        if (Math.abs( pt1.x - pt2.x ) > Math.abs( pt1.y - pt2.y )) {
            if (pt1.x > pt2.x == pt1.x < pt3.x) {
                return distanceFromLineSqrd( pt1, pt2, pt3 ) < distSqrd;
            }
            else if (pt2.x > pt1.x == pt2.x < pt3.x) {
                return distanceFromLineSqrd( pt2, pt1, pt3 ) < distSqrd;
            }
            else {
                return distanceFromLineSqrd( pt3, pt1, pt2 ) < distSqrd;
            }
        }
        else {
            if (pt1.y > pt2.y == pt1.y < pt3.y) {
                return distanceFromLineSqrd( pt1, pt2, pt3 ) < distSqrd;
            }
            else if (pt2.y > pt1.y == pt2.y < pt3.y) {
                return distanceFromLineSqrd( pt2, pt1, pt3 ) < distSqrd;
            }
            else {
                return distanceFromLineSqrd( pt3, pt1, pt2 ) < distSqrd;
            }
        }
    }

    private final static NumberComparator NUMBER_COMPARATOR = new NumberComparator();

    protected double x;

    protected double y;

    protected double z;

    protected Point(Point pt ) {
        this( pt.x, pt.y, pt.z );
    }

    protected Point(double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0.0;
    }

    public Point() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    @Override
    public boolean equals( Object obj ) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Point) {
            final Point a = (Point) obj;
            return NUMBER_COMPARATOR.compare( x, a.x ) == 0 && NUMBER_COMPARATOR.compare( y, a.y ) == 0;
        }
        else {
            return false;
        }
    }

    public void set( Point other ) {
        x = other.x;
        y = other.y;
        z = other.z;
    }

    public void setX( double x ) {
        this.x = x;
    }

    public void setY( double y ) {
        this.y = y;
    }

    public void setZ( double z ) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + ", z=" + z + "]";
    }

}// end struct IntPoint