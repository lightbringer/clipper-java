package de.lighti.clipper.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import javax.swing.JPanel;

import de.lighti.clipper.Clipper;
import de.lighti.clipper.Clipper.ClipType;
import de.lighti.clipper.Clipper.EndType;
import de.lighti.clipper.Clipper.JoinType;
import de.lighti.clipper.Clipper.PolyFillType;
import de.lighti.clipper.Clipper.PolyType;
import de.lighti.clipper.ClipperOffset;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;

public class PolygonCanvas extends JPanel {

    private final Paths subjects = new Paths();

    private final Paths clips = new Paths();

    private final Paths solution = new Paths();

    /**
     *
     */
    private static final long serialVersionUID = 3171660079906158448L;

    private final static int MARGIN = 10;

    private ClipType clipType = ClipType.INTERSECTION;

    private PolyFillType fillType = PolyFillType.EVEN_ODD;
    private float zoom = 0.000001f;
    private int vertexCount = ClipperDialog.DEFAULT_VERTEX_COUNT;

    private final StatusBar statusBar;
    private float offset;

    private Point origin;
    private final Point cur;

    public PolygonCanvas( StatusBar statusBar ) {
        setBackground( Color.WHITE );
        this.statusBar = statusBar;
        zoom = 1f;
        origin = new Point();
        cur = new Point();

        addMouseWheelListener( e -> {

            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                zoom += zoom * e.getUnitsToScroll() / 10f;
                statusBar.setText( "Zoomlevel: " + zoom );
                PolygonCanvas.this.repaint();
            }

        } );
        addMouseListener( new MouseAdapter() {

            @Override
            public void mousePressed( MouseEvent e ) {
                origin = e.getPoint();
            }
        } );
        addMouseMotionListener( new MouseAdapter() {

            @Override
            public void mouseDragged( MouseEvent e ) {
                final Point p = e.getPoint();

                cur.x = p.x;
                cur.y = p.y;
                repaint();
            }
        } );
    }

    private Polygon createPolygonFromPath( Path p ) {
        final int[] x = new int[p.size()];
        final int[] y = new int[p.size()];

        for (int i = p.size() - 1; i >= 0; i--) {
            final de.lighti.clipper.Point ip = p.get( i );
            x[i] = (int) (ip.getX() * zoom) + cur.x - origin.x;
            y[i] = (int) (ip.getY() * zoom) + cur.y - origin.y;
        }

        return new Polygon( x, y, p.size() );
    }

    public void generateAustPlusRandomEllipses() {
        final int scale = 1;
        subjects.clear();
        //load map of Australia from resource ...
        LittleEndianDataInputStream polyStream = null;
        try {

            polyStream = new LittleEndianDataInputStream( new FileInputStream( "aust.bin" ) );

            final int polyCnt = polyStream.readInt();
            for (int i = 0; i < polyCnt; ++i) {
                final int vertCnt = polyStream.readInt();
                final Path pg = new Path( vertCnt );
                for (int j = 0; j < vertCnt; ++j) {
                    final float x = polyStream.readFloat() * scale;
                    final float y = polyStream.readFloat() * scale;
                    pg.add( new de.lighti.clipper.Point( (int) x, (int) y ) );
                }
                subjects.add( pg );
            }
        }
        catch (final IOException e) {
            statusBar.setText( "Error: " + e.getMessage() );
        }
        finally {
            try {
                if (polyStream != null) {
                    polyStream.close();
                }
            }
            catch (final IOException e) {
                statusBar.setText( "Error: " + e.getMessage() );
            }
        }
        clips.clear();
        final Random rand = new Random();

        final int ellipse_size = 100, margin = 10;
        for (int i = 0; i < vertexCount; ++i) {
            final int w = getWidth() - ellipse_size - margin * 2;
            final int h = getHeight() - ellipse_size - margin * 2;

            final int x = rand.nextInt( w ) + margin;
            final int y = rand.nextInt( h ) + margin;
            final int size = rand.nextInt( ellipse_size - 20 ) + 20;
            final Ellipse2D path = new Ellipse2D.Float( x, y, size, size );
            final PathIterator pit = path.getPathIterator( null, 0.1 );
            final double[] coords = new double[6];

            final Path clip = new Path();
            while (!pit.isDone()) {
                final int type = pit.currentSegment( coords );
                switch (type) {
                    case PathIterator.SEG_LINETO:
                        clip.add( new de.lighti.clipper.Point( (int) (coords[0] * scale), (int) (coords[1] * scale) ) );
                        break;
                    default:
                        break;
                }

                pit.next();
            }

            clips.add( clip );
            updateSolution();
        }

    }

    private de.lighti.clipper.Point GenerateRandomPoint(int l, int t, int r, int b, Random rand ) {
        return new de.lighti.clipper.Point( rand.nextInt( r ) + l, rand.nextInt( b ) + t );
    }

    public void generateRandomPolygon() {

        final Random rand = new Random();
        final int l = MARGIN;
        final int t = MARGIN;
        final int r = getWidth() - MARGIN;
        final int b = getHeight() - MARGIN;

        subjects.clear();
        clips.clear();

        final Path subj = new Path( vertexCount );
        for (int i = 0; i < vertexCount; ++i) {
            subj.add( GenerateRandomPoint( l, t, r, b, rand ) );
        }
        subjects.add( subj );

        final Path clip = new Path( vertexCount );
        for (int i = 0; i < vertexCount; ++i) {
            clip.add( GenerateRandomPoint( l, t, r, b, rand ) );
        }
        clips.add( clip );

        updateSolution();
    }

    public Paths getClips() {
        return clips;
    }

    public ClipType getClipType() {
        return clipType;
    }

    public PolyFillType getFillType() {
        return fillType;
    }

    public Paths getSolution() {
        return solution;
    }

    public Paths getSubjects() {
        return subjects;
    }

    @Override
    protected void paintComponent( Graphics g ) {
        super.paintComponent( g );

        for (final Path p : subjects) {
            final Polygon s = createPolygonFromPath( p );
            g.setColor( new Color( 0xC3, 0xC9, 0xCF, 196 ) );
            g.drawPolygon( s );
            g.setColor( new Color( 0xDD, 0xDD, 0xF0, 127 ) );
            g.fillPolygon( s );
        }

        for (final Path p : clips) {
            final Polygon s = createPolygonFromPath( p );

            g.setColor( new Color( 0xF9, 0xBE, 0xA6, 196 ) );
            g.drawPolygon( s );
            g.setColor( new Color( 0xFF, 0xE0, 0xE0, 127 ) );
            g.fillPolygon( s );
        }

        for (final Path p : solution) {
            final Polygon s = createPolygonFromPath( p );
            if (p.orientation()) {
                g.setColor( new Color( 0, 0x33, 0, 255 ) );
            }
            else {
                g.setColor( new Color( 0x33, 0, 0, 255 ) );
            }
            g.drawPolygon( s );
            if (p.orientation()) {
                g.setColor( new Color( 0x66, 0xEF, 0x7F, 127 ) );
            }
            else {
                g.setColor( new Color( 0x66, 0x00, 0x00, 127 ) );
            }

            g.fillPolygon( s );
        }

    }

    public void setClipType( ClipType clipType ) {
        final ClipType oldType = this.clipType;
        this.clipType = clipType;
        if (oldType != clipType) {
            updateSolution();

        }
    }

    public void setFillType( PolyFillType fillType ) {
        final PolyFillType oldType = this.fillType;
        this.fillType = fillType;
        if (oldType != fillType) {
            updateSolution();

        }
    }

    public void setOffset( float offset ) {
        this.offset = offset;
    }

    public void setPolygon( PolyType type, Paths paths ) {
        switch (type) {
            case CLIP:
                clips.clear();
                clips.addAll( paths );
                break;
            case SUBJECT:
                subjects.clear();
                subjects.addAll( paths );
                break;
            default:
                throw new IllegalStateException();

        }
        updateSolution();

    }

    public void setVertexCount( int value ) {
        vertexCount = value;

    }

    public void updateSolution() {
        solution.clear();
        if (clipType != null) {
            final DefaultClipper clp = new DefaultClipper( Clipper.STRICTLY_SIMPLE );
            clp.addPaths( subjects, PolyType.SUBJECT, true );
            clp.addPaths( clips, PolyType.CLIP, true );
            if (clp.execute( clipType, solution, fillType, fillType )) {
                if (offset > 0f) {
                    final ClipperOffset clo = new ClipperOffset();
                    clo.addPaths( solution, JoinType.ROUND, EndType.CLOSED_POLYGON );
                    clo.execute( clips, offset );
                }
                int sum = 0;
                for (final Path p : solution) {
                    sum += p.size();
                }
                statusBar.setText( "Operation successful. Solution has " + sum + " vertices." );
            }
            else {
                statusBar.setText( "Operation failed" );
            }
        }
        else {
            statusBar.setText( "Operation successful. Solution cleared" );
        }
        repaint();
    }
}
