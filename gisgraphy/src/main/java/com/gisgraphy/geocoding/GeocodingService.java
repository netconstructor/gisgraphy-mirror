package com.gisgraphy.geocoding;

import org.springframework.beans.factory.annotation.Autowired;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.AddressQuery;
import com.gisgraphy.addressparser.AddressResultsDto;
import com.gisgraphy.addressparser.IAddressParserService;
import com.gisgraphy.addressparser.exception.AddressParserException;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FullTextSearchEngine;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.StreetSearchMode;
import com.gisgraphy.domain.geoloc.service.geoloc.StreetSearchQuery;
import com.gisgraphy.domain.valueobject.FulltextResultsDto;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.SolrResponseDto;
import com.gisgraphy.domain.valueobject.Output.OutputStyle;
import com.gisgraphy.helper.GeolocHelper;
import com.gisgraphy.serializer.OutputFormat;

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
	AddressQuery addressQuery = new AddressQuery(rawAddress, countryCode);
	AddressResultsDto address;
	try {
	    address = addressParser.execute(addressQuery);
	} catch (AddressParserException e) {
	    throw new GeocodingException("An error occurs during parsing of address" + e.getMessage(), e);
	}
	if (address != null && address.getResult().size()>=1) {
	    return geocode(address.getResult().get(0), countryCode);
	} else {
	    SolrResponseDto city = null;
	    if (address == null) {
		city = findCityInText(rawAddress, countryCode);
	    }
	    if (city == null) {
		// fulltext street wo position=>not managed yet
	    } else {
		StreetSearchQuery streetSearchQuery = new StreetSearchQuery(
			GeolocHelper.createPoint(city.getLng().floatValue(),city.getLat().floatValue())
			);
		streetSearchQuery.withDistanceField(false);
		streetSearchQuery.withStreetSearchMode(StreetSearchMode.FULLTEXT);
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
	return null;
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
	/*
	 * if address==null=>throw if pobox
	 * 
	 * else if intersection find city zip+name+state si pas null else if
	 * city || zip ==null find street in fulltext else find city (city state
	 * zip) if not null pour toute les ville de meme nom exactement find
	 * street in fulltext if null if null find street in contains find
	 * street in fulltext
	 */
	// TODO Auto-generated method stub
	return null;
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

    @Autowired
    public void setAddressParser(IAddressParserService addressParser) {
	this.addressParser = addressParser;
    }

    @Autowired
    public void setFullTextSearchEngine(IFullTextSearchEngine fullTextSearchEngine) {
	this.fullTextSearchEngine = fullTextSearchEngine;
    }

}
