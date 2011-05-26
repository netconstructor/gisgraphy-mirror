package com.gisgraphy.geocoding;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.AddressQuery;
import com.gisgraphy.addressparser.AddressResultsDto;
import com.gisgraphy.addressparser.GeocodingLevels;
import com.gisgraphy.addressparser.IAddressParserService;
import com.gisgraphy.addressparser.exception.AddressParserException;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.StreetSearchMode;
import com.gisgraphy.domain.geoloc.service.geoloc.GeolocSearchEngine;
import com.gisgraphy.domain.geoloc.service.geoloc.IStreetSearchEngine;
import com.gisgraphy.domain.geoloc.service.geoloc.StreetSearchQuery;
import com.gisgraphy.domain.valueobject.FulltextResultsDto;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.SolrResponseDto;
import com.gisgraphy.domain.valueobject.StreetDistance;
import com.gisgraphy.domain.valueobject.StreetSearchResultsDto;
import com.gisgraphy.helper.GeolocHelper;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * Geocode internationnal address via gisgraphy services
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 * 
 */
public class GeocodingService implements IGeocodingService {

	private IAddressParserService addressParser;
	private IFullTextSearchEngine fullTextSearchEngine;
	private IStreetSearchEngine streetSearchEngine;
	public final static int ACCEPT_DISTANCE_BETWEEN_CITY_AND_STREET=30000;

	 /**
     * The logger
     */
    protected static final Logger logger = LoggerFactory
	    .getLogger(GeocodingService.class);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gisgraphy.geocoding.IGeocodingService#geocode(java.lang.String)
	 */
	public AddressResultsDto geocode(String rawAddress, String countryCode) throws GeocodingException {
		if (rawAddress == null || "".equals(rawAddress.trim())) {
			throw new GeocodingException("Can not geocode a null or empty address");
		}
		if (countryCode == null || "".equals(countryCode.trim()) || countryCode.length() != 2) {
			throw new GeocodingException("wrong countrycode : " + countryCode);
		}
		Long startTime = System.currentTimeMillis();
		AddressQuery addressQuery = new AddressQuery(rawAddress, countryCode);
		AddressResultsDto address;
		try {
			address = addressParser.execute(addressQuery);
		} catch (AddressParserException e) {
			throw new GeocodingException("An error occurs during parsing of address" + e.getMessage(), e);
		}
		if (address != null && address.getResult().size() >= 1) {
			return geocode(address.getResult().get(0), countryCode);
		} else {
			SolrResponseDto city = null;
			if (address == null) {
				city = findCityInText(rawAddress, countryCode);
			}
			if (city == null) {
				List<SolrResponseDto> streets = findStreetInText(rawAddress, countryCode);
				AddressResultsDto results = buildAddressResultDto(streets, city);
				Long endTime = System.currentTimeMillis();
				results.setQTime(endTime - startTime);
				return results;

			} else {
				StreetSearchQuery streetSearchQuery = new StreetSearchQuery(GeolocHelper.createPoint(city.getLng().floatValue(), city.getLat().floatValue()));
				streetSearchQuery.withDistanceField(false);
				streetSearchQuery.withStreetSearchMode(StreetSearchMode.FULLTEXT);
				StreetSearchResultsDto streetSearchResultsDto = streetSearchEngine.executeQuery(streetSearchQuery);
				AddressResultsDto results = buildAddressResultDtoForStreetDistance(streetSearchResultsDto.getResult(), city);
				Long endTime = System.currentTimeMillis();
				results.setQTime(endTime - startTime);
				return results;
				/*
				 * find street with fulltext optionnal if null return null else
				 * return list
				 */
			}
		}
		/*
		 * if rawAddress == null throw parseAddress() if address==null=>
		 * findcityIntext( fulltext city or subdivision with allwords required =
		 * false) if city =null fulltext street wo position else find street
		 * with fulltext optionnal if null return null else return list else
		 * geocode(Address)
		 */
		// TODO Auto-generated method stub
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gisgraphy.geocoding.IGeocodingService#geocode(com.gisgraphy.addressparser
	 * .Address)
	 */
	public AddressResultsDto geocode(Address address, String countryCode) throws GeocodingException {
		if (address == null) {
			throw new GeocodingException("Can not geocode a null address");
		}
		if (countryCode == null || "".equals(countryCode.trim()) || countryCode.length() != 2) {
			throw new GeocodingException("wrong countrycode : " + countryCode);
		}
		if (address.getStreetName()==null && address.getCity()==null && address.getZipCode()==null){
			throw new GeocodingException("city street name and zip is null, we got too less information to geocode : ");
		}
		if (isIntersection(address)){
			throw new GeocodingException("street intersection is not managed yet");
		}
		Long startTime = System.currentTimeMillis();
		if (address.getCity() == null && address.getZipCode()==null){
			List<SolrResponseDto> streets = findStreetInText(address.getStreetName(), countryCode);
			AddressResultsDto results = buildAddressResultDto(streets, null);
			Long endTime = System.currentTimeMillis();
			results.setQTime(endTime - startTime);
			return results;
		}
		/*
        		 * 	else find city (city state zip)with all word required false
        		 * 			if not null
                                 * pour toute les ville de meme nom exactement
                        		 * 				find street in fulltext around
                        		 * 		  			if null
                        * 			 				find street in contains around
							if null
								Find street in text
								
		 * 
		 */
		return null;
	}

	protected AddressResultsDto buildAddressResultDto(List<SolrResponseDto> streets, SolrResponseDto city) {
		List<Address> addresses = new ArrayList<Address>();
		Point cityLocation = null;
		if (city!=null){
			cityLocation = GeolocHelper.createPoint(city.getLng().floatValue(),city.getLat().floatValue());
		}
		if (streets != null && streets.size() > 0) {
			for (SolrResponseDto street : streets) {
				Address address = new Address();
				//todo don't add if too far from city
				Point streetLocation = GeolocHelper.createPoint(street.getLng().floatValue(),street.getLat().floatValue());
				if (cityLocation!= null && GeolocHelper.distance(streetLocation, cityLocation)>=ACCEPT_DISTANCE_BETWEEN_CITY_AND_STREET){
					logger.debug("city is too for from street, ignoring street");
					continue;
				}
				address.setLat(street.getLat());
				address.setLng(street.getLng());
				
				address.setGeocodingLevel(GeocodingLevels.STREET);
				address.setStreetName(street.getName());
				if (street.getStreet_type() != null) {
					address.setStreetType(street.getStreet_type());//TODO tell in the doc that street type can be highway or parsed street type
				}
				populateAddressFromCity(city, address);
				addresses.add(address);

			}
		} else {
			Address address = new Address();
			if (city != null) {
				//the best we can do is city
				address.setGeocodingLevel(GeocodingLevels.CITY);
				address.setLat(city.getLat());
				address.setLng(city.getLng());
				populateAddressFromCity(city, address);
				addresses.add(address);
			}
		}
		return new AddressResultsDto(addresses, 0L);
	}

	protected AddressResultsDto buildAddressResultDtoForStreetDistance(List<StreetDistance> streetDistances, SolrResponseDto city) {
		List<Address> addresses = new ArrayList<Address>();
		Point cityLocation = null;
		if (city!=null){
			cityLocation = GeolocHelper.createPoint(city.getLng().floatValue(),city.getLat().floatValue());
		}
		if (streetDistances != null && streetDistances.size() > 0) {
			for (StreetDistance streetDistance : streetDistances) {
				Address address = new Address();
				if (cityLocation!= null && GeolocHelper.distance(streetDistance.getLocation(), cityLocation)>=ACCEPT_DISTANCE_BETWEEN_CITY_AND_STREET){
					logger.debug("city is too for from street, ignoring street");
					continue;
				}
				address.setLat(streetDistance.getLat());
				address.setLng(streetDistance.getLng());
				address.setGeocodingLevel(GeocodingLevels.STREET);
				address.setStreetName(streetDistance.getName());
				if (streetDistance.getStreetType() != null) {
					address.setStreetType(streetDistance.getStreetType().name());//TODO tell in the doc that street type can be highway or parsed street type
				}
				populateAddressFromCity(city, address);
				addresses.add(address);

			}
		} else {
			Address address = new Address();
			if (city != null) {
				address.setGeocodingLevel(GeocodingLevels.CITY);
				address.setLat(city.getLat());
				address.setLng(city.getLng());
				populateAddressFromCity(city, address);
				addresses.add(address);
			}
			//the best we can do is city
		}
		return new AddressResultsDto(addresses, 0L);
	}

	private void populateAddressFromCity(SolrResponseDto city, Address address) {
		if (city!=null){
			address.setCity(city.getName());
			if (city.getAdm2_name() != null) {
				address.setState(city.getAdm2_name());
			} else if (city.getAdm1_name() != null) {
				address.setState(city.getAdm1_name());
			}
			if (city.getZipcodes() != null && city.getZipcodes().size() > 0) {
				address.setZipCode(city.getZipcodes().get(0));
			}
		}
	}

	

	private boolean isIntersection(Address address) {
		return address.getStreetNameIntersection()!=null ;
	}

	protected SolrResponseDto findCityInText(String text, String countryCode) {
		FulltextQuery query = new FulltextQuery(text, Pagination.DEFAULT_PAGINATION, Output.DEFAULT_OUTPUT, com.gisgraphy.fulltext.Constants.CITY_AND_CITYSUBDIVISION_PLACETYPE, countryCode);
		query.withAllWordsRequired(false);
		FulltextResultsDto results = fullTextSearchEngine.executeQuery(query);
		if (results.getResultsSize() >= 1) {
			return results.getResults().get(0);
		} else {
			return null;
		}
	}

	protected List<SolrResponseDto> findStreetInText(String text, String countryCode) {
		FulltextQuery query = new FulltextQuery(text, Pagination.DEFAULT_PAGINATION, Output.DEFAULT_OUTPUT, com.gisgraphy.fulltext.Constants.STREET_PLACETYPE, countryCode);
		query.withAllWordsRequired(false);
		FulltextResultsDto results = fullTextSearchEngine.executeQuery(query);
		return results.getResults();

	}

	@Autowired
	public void setAddressParser(IAddressParserService addressParser) {
		this.addressParser = addressParser;
	}

	@Autowired
	public void setFullTextSearchEngine(IFullTextSearchEngine fullTextSearchEngine) {
		this.fullTextSearchEngine = fullTextSearchEngine;
	}

	@Autowired
	public void setStreetSearchEngine(IStreetSearchEngine streetSearchEngine) {
		this.streetSearchEngine = streetSearchEngine;
	}

}
