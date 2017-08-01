package de.lighti.clipper.console;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;

import de.lighti.clipper.Clipper;
import de.lighti.clipper.Clipper.ClipType;
import de.lighti.clipper.Clipper.PolyFillType;
import de.lighti.clipper.Clipper.PolyType;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point;

public class Program {

    static Path IntsToPolygon( int[] ints ) {
        final int len1 = ints.length / 2;
        final Path result = new Path( len1 );
        for (int i = 0; i < len1; i++) {
            result.add( new Point( ints[i * 2], ints[i * 2 + 1] ) );
        }
        return result;
    }

    static boolean LoadFromFile( String filename, Paths ppg, int dec_places ) throws IOException {
        return LoadFromFile( filename, ppg, dec_places, 0, 0 );
    }

    static boolean LoadFromFile( String filename, Paths ppg, int dec_places, int xOffset, int yOffset ) throws IOException {
        final double scaling = Math.pow( 10, dec_places );

        ppg.clear();
        if (!new File( filename ).exists()) {
            return false;
        }
        final String delimiters = ", ";
        final BufferedReader sr = new BufferedReader( new FileReader( filename ) );
        try {
            String line;
            if ((line = sr.readLine()) == null) {
                return false;
            }
            final int polyCnt = Integer.parseInt( line );
            if (polyCnt < 0) {
                return false;
            }

            for (int i = 0; i < polyCnt; i++) {
                if ((line = sr.readLine()) == null) {
                    return false;
                }
                final int vertCnt = Integer.parseInt( line );
                if (vertCnt < 0) {
                    return false;
                }
                final Path pg = new Path( vertCnt );
                ppg.add( pg );
                if (scaling > 0.999 & scaling < 1.001) {
                    for (int j = 0; j < vertCnt; j++) {
                        int x, y;
                        if ((line = sr.readLine()) == null) {
                            return false;
                        }
                        final StringTokenizer tokens = new StringTokenizer( line, delimiters );

                        if (tokens.countTokens() < 2) {
                            return false;
                        }

                        x = Integer.parseInt( tokens.nextToken() );
                        y = Integer.parseInt( tokens.nextToken() );

                        x = x + xOffset;
                        y = y + yOffset;
                        pg.add( new Point( x, y ) );
                    }
                }
                else {
                    for (int j = 0; j < vertCnt; j++) {
                        double x, y;
                        if ((line = sr.readLine()) == null) {
                            return false;
                        }
                        final StringTokenizer tokens = new StringTokenizer( line, delimiters );

                        if (tokens.countTokens() < 2) {
                            return false;
                        }
                        x = Double.parseDouble( tokens.nextToken() );
                        y = Double.parseDouble( tokens.nextToken() );

                        x = x * scaling + xOffset;
                        y = y * scaling + yOffset;
                        pg.add( new Point( (int) Math.round( x ), (int) Math.round( y ) ) );
                    }
                }
            }
            return true;
        }
        finally {
            sr.close();
        }
    }

    public static void main( String[] args ) {
        //Enforce a US locale, or the SVG data will have decimals in form ##,## in countries
        //where ',' is the decimal separator. SVG requires a '.' instead
        Locale.setDefault( Locale.US );

        if (args.length < 5) {
            final String appname = System.getProperty( "sun.java.command" );
            System.out.println( "" );
            System.out.println( "Usage:" );
            System.out.println( String.format( "  %1$s CLIPTYPE s_file c_file INPUT_DEC_PLACES SVG_SCALE [S_FILL, C_FILL]", appname ) );
            System.out.println( "  where ..." );
            System.out.println( "  CLIPTYPE = INTERSECTION|UNION|DIFFERENCE|XOR" );
            System.out.println( "  FILLMODE = NONZERO|EVENODD" );
            System.out.println( "  INPUT_DEC_PLACES = signific. decimal places for subject & clip coords." );
            System.out.println( "  SVG_SCALE = scale of SVG image as power of 10. (Fractions are accepted.)" );
            System.out.println( "  both S_FILL and C_FILL are optional. The default is EVENODD." );
            System.out.println( "Example:" );
            System.out.println( "  Intersect polygons, rnd to 4 dec places, SVG is 1/100 normal size ..." );
            System.out.println( String.format( "  %1$s INTERSECTION subj.txt clip.txt 0 0 NONZERO NONZERO", appname ) );
            return;
        }

        ClipType ct;
        switch (args[0].toUpperCase()) {
            case "INTERSECTION":
                ct = ClipType.INTERSECTION;
                break;
            case "UNION":
                ct = ClipType.UNION;
                break;
            case "DIFFERENCE":
                ct = ClipType.DIFFERENCE;
                break;
            case "XOR":
                ct = ClipType.XOR;
                break;
            default:
                System.out.println( "Error: invalid operation - " + args[0] );
                return;
        }

        final String subjFilename = args[1];
        final String clipFilename = args[2];
        if (!new File( subjFilename ).exists()) {
            System.out.println( "Error: file - " + subjFilename + " - does not exist." );
            return;
        }
        if (!new File( clipFilename ).exists()) {
            System.out.println( "Error: file - " + clipFilename + " - does not exist." );
            return;
        }

        int decimal_places = Integer.parseInt( args[3] );
        if (decimal_places < 0) {
            System.out.println( "Error: invalid number of decimal places - " + args[3] );
            return;
        }
        if (decimal_places > 8) {
            decimal_places = 8;
        }
        else if (decimal_places < 0) {
            decimal_places = 0;
        }

        double svg_scale = Double.parseDouble( args[4] );
        if (svg_scale < 0) {
            System.out.println( "Error: invalid value for SVG_SCALE - " + args[4] );
            return;
        }
        if (svg_scale < -18) {
            svg_scale = -18;
        }
        else if (svg_scale > 18) {
            svg_scale = 18;
        }
        svg_scale = Math.pow( 10, svg_scale - decimal_places );//nb: also compensate for decimal places

        PolyFillType pftSubj = PolyFillType.EVEN_ODD;
        PolyFillType pftClip = PolyFillType.EVEN_ODD;
        if (args.length > 6) {
            switch (args[5].toUpperCase()) {
                case "EVENODD":
                    pftSubj = PolyFillType.EVEN_ODD;
                    break;
                case "NONZERO":
                    pftSubj = PolyFillType.NON_ZERO;
                    break;
                default:
                    System.out.println( "Error: invalid cliptype - " + args[5] );
                    return;
            }
            switch (args[6].toUpperCase()) {
                case "EVENODD":
                    pftClip = PolyFillType.EVEN_ODD;
                    break;
                case "NONZERO":
                    pftClip = PolyFillType.NON_ZERO;
                    break;
                default:
                    System.out.println( "Error: invalid cliptype - " + args[6] );
                    return;
            }
        }
        try {
            final Paths subjs = new Paths();
            final Paths clips = new Paths();
            if (!LoadFromFile( subjFilename, subjs, decimal_places )) {
                System.out.println( "Error processing subject polygons file - " + subjFilename );
                OutputFileFormat();
                return;
            }
            if (!LoadFromFile( clipFilename, clips, decimal_places )) {
                System.out.println( "Error processing clip polygons file - " + clipFilename );
                OutputFileFormat();
                return;
            }

            System.out.println( "wait ..." );
            final DefaultClipper cp = new DefaultClipper( Clipper.STRICTLY_SIMPLE );
            cp.addPaths( subjs, PolyType.SUBJECT, true );
            cp.addPaths( clips, PolyType.CLIP, true );

            final Paths solution = new Paths();
            //Paths solution = new Paths();
            if (cp.execute( ct, solution, pftSubj, pftClip )) {
                saveToFile( "solution.txt", solution, decimal_places );

                //solution = Clipper.OffsetPolygons(solution, -4, JoinType.jtRound);

                final SVGBuilder svg = new SVGBuilder();
                svg.style.showCoords = true;
                svg.style.brushClr = new Color( 0, 0, 0x9c, 0x20 );
                svg.style.penClr = new Color( 0xd3, 0xd3, 0xda );
                svg.addPaths( subjs );
                svg.style.brushClr = new Color( 0x20, 0x9c, 0, 0 );
                svg.style.penClr = new Color( 0xff, 0xa0, 0x7a );
                svg.addPaths( clips );
                svg.style.brushClr = new Color( 0x80, 0xff, 0x9c, 0xAA );
                svg.style.penClr = new Color( 0, 0x33, 0 );
                svg.addPaths( solution );
                svg.saveToFile( "solution.svg", svg_scale );

                System.out.println( "finished!" );
            }
            else {
                System.out.println( "failed!" );
            }
        }
        catch (final IOException e) {
            System.out.println( "An error occured: " + e.getMessage() );
        }
    }

    static Path MakeRandomPolygon( Random r, int maxWidth, int maxHeight, int edgeCount ) {
        return MakeRandomPolygon( r, maxWidth, maxHeight, edgeCount, 1 );
    }

    static Path MakeRandomPolygon( Random r, int maxWidth, int maxHeight, int edgeCount, int scale ) {
        final Path result = new Path( edgeCount );
        for (int i = 0; i < edgeCount; i++) {
            result.add( new Point( r.nextInt( maxWidth ) * scale, r.nextInt( maxHeight ) * scale ) );
        }
        return result;
    }

    static void OutputFileFormat() {
        System.out.println( "The expected (text) file format is ..." );
        System.out.println( "Polygon Count" );
        System.out.println( "First polygon vertex count" );
        System.out.println( "first X, Y coordinate of first polygon" );
        System.out.println( "second X, Y coordinate of first polygon" );
        System.out.println( "etc." );
        System.out.println( "Second polygon vertex count (if there is one)" );
        System.out.println( "first X, Y coordinate of second polygon" );
        System.out.println( "second X, Y coordinate of second polygon" );
        System.out.println( "etc." );
    }

    static void saveToFile( String filename, Paths ppg, int dec_places ) throws IOException {
        final double scaling = Math.pow( 10, dec_places );
        final BufferedWriter writer = new BufferedWriter( new FileWriter( filename ) );
        try {
            writer.write( String.format( "%d%n", ppg.size() ) );
            for (final Path pg : ppg) {
                writer.write( String.format( "%d%n", pg.size() ) );
                for (final Point ip : pg) {
                    writer.write( String.format( "%.2f, %.2f%n", ip.getX() / scaling, ip.getY() / scaling ) );
                }
            }
        }
        finally {
            writer.close();
        }
    }
} //class Program

