package de.lighti.clipper.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.lighti.clipper.Clipper.ClipType;
import de.lighti.clipper.Clipper.PolyFillType;
import de.lighti.clipper.Clipper.PolyType;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point;

public class ClipperDialog extends JFrame {
    static boolean loadFromFile( String filename, Paths ppg, int dec_places ) throws IOException {
        return loadFromFile( filename, ppg, dec_places, 0, 0 );
    }

    static boolean loadFromFile( String filename, Paths ppg, int dec_places, long xOffset, long yOffset ) throws IOException {
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
                        long x;
                        long y;
                        if ((line = sr.readLine()) == null) {
                            return false;
                        }
                        final StringTokenizer tokens = new StringTokenizer( line, delimiters );

                        if (tokens.countTokens() < 2) {
                            return false;
                        }

                        x = Long.parseLong( tokens.nextToken() );
                        y = Long.parseLong( tokens.nextToken() );

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
        new ClipperDialog().setVisible( true );
    }

    public static int DEFAULT_VERTEX_COUNT = 5;
    private static final long serialVersionUID = 7437089068822709778L;
    private StatusBar statusStrip1;
    private JPanel panel1;
    private JPanel groupBox3;
    private JRadioButton rbNone;
    private JRadioButton rbXor;
    private JRadioButton rbDifference;
    private JRadioButton rbUnion;
    private JRadioButton rbIntersect;
    private JPanel groupBox2;
    private JRadioButton rbTest2;
    private JRadioButton rbTest1;
    private JPanel groupBox1;
    private JLabel label2;
    private JSpinner nudOffset;
    private JLabel lblCount;
    private JSpinner nudCount;
    private JRadioButton rbNonZero;
    private JRadioButton rbEvenOdd;
    private JButton bRefresh;
    private JPanel panel2;
    private PolygonCanvas pictureBox1;
    private JButton bSave;

    public ClipperDialog() {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            UIManager.getDefaults().put( "Button.showMnemonics", Boolean.TRUE );
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //Too bad ...
        }
        setJMenuBar( createMenuBar() );
        createControls();

        setDefaultCloseOperation( EXIT_ON_CLOSE );
        setPreferredSize( new Dimension( 716, 521 ) );
        setResizable( false );
        setTitle( "Clipper Java Demo" );
        pack();

    }

    private void createControls() {

        statusStrip1 = new StatusBar();
        //        this.toolStripStatusLabel1 = new System.Windows.Forms.ToolStripStatusLabel();
        panel1 = new JPanel();
        bSave = new JButton();
        groupBox3 = new JPanel();
        rbNone = new JRadioButton();
        rbXor = new JRadioButton();
        rbDifference = new JRadioButton();
        rbUnion = new JRadioButton();
        rbIntersect = new JRadioButton();
        groupBox2 = new JPanel();
        rbTest2 = new JRadioButton();
        rbTest1 = new JRadioButton();
        groupBox1 = new JPanel();
        label2 = new JLabel();
        nudOffset = new JSpinner();
        lblCount = new JLabel();
        nudCount = new JSpinner();
        rbNonZero = new JRadioButton();
        rbEvenOdd = new JRadioButton();
        bRefresh = new JButton();
        panel2 = new JPanel();
        pictureBox1 = new PolygonCanvas( statusStrip1 );

        //
        // panel1
        //

        panel1.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        panel1.add( groupBox3 );

        panel1.add( groupBox1 );
        panel1.add( groupBox2 );
        panel1.add( bRefresh );
        panel1.add( bSave );

        panel1.setPreferredSize( new Dimension( 121, 459 ) );

        //
        // bSave
        //

        bSave.setPreferredSize( new Dimension( 100, 25 ) );

        final AbstractAction bSaveAction = new AbstractAction( "Save SVG" ) {
            /**
             *
             */
            private static final long serialVersionUID = -8863563653315329743L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                final JFileChooser fc = new JFileChooser( System.getProperty( "user.dir" ) );
                final int returnVal = fc.showSaveDialog( ClipperDialog.this );

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {

                        final File file = fc.getSelectedFile();
                        final SVGBuilder svg = new SVGBuilder();
                        svg.style.brushClr = new Color( 0, 0, 0x9c, 0x20 );
                        svg.style.penClr = new Color( 0xd3, 0xd3, 0xda );
                        svg.addPaths( pictureBox1.getSubjects() );
                        svg.style.brushClr = new Color( 0x20, 0x9c, 0, 0 );
                        svg.style.penClr = new Color( 0xff, 0xa0, 0x7a );
                        svg.addPaths( pictureBox1.getClips() );
                        svg.style.brushClr = new Color( 0x80, 0xff, 0x9c, 0xAA );
                        svg.style.penClr = new Color( 0, 0x33, 0 );
                        svg.addPaths( pictureBox1.getSolution() );
                        svg.saveToFile( file.getAbsolutePath(), 1 );

                        statusStrip1.setText( "Save successful" );
                    }
                    catch (final IOException ex) {
                        statusStrip1.setText( "Error: " + ex.getMessage() );
                    }
                }

            }
        };
        bSaveAction.putValue( Action.MNEMONIC_KEY, KeyEvent.VK_A );
        bSave.setAction( bSaveAction );
        bSave.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_A, 0 ), "Save" );
        bSave.getActionMap().put( "Save", bSaveAction );

        //
        // groupBox3
        //
        groupBox3.add( rbIntersect );
        groupBox3.add( rbUnion );
        groupBox3.add( rbDifference );
        groupBox3.add( rbXor );
        groupBox3.add( rbNone );

        groupBox3.setBorder( BorderFactory.createTitledBorder( "Boolean Op" ) );
        groupBox3.setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
        groupBox3.setPreferredSize( new Dimension( 100, 135 ) );

        final ButtonGroup group3 = new ButtonGroup();
        group3.add( rbNone );
        group3.add( rbXor );
        group3.add( rbDifference );
        group3.add( rbUnion );
        group3.add( rbIntersect );

        //
        // rbNone
        //
        rbNone.setAction( new AbstractAction( "None" ) {
            private static final long serialVersionUID = 4405963373838217293L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                pictureBox1.setClipType( null );
            }
        } );
        //
        // rbXor
        //

        rbXor.setAction( new AbstractAction( "XOR" ) {
            private static final long serialVersionUID = -4865012993106866716L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                pictureBox1.setClipType( ClipType.XOR );
            }
        } );
        //
        // rbDifference
        //
        rbDifference.setAction( new AbstractAction( "Difference" ) {
            private static final long serialVersionUID = -619610168436846559L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                pictureBox1.setClipType( ClipType.DIFFERENCE );
            }
        } );
        //
        // rbUnion
        //
        rbUnion.setAction( new AbstractAction( "Union" ) {
            private static final long serialVersionUID = -8369519233115242994L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                pictureBox1.setClipType( ClipType.UNION );
            }
        } );
        //
        // rbIntersect
        //

        rbIntersect.setSelected( true );
        rbIntersect.setAction( new AbstractAction( "Intersect" ) {
            private static final long serialVersionUID = 5202593451595347999L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                pictureBox1.setClipType( ClipType.INTERSECTION );
            }
        } );

        //
        // groupBox2
        //
        groupBox2.add( rbTest1 );
        groupBox2.add( rbTest2 );

        final ButtonGroup group2 = new ButtonGroup();
        group2.add( rbTest1 );
        group2.add( rbTest2 );

        groupBox2.setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
        groupBox2.setBorder( BorderFactory.createTitledBorder( "Sample" ) );
        groupBox2.setPreferredSize( new Dimension( 100, 61 ) );
        //
        // rbTest2
        //
        rbTest2.setText( "Two" );
        //
        // rbTest1
        //
        rbTest1.setText( "One" );
        rbTest1.setSelected( true );
        //
        // groupBox1
        //
        groupBox1.setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
        groupBox1.add( rbEvenOdd );
        groupBox1.add( rbNonZero );
        groupBox1.add( lblCount );
        groupBox1.add( nudCount );
        groupBox1.add( label2 );
        groupBox1.add( nudOffset );

        final ButtonGroup group1 = new ButtonGroup();
        group1.add( rbEvenOdd );
        group1.add( rbNonZero );

        groupBox1.setBorder( BorderFactory.createTitledBorder( "Options" ) );
        groupBox1.setPreferredSize( new Dimension( 100, 159 ) );

        //
        // label2
        //
        label2.setText( "Offset:" );

        //
        // nudOffset
        nudOffset.setPreferredSize( new Dimension( 54, 20 ) );
        final SpinnerNumberModel nudOffsetModel = new SpinnerNumberModel( 0f, -10f, 10f, 1f );
        nudOffsetModel.addChangeListener( e -> pictureBox1.setOffset( nudOffsetModel.getNumber().floatValue() ) );
        nudOffset.setModel( nudOffsetModel );
        final JSpinner.NumberEditor nudOffsetEditor = (JSpinner.NumberEditor) nudOffset.getEditor();
        final DecimalFormat nudOffsetEditorFormat = nudOffsetEditor.getFormat();
        nudOffsetEditorFormat.setMinimumFractionDigits( 1 );

        //
        // lblCount
        //
        lblCount.setText( "Vertex Count:" );
        //
        // nudCount
        //

        final SpinnerNumberModel nudCountModel = new SpinnerNumberModel( DEFAULT_VERTEX_COUNT, 3, 100, 1 );
        nudCountModel.addChangeListener( e -> pictureBox1.setVertexCount( nudCountModel.getNumber().intValue() ) );
        nudCount.setModel( nudCountModel );
        final JSpinner.NumberEditor nudCountEditor = (JSpinner.NumberEditor) nudCount.getEditor();
        final DecimalFormat nudCountEditorFormat = nudCountEditor.getFormat();
        nudCountEditorFormat.setMaximumFractionDigits( 0 );
        nudCount.setPreferredSize( new Dimension( 54, 20 ) );

        //
        // rbNonZero
        //
        rbNonZero.setAction( new AbstractAction( "NonZero" ) {
            private static final long serialVersionUID = 5202593451595347999L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                pictureBox1.setFillType( PolyFillType.NON_ZERO );
            }
        } );

        //
        // rbEvenOdd
        //

        rbEvenOdd.setAction( new AbstractAction( "EvenOdd" ) {
            private static final long serialVersionUID = 5202593451595347999L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                pictureBox1.setFillType( PolyFillType.EVEN_ODD );
            }
        } );
        rbEvenOdd.setSelected( true );

        //
        // bRefresh
        //

        bRefresh.setPreferredSize( new Dimension( 100, 25 ) );
        final AbstractAction bRefreshAction = new AbstractAction( "New Sample" ) {

            /**
             *
             */
            private static final long serialVersionUID = 4405963373838217293L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                if (rbTest1.isSelected()) {
                    pictureBox1.generateRandomPolygon();
                }
                else {
                    pictureBox1.generateAustPlusRandomEllipses();
                }
            }
        };
        bRefreshAction.putValue( Action.MNEMONIC_KEY, KeyEvent.VK_N );

        bRefresh.setAction( bRefreshAction );
        bRefresh.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_N, 0 ), "Refresh" );
        bRefresh.getActionMap().put( "Refresh", bRefreshAction );

        //
        // panel2
        //
        panel2.add( pictureBox1 );
        //        this.panel2.Dock = System.Windows.Forms.DockStyle.Fill;
        panel2.setPreferredSize( new Dimension( 595, 459 ) );

        //
        // pictureBox1
        //

        pictureBox1.setPreferredSize( new Dimension( 591, 455 ) );

        //
        // Form1
        //

        final JSplitPane root = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        root.setLeftComponent( panel1 );
        root.setRightComponent( panel2 );
        root.setDividerLocation( panel1.getPreferredSize().width );
        root.setDividerSize( 1 );

        setContentPane( new JPanel( new BorderLayout() ) );
        getContentPane().add( root, BorderLayout.CENTER );
        getContentPane().add( statusStrip1, BorderLayout.SOUTH );

    }

    private JMenuBar createMenuBar() {
        final JMenuBar menubar = new JMenuBar();
        menubar.setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
        final JMenuItem loadSubjectItem = new JMenuItem( new AbstractAction( "Load Subject" ) {

            /**
             *
             */
            private static final long serialVersionUID = 5372200924672915516L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                loadFile( PolyType.SUBJECT );

            }
        } );
        menubar.add( loadSubjectItem );

        final JMenuItem loadCLipItem = new JMenuItem( new AbstractAction( "Load Clip" ) {

            /**
             *
             */
            private static final long serialVersionUID = -6723609311301727992L;

            @Override
            public void actionPerformed( ActionEvent e ) {
                loadFile( PolyType.CLIP );

            }
        } );
        menubar.add( loadCLipItem );

        return menubar;
    }

    private void loadFile( PolyType type ) {
        final JFileChooser fc = new JFileChooser( System.getProperty( "user.dir" ) );
        final int returnVal = fc.showOpenDialog( ClipperDialog.this );

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            //This is where a real application would open the file.

            try {
                final Paths paths = new Paths();
                final boolean success = loadFromFile( file.getAbsolutePath(), paths, 0 );

                if (!success) {
                    statusStrip1.setText( "Error: check file syntax" );
                }
                else {
                    pictureBox1.setPolygon( type, paths );
                    statusStrip1.setText( "File loaded successful" );
                }
            }
            catch (final IOException e) {
                statusStrip1.setText( "Error: " + e.getMessage() );
            }

        }
        else {
            statusStrip1.setText( "User cancelled" );
        }
    }
}
