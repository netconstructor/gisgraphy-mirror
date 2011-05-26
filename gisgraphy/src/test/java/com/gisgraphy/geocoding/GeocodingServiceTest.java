package com.gisgraphy.geocoding;

import java.util.ArrayList;
import java.util.List;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.AddressQuery;
import com.gisgraphy.addressparser.AddressResultsDto;
import com.gisgraphy.addressparser.GeocodingLevels;
import com.gisgraphy.addressparser.IAddressParserService;
import com.gisgraphy.addressparser.exception.AddressParserException;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.valueobject.FulltextResultsDto;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.SolrResponseDto;
import com.gisgraphy.domain.valueobject.StreetDistance;
import com.gisgraphy.test.GeolocTestHelper;

public class GeocodingServiceTest {

    public  boolean called = false;
    
    @Test(expected = GeocodingException.class)
    public void geocodeRawAdressShouldThrowIfAddressIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = null;
	geocodingService.geocode(rawAddress, "US");
    }

    public void geocodeRawAdressShouldThrowIfAddressIsEmpty() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = " ";
	geocodingService.geocode(rawAddress, "US");
    }

    @Test(expected = GeocodingException.class)
    public void geocodeRawAdressShouldThrowIfCountryCodeIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = "t";
	geocodingService.geocode(rawAddress, null);
    }

    @Test(expected = GeocodingException.class)
    public void geocodeRawAdressShouldThrowIfCountryCodeHasenTALengthOf2() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = "t";
	geocodingService.geocode(rawAddress, "abc");
    }

    @Test
    public void geocodeRawAdressShouldThrowGeocodingExceptionWhenAddressParserExceptionOccurs() {
	GeocodingService geocodingService = new GeocodingService();
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubThrow(new AddressParserException());
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "t";
	try {
	    geocodingService.geocode(rawAddress, "ac");
	} catch (GeocodingException e) {
	    Assert.assertEquals(AddressParserException.class, e.getCause().getClass());
	}
    }
    
    @Test
    public void geocodeRawAdressShouldCallGeocodeAdressIfParsedAddressIsSuccess() {
	called=false;
	GeocodingService geocodingService = new GeocodingService(){
	    @Override
	    public AddressResultsDto geocode(Address address, String countryCode) throws GeocodingException {
	        called= true;
		return null;
	    }
	};
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	List<Address> addressList = new ArrayList<Address>(){
	    {
		add(new Address());
	    }
	};
	AddressResultsDto addressresults = new AddressResultsDto(addressList, 3L);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andReturn(addressresults);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "t";
	    geocodingService.geocode(rawAddress, "ac");
	    Assert.assertTrue(called);
    }

    @Test
    public void geocodeRawAdressShouldCallFindCityInTextIfParsedAddressIsNull() {
	called= false;
	GeocodingService geocodingService = new GeocodingService(){
	   
	    @Override
	    protected SolrResponseDto findCityInText(String text, String countryCode) {
	        called =true;
		return null;
	    }
	    
	    @Override
	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode) {
	    	return null;
	    }
	};
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "t";
	geocodingService.geocode(rawAddress, "ac");
	Assert.assertTrue(called);
    }

    @Test
    public void findCityInText() {
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	SolrResponseDto solrResponseDto = EasyMock.createNiceMock(SolrResponseDto.class);
	results.add(solrResponseDto);
	FulltextResultsDto mockResultDTO = EasyMock.createMock(FulltextResultsDto.class);
	EasyMock.expect(mockResultDTO.getResultsSize()).andReturn(1);
	EasyMock.expect(mockResultDTO.getResults()).andReturn(results);
	EasyMock.replay(mockResultDTO);

	GeocodingService geocodingService = new GeocodingService();
	String text = "toto";
	String countryCode = "FR";
	IFullTextSearchEngine mockfullFullTextSearchEngine = EasyMock.createMock(IFullTextSearchEngine.class);
	FulltextQuery query = new FulltextQuery(text, Pagination.DEFAULT_PAGINATION, Output.DEFAULT_OUTPUT, com.gisgraphy.fulltext.Constants.CITY_AND_CITYSUBDIVISION_PLACETYPE, countryCode);
	EasyMock.expect(mockfullFullTextSearchEngine.executeQuery(query)).andReturn(mockResultDTO);
	EasyMock.replay(mockfullFullTextSearchEngine);
	geocodingService.setFullTextSearchEngine(mockfullFullTextSearchEngine);

	SolrResponseDto actual = geocodingService.findCityInText(text, countryCode);
	Assert.assertEquals(solrResponseDto, actual);
	EasyMock.verify(mockfullFullTextSearchEngine);
    }
    
    @Test
    public void findStreetInText() {
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	SolrResponseDto solrResponseDto = EasyMock.createNiceMock(SolrResponseDto.class);
	results.add(solrResponseDto);
	FulltextResultsDto mockResultDTO = EasyMock.createMock(FulltextResultsDto.class);
	EasyMock.expect(mockResultDTO.getResultsSize()).andReturn(1);
	EasyMock.expect(mockResultDTO.getResults()).andReturn(results);
	EasyMock.replay(mockResultDTO);

	GeocodingService geocodingService = new GeocodingService();
	String text = "toto";
	String countryCode = "FR";
	IFullTextSearchEngine mockfullFullTextSearchEngine = EasyMock.createMock(IFullTextSearchEngine.class);
	FulltextQuery query = new FulltextQuery(text, Pagination.DEFAULT_PAGINATION, Output.DEFAULT_OUTPUT, com.gisgraphy.fulltext.Constants.STREET_PLACETYPE, countryCode);
	EasyMock.expect(mockfullFullTextSearchEngine.executeQuery(query)).andReturn(mockResultDTO);
	EasyMock.replay(mockfullFullTextSearchEngine);
	geocodingService.setFullTextSearchEngine(mockfullFullTextSearchEngine);

	List<SolrResponseDto> actual = geocodingService.findStreetInText(text, countryCode);
	Assert.assertEquals(results, actual);
	EasyMock.verify(mockfullFullTextSearchEngine);
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    	SolrResponseDto street = GeolocTestHelper.createSolrResponseDtoForStreet();
    	streets.add(street);
    	SolrResponseDto city = GeolocTestHelper.createSolrResponseDtoForCity();
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDto(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("latitude is not correct",street.getLat(), address.getLat());
    	Assert.assertEquals("longitude is not correct",street.getLng(), address.getLng());
    	Assert.assertEquals("geocoding level is not correct",GeocodingLevels.STREET, address.getGeocodingLevel());
    	Assert.assertEquals("street name is not correct",street.getName(), address.getStreetName());
    	Assert.assertEquals("street type is not correct",street.getStreet_type(), address.getStreetType());
    	Assert.assertEquals("city name is not correct",city.getName(), address.getCity());
    	Assert.assertEquals("Adm Name should be the deeper one",city.getAdm2_name(), address.getState());
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDtoWithNullCity(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    	SolrResponseDto street = GeolocTestHelper.createSolrResponseDtoForStreet();
    	streets.add(street);
    	SolrResponseDto city = null;
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDto(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("latitude is not correct",street.getLat(), address.getLat());
    	Assert.assertEquals("longitude is not correct",street.getLng(), address.getLng());
    	Assert.assertEquals("geocoding level is not correct",GeocodingLevels.STREET, address.getGeocodingLevel());
    	Assert.assertEquals("street name is not correct",street.getName(), address.getStreetName());
    	Assert.assertEquals("street type is not correct",street.getStreet_type(), address.getStreetType());
    	Assert.assertNull("city name is not correct", address.getCity());
    	Assert.assertNull("Adm Name should be the deeper one", address.getState());
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDtoWithStreetTooForFromCity(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    	SolrResponseDto street = GeolocTestHelper.createSolrResponseDtoForStreet();
    	streets.add(street);
    	SolrResponseDto city = GeolocTestHelper.createSolrResponseDtoForCityFarFarAway();
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDto(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(0, addressResultsDto.getResult().size());
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDtoWithNullStreet(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<SolrResponseDto> streets = null;
    	SolrResponseDto city = GeolocTestHelper.createSolrResponseDtoForCity();
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDto(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("latitude is not correct", city.getLat(),city.getLat());
    	Assert.assertEquals("longitude is not correct", city.getLng(),address.getLng());
    	Assert.assertEquals("geocoding level is not correct",GeocodingLevels.CITY, address.getGeocodingLevel());
    	Assert.assertNull("street name is not correct", address.getStreetName());
    	Assert.assertNull("street type is not correct", address.getStreetType());
    	Assert.assertEquals("city name is not correct",city.getName(), address.getCity());
    	Assert.assertEquals("Adm Name should be the deeper one",city.getAdm2_name(), address.getState());
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDtoWithNullStreetAndCity(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<SolrResponseDto> streets = null;
    	SolrResponseDto city = null;
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDto(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(0, addressResultsDto.getResult().size());
    }

    
    @Test
    public void buildAddressResultDtoFromStreetResponseDto(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<StreetDistance> streets = new ArrayList<StreetDistance>();
    	StreetDistance street = GeolocTestHelper.createStreetSearchDtoForStreet();
    	streets.add(street);
    	SolrResponseDto city = GeolocTestHelper.createSolrResponseDtoForCity();
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoForStreetDistance(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("latitude is not correct",street.getLat(), address.getLat());
    	Assert.assertEquals("longitude is not correct",street.getLng(), address.getLng());
    	Assert.assertEquals("geocoding level is not correct",GeocodingLevels.STREET, address.getGeocodingLevel());
    	Assert.assertEquals("street name is not correct",street.getName(), address.getStreetName());
    	Assert.assertEquals("street type is not correct",street.getStreetType().name(), address.getStreetType());
    	Assert.assertEquals("city name is not correct",city.getName(), address.getCity());
    	Assert.assertEquals("Adm Name should be the deeper one",city.getAdm2_name(), address.getState());
    }
    
    @Test
    public void buildAddressResultDtoFromStreetResponseDtoWithStreetTooFarForStreet(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<StreetDistance> streets = new ArrayList<StreetDistance>();
    	StreetDistance street = GeolocTestHelper.createStreetSearchDtoForStreet();
    	streets.add(street);
    	SolrResponseDto city = GeolocTestHelper.createSolrResponseDtoForCityFarFarAway();
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoForStreetDistance(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(0, addressResultsDto.getResult().size());
    }
    
    @Test
    public void buildAddressResultDtoFromStreetResponseDtoWithNullCity(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<StreetDistance> streets = new ArrayList<StreetDistance>();
    	StreetDistance street = GeolocTestHelper.createStreetSearchDtoForStreet();
    	streets.add(street);
    	SolrResponseDto city = null;
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoForStreetDistance(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("latitude is not correct",street.getLat(), address.getLat());
    	Assert.assertEquals("longitude is not correct",street.getLng(), address.getLng());
    	Assert.assertEquals("geocoding level is not correct",GeocodingLevels.STREET, address.getGeocodingLevel());
    	Assert.assertEquals("street name is not correct",street.getName(), address.getStreetName());
    	Assert.assertEquals("street type is not correct",street.getStreetType().name(), address.getStreetType());
    	Assert.assertNull("city name is not correct", address.getCity());
    	Assert.assertNull("Adm Name should be the deeper one", address.getState());
    }
    
    @Test
    public void buildAddressResultDtoFromStreetResponseDtoWithNullStreet(){
    	//setup
    	GeocodingService geocodingService = new GeocodingService();
    	List<StreetDistance> streets = null;
    	SolrResponseDto city = GeolocTestHelper.createSolrResponseDtoForCity();
    	//exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoForStreetDistance(streets, city);
    	
    	//verify
    	Assert.assertNotNull("qtime should not be null",addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list",addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("latitude is not correct",city.getLat(),address.getLat());
    	Assert.assertEquals("longitude is not correct", city.getLng(),address.getLng());
    	Assert.assertEquals("geocoding level is not correct", GeocodingLevels.CITY,address.getGeocodingLevel());
    	Assert.assertNull("street name is not correct",address.getStreetName());
    	Assert.assertNull("street type is not correct", address.getStreetType());
    	Assert.assertEquals("city name is not correct",city.getName(), address.getCity());
    	Assert.assertEquals("Adm Name should be the deeper one",city.getAdm2_name(), address.getState());
    }
	
    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldThrowIfAddressIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, null);
    }

    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldThrowIfCountryCodeIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, null);
    }

    @Test(expected = GeocodingException.class)
    public void geocodeAdressShouldThrowIfCountryCodeHasenTALengthOf2() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, "abc");
    }

}
