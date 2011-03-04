package com.gisgraphy.domain.geoloc.importer;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.repository.IGisFeatureDao;
import com.gisgraphy.domain.valueobject.FulltextResultsDto;
import com.gisgraphy.helper.GeolocHelper;
import com.vividsolutions.jts.geom.Point;


public class GeonamesZipCodeImporterTest {
  
    
    @Test
    public void doAFulltextSearch(){
	String queryString = "query";
	FulltextQuery fulltextQuery = new FulltextQuery(queryString);
	String countryCode="cc";
	fulltextQuery.limitToCountryCode(countryCode);
	fulltextQuery.withPlaceTypes(com.gisgraphy.fulltext.Constants.CITY_AND_CITYSUBDIVISION_PLACETYPE);
	
	IFullTextSearchEngine fullTextSearchEngine = EasyMock.createMock(IFullTextSearchEngine.class);
	EasyMock.expect(fullTextSearchEngine.executeQuery(fulltextQuery)).andReturn(new FulltextResultsDto());
	EasyMock.replay(fullTextSearchEngine);
	
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter();
	importer.setFullTextSearchEngine(fullTextSearchEngine);
	
	importer.doAFulltextSearch(queryString, countryCode);
	EasyMock.verify(fullTextSearchEngine);
    }
    
    @Test
    public void initFeatureIdGenerator(){
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter();

	IGisFeatureDao gisFeatureDao = EasyMock.createMock(IGisFeatureDao.class);
	long maxFeatureId = 123456L;
	EasyMock.expect(gisFeatureDao.getMaxFeatureId()).andReturn(maxFeatureId);
	EasyMock.replay(gisFeatureDao);
	
	importer.setGisFeatureDao(gisFeatureDao);
	importer.initFeatureIdGenerator();
	
	EasyMock.verify(gisFeatureDao);
	
	Assert.assertEquals(maxFeatureId+importer.featureIdIncrement, importer.generatedFeatureId);
	
    }
    
    final StringBuffer count = new StringBuffer();
    
    @Test
    public void findFeatureWithName(){
	String lat = "3.5";
	String lng = "44";
	String accuracy = "5";
	String placeName = "place name";
	String countryCode = "FR";
	String adm1Name = "adm1name";
	String adm1Code = "adm1code";
	String adm2Name = "adm2name";
	String adm2Code = "adm2code";
	String adm3Name = "adm3name";
	String adm3COde = "adm3code";
	FulltextQuery fulltextQuery = new FulltextQuery(placeName +" "+adm1Name);
	fulltextQuery.limitToCountryCode(countryCode);
	fulltextQuery.withPlaceTypes(com.gisgraphy.fulltext.Constants.CITY_AND_CITYSUBDIVISION_PLACETYPE);
	
	
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter(){
	    
	    @Override
	    protected FulltextResultsDto doAFulltextSearch(String query, String countryCode) {
		count.append("_");
		return new FulltextResultsDto();
	    }
	};
	String[] fields = {countryCode,"post",placeName,adm1Name,adm1Code,adm2Name,adm2Code,adm3Name,adm3COde,lat,lng,accuracy};
	Point point = GeolocHelper.createPoint(new Float(lng), new Float(lat));
	int maxDistance = importer.getAccurateDistance(new Integer(accuracy));
	Assert.assertNull(importer.findFeature(fields,point,maxDistance));
	Assert.assertEquals(2,count.toString().length());
	
    }

}
