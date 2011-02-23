package com.gisgraphy.geocoding;

import java.util.List;

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
	 * @return A list of geocoded address in an {@link AddressResultsDto} or null if the address can not be parsed
	 */
	public AddressResultsDto geocode(String rawAddress);
	
	/**
	 * @param address the address to geocode
	 * @return  A list of geocoded address in an {@link AddressResultsDto} with the lat and long field
	 */
	public AddressResultsDto geocode(Address address);

}
