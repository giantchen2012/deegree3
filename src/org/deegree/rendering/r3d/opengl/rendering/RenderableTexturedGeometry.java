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

package org.deegree.rendering.r3d.opengl.rendering;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.rendering.r3d.opengl.rendering.texture.TexturePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>RenderableTexturedGeometry</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RenderableTexturedGeometry extends RenderableGeometry {

    private final transient static Logger LOG = LoggerFactory.getLogger( RenderableTexturedGeometry.class );

    /**
     * 
     */
    private static final long serialVersionUID = -3278184950712812856L;

    private transient String texture;

    // 2D
    private transient float[] textureCoordinates;

    private transient FloatBuffer textureBuffer = null;

    /**
     * @param geometry
     * @param openGLType
     * @param vertexNormals
     * @param vertexColors
     * @param specularColor
     * @param ambientColor
     * @param diffuseColor
     * @param emmisiveColor
     * @param shininess
     * @param texture
     *            to use
     * @param textureCoordinates
     *            of this data
     */
    public RenderableTexturedGeometry( float[] geometry, int openGLType, float[] vertexNormals, byte[] vertexColors,
                                       int specularColor, int ambientColor, int diffuseColor, int emmisiveColor,
                                       float shininess, String texture, float[] textureCoordinates ) {
        super( geometry, openGLType, vertexNormals, vertexColors, specularColor, ambientColor, diffuseColor,
               emmisiveColor, shininess );
        this.texture = texture;
        this.textureCoordinates = textureCoordinates;
    }

    /**
     * @param originalGeometry
     * @param openGLType
     * @param texture
     * @param textureCoordinates
     */
    public RenderableTexturedGeometry( float[] originalGeometry, int openGLType, String texture,
                                       float[] textureCoordinates ) {
        super( originalGeometry, openGLType );
        this.texture = texture;
        this.textureCoordinates = textureCoordinates;
    }

    /**
     * Load the float buffers and enable the client state.
     * 
     * @param context
     */
    @Override
    protected void enableArrays( GL context ) {
        super.enableArrays( context );
        if ( textureCoordinates != null ) {
            LOG.trace( "Enabling array state and texture 2d" );
            context.glEnable( GL.GL_TEXTURE_2D );
            if ( textureBuffer == null ) {
                LOG.trace( "Loading texture coordinates into float buffer" );
                textureBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( textureCoordinates ) );
            }
            TexturePool.loadTexture( context, texture );
            context.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
            context.glTexCoordPointer( 2, GL.GL_FLOAT, 0, textureBuffer );
        }

    }

    /**
     * @param context
     */
    @Override
    public void disableArrays( GL context ) {
        super.disableArrays( context );
        if ( textureCoordinates != null ) {
            LOG.trace( "Disabling array state and texture 2d" );
            context.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
            context.glDisable( GL.GL_TEXTURE_2D );
        }
    }

    /**
     * @return the texture
     */
    public final String getTexture() {
        return texture;
    }

    /**
     * @param texture
     *            the texture to set
     */
    public final void setTexture( String texture ) {
        this.texture = texture;
    }

    /**
     * @return the textureCoordinates
     */
    public final float[] getTextureCoordinates() {
        return textureCoordinates;
    }

    /**
     * @param textureCoordinates
     *            the textureCoordinates to set
     */
    public final void setTextureCoordinates( float[] textureCoordinates ) {
        this.textureCoordinates = textureCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\ntextureID: " ).append( texture );
        if ( textureCoordinates != null && textureCoordinates.length > 0 ) {
            sb.append( "\ntextureCoordinates:\n" );
            for ( int i = 0; ( i + 1 ) < textureCoordinates.length; i += 2 ) {
                sb.append( textureCoordinates[i] ).append( "," ).append( textureCoordinates[i + 1] ).append( "\n" );
            }
        }
        return sb.toString();
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
        LOG.trace( "Serializing to object stream" );
        out.writeUTF( texture );
        out.writeObject( textureCoordinates );
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
        LOG.trace( "Deserializing from object stream" );
        texture = in.readUTF();
        textureCoordinates = (float[]) in.readObject();
    }
}
