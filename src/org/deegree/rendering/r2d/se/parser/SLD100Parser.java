//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.rendering.r2d.se.parser;

import static java.awt.Color.decode;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.xml.CommonNamespaces.SLDNS;
import static org.deegree.commons.xml.stax.StAXParsingHelper.asQName;
import static org.deegree.rendering.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.feature.Feature;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.se.unevaluated.Continuation.Updater;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Graphic;
import org.deegree.rendering.r2d.styling.components.Mark;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.deegree.rendering.r2d.styling.components.Mark.SimpleMark;
import org.deegree.rendering.r2d.styling.components.Stroke.LineCap;
import org.deegree.rendering.r2d.styling.components.Stroke.LineJoin;
import org.slf4j.Logger;

/**
 * <code>SLD100Parser</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SLD100Parser {

    static final Logger LOG = getLogger( SLD100Parser.class );

    // done and tested, same as SE
    private static Pair<Fill, Continuation<Fill>> parseFill( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Fill" );

        Fill base = new Fill();
        Continuation<Fill> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Fill" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "GraphicFill" ) ) {
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );
                if ( pair != null ) {
                    base.graphic = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Fill>() {
                            @Override
                            public void updateStep( Fill base, Feature f ) {
                                pair.second.evaluate( base.graphic, f );
                            }
                        };
                    }
                }
            }

            if ( in.getLocalName().endsWith( "Parameter" ) ) {
                String cssName = in.getAttributeValue( null, "name" );
                if ( cssName.equals( "fill" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Fill>() {
                        @Override
                        public void update( Fill obj, String val ) {
                            // keep alpha value
                            int alpha = obj.color.getAlpha();
                            obj.color = decode( val );
                            obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
                        }
                    }, contn );
                }

                if ( cssName.equals( "fill-opacity" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Fill>() {
                        @Override
                        public void update( Fill obj, String val ) {
                            // keep original color
                            float alpha = Float.parseFloat( val );
                            float[] cols = obj.color.getRGBColorComponents( null );
                            obj.color = new Color( cols[0], cols[1], cols[2], alpha );
                        }
                    }, contn );
                }
            }
        }

        in.require( END_ELEMENT, SLDNS, "Fill" );

        return new Pair<Fill, Continuation<Fill>>( base, contn );
    }

    // done and tested, same as SE
    private static Pair<Stroke, Continuation<Stroke>> parseStroke( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Stroke" );

        Stroke base = new Stroke();
        Continuation<Stroke> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Stroke" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().endsWith( "Parameter" ) ) {
                String name = in.getAttributeValue( null, "name" );

                if ( name.equals( "stroke" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            // keep alpha value
                            int alpha = obj.color.getAlpha();
                            obj.color = decode( val );
                            obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-opacity" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            // keep original color
                            float alpha = Float.parseFloat( val );
                            float[] cols = obj.color.getRGBColorComponents( null );
                            obj.color = new Color( cols[0], cols[1], cols[2], alpha );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-width" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.width = Double.parseDouble( val );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-linejoin" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.linejoin = LineJoin.valueOf( val.toUpperCase() );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-linecap" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.linecap = LineCap.valueOf( val.toUpperCase() );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-dasharray" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.dasharray = splitAsDoubles( val, " " );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-dashoffset" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.dashoffset = Double.parseDouble( val );
                        }
                    }, contn );
                }

                in.require( END_ELEMENT, SLDNS, null );
            }

            if ( in.getLocalName().equals( "GraphicFill" ) ) {
                in.nextTag();
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );
                if ( pair != null ) {
                    base.fill = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Stroke>() {
                            @Override
                            public void updateStep( Stroke base, Feature f ) {
                                pair.second.evaluate( base.fill, f );
                            }
                        };
                    }
                }
                in.require( END_ELEMENT, SLDNS, "Graphic" );
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "GraphicFill" );
            }

            if ( in.getLocalName().equals( "GraphicStroke" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "GraphicStroke" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "Graphic" ) ) {
                        final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );

                        if ( pair != null ) {
                            base.stroke = pair.first;
                            if ( pair.second != null ) {
                                contn = new Continuation<Stroke>() {
                                    @Override
                                    public void updateStep( Stroke base, Feature f ) {
                                        pair.second.evaluate( base.stroke, f );
                                    }
                                };
                            }
                        }

                        in.require( END_ELEMENT, SLDNS, "Graphic" );
                    }

                    if ( in.getLocalName().equals( "InitialGap" ) ) {
                        contn = updateOrContinue( in, "InitialGap", base, new Updater<Stroke>() {
                            @Override
                            public void update( Stroke obj, String val ) {
                                obj.strokeInitialGap = Double.parseDouble( val );
                            }
                        }, contn );
                        in.require( END_ELEMENT, SLDNS, "InitialGap" );
                    }

                    if ( in.getLocalName().equals( "Gap" ) ) {
                        contn = updateOrContinue( in, "Gap", base, new Updater<Stroke>() {
                            @Override
                            public void update( Stroke obj, String val ) {
                                obj.strokeGap = Double.parseDouble( val );
                            }
                        }, contn );
                        in.require( END_ELEMENT, SLDNS, "Gap" );
                    }

                }
            }
        }

        in.require( END_ELEMENT, SLDNS, "Stroke" );

        return new Pair<Stroke, Continuation<Stroke>>( base, contn );
    }

    private static Pair<Mark, Continuation<Mark>> parseMark( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Mark" );

        Mark base = new Mark();
        Continuation<Mark> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Mark" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "WellKnownName" ) ) {
                in.next();
                base.wellKnown = SimpleMark.valueOf( in.getText().toUpperCase() );
                in.nextTag();
            }

            if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fill = parseFill( in );
                base.fill = fill.first;
                if ( fill.second != null ) {
                    contn = new Continuation<Mark>() {
                        @Override
                        public void updateStep( Mark base, Feature f ) {
                            fill.second.evaluate( base.fill, f );
                        }
                    };
                }
                in.nextTag();
            }

            if ( in.getLocalName().equals( "Stroke" ) ) {
                final Pair<Stroke, Continuation<Stroke>> stroke = parseStroke( in );
                base.stroke = stroke.first;
                if ( stroke.second != null ) {
                    contn = new Continuation<Mark>() {
                        @Override
                        public void updateStep( Mark base, Feature f ) {
                            stroke.second.evaluate( base.stroke, f );
                        }
                    };
                }
                in.nextTag();
            }
        }

        in.require( END_ELEMENT, SLDNS, "Mark" );

        return new Pair<Mark, Continuation<Mark>>( base, contn );
    }

    private static BufferedImage parseExternalGraphic( XMLStreamReader in )
                            throws IOException {

        // TODO inline content
        // TODO color replacement

        // TODO in case of svg, load/render it with batik
        // String format = getNodeAsString( g, new XPath( "se:Format", nscontext ), null );

        // String str = getNodeAsString( g, new XPath( "se:OnlineResource/@xlink:href", nscontext ), null );
        // if ( str != null ) {
        // URL url = resolve( str );
        // LOG.debug( "Loading external graphic from URL {}", url );
        // return ImageIO.read( url );

        // }

        return null;
    }

    private static Pair<Graphic, Continuation<Graphic>> parseGraphic( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Graphic" );

        Graphic base = new Graphic();
        Continuation<Graphic> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Graphic" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Mark" ) ) {
                final Pair<Mark, Continuation<Mark>> pair = parseMark( in );
                in.nextTag();
                if ( pair != null ) {
                    base.mark = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Graphic>() {
                            @Override
                            public void updateStep( Graphic base, Feature f ) {
                                pair.second.evaluate( base.mark, f );
                            }
                        };
                        break;
                    }
                }
            }
            if ( in.getLocalName().equals( "ExternalGraphic" ) ) {
                try {
                    base.image = parseExternalGraphic( in );
                    if ( base.image != null ) {
                        break;
                    }
                } catch ( IOException e ) {
                    LOG.debug( "Stack trace", e );
                    LOG.warn( get( "R2D.EXTERNAL_GRAPHIC_NOT_LOADED" ),
                              new Object[] { in.getLocation().getLineNumber(), in.getLocation().getColumnNumber(),
                                            in.getLocation().getSystemId() } );
                }
            }

            contn = updateOrContinue( in, "Opacity", base, new Updater<Graphic>() {
                public void update( Graphic obj, String val ) {
                    obj.opacity = Double.parseDouble( val );
                }
            }, contn );

            contn = updateOrContinue( in, "Size", base, new Updater<Graphic>() {
                public void update( Graphic obj, String val ) {
                    obj.size = Double.parseDouble( val );
                }
            }, contn );

            contn = updateOrContinue( in, "Rotation", base, new Updater<Graphic>() {
                public void update( Graphic obj, String val ) {
                    obj.rotation = Double.parseDouble( val );
                }
            }, contn );

            contn = updateOrContinue( in, "se:AnchorPoint/se:AnchorPointX", base, new Updater<Graphic>() {
                public void update( Graphic obj, String val ) {
                    obj.anchorPointX = Double.parseDouble( val );
                }
            }, contn );

            contn = updateOrContinue( in, "se:AnchorPoint/se:AnchorPointY", base, new Updater<Graphic>() {
                public void update( Graphic obj, String val ) {
                    obj.anchorPointY = Double.parseDouble( val );
                }
            }, contn );

            contn = updateOrContinue( in, "se:Displacement/se:DisplacementX", base, new Updater<Graphic>() {
                public void update( Graphic obj, String val ) {
                    obj.displacementX = Double.parseDouble( val );
                }
            }, contn );

            contn = updateOrContinue( in, "se:Displacement/se:DisplacementY", base, new Updater<Graphic>() {
                public void update( Graphic obj, String val ) {
                    obj.displacementY = Double.parseDouble( val );
                }
            }, contn );
        }
        in.require( END_ELEMENT, SLDNS, "Graphic" );

        return new Pair<Graphic, Continuation<Graphic>>( base, contn );
    }

    public static Symbolizer<PointStyling> parsePointSymbolizer( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "PointSymbolizer" );

        QName geometry = null;
        PointStyling baseOrEvaluated = new PointStyling();

        while ( !( in.isEndElement() && in.getLocalName().equals( "PointSymbolizer" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Geometry" ) ) {
                in.next();
                geometry = asQName( in, in.getText() );
                in.next();
                in.nextTag();
            }
            if ( in.getLocalName().equals( "Graphic" ) ) {
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );

                if ( pair == null ) {
                    return new Symbolizer<PointStyling>( baseOrEvaluated, geometry, null );
                }

                baseOrEvaluated.graphic = pair.first;

                if ( pair.second != null ) {
                    return new Symbolizer<PointStyling>( baseOrEvaluated, new Continuation<PointStyling>() {
                        @Override
                        public void updateStep( PointStyling base, Feature f ) {
                            pair.second.evaluate( base.graphic, f );
                        }
                    }, geometry, null );
                }
            }
        }

        in.require( END_ELEMENT, SLDNS, "PointSymbolizer" );
        return new Symbolizer<PointStyling>( baseOrEvaluated, geometry, null );
    }

    private static <T> Continuation<T> updateOrContinue( XMLStreamReader in, String name, T obj,
                                                         final Updater<T> updater, Continuation<T> contn )
                            throws XMLStreamException {
        if ( in.getLocalName().endsWith( name ) ) {
            final LinkedList<Pair<String, Pair<Expression, String>>> text = new LinkedList<Pair<String, Pair<Expression, String>>>(); // no
            // real 'alternative', have we?
            boolean textOnly = true;
            while ( !( in.isEndElement() && in.getLocalName().endsWith( name ) ) ) {
                in.next();
                if ( in.isStartElement() ) {
                    Expression expr = null;
                    try {
                        in.nextTag();
                        expr = Filter110XMLDecoder.parseExpression( in );
                    } catch ( XMLStreamException e ) {
                        throw new XMLParsingException( in, e.getMessage() );
                    }
                    Pair<Expression, String> second;
                    second = new Pair<Expression, String>( expr, get( "R2D.LINE", in.getLocation().getLineNumber(),
                                                                      in.getLocation().getColumnNumber(),
                                                                      in.getLocation().getSystemId() ) );
                    text.add( new Pair<String, Pair<Expression, String>>( null, second ) );
                    textOnly = false;
                }
                if ( in.isCharacters() ) {
                    if ( textOnly && !text.isEmpty() ) { // concat text in case of multiple text nodes from
                        // beginning
                        String txt = text.removeLast().first;
                        text.add( new Pair<String, Pair<Expression, String>>( txt + in.getText(), null ) );
                    } else {
                        text.add( new Pair<String, Pair<Expression, String>>( in.getText(), null ) );
                    }
                }
            }
            in.require( END_ELEMENT, SLDNS, null );

            if ( textOnly ) {
                updater.update( obj, text.getFirst().first );
            } else {
                contn = new Continuation<T>( contn ) {
                    @Override
                    public void updateStep( T base, Feature f ) {
                        String tmp = "";
                        for ( Pair<String, Pair<Expression, String>> p : text ) {
                            if ( p.first != null ) {
                                tmp += p.first;
                            }
                            if ( p.second != null ) {
                                try {
                                    Object[] evald = p.second.first.evaluate( f );
                                    if ( evald.length == 0 ) {
                                        LOG.warn( get( "R2D.EXPRESSION_TO_NULL" ), p.second.second );
                                    } else {
                                        tmp += evald[0];
                                    }
                                } catch ( FilterEvaluationException e ) {
                                    LOG.warn( get( "R2D.ERROR_EVAL" ), e.getLocalizedMessage(), p.second.second );
                                }
                            }
                        }

                        updater.update( base, tmp );
                    }
                };
            }
        }

        return contn;
    }

}
