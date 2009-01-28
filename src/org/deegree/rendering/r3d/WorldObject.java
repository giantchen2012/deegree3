//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering.r3d;

import java.io.IOException;
import java.io.Serializable;

import org.deegree.model.geometry.Envelope;
import org.deegree.rendering.r3d.geometry.SimpleAccessGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>WorldRenderableObject</code> top level class, all data objects can be stored in a dbase.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <G>
 *            the geometry type of the quality model
 * @param <QM>
 *            the quality model type
 *
 */
public class WorldObject<G extends SimpleAccessGeometry, QM extends QualityModel<G>> implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger( WorldObject.class );

    /**
     * 
     */
    private static final long serialVersionUID = 2998719476993351372L;

    private transient String id;

    private transient String time;

    private transient Envelope bbox;

    /**
     * The quality levels of the this object
     */
    protected transient QM[] qualityLevels;

    /**
     * @param id
     *            of this object
     * @param time
     *            this object was created in the dbase
     * @param bbox
     *            of this object (may not be null)
     * @param qualityLevels
     *            this data object may render.
     */
    public WorldObject( String id, String time, Envelope bbox, QM[] qualityLevels ) {
        this.id = id;
        this.time = time;
        if ( bbox == null ) {
            throw new NullPointerException( "Bbox may not be null" );
        }
        this.bbox = bbox;
        this.qualityLevels = qualityLevels;
    }

    /**
     * @param index
     *            to get the level for
     * @return the quality model at the given index.
     */
    public QM getQualityLevel( int index ) {
        if ( index < 0 || index > qualityLevels.length ) {
            return null;
        }
        return qualityLevels[index];
    }

    /**
     * Set the model at the given quality level. If the index is out of bounds nothing will happen, if the model is
     * <code>null</code> the array at given location will be null (deleted).
     * 
     * @param index
     *            to place the model at
     * @param model
     *            to place
     */
    public void setQualityLevel( int index, QM model ) {
        if ( qualityLevels != null && model != null ) {
            if ( index > 0 || index < qualityLevels.length ) {
                qualityLevels[index] = model;
            }
        }
    }

    /**
     * @param newLevels
     *            an instantiated array of QualityModels (which may be null), if the given array is <code>null</code>
     *            nothing will happen.
     */
    protected void resetQualityLevels( QM[] newLevels ) {
        if ( newLevels != null ) {
            qualityLevels = newLevels;
        }
    }

    /**
     * @return the id
     */
    public final String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public final void setId( String id ) {
        this.id = id;
    }

    /**
     * @return the time
     */
    public final String getTime() {
        return time;
    }

    /**
     * @param time
     *            the time to set
     */
    public final void setTime( String time ) {
        this.time = time;
    }

    /**
     * @return the bbox
     */
    public final Envelope getBbox() {
        return bbox;
    }

    /**
     * @param bbox
     *            the bbox to set
     */
    public final void setBbox( Envelope bbox ) {
        this.bbox = bbox;
    }

    /**
     * Method called while serializing this object
     * 
     * @param out
     *            to write to.
     * @throws IOException
     */
    private void writeObject( java.io.ObjectOutputStream out )
                            throws IOException {
        LOG.trace( "Serializing to object stream." );
        out.writeObject( qualityLevels );
    }

    /**
     * Method called while de-serializing (instancing) this object.
     * 
     * @param in
     *            to create the methods from.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject( java.io.ObjectInputStream in )
                            throws IOException, ClassNotFoundException {
        LOG.trace( "Deserializing from object stream." );
        qualityLevels = (QM[]) in.readObject();
    }
}
