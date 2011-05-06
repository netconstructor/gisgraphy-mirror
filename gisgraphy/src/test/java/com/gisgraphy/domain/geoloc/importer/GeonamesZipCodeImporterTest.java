package com.gisgraphy.domain.geoloc.importer;

import java.util.ArrayList;
import java.util.List;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gisgraphy.domain.geoloc.entity.Adm;
import com.gisgraphy.domain.geoloc.entity.City;
import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.entity.ZipCode;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.repository.CityDao;
import com.gisgraphy.domain.repository.IAdmDao;
import com.gisgraphy.domain.repository.ICityDao;
import com.gisgraphy.domain.repository.IGisFeatureDao;
import com.gisgraphy.domain.valueobject.FulltextResultsDto;
import com.gisgraphy.domain.valueobject.GISSource;
import com.gisgraphy.domain.valueobject.SolrResponseDto;
import com.gisgraphy.helper.GeolocHelper;
import com.vividsolutions.jts.geom.Point;


public class GeonamesZipCodeImporterTest {
  
    FulltextResultsDto dtoWithTwoResults;
    FulltextResultsDto dtoWithOneResult;
    SolrResponseDto dtoTwo ;
    SolrResponseDto dtoOne ;
    
    @Before
    public void setup(){
	dtoOne = EasyMock.createMock(SolrResponseDto.class);
	EasyMock.expect(dtoOne.getFeature_id()).andStubReturn(123L);
	EasyMock.expect(dtoOne.getLat()).andStubReturn(20D);
	EasyMock.expect(dtoOne.getLng()).andStubReturn(2D);
	EasyMock.replay(dtoOne);
	
	dtoTwo = EasyMock.createMock(SolrResponseDto.class);
	EasyMock.expect(dtoTwo.getFeature_id()).andStubReturn(456L);
	EasyMock.expect(dtoTwo.getLat()).andStubReturn(34D);
	EasyMock.expect(dtoTwo.getLng()).andStubReturn(5D);
	
	EasyMock.replay(dtoTwo);
	
	List<SolrResponseDto> oneResult =new ArrayList<SolrResponseDto>();
	oneResult.add(dtoOne);
	
	List<SolrResponseDto> twoResult =new ArrayList<SolrResponseDto>();
	twoResult.add(dtoOne);
	twoResult.add(dtoTwo);
	
	dtoWithOneResult = EasyMock.createMock(FulltextResultsDto.class);
	EasyMock.expect(dtoWithOneResult.getNumFound()).andStubReturn(1L);
	EasyMock.expect(dtoWithOneResult.getResultsSize()).andStubReturn(1);
	EasyMock.expect(dtoWithOneResult.getResults()).andStubReturn(oneResult);
	EasyMock.replay(dtoWithOneResult);
	
	dtoWithTwoResults = EasyMock.createMock(FulltextResultsDto.class);
	EasyMock.expect(dtoWithTwoResults.getNumFound()).andStubReturn(2L);
	EasyMock.expect(dtoWithTwoResults.getResultsSize()).andStubReturn(2);
	EasyMock.expect(dtoWithTwoResults.getResults()).andStubReturn(twoResult);
	EasyMock.replay(dtoWithTwoResults);
	
    }
    
    
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
    public void findFeatureExtendedThenBasicWithOutResult(){
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
    
    @Test
    public void findFeatureBasicWithOneResult(){
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
		return dtoWithOneResult;
	    }
	};
	String[] fields = {countryCode,"post",placeName,adm1Name,adm1Code,adm2Name,adm2Code,adm3Name,adm3COde,lat,lng,accuracy};
	Point point = GeolocHelper.createPoint(new Float(lng), new Float(lat));
	int maxDistance = importer.getAccurateDistance(new Integer(accuracy));
	Long actualFeatureId = importer.findFeature(fields,point,maxDistance);
	Assert.assertEquals(dtoWithOneResult.getResults().get(0).getFeature_id(),actualFeatureId);
	
    }
    
    @Test
    public void findFeatureBasicWithSeveralResult(){
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
	final long featureId = 456L;
	
	
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter(){
	    
	    @Override
	    protected FulltextResultsDto doAFulltextSearch(String query, String countryCode) {
		return dtoWithTwoResults;
	    }
	    
	    @Override
	    protected Long findNearest(Point zipPoint, int maxDistance, FulltextResultsDto results) {
	        return featureId;
	    }
	};
	String[] fields = {countryCode,"post",placeName,adm1Name,adm1Code,adm2Name,adm2Code,adm3Name,adm3COde,lat,lng,accuracy};
	Point point = GeolocHelper.createPoint(new Float(lng), new Float(lat));
	int maxDistance = importer.getAccurateDistance(new Integer(accuracy));
	Long actualFeatureId = importer.findFeature(fields,point,maxDistance);
	Assert.assertEquals(new Long(featureId),actualFeatureId);
	
    }
    
    @Test
    public void findFeatureExtendedThenBasicWithOneResult(){
	
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
	final long featureId = 456L;
	
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter(){
	    int count = 0;
	    
	    @Override
	    protected FulltextResultsDto doAFulltextSearch(String query, String countryCode) {
		count = count+1;
		if (count == 1){
		    return new FulltextResultsDto();
		} else if (count==2){
		    return dtoWithOneResult;
		}
		else return null;
	    }
	    
	    @Override
	    protected Long findNearest(Point zipPoint, int maxDistance, FulltextResultsDto results) {
	        
		return featureId;
	    }
	};
	String[] fields = {countryCode,"post",placeName,adm1Name,adm1Code,adm2Name,adm2Code,adm3Name,adm3COde,lat,lng,accuracy};
	Point point = GeolocHelper.createPoint(new Float(lng), new Float(lat));
	int maxDistance = importer.getAccurateDistance(new Integer(accuracy));
	Long actualFeatureId = importer.findFeature(fields,point,maxDistance);
	Assert.assertEquals(dtoWithOneResult.getResults().get(0).getFeature_id(),actualFeatureId);
	
    }
    
    @Test
    public void findFeatureNoResultThenTwoResults(){
	
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
	final long featureId = 456L;
	
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter(){
	    int count = 0;
	    
	    @Override
	    protected FulltextResultsDto doAFulltextSearch(String query, String countryCode) {
		count = count+1;
		if (count == 1){
		    return new FulltextResultsDto();
		} else if (count==2){
		    return dtoWithTwoResults;
		}
		else return null;
	    }
	    
	    @Override
	    protected Long findNearest(Point zipPoint, int maxDistance, FulltextResultsDto results) {
	        
		return featureId;
	    }
	};
	String[] fields = {countryCode,"post",placeName,adm1Name,adm1Code,adm2Name,adm2Code,adm3Name,adm3COde,lat,lng,accuracy};
	Point point = GeolocHelper.createPoint(new Float(lng), new Float(lat));
	int maxDistance = importer.getAccurateDistance(new Integer(accuracy));
	Long actualFeatureId = importer.findFeature(fields,point,maxDistance);
	Assert.assertEquals(new Long(featureId),actualFeatureId);
	
    }
    
    @Test
    public void findFeatureNoResultThenNoResults(){
	
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
	    int count = 0;
	    
	    @Override
	    protected FulltextResultsDto doAFulltextSearch(String query, String countryCode) {
		count = count+1;
		if (count == 1){
		    return new FulltextResultsDto();
		} else if (count==2){
		    return new FulltextResultsDto();
		}
		else return null;
	    }
	    
	};
	String[] fields = {countryCode,"post",placeName,adm1Name,adm1Code,adm2Name,adm2Code,adm3Name,adm3COde,lat,lng,accuracy};
	Point point = GeolocHelper.createPoint(new Float(lng), new Float(lat));
	int maxDistance = importer.getAccurateDistance(new Integer(accuracy));
	Long actualFeatureId = importer.findFeature(fields,point,maxDistance);
	Assert.assertNull(actualFeatureId);
	
    }
    
    @Test
    public void findNearest(){
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter();
	Long FeatureId = importer.findNearest(GeolocHelper.createPoint(5F, 34F), 5, dtoWithTwoResults);
	Assert.assertEquals(dtoTwo.getFeature_id(), FeatureId);
    }
    
    @Test
    public void addAndSaveZipCodeToFeatureWithUnknowFeature(){
	Long featureId = 123456L; 
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter();
	IGisFeatureDao gisFeatureDaoMock = EasyMock.createMock(IGisFeatureDao.class);
	EasyMock.expect(gisFeatureDaoMock.getByFeatureId(featureId)).andReturn(null);
	EasyMock.replay(gisFeatureDaoMock);
	importer.setGisFeatureDao(gisFeatureDaoMock);
	Assert.assertNull(importer.addAndSaveZipCodeToFeature("code", featureId));
	EasyMock.verify(gisFeatureDaoMock);
    }
    
    @Test
    public void addAndSaveZipCodeToFeatureWithAlreadyExistingCode(){
	Long featureId = 123456L; 
	GisFeature gisFeature = new GisFeature();
	gisFeature.setFeatureId(123456L);
	gisFeature.addZipCode(new ZipCode("code"));
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter();
	IGisFeatureDao gisFeatureDaoMock = EasyMock.createMock(IGisFeatureDao.class);
	EasyMock.expect(gisFeatureDaoMock.getByFeatureId(featureId)).andReturn(gisFeature);
	EasyMock.replay(gisFeatureDaoMock);
	importer.setGisFeatureDao(gisFeatureDaoMock);
	GisFeature actual = importer.addAndSaveZipCodeToFeature("code", featureId);
	Assert.assertTrue(actual.getZipCodes().contains(new ZipCode("code")));
	Assert.assertEquals(featureId,actual.getFeatureId());
	EasyMock.verify(gisFeatureDaoMock);
    }
    
    @Test
    public void addAndSaveZipCodeToFeatureShouldAdd(){
	Long featureId = 123456L; 
	GisFeature gisFeature = new GisFeature();
	gisFeature.setFeatureId(123456L);
	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter();
	IGisFeatureDao gisFeatureDaoMock = EasyMock.createMock(IGisFeatureDao.class);
	EasyMock.expect(gisFeatureDaoMock.getByFeatureId(featureId)).andReturn(gisFeature);
	EasyMock.expect(gisFeatureDaoMock.save(gisFeature)).andReturn(gisFeature);
	EasyMock.replay(gisFeatureDaoMock);
	importer.setGisFeatureDao(gisFeatureDaoMock);
	GisFeature actual = importer.addAndSaveZipCodeToFeature("code", featureId);
	Assert.assertTrue(actual.getZipCodes().contains(new ZipCode("code")));
	Assert.assertEquals(featureId,actual.getFeatureId());
	EasyMock.verify(gisFeatureDaoMock);
    }
    
    @Test
    public void addNewEntityAndZip(){
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
    	String adm3Code = "adm3code";
    	String[] fields = {countryCode,"post",placeName,adm1Name,adm1Code,adm2Name,adm2Code,adm3Name,adm3Code,lat,lng,accuracy};
    	
    	GeonamesZipCodeImporter importer = new GeonamesZipCodeImporter();
    	long generatedId = 1234L;
	importer.generatedFeatureId=generatedId;
    	
    	
    	ICityDao cityDaoMock = EasyMock.createMock(ICityDao.class);
    	EasyMock.expect(cityDaoMock.save((City) EasyMock.anyObject())).andReturn(new City());
    	EasyMock.replay(cityDaoMock);
    	importer.setCityDao(cityDaoMock);
    	
    	IAdmDao admDaoMock = EasyMock.createMock(IAdmDao.class);
    	City mockCity = new City();
    	mockCity.setFeatureClass("P");
    	mockCity.setFeatureCode("PPL");
    	mockCity.setSource(GISSource.GEONAMES_ZIP);
    	mockCity.setName(placeName);
    	mockCity.setFeatureId(generatedId+1);
    	mockCity.setAdm1Code(adm1Code);
    	mockCity.setAdm2Code(adm2Code);
    	mockCity.setAdm3Code(adm3Code);
    	Point point = GeolocHelper.createPoint(new Float(lng), new Float(lat));
    	mockCity.setLocation(point);
    	EasyMock.expect(admDaoMock.suggestMostAccurateAdm(countryCode, adm1Code, adm2Code, adm3Code, null, mockCity)).andReturn(new Adm(3));
    	EasyMock.replay(admDaoMock);
    	importer.setAdmDao(admDaoMock);
    	
    	ImporterConfig importerConfig = new ImporterConfig();
    	importerConfig.setTryToDetectAdmIfNotFound(true);
    	importer.setImporterConfig(importerConfig);
    	
    	GisFeature city = importer.addNewEntityAndZip(fields);
    	
    	
    	
    	
    	Assert.assertEquals(new Long(generatedId+1), city.getFeatureId());
    	Assert.assertEquals(placeName, city.getName());
    	Assert.assertEquals(new Double(lng) , new Double(city.getLocation().getX()));
    	Assert.assertEquals(new Double(lat) , new Double(city.getLocation().getY()));
    	Assert.assertEquals("P", city.getFeatureClass());
    	Assert.assertEquals("PPL", city.getFeatureCode());
    	Assert.assertEquals(GISSource.GEONAMES_ZIP, city.getSource());
    	Assert.assertEquals(countryCode, city.getCountryCode());
    	Assert.assertNotNull(city.getZipCodes());
    	Assert.assertEquals(1, city.getZipCodes().size());
    	Assert.assertEquals(new ZipCode("post"), city.getZipCodes().get(0));
    	EasyMock.verify(cityDaoMock);
    	EasyMock.verify(admDaoMock);
    	
    	
    }

}
