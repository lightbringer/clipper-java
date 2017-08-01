package de.lighti.clipper.gui;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.lighti.clipper.Clipper.PolyFillType;
import de.lighti.clipper.LongRect;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point;

//a very simple class that builds an SVG file with any number of
//polygons of the specified formats ...
public class SVGBuilder {

    public class PolyInfo {
        public Path polygons;
        public StyleInfo si;
    }

    public class StyleInfo {
        public PolyFillType pft;
        public Color brushClr;
        public Color penClr;
        public double penWidth;
        public int[] dashArray;
        public boolean showCoords;

        public StyleInfo() {
            pft = PolyFillType.NON_ZERO;
            brushClr = Color.WHITE;
            dashArray = null;
            penClr = Color.BLACK;
            penWidth = 0.8;
            showCoords = false;
        }

        @Override
        public StyleInfo clone() {
            final StyleInfo si = new StyleInfo();
            si.pft = pft;
            si.brushClr = brushClr;
            si.dashArray = dashArray;
            si.penClr = penClr;
            si.penWidth = penWidth;
            si.showCoords = showCoords;
            return si;
        }
    }

    ////////////////////////////////////////////////

    ////////////////////////////////////////////////

    ////////////////////////////////////////////////
    public StyleInfo style;

    private final List<PolyInfo> PolyInfoList;

    ////////////////////////////////////////////////
    private final static String SVG_HEADER = "<?xml version=\"1.0\" standalone=\"no\"?>\n" + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\"\n"
                    + "\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n\n" + "<svg width=\"%dpx\" height=\"%dpx\" viewBox=\"0 0 %d %d\" "
                    + "version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n\n";

    private final static String SVG_PATH_FORMAT = "\"%n style=\"fill:#%s;" + " fill-opacity:%.2f; fill-rule:%s; stroke:#%s;"
                    + " stroke-opacity:%.2f; stroke-width:%.2f;\"/>%n%n";

    ////////////////////////////////////////////////

    public SVGBuilder() {
        PolyInfoList = new ArrayList<PolyInfo>();
        style = new StyleInfo();
    }

    ////////////////////////////////////////////////
    public void addPaths( Paths poly ) {
        if (poly.size() == 0) {
            return;
        }
        for (final Path p : poly) {
            final PolyInfo pi = new PolyInfo();
            pi.polygons = p;
            pi.si = style.clone();
            PolyInfoList.add( pi );
        }
    }

    public boolean saveToFile( String filename, double scale ) throws IOException {
        return SaveToFile( filename, scale, 10 );
    }

    public boolean SaveToFile( String filename ) throws IOException {
        return saveToFile( filename, 1 );
    }

    public boolean SaveToFile( String filename, double scale, int margin ) throws IOException {
        //Temporarily set the locale to US to avoid decimal confusion
        //in SVG. Apparently has to be #.##
        final Locale loc = Locale.getDefault();
        Locale.setDefault( Locale.US );

        if (scale == 0) {
            scale = 1.0;
        }
        if (margin < 0) {
            margin = 0;
        }

        //calculate the bounding rect ...
        int i = 0, j = 0;
        while (i < PolyInfoList.size()) {
            j = 0;
            while (j < PolyInfoList.get( i ).polygons.size() && PolyInfoList.get( i ).polygons.size() == 0) {
                j++;
            }
            if (j < PolyInfoList.get( i ).polygons.size()) {
                break;
            }
            i++;
        }
        if (i == PolyInfoList.size()) {
            return false;
        }
        final LongRect rec = new LongRect();
        rec.left = PolyInfoList.get( i ).polygons.get( j ).getX();
        rec.right = rec.left;
        rec.top = PolyInfoList.get( 0 ).polygons.get( j ).getY();
        rec.bottom = rec.top;

        for (; i < PolyInfoList.size(); i++) {

            for (final Point pt : PolyInfoList.get( i ).polygons) {
                if (pt.getX() < rec.left) {
                    rec.left = pt.getX();
                }
                else if (pt.getX() > rec.right) {
                    rec.right = pt.getX();
                }
                if (pt.getY() < rec.top) {
                    rec.top = pt.getY();
                }
                else if (pt.getY() > rec.bottom) {
                    rec.bottom = pt.getY();
                }
            }

        }

        rec.left = (int) (rec.left * scale);
        rec.top = (int) (rec.top * scale);
        rec.right = (int) (rec.right * scale);
        rec.bottom = (int) (rec.bottom * scale);
        final long offsetX = (long) -rec.left + margin;
        final long offsetY = (long) -rec.top + margin;

        final BufferedWriter writer = new BufferedWriter( new FileWriter( filename ) );
        try {
            writer.write( String.format( SVG_HEADER, rec.right - rec.left + margin * 2, rec.bottom - rec.top + margin * 2, rec.right - rec.left + margin * 2,
                            rec.bottom - rec.top + margin * 2 ) );

            for (final PolyInfo pi : PolyInfoList) {
                writer.write( " <path d=\"" );
                final Path p = pi.polygons;
                if (p.size() < 3) {
                    continue;
                }
                writer.write( String.format( " M %.2f %.2f", p.get( 0 ).getX() * scale + offsetX, p.get( 0 ).getY() * scale + offsetY ) );
                for (int k = 1; k < p.size(); k++) {
                    writer.write( String.format( " L %.2f %.2f", p.get( k ).getX() * scale + offsetX, p.get( k ).getY() * scale + offsetY ) );
                }
                writer.write( " z" );

                writer.write( String.format( SVG_PATH_FORMAT, Integer.toHexString( pi.si.brushClr.getRGB() & 0xffffff ),
                                (float) pi.si.brushClr.getAlpha() / 255, pi.si.pft == PolyFillType.EVEN_ODD ? "evenodd" : "nonzero",
                                Integer.toHexString( pi.si.penClr.getRGB() & 0xffffff ), (float) pi.si.penClr.getAlpha() / 255, pi.si.penWidth ) );

                if (pi.si.showCoords) {
                    writer.write( String.format( "<g font-family=\"Verdana\" font-size=\"11\" fill=\"black\">%n%n" ) );

                    for (final Point pt : p) {
                        final long x = (long) pt.getX();
                        final long y = (long) pt.getY();
                        writer.write( String.format( "<text x=\"%d\" y=\"%d\">%d,%d</text>\n", (int) (x * scale + offsetX), (int) (y * scale + offsetY), x, y ) );

                    }
                    writer.write( String.format( "%n" ) );

                    writer.write( String.format( "</g>%n" ) );
                }
            }
            writer.write( String.format( "</svg>%n" ) );
            writer.close();
            return true;

        }
        finally {
            //Reset locale
            Locale.setDefault( loc );
            writer.close();
        }
    }
}