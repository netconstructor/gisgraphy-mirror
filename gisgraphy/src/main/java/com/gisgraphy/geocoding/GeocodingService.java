package com.gisgraphy.geocoding;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.AddressResultsDto;
import com.gisgraphy.addressparser.IAddressParserService;

/**
 * 
 * Geocode internationnal address via gisgraphy services
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 *
 */
public class GeocodingService implements IGeocodingService {
	
	@Autowired
	IAddressParserService addressParser;

	/* (non-Javadoc)
	 * @see com.gisgraphy.geocoding.IGeocodingService#geocode(java.lang.String)
	 */
	public AddressResultsDto geocode(String rawAddress) {
		/*
		 * if rawAddress == null throw
		 * parseAddress()
		 * if address==null=>
         *		findcityIntext( fulltext city or subdivision with allwords required = false)
         *			if city =null
         *				fulltext street wo position
         *			else
         *				find street with fulltext optionnal
         *					if null
         *						return null
         *					else
         *						return list
		 * else 
		 * 		geocode(Address)
		 * 		
		 * 
		 * 
		 */
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.gisgraphy.geocoding.IGeocodingService#geocode(com.gisgraphy.addressparser.Address)
	 */
	public AddressResultsDto geocode(Address address) {
		/*
		 * if address==null=>throw
		 * if pobox
		 * 	
		 * else
		 * 	if intersection
		 * 		find city zip+name+state si pas null
		 * 	else 
		 * 		if city || zip ==null
        		 * 		find street in fulltext
        		 * 	else find city (city state zip)
        		 * 			if not null
                                        		 * pour toute les ville de meme nom exactement
                        		 * 				find street in fulltext
                        		 * 		  			if null
                        		 * 	if null 					find street in contains
                		 * 			find street in fulltext
		 * 
		 */
		// TODO Auto-generated method stub
		return null;
	}

}
