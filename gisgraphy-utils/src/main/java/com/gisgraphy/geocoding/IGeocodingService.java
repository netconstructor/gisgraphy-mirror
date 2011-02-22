package com.gisgraphy.geocoding;

import java.util.List;

import com.gisgraphy.addressparser.Address;

/**
 * 
 * Geocode internationnal address via gisgraphy services
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 *
 */
public interface IGeocodingService {
	
	/**
	 * 
	 * The address will be parsed via the address parser and the geocoded
	 * @param rawAddress an address as string
	 * @return a list of geocoded address or null if the address can not be parsed
	 */
	public List<Address> geocode(String rawAddress);
	
	/**
	 * @param address the address to geocode
	 * @return a list of geocoded address with the lat and long field
	 */
	public List<Address> geocode(Address address);

}
