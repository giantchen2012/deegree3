//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wms.client;

import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.DescribeLayer;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.struct.Tree;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.ows.metadata.domain.Domain;
import org.deegree.protocol.ows.metadata.operation.DCP;
import org.deegree.protocol.ows.metadata.operation.Operation;
import org.deegree.protocol.ows.metadata.party.Address;
import org.deegree.protocol.ows.metadata.party.ContactInfo;
import org.deegree.protocol.ows.metadata.party.ResponsibleParty;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class WMSCapabilitiesAdapterTest {

    @Test
    public void testWMS130CapabilitiesHasLayerUnknown()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        assertFalse( capabilities.hasLayer( "Unknown Layer" ) );
    }

    @Test
    public void testWMS130CapabilitiesHasLayer()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        assertTrue( capabilities.hasLayer( "cite:NamedPlaces" ) );
    }

    @Test
    public void testWMS130CapabilitiesCoordinateSystem()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        LinkedList<String> coordinateSystems = capabilities.getCoordinateSystems( "cite:NamedPlaces" );
        assertEquals( 2, coordinateSystems.size() );
    }

    @Test
    public void testWMS130CapabilitiesOperationUnsupported()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        assertFalse( capabilities.isOperationSupported( DescribeLayer ) );
    }

    @Test
    public void testWMS130CapabilitiesOperationSupported()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        assertTrue( capabilities.isOperationSupported( GetMap ) );
    }

    @Test
    public void testWMS130CapabilitiesgetBoundingBox()
                            throws XMLStreamException, UnknownCRSException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        Envelope boundingBox = capabilities.getBoundingBox( "EPSG:4326", "citelayers" );
        assertNotNull( boundingBox );
        Envelope bbox = ( new GeometryFactory() ).createEnvelope( -90, -180, 90, 180, CRSManager.lookup( "EPSG:4326" ) );
        assertTrue( boundingBox.equals( bbox ) );
    }

    @Test
    public void testWMS130CapabilitiesgetLatLonBoundingBox()
                            throws XMLStreamException, UnknownCRSException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        Envelope boundingBox = capabilities.getLatLonBoundingBox( "citelayers" );
        assertNotNull( boundingBox );
        Envelope bbox = ( new GeometryFactory() ).createEnvelope( -180, -90, 180, 90, CRSManager.getCRSRef( WGS84 ) );
        assertTrue( boundingBox.equals( bbox ) );
    }

    @Test
    public void testWMS130CapabilitiesNamesLayers()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        List<String> namedLayers = capabilities.getNamedLayers();
        assertTrue( namedLayers.contains( "citelayers" ) );
        assertTrue( namedLayers.contains( "cite:Bridges" ) );
    }

    @Test
    public void testWMS130CapabilitiesLayerTree()
                            throws XMLStreamException, UnknownCRSException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        Tree<LayerMetadata> layerTree = capabilities.getLayerTree();
        LayerMetadata rootLayer = layerTree.value;
        assertNull( rootLayer.getName() );
        Description description = rootLayer.getDescription();
        assertEquals( "deegree demo WMS", description.getAbstract( null ).getString() );

        List<Tree<LayerMetadata>> children = layerTree.children;
        assertEquals( getNoOfChildrenOfRootLayer(), children.size() );
    }

    @Test
    public void testWMS130CapabilitiesFormatUnknownRequest()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        LinkedList<String> getMapFormats = capabilities.getFormats( DescribeLayer );
        assertNull( getMapFormats );
    }

    @Test
    public void testWMS111CapabilitiesFormats()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        LinkedList<String> getMapFormats = capabilities.getFormats( GetMap );
        assertEquals( 8, getMapFormats.size() );
        assertTrue( getMapFormats.contains( "image/png" ) );
        assertTrue( getMapFormats.contains( "image/bmp" ) );
    }

    @Test
    public void testWMS130CapabilitiesAddresses()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        assertEquals( getGetGetMapUrl(), capabilities.getAddress( GetMap, true ) );
        assertEquals( getPostGetMapUrl(), capabilities.getAddress( GetMap, false ) );

    }

    @Test
    public void testWMS130CapabilitiesAddressesUnknownRequest()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        assertNull( capabilities.getAddress( DescribeLayer, true ) );
    }

    @Test
    public void testWMS130CapabilitiesServiceIdentification()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        ServiceIdentification serviceIdentification = capabilities.parseServiceIdentification();

        assertEquals( "OGC:WMS", serviceIdentification.getName() );
        assertEquals( "wms reference implementation", serviceIdentification.getAbstract( null ).getString() );
        assertEquals( "WMS", serviceIdentification.getServiceType().getCode() );
        Pair<List<LanguageString>, CodeType> keywords = serviceIdentification.getKeywords().get( 0 );
        assertEquals( 2, keywords.first.size() );
        assertEquals( "deegree wms", serviceIdentification.getTitle( null ).getString() );
        assertEquals( "none", serviceIdentification.getFees() );
        assertEquals( 1, serviceIdentification.getAccessConstraints().size() );
        assertEquals( "none", serviceIdentification.getAccessConstraints().get( 0 ) );

        assertEquals( 0, serviceIdentification.getProfiles().size() );
        assertEquals( getServiceVersion(), serviceIdentification.getServiceTypeVersion().get( 0 ) );
    }


    @Ignore
    @Test
    public void testWMS130CapabilitiesServiceProvider()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        ServiceProvider serviceProvider = capabilities.parseServiceProvider();
        ResponsibleParty serviceContact = serviceProvider.getServiceContact();
        assertEquals( "Andreas Poth", serviceContact.getIndividualName() );
        assertEquals( "lat/lon", serviceContact.getOrganizationName() );
        assertEquals( "Technical Director", serviceContact.getPositionName() );
        assertNull( serviceContact.getRole() );

        ContactInfo contactInfo = serviceContact.getContactInfo();
        assertNull( contactInfo.getContactInstruction() );
        assertNull( contactInfo.getHoursOfService() );
        assertEquals( "", contactInfo.getOnlineResource().toExternalForm() );
        assertEquals( "00492281849629", contactInfo.getPhone().getFacsimile().get( 0 ) );
        assertEquals( "0049228184960", contactInfo.getPhone().getVoice().get( 0 ) );

        Address address = contactInfo.getAddress();
        assertEquals( "NRW", address.getAdministrativeArea() );
        assertEquals( "Bonn", address.getCity() );
        assertEquals( "53177", address.getPostalCode() );
        assertEquals( "Germany", address.getCountry() );
        assertEquals( "lat/lon", address.getDeliveryPoint() );
        assertEquals( "info@lat-lon.de", address.getElectronicMailAddress().get( 0 ) );
    }

    @Test
    public void testWMS130CapabilitiesLanguages()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        List<String> languages = capabilities.parseLanguages();
        assertNull( languages );
    }

    @Test
    public void testWMS130CapabilitiesOperationsMetadata()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        OperationsMetadata operationsMetadata = capabilities.parseOperationsMetadata();
        List<Operation> operations = operationsMetadata.getOperation();
        assertEquals( 4, operations.size() );

        Operation getMapOperation = operationsMetadata.getOperation( "GetMap" );

        List<URL> getUrls = getMapOperation.getGetUrls();
        assertEquals( 1, getUrls.size() );
        assertEquals( getGetGetMapUrl(), getUrls.get( 0 ).toExternalForm() );

        List<URL> postUrls = getMapOperation.getPostUrls();
        assertEquals( 1, postUrls.size() );
        assertEquals( getPostGetMapUrl(), postUrls.get( 0 ).toExternalForm() );

        List<DCP> dcps = getMapOperation.getDCPs();
        assertEquals( 1, dcps.size() );
        DCP dcp = dcps.get( 0 );

        List<Pair<URL, List<Domain>>> getEndpoints = dcp.getGetEndpoints();
        assertEquals( 1, getEndpoints.size() );
        assertEquals( getGetGetMapUrl(), getEndpoints.get( 0 ).getFirst().toExternalForm() );

        List<Pair<URL, List<Domain>>> postEndpoints = dcp.getPostEndpoints();
        assertEquals( 1, postEndpoints.size() );
        assertEquals( getPostGetMapUrl(), postEndpoints.get( 0 ).getFirst().toExternalForm() );
    }

    protected abstract String getGetGetMapUrl();

    protected abstract String getPostGetMapUrl();

    protected abstract int getNoOfChildrenOfRootLayer();

    protected abstract Version getServiceVersion();
    
    /**
     * @return the {@link WMSCapabilitiesAdapter} to test
     */
    protected abstract WMSCapabilitiesAdapter createCapabilities()
                            throws XMLStreamException;


}
