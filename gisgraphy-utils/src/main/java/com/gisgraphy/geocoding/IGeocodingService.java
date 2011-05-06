package com.gisgraphy.geocoding;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.AddressResultsDto;

/**
 * 
 * Geocode internationnal address via gisgraphy services
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 *
 */
public interface IGeocodingService {
	
	/**
	 * 
	 * parsed and geocode a raw address
	 * @param rawAddress an address as string
	 * @param countryCode the countryCode (two letters) of the address
	 * @return A list of geocoded address in an {@link AddressResultsDto} or null if the address can not be parsed
	 * @throws GeocodingException when error occurs
	 */
	public AddressResultsDto geocode(String rawAddress, String countryCode) throws GeocodingException;
	
	/**
	 * @param address the address to geocode
	 * @param countryCode the countryCode (two letters) of the address
	 * @return  A list of geocoded address in an {@link AddressResultsDto} with the lat and long field
         * @throws GeocodingException when error occurs
	 */
	public AddressResultsDto geocode(Address address, String countryCode) throws GeocodingException;

}
