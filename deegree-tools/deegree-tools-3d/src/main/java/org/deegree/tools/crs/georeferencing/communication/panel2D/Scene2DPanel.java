//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tools.crs.georeferencing.communication.panel2D;

import static java.awt.Cursor.getPredefinedCursor;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.deegree.geometry.primitive.Ring;
import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.RowColumn;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;

/**
 * The JPanel that should display a BufferedImage.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DPanel extends AbstractPanel2D {

    private static final long serialVersionUID = -7422188293141335837L;

    public final static String SCENE2D_PANEL_NAME = "Scene2DPanel";

    private List<Polygon> polygonList;

    private Rectangle imageDimension;

    private List<Ring> worldPolygonList;

    private ArrayList<Polygon> polygonListTranslated;

    ApplicationState state;

    public Scene2DPanel( ApplicationState state ) {
        this.state = state;
        this.setName( SCENE2D_PANEL_NAME );
        this.selectedPoints = new ArrayList<Point4Values>();
    }

    @Override
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        final Graphics2D g2 = (Graphics2D) g;
        if ( state.mapController != null ) {
            if ( state.mapController.needsRepaint() && !state.previewing ) {
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {
                        Component glassPane = ( (JFrame) getTopLevelAncestor() ).getGlassPane();
                        MouseAdapter mouseAdapter = new MouseAdapter() {
                            // else the wait cursor will not appear
                        };
                        glassPane.addMouseListener( mouseAdapter );
                        glassPane.setCursor( getPredefinedCursor( Cursor.WAIT_CURSOR ) );
                        glassPane.setVisible( true );
                        state.mapController.paintMap( g2, state.previewing );
                        glassPane.removeMouseListener( mouseAdapter );
                        glassPane.setCursor( getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
                        glassPane.setVisible( false );
                    }
                } );
            } else {
                state.mapController.paintMap( g2, state.previewing );
            }
        }

        if ( lastAbstractPoint != null ) {
            g2.fillOval( new Double( lastAbstractPoint.getNewValue().x ).intValue() - selectedPointSize,
                         new Double( lastAbstractPoint.getNewValue().y ).intValue() - selectedPointSize,
                         selectedPointSize * 2, selectedPointSize * 2 );
        }

        if ( polygonList != null ) {
            for ( Polygon polygon : polygonList ) {
                g2.drawPolygon( polygon );
            }
        }

        if ( selectedPoints != null ) {
            for ( Point4Values point : selectedPoints ) {
                g2.fillOval( new Double( point.getNewValue().x ).intValue() - selectedPointSize,
                             new Double( point.getNewValue().y ).intValue() - selectedPointSize, selectedPointSize * 2,
                             selectedPointSize * 2 );
            }
        }
    }

    public Rectangle getImageDimension() {
        return imageDimension;
    }

    public void setImageDimension( Rectangle imageDimension ) {
        this.imageDimension = imageDimension;
    }

    private void updateSelectedPoints( Scene2DValues sceneValues ) {
        List<Point4Values> selectedPointsTemp = new ArrayList<Point4Values>();
        for ( Point4Values p : selectedPoints ) {
            int[] pValues = sceneValues.getPixelCoord( p.getWorldCoords() );
            double x = pValues[0];
            double y = pValues[1];
            GeoReferencedPoint pi = new GeoReferencedPoint( x, y );
            selectedPointsTemp.add( new Point4Values( pi, p.getInitialValue(), pi, p.getWorldCoords(), p.getRc() ) );
        }
        selectedPoints.clear();
        selectedPoints.addAll( selectedPointsTemp );
        if ( lastAbstractPoint != null ) {
            AbstractGRPoint worldCoords = lastAbstractPoint.getWorldCoords();
            AbstractGRPoint initialValue = lastAbstractPoint.getInitialValue();
            RowColumn rc = lastAbstractPoint.getRc();
            int[] p = sceneValues.getPixelCoord( worldCoords );
            double x = p[0];
            double y = p[1];

            GeoReferencedPoint pi = new GeoReferencedPoint( x, y );
            lastAbstractPoint = new Point4Values( pi, initialValue, pi, worldCoords, rc );
        }

    }

    @Override
    public void updatePoints( Scene2DValues sceneValues ) {
        if ( worldPolygonList != null ) {

            setPolygonList( worldPolygonList, sceneValues );
        }
        updateSelectedPoints( sceneValues );

    }

    @Override
    public void setPolygonList( List<Ring> polygonRing, Scene2DValues sceneValues ) {
        if ( polygonRing != null ) {
            this.worldPolygonList = polygonRing;
            polygonListTranslated = new ArrayList<Polygon>();

            int sizeOfPoints = 0;
            for ( Ring p : polygonRing ) {
                sizeOfPoints += p.getControlPoints().size();

            }
            for ( Ring ring : polygonRing ) {
                int[] x2 = new int[ring.getControlPoints().size()];
                int[] y2 = new int[ring.getControlPoints().size()];
                for ( int i = 0; i < ring.getControlPoints().size(); i++ ) {
                    double x = ring.getControlPoints().getX( i );
                    double y = ring.getControlPoints().getY( i );
                    int[] p = sceneValues.getPixelCoord( new GeoReferencedPoint( x, y ) );
                    x2[i] = new Double( p[0] ).intValue();
                    y2[i] = new Double( p[1] ).intValue();

                }
                Polygon p = new Polygon( x2, y2, ring.getControlPoints().size() );
                polygonListTranslated.add( p );

            }

            this.polygonList = polygonListTranslated;
        } else {
            this.polygonList = null;
            this.worldPolygonList = null;
        }

    }

    public List<Ring> getWorldPolygonList() {
        return worldPolygonList;
    }

}