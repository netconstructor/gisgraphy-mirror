package com.gisgraphy.addressparser;

import java.io.OutputStream;


import com.gisgraphy.addressparser.exception.AddressParserException;

/**
 * 
 * do a textual annalyse of a raw address, it doesn't care if the street, city, and so on exists, it is based on address patterns 
 * of the universal postal union recommandations
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 *
 */
public interface IAddressParserService {
    
    /**
     * Execute the query. It is thread safe
     * 
     * @param query
     *                The query to execute
     * @throws AddressParserException
     *                 If an error occurred
     */
    public AddressResultsDto execute(AddressQuery query)
	    throws AddressParserException;
    
    /**
     * Execute the query and serialize the results in an {@link OutputStream}.
     * It is thread safe and can be used in a servlet container
     * 
     * @param query
     *                the query to execute
     * @param outputStream
     *                the outputstream we want to serialize in
     * @throws AddressParserException
     *                 If an error occurred
     */
    public void executeAndSerialize(AddressQuery query, OutputStream outputStream)
	    throws AddressParserException;

    /**
     * Execute the query and returns the results as String. It is thread safe
     * 
     * @param query
     *                the query to execute
     * @throws AddressParserException
     *                 If an error occurred
     */
    public String executeToString(AddressQuery query) throws AddressParserException;

}
