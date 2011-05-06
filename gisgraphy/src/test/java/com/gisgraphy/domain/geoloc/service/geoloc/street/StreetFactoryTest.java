package com.gisgraphy.domain.geoloc.service.geoloc.street;

import org.junit.Assert;
import org.junit.Test;

import com.gisgraphy.domain.geoloc.entity.OpenStreetMap;
import com.gisgraphy.domain.geoloc.entity.Street;
import com.gisgraphy.helper.GeolocHelper;
import com.vividsolutions.jts.geom.Point;


public class StreetFactoryTest {

    @Test
    public void  createShouldCreate(){
	long gid = 12345L;
	String name= "california street"; 
	Double length = 5.6D;
	boolean oneWay = true;
	StreetType streetType = StreetType.SECONDARY_LINK;
	String countryCode = "FR";
	Point location = GeolocHelper.createPoint(10.2F, 9.5F);
	OpenStreetMap openStreetMap = new OpenStreetMap();
	openStreetMap.setGid(gid);
	openStreetMap.setCountryCode(countryCode);
	openStreetMap.setName(name);
	openStreetMap.setLocation(location);
	openStreetMap.setLength(length);
	openStreetMap.setStreetType(streetType);
	openStreetMap.setOneWay(oneWay);
	
	StreetFactory factory = new StreetFactory();
	Street street = factory.create(openStreetMap);
	
	Assert.assertEquals(new Long(gid), street.getFeatureId());
	Assert.assertEquals(name, street.getName());
	Assert.assertEquals(location, street.getLocation());
	Assert.assertEquals(length, street.getLength());
	Assert.assertEquals(streetType, street.getStreetType());
	Assert.assertEquals(oneWay, street.isOneWay());
	Assert.assertEquals(countryCode, street.getCountryCode());
	
	
    }
    
}
