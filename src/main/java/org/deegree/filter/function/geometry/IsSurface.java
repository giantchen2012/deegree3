//$HeadURL$
package org.deegree.filter.function.geometry;

import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.property.Property;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.custom.FunctionProvider;
import org.deegree.filter.expression.Function;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Surface;

/**
 * <code>IsSurface</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IsSurface extends Function implements FunctionProvider {

    /****/
    public IsSurface() {
        // needed for SPI
        super( "IsSurface", null );
    }

    /**
     * @param exprs
     */
    public IsSurface( List<Expression> exprs ) {
        super( "IsSurface", exprs );
        if ( exprs.size() != 1 ) {
            throw new IllegalArgumentException( "IsSurface requires exactly one parameter." );
        }
    }

    @Override
    public IsSurface create( List<Expression> params ) {
        return new IsSurface( params );
    }

    @Override
    public TypedObjectNode[] evaluate( MatchableObject f )
                            throws FilterEvaluationException {
        Object[] vals = getParams()[0].evaluate( f );

        if ( vals.length != 1 || !( vals[0] instanceof Geometry ) && !( vals[0] instanceof Property )
             && !( ( (Property) vals[0] ).getValue() instanceof Geometry ) ) {
            return new TypedObjectNode[0];
            // throw new FilterEvaluationException( "The argument to the Is*** functions must be a geometry." );
        }
        Geometry geom = vals[0] instanceof Geometry ? (Geometry) vals[0] : (Geometry) ( (Property) vals[0] ).getValue();

        // TODO is handling of multi geometries like this ok?
        boolean isSurface = geom instanceof Surface || geom instanceof MultiPolygon || geom instanceof MultiSurface;
        return new TypedObjectNode[] { new PrimitiveValue( Boolean.valueOf( isSurface ) ) };
    }
}