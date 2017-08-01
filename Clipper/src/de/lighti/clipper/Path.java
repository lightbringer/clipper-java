package de.lighti.clipper;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A pure convenience class to avoid writing List<IntPoint> everywhere.
 *
 * @author Tobias Mahlmann
 *
 */
public class Path extends ArrayList<Point> {
    static class Join {
        Path.OutPt outPt1;
        Path.OutPt outPt2;
        private Point offPt;

        public Point getOffPt() {
            return offPt;
        }

        public void setOffPt( Point offPt ) {
            this.offPt = offPt;
        }

    }

    static class OutPt {
        public static OutRec getLowerMostRec( OutRec outRec1, OutRec outRec2 ) {
            //work out which polygon fragment has the correct hole state ...
            if (outRec1.bottomPt == null) {
                outRec1.bottomPt = outRec1.pts.getBottomPt();
            }
            if (outRec2.bottomPt == null) {
                outRec2.bottomPt = outRec2.pts.getBottomPt();
            }
            final Path.OutPt bPt1 = outRec1.bottomPt;
            final Path.OutPt bPt2 = outRec2.bottomPt;
            if (bPt1.getPt().getY() > bPt2.getPt().getY()) {
                return outRec1;
            }
            else if (bPt1.getPt().getY() < bPt2.getPt().getY()) {
                return outRec2;
            }
            else if (bPt1.getPt().getX() < bPt2.getPt().getX()) {
                return outRec1;
            }
            else if (bPt1.getPt().getX() > bPt2.getPt().getX()) {
                return outRec2;
            }
            else if (bPt1.next == bPt1) {
                return outRec2;
            }
            else if (bPt2.next == bPt2) {
                return outRec1;
            }
            else if (isFirstBottomPt( bPt1, bPt2 )) {
                return outRec1;
            }
            else {
                return outRec2;
            }
        }

        private static boolean isFirstBottomPt( Path.OutPt btmPt1, Path.OutPt btmPt2 ) {
            Path.OutPt p = btmPt1.prev;
            while (p.getPt().equals( btmPt1.getPt() ) && !p.equals( btmPt1 )) {
                p = p.prev;
            }
            final double dx1p = Math.abs( Point.getDeltaX( btmPt1.getPt(), p.getPt() ) );
            p = btmPt1.next;
            while (p.getPt().equals( btmPt1.getPt() ) && !p.equals( btmPt1 )) {
                p = p.next;
            }
            final double dx1n = Math.abs( Point.getDeltaX( btmPt1.getPt(), p.getPt() ) );

            p = btmPt2.prev;
            while (p.getPt().equals( btmPt2.getPt() ) && !p.equals( btmPt2 )) {
                p = p.prev;
            }
            final double dx2p = Math.abs( Point.getDeltaX( btmPt2.getPt(), p.getPt() ) );
            p = btmPt2.next;
            while (p.getPt().equals( btmPt2.getPt() ) && p.equals( btmPt2 )) {
                p = p.next;
            }
            final double dx2n = Math.abs( Point.getDeltaX( btmPt2.getPt(), p.getPt() ) );

            if (Math.max( dx1p, dx1n ) == Math.max( dx2p, dx2n ) && Math.min( dx1p, dx1n ) == Math.min( dx2p, dx2n )) {
                return btmPt1.area() > 0; //if otherwise identical use orientation
            }
            else {
                return dx1p >= dx2p && dx1p >= dx2n || dx1n >= dx2p && dx1n >= dx2n;
            }
        }

        int idx;
        private Point pt;
        OutPt next;

        OutPt prev;

        public Path.OutPt duplicate( boolean InsertAfter ) {
            final Path.OutPt result = new Path.OutPt();
            result.setPt( new Point( getPt() ) );
            result.idx = idx;
            if (InsertAfter) {
                result.next = next;
                result.prev = this;
                next.prev = result;
                next = result;
            }
            else {
                result.prev = prev;
                result.next = this;
                prev.next = result;
                prev = result;
            }
            return result;
        }

        Path.OutPt getBottomPt() {
            Path.OutPt dups = null;
            Path.OutPt p = next;
            Path.OutPt pp = this;
            while (p != pp) {
                if (p.getPt().getY() > pp.getPt().getY()) {
                    pp = p;
                    dups = null;
                }
                else if (p.getPt().getY() == pp.getPt().getY() && p.getPt().getX() <= pp.getPt().getX()) {
                    if (p.getPt().getX() < pp.getPt().getX()) {
                        dups = null;
                        pp = p;
                    }
                    else {
                        if (p.next != pp && p.prev != pp) {
                            dups = p;
                        }
                    }
                }
                p = p.next;
            }
            if (dups != null) {
                //there appears to be at least 2 vertices at bottomPt so ...
                while (dups != p) {
                    if (!isFirstBottomPt( p, dups )) {
                        pp = dups;
                    }
                    dups = dups.next;
                    while (!dups.getPt().equals( pp.getPt() )) {
                        dups = dups.next;
                    }
                }
            }
            return pp;
        }

        public static int getPointCount( OutPt pts ) {
            if (pts == null) return 0;
            int result = 0;
            OutPt p = pts;
            do {
                result++;
                p = p.next;
            }
            while (p != pts);
            return result;
        }

        public Point getPt() {
            return pt;
        }

        public void reversePolyPtLinks() {

            Path.OutPt pp1;
            Path.OutPt pp2;
            pp1 = this;
            do {
                pp2 = pp1.next;
                pp1.next = pp1.prev;
                pp1.prev = pp2;
                pp1 = pp2;
            }
            while (pp1 != this);
        }

        public void setPt( Point pt ) {
            this.pt = pt;
        }

        private double area() {
            Path.OutPt op = this;
            double a = 0;
            do {
                a = a + (double) (op.prev.getPt().getX() + op.getPt().getX()) * (double) (op.prev.getPt().getY() - op.getPt().getY());
                op = op.next;
            } while (op != this);
            return a * 0.5;
        }
    }

    /** OutRec: contains a path in the clipping solution. Edges in the AEL will
    carry a pointer to an OutRec when they are part of the clipping solution.*/
    static class OutRec {
        int Idx;

        boolean isHole;

        boolean isOpen;
        OutRec firstLeft; //see comments in clipper.pas
        private Path.OutPt pts;
        Path.OutPt bottomPt;
        PolyNode polyNode;

        public double area() {
            return pts.area();
        }

        public void fixHoleLinkage() {
            //skip if an outermost polygon or
            //already already points to the correct FirstLeft ...
            if (firstLeft == null || isHole != firstLeft.isHole && firstLeft.pts != null) {
                return;
            }

            OutRec orfl = firstLeft;
            while (orfl != null && (orfl.isHole == isHole || orfl.pts == null)) {
                orfl = orfl.firstLeft;
            }
            firstLeft = orfl;
        }

        public Path.OutPt getPoints() {
            return pts;
        }

        public static OutRec parseFirstLeft( OutRec firstLeft ) {
            while (firstLeft != null && firstLeft.getPoints() == null) {
                firstLeft = firstLeft.firstLeft;
            }
            return firstLeft;
        }

        public void setPoints( Path.OutPt pts ) {
            this.pts = pts;
        }
    }

    private static Path.OutPt excludeOp( Path.OutPt op ) {
        final Path.OutPt result = op.prev;
        result.next = op.next;
        op.next.prev = result;
        result.idx = 0;
        return result;
    }

    /**
     *
     */
    private static final long serialVersionUID = -7120161578077546673L;

    public Path() {
        super();

    }

    public Path( int cnt ) {
        super( cnt );
    }

    public double area() {
        final int cnt = size();
        if (cnt < 3) {
            return 0;
        }
        double a = 0;
        for (int i = 0, j = cnt - 1; i < cnt; ++i) {
            a += ((double) get( j ).getX() + get( i ).getX()) * ((double) get( j ).getY() - get( i ).getY());
            j = i;
        }
        return -a * 0.5;
    }

    public Path cleanPolygon() {
        return cleanPolygon( 1.415 );
    }

    public Path cleanPolygon( double distance ) {
        //distance = proximity in units/pixels below which vertices will be stripped.
        //Default ~= sqrt(2) so when adjacent vertices or semi-adjacent vertices have
        //both x & y coords within 1 unit, then the second vertex will be stripped.

        int cnt = size();

        if (cnt == 0) {
            return new Path();
        }

        Path.OutPt[] outPts = new Path.OutPt[cnt];
        for (int i = 0; i < cnt; ++i) {
            outPts[i] = new Path.OutPt();
        }

        for (int i = 0; i < cnt; ++i) {
            outPts[i].pt = get( i );
            outPts[i].next = outPts[(i + 1) % cnt];
            outPts[i].next.prev = outPts[i];
            outPts[i].idx = 0;
        }

        final double distSqrd = distance * distance;
        Path.OutPt op = outPts[0];
        while (op.idx == 0 && op.next != op.prev) {
            if (Point.arePointsClose( op.pt, op.prev.pt, distSqrd )) {
                op = excludeOp( op );
                cnt--;
            }
            else if (Point.arePointsClose( op.prev.pt, op.next.pt, distSqrd )) {
                excludeOp( op.next );
                op = excludeOp( op );
                cnt -= 2;
            }
            else if (Point.slopesNearCollinear( op.prev.pt, op.pt, op.next.pt, distSqrd )) {
                op = excludeOp( op );
                cnt--;
            }
            else {
                op.idx = 1;
                op = op.next;
            }
        }

        if (cnt < 3) {
            cnt = 0;
        }
        final Path result = new Path( cnt );
        for (int i = 0; i < cnt; ++i) {
            result.add( op.pt );
            op = op.next;
        }
        outPts = null;
        return result;
    }

    public int isPointInPolygon(Point pt) {
        //returns 0 if false, +1 if true, -1 if pt ON polygon boundary
        //See "The Point in Polygon Problem for Arbitrary Polygons" by Hormann & Agathos
        //http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.88.5498&rep=rep1&type=pdf
        int result = 0;
        final int cnt = size();
        if (cnt < 3) {
            return 0;
        }
        Point ip = get( 0 );
        for (int i = 1; i <= cnt; ++i) {
            final Point ipNext = i == cnt ? get( 0 ) : get( i );
            if (ipNext.getY() == pt.getY()) {
                if (ipNext.getX() == pt.getX() || ip.getY() == pt.getY() && ipNext.getX() > pt.getX() == ip.getX() < pt.getX()) {
                    return -1;
                }
            }
            if (ip.getY() < pt.getY() != ipNext.getY() < pt.getY()) {
                if (ip.getX() >= pt.getX()) {
                    if (ipNext.getX() > pt.getX()) {
                        result = 1 - result;
                    }
                    else {
                        final double d = (double) (ip.getX() - pt.getX()) * (ipNext.getY() - pt.getY()) - (double) (ipNext.getX() - pt.getX())
                                        * (ip.getY() - pt.getY());
                        if (d == 0) {
                            return -1;
                        }
                        else if (d > 0 == ipNext.getY() > ip.getY()) {
                            result = 1 - result;
                        }
                    }
                }
                else {
                    if (ipNext.getX() > pt.getX()) {
                        final double d = (double) (ip.getX() - pt.getX()) * (ipNext.getY() - pt.getY()) - (double) (ipNext.getX() - pt.getX())
                                        * (ip.getY() - pt.getY());
                        if (d == 0) {
                            return -1;
                        }
                        else if (d > 0 == ipNext.getY() > ip.getY()) {
                            result = 1 - result;
                        }
                    }
                }
            }
            ip = ipNext;
        }
        return result;
    }

    public boolean orientation() {
        return area() >= 0;
    }

    public void reverse() {
        Collections.reverse( this );
    }

    public Path TranslatePath(Point delta ) {
        final Path outPath = new Path( size() );
        for (int i = 0; i < size(); i++) {
            outPath.add( new Point( get( i ).getX() + delta.getX(), get( i ).getY() + delta.getY() ) );
        }
        return outPath;
    }
}
