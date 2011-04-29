package com.gisgraphy.geocoding;

import java.util.ArrayList;
import java.util.List;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.AddressQuery;
import com.gisgraphy.addressparser.AddressResultsDto;
import com.gisgraphy.addressparser.IAddressParserService;
import com.gisgraphy.addressparser.exception.AddressParserException;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.valueobject.FulltextResultsDto;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.SolrResponseDto;

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
