/*******************************************************************************
 *   Gisgraphy Project 
 * 
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 * 
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *   Lesser General Public License for more details.
 * 
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 * 
 *  Copyright 2008  Gisgraphy project 
 *  David Masclet <davidmasclet@gisgraphy.com>
 *  
 *  
 *******************************************************************************/
package com.gisgraphy.domain.geoloc.service.geoloc;

import static com.gisgraphy.domain.valueobject.Pagination.paginate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Test;

import com.gisgraphy.domain.geoloc.entity.City;
import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.AbstractIntegrationHttpSolrTestCase;
import com.gisgraphy.domain.repository.ICityDao;
import com.gisgraphy.domain.valueobject.Constants;
import com.gisgraphy.domain.valueobject.GeolocResultsDto;
import com.gisgraphy.domain.valueobject.GisFeatureDistance;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.Output.OutputFormat;
import com.gisgraphy.helper.FileHelper;
import com.gisgraphy.helper.GeolocHelper;
import com.gisgraphy.service.IStatsUsageService;
import com.gisgraphy.stats.StatsUsageType;
import com.gisgraphy.test.FeedChecker;
import com.gisgraphy.test.GeolocTestHelper;

public class GeolocSearchEngineTest extends AbstractIntegrationHttpSolrTestCase {

    @Resource
    IGeolocSearchEngine geolocSearchEngine;

    @Resource
    private GeolocTestHelper geolocTestHelper;

    @Resource
    ICityDao cityDao;

    @Resource
    IStatsUsageService statsUsageService;

    @Test
    public void testExecuteAndSerializeShouldSerialize() {
	City p1 = GeolocTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	// N 48° 52' 0'' 2° 20' 0'' E
	City p2 = GeolocTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	// N 44 50 0 ; 0 34 0 W
	City p3 = GeolocTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);
	// N49° 1' 0'' E 2° 28' 0''

	this.cityDao.save(p1);
	this.cityDao.save(p2);
	this.cityDao.save(p3);

	File tempDir = FileHelper.createTempDir(this.getClass()
		.getSimpleName());
	File file = new File(tempDir.getAbsolutePath()
		+ System.getProperty("file.separator") + "serializegeoloc.txt");

	Pagination pagination = paginate().from(1).to(15);
	Output output = Output.withFormat(OutputFormat.XML).withIndentation();
	GeolocQuery query = new GeolocQuery(p1.getLocation(), 1000000,
		pagination, output, GisFeature.class);
	FileOutputStream outputStream = null;
	try {
	    outputStream = new FileOutputStream(file);
	} catch (FileNotFoundException e) {
	    fail("Error when instanciate OutputStream");
	}
	geolocSearchEngine.executeAndSerialize(query, outputStream);

	String content = "";
	try {
	    content = GeolocTestHelper.readFileAsString(file.getAbsolutePath());
	} catch (IOException e) {
	    fail("can not get content of file " + file.getAbsolutePath());
	}
	FeedChecker.assertQ("The query returns incorrect values", content, "/"
		+ Constants.GEOLOCRESULTSDTO_JAXB_NAME + "/"
		+ Constants.GISFEATUREDISTANCE_JAXB_NAME + "[1]/name[.='"
		+ p1.getName() + "']", "/"
		+ Constants.GEOLOCRESULTSDTO_JAXB_NAME + "/"
		+ Constants.GISFEATUREDISTANCE_JAXB_NAME + "[2]/name[.='"
		+ p3.getName() + "']", "/"
		+ Constants.GEOLOCRESULTSDTO_JAXB_NAME + "/"
		+ Constants.GISFEATUREDISTANCE_JAXB_NAME + "[3]/name[.='"
		+ p2.getName() + "']");

	// delete temp dir
	assertTrue("the tempDir has not been deleted", GeolocTestHelper
		.DeleteNonEmptyDirectory(tempDir));

    }

    @Test
    public void testExecuteQueryToStringShouldReturnsAValidStringWithResults() {
	City p1 = GeolocTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	// N 48° 52' 0'' 2° 20' 0'' E
	City p2 = GeolocTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	// N 44 50 0 ; 0 34 0 W
	City p3 = GeolocTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);
	// N49° 1' 0'' E 2° 28' 0''

	this.cityDao.save(p1);
	this.cityDao.save(p2);
	this.cityDao.save(p3);

	Pagination pagination = paginate().from(1).to(15);
	Output output = Output.withFormat(OutputFormat.XML).withIndentation();
	GeolocQuery query = new GeolocQuery(p1.getLocation(), 1000001,
		pagination, output, City.class);
	String results = geolocSearchEngine.executeQueryToString(query);
	FeedChecker.assertQ("The query returns incorrect values", results, "/"
		+ Constants.GEOLOCRESULTSDTO_JAXB_NAME + "/"
		+ Constants.GISFEATUREDISTANCE_JAXB_NAME + "[1]/name[.='"
		+ p1.getName() + "']", "/"
		+ Constants.GEOLOCRESULTSDTO_JAXB_NAME + "/"
		+ Constants.GISFEATUREDISTANCE_JAXB_NAME + "[2]/name[.='"
		+ p3.getName() + "']", "/"
		+ Constants.GEOLOCRESULTSDTO_JAXB_NAME + "/"
		+ Constants.GISFEATUREDISTANCE_JAXB_NAME + "[3]/name[.='"
		+ p2.getName() + "']");
    }

    @Test
    public void testExecuteQueryShouldReturnsAValidDTOOrderedByDistance() {
	City p1 = GeolocTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	// N 48° 52' 0'' 2° 20' 0'' E
	City p2 = GeolocTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	// N 44 50 0 ; 0 34 0 W
	City p3 = GeolocTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);
	// N49° 1' 0'' E 2° 28' 0''

	this.cityDao.save(p1);
	this.cityDao.save(p2);
	this.cityDao.save(p3);

	Pagination pagination = paginate().from(1).to(15);
	Output output = Output.withFormat(OutputFormat.XML).withIndentation();
	GeolocQuery query = new GeolocQuery(p1.getLocation(), 1000001,
		pagination, output, City.class);
	GeolocResultsDto results = geolocSearchEngine.executeQuery(query);
	assertEquals(3, results.getResult().size());
	assertEquals(p1.getName(), results.getResult().get(0).getName());
	assertEquals(p3.getName(), results.getResult().get(1).getName());
	assertEquals(p2.getName(), results.getResult().get(2).getName());
    }

    @Test
    public void testExecuteQueryShouldReturnsAnEmptyListIfThereIsNoResults() {
	City p1 = GeolocTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);

	Pagination pagination = paginate().from(1).to(15);
	Output output = Output.withFormat(OutputFormat.XML).withIndentation();
	GeolocQuery query = new GeolocQuery(p1.getLocation(), 1000001,
		pagination, output, City.class);
	GeolocResultsDto results = geolocSearchEngine.executeQuery(query);
	assertNotNull(
		"Geoloc search engine should never return null but an empty list",
		results);
	assertEquals(0, results.getResult().size());
    }

    @Test
    public void testExecuteQueryInJSONShouldReturnValidJson() {
	City p1 = geolocTestHelper
		.createAndSaveCityWithFullAdmTreeAndCountry(10000000L);
	p1.setAdm4Code("D4");
	p1.setAdm4Name("adm");

	this.cityDao.save(p1);

	Pagination pagination = paginate().from(1).to(15);
	Output output = Output.withFormat(OutputFormat.JSON).withIndentation();
	GeolocQuery query = new GeolocQuery(p1.getLocation(), 40000,
		pagination, output, City.class);
	String results = geolocSearchEngine.executeQueryToString(query);
	GeolocResultsDto geolocResultsDto = geolocSearchEngine.executeQuery(query);
	new GisFeatureDistance(((GisFeature)p1),0D);
	FeedChecker.checkGeolocResultsDtoJSON(geolocResultsDto, results);
    }

   
    
    @Test
    public void testExecuteQueryInGEORSShouldReturnValidFeed() {
	GisFeature city = geolocTestHelper
		.createAndSaveCityWithFullAdmTreeAndCountry(1L);
	city.setAdm4Code("D4");
	city.setAdm4Name("adm");

	this.cityDao.save((City)city);

	Pagination pagination = Pagination.DEFAULT_PAGINATION;
	Output output = Output.withFormat(OutputFormat.GEORSS).withIndentation();
	GeolocQuery query = new GeolocQuery(city.getLocation(), 40000,
		pagination, output, City.class);
	GeolocResultsDto geolocResultsDto = geolocSearchEngine.executeQuery(query);
	String results = geolocSearchEngine.executeQueryToString(query);
	FeedChecker.checkGeolocResultsDtoGEORSS(geolocResultsDto, results);
    }

	
    
    @Test
    public void testExecuteQueryInATOMShouldReturnValidFeed() {
	City p1 = geolocTestHelper
		.createAndSaveCityWithFullAdmTreeAndCountry(1L);
	p1.setAdm4Code("D4");
	p1.setAdm4Name("adm");

	this.cityDao.save(p1);

	Pagination pagination = Pagination.DEFAULT_PAGINATION;
	Output output = Output.withFormat(OutputFormat.ATOM).withIndentation();
	GeolocQuery query = new GeolocQuery(p1.getLocation(), 40000,
		pagination, output, City.class);
	GeolocResultsDto geolocResultsDto = geolocSearchEngine.executeQuery(query);
	String results = geolocSearchEngine.executeQueryToString(query);
	FeedChecker.checkGeolocResultsDtoATOM(geolocResultsDto, results);
    }

   

    public void testStatsShouldBeIncreaseForAllCall() {
	statsUsageService.resetUsage(StatsUsageType.GEOLOC);
	Pagination pagination = paginate().from(1).to(15);
	Output output = Output.withFormat(OutputFormat.JSON).withIndentation();
	GeolocQuery query = new GeolocQuery(GeolocHelper.createPoint(2F, 3F),
		40000, pagination, output, City.class);
	geolocSearchEngine.executeQueryToString(query);
	assertEquals(new Long(1), statsUsageService
		.getUsage(StatsUsageType.GEOLOC));
	geolocSearchEngine.executeQuery(query);
	assertEquals(new Long(2), statsUsageService
		.getUsage(StatsUsageType.GEOLOC));
	geolocSearchEngine.executeAndSerialize(query,
		new ByteArrayOutputStream());
	assertEquals(new Long(3), statsUsageService
		.getUsage(StatsUsageType.GEOLOC));
    }

    public void testExecuteQueryWithNullQueryShouldThrows() {
	try {
	    geolocSearchEngine.executeQuery(null);
	    fail("executeQuery does not accept null query");
	} catch (IllegalArgumentException e) {

	} catch (GeolocSearchException e) {
	    fail("executequery does not accept null query and must throws an IllegalArgumentException, not GeolocServiceException");
	}
    }

    public void testExecuteQueryToStringWithNullQueryShouldThrows() {
	try {
	    geolocSearchEngine.executeQueryToString(null);
	    fail("executeQueryToString does not accept null query");
	} catch (IllegalArgumentException e) {

	} catch (GeolocSearchException e) {
	    fail("executequeryToString does not accept null query and must throws an IllegalArgumentException, not GeolocServiceException");
	}
    }

    public void testExecuteAndSerializeWithNullQueryShouldThrows() {
	try {
	    geolocSearchEngine.executeAndSerialize(null,
		    new ByteArrayOutputStream());
	    fail("executeAndSerialize does not accept null query");
	} catch (IllegalArgumentException e) {

	} catch (GeolocSearchException e) {
	    fail("executeAndSerialize does not accept null query and must throws an IllegalArgumentException, not GeolocServiceException");
	}
    }

    public void testExecuteAndSerializeWithNullOutputStreamShouldThrows() {
	try {
	    geolocSearchEngine.executeAndSerialize(new GeolocQuery(GeolocHelper
		    .createPoint(1F, 1F)), null);
	    fail("executeAndSerialize does not accept null query");
	} catch (IllegalArgumentException e) {

	} catch (GeolocSearchException e) {
	    fail("executeAndSerialize does not accept null outputStream and must throws an IllegalArgumentException, not GeolocServiceException");
	}
    }

}
