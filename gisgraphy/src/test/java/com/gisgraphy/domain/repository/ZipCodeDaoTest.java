package com.gisgraphy.domain.repository;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.entity.ZipCode;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.AbstractIntegrationHttpSolrTestCase;
import com.gisgraphy.test.GeolocTestHelper;


public class ZipCodeDaoTest extends AbstractIntegrationHttpSolrTestCase{
	
	
	private IZipCodeDao zipCodeDao;
	private IGisFeatureDao gisFeatureDao;
	
	@Test
	public void testGetByCodeAndCountry(){
		String code = "code1";
		String countryCode = "FR";
		ZipCode zip1 = new ZipCode(code);
		
		GisFeature gisFeature = GeolocTestHelper.createGisFeature("asciiname", 3F, 4F, 1L);
		gisFeature.setCountryCode(countryCode);
		gisFeature.addZipCode(zip1);
		gisFeatureDao.save(gisFeature);
		
		ZipCode actual = zipCodeDao.getByCodeAndCountry(code, countryCode);
		Assert.assertEquals(zip1, actual);
		actual = zipCodeDao.getByCodeAndCountry(code, "DE");
		Assert.assertNull(actual);
	}
	
	@Test
	public void testGetByCodeAndCountrySmart(){
		String code = "code1";
		String countryCode = "FR";
		
		String smartCode = "H1B";
		String smartCountryCode = "GB";
		
		ZipCode smartZip = new ZipCode(smartCode);
		ZipCode zip1 = new ZipCode(code);
		
		GisFeature gisFeature = GeolocTestHelper.createGisFeature("asciiname", 3F, 4F, 1L);
		gisFeature.setCountryCode(countryCode);
		gisFeature.addZipCode(zip1);
		gisFeatureDao.save(gisFeature);
		
		GisFeature smartGisFeature = GeolocTestHelper.createGisFeature("asciiname", 3F, 4F, 2L);
		smartGisFeature.setCountryCode(smartCountryCode);
		smartGisFeature.addZipCode(smartZip);
		gisFeatureDao.save(smartGisFeature);
		
		ZipCode actual = zipCodeDao.getByCodeAndCountrySmart(code, countryCode);
		Assert.assertEquals(zip1, actual);
		actual = zipCodeDao.getByCodeAndCountrySmart(code, "DE");
		Assert.assertNull(actual);
		
		//smart case
		 actual = zipCodeDao.getByCodeAndCountrySmart(smartCode, smartCountryCode);
		Assert.assertEquals("smart search for zipCode should handle strictly equals countrycode",smartZip, actual);
		
		 actual = zipCodeDao.getByCodeAndCountrySmart("h1b", smartCountryCode);
		 Assert.assertEquals("smart search for zipCode should be case insensitive",smartZip, actual);
		 
		 actual = zipCodeDao.getByCodeAndCountrySmart("H1B FGH", smartCountryCode);
		 Assert.assertEquals("smart search for zipCode should search for code that starts with",smartZip, actual);
	}
	
	@Test
	public void testListByCode(){
		String code = "code1";
		ZipCode zip1 = new ZipCode(code);
		ZipCode zip2 = new ZipCode(code);
		
		GisFeature gisFeature = GeolocTestHelper.createGisFeature("asciiname", 3F, 4F, 1L);
		gisFeature.addZipCode(zip1);
		gisFeatureDao.save(gisFeature);
		
		GisFeature gisFeature2 = GeolocTestHelper.createGisFeature("asciiname2", 5F, 6F, 2L);
		gisFeature2.addZipCode(zip2);
		gisFeatureDao.save(gisFeature2);
		
		List<ZipCode> actual = zipCodeDao.listByCode(code);
		Assert.assertEquals(2, actual.size());
		Assert.assertEquals(new Long(1) ,actual.get(0).getGisFeature().getFeatureId());
		Assert.assertEquals(new Long(2) ,actual.get(1).getGisFeature().getFeatureId());
		
		
	}
	
	@Test
	public void testListByCodeShouldReturnAnEmptyListWhenThereIsNoResults(){
		List<ZipCode> actual = zipCodeDao.listByCode("Nocode");
		Assert.assertNotNull(actual);
		Assert.assertEquals(0, actual.size());
	}
	

	public void setZipCodeDao(IZipCodeDao zipCodeDao) {
		this.zipCodeDao = zipCodeDao;
	}

	public void setGisFeatureDao(GisFeatureDao gisFeatureDao) {
		this.gisFeatureDao = gisFeatureDao;
	}

}
