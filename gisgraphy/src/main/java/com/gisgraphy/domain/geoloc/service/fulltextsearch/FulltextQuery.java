/*******************************************************************************
 *   Gisgraphy Project 
 * 
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 * 
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *   Lesser General Public License for more details.
 * 
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 * 
 *  Copyright 2008  Gisgraphy project 
 *  David Masclet <davidmasclet@gisgraphy.com>
 *  
 *  
 *******************************************************************************/
/**
 *
 */
package com.gisgraphy.domain.geoloc.service.fulltextsearch;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.service.AbstractGisQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.spell.SpellCheckerConfig;
import com.gisgraphy.domain.repository.ICountryDao;
import com.gisgraphy.domain.valueobject.Constants;
import com.gisgraphy.domain.valueobject.GisgraphyConfig;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.Output.OutputStyle;
import com.gisgraphy.helper.GeolocHelper;
import com.gisgraphy.serializer.OutputFormat;
import com.gisgraphy.servlet.FulltextServlet;
import com.gisgraphy.servlet.GisgraphyServlet;

/**
 * A fulltext Query
 * 
 * @see Pagination
 * @see Output
 * @see IFullTextSearchEngine
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
@Component
public class FulltextQuery extends AbstractGisQuery {
	
	protected static final String NESTED_QUERY_TEMPLATE= "_query_:\"{!dismax qf='all_name^1.1 iso_all_name^1 zipcode^1.1 all_adm1_name^0.5 all_adm2_name^0.5 all_country_name^0.5' pf=name^1.1 bf=population^2.0}%s\"";
	protected static final String NESTED_QUERY_NUMERIC_TEMPLATE="_query_:\"{!dismax qf='feature_id^1.1 all_name^1.1 iso_all_name^1 zipcode^1.1 all_adm1_name^0.5 all_adm2_name^0.5 all_country_name^0.5' pf=name^1.1 bf=population^2.0}%s\"";
    public final static int QUERY_MAX_LENGTH = 200;
    
    /**
     * The logger
     */
    public static final Logger logger = LoggerFactory
	    .getLogger(GisgraphyConfig.class);

    /**
     * Constructor needed by CGLib
     */
    @SuppressWarnings("unused")
    private FulltextQuery() {
	super();
    }

    /**
     * The type of GIS to search , if null : search for all place type.
     */
    private Class<? extends GisFeature> placeType = null;

    @Autowired
    @Qualifier("countryDao")
    private ICountryDao countryDao;

    private String query = "";
    private String countryCode;
    
    private boolean spellchecking = SpellCheckerConfig.activeByDefault;

    /**
     * @param req
     *                an HttpServletRequest to construct a {@link FulltextQuery}
     */
    public FulltextQuery(HttpServletRequest req) {
	super();
	String httpQueryParameter = req.getParameter(FulltextServlet.QUERY_PARAMETER);
	if (httpQueryParameter != null){
		this.query = httpQueryParameter.trim();
	}
	if (query == null || "".equals(query.trim())) {
	    throw new FullTextSearchException("query is not specified or empty");
	}
	if (query.length() > FulltextQuery.QUERY_MAX_LENGTH) {
	    throw new FullTextSearchException("query is limited to "
		    + FulltextQuery.QUERY_MAX_LENGTH + "characters");
	}
	// pagination
	Pagination pagination = null;
	int from;
	int to;
	try {
	    from = Integer.valueOf(
		    req.getParameter(FulltextServlet.FROM_PARAMETER))
		    .intValue();
	} catch (Exception e) {
	    from = Pagination.DEFAULT_FROM;
	}

	try {
	    to = Integer
		    .valueOf(req.getParameter(FulltextServlet.TO_PARAMETER))
		    .intValue();
	} catch (NumberFormatException e) {
	    to = -1;
	}

	pagination = Pagination.paginateWithMaxResults(this.getMaxLimitResult()).from(from).to(to)
		.limitNumberOfResults(getMaxLimitResult());
	// output
	OutputFormat format = OutputFormat.getFromString(req
		.getParameter(FulltextServlet.FORMAT_PARAMETER));
	OutputStyle style = OutputStyle.getFromString(req
		.getParameter(FulltextServlet.STYLE_PARAMETER));
	String languageparam = req.getParameter(FulltextServlet.LANG_PARAMETER);
	Output output = Output.withFormat(format).withLanguageCode(
		languageparam).withStyle(style);

	// placetype
	Class<? extends GisFeature> clazz = GeolocHelper
		.getClassEntityFromString(req
			.getParameter(FulltextServlet.PLACETYPE_PARAMETER));

	// countrycode
	String countrycodeParam = req
		.getParameter(FulltextServlet.COUNTRY_PARAMETER);
	this.countryCode = countrycodeParam == null ? null : countrycodeParam
		.toUpperCase();

	//indentation
	if ("true".equalsIgnoreCase(req
		.getParameter(GisgraphyServlet.INDENT_PARAMETER))
		|| "on".equalsIgnoreCase(req
			.getParameter(GisgraphyServlet.INDENT_PARAMETER))) {
	    output.withIndentation();
	}
	//spellchecking
	if ("true".equalsIgnoreCase(req
			.getParameter(FulltextServlet.SPELLCHECKING_PARAMETER))
			|| "on".equalsIgnoreCase(req
				.getParameter(FulltextServlet.SPELLCHECKING_PARAMETER))) {
		    this.withSpellChecking();
		}
	else if ("false".equalsIgnoreCase(req.getParameter(FulltextServlet.SPELLCHECKING_PARAMETER))) {
		this.withoutSpellChecking();
	}

	this.pagination = pagination;
	this.placeType = clazz;
	this.output = output;
    }

    /**
     * @param query
     *                The text to query, if the query is a number zipcode will
     *                be searched
     * @param pagination
     *                The pagination specification, if null : use default
     * @param output
     *                {@link Output} The output specification , if null : use
     *                default
     * @param placeType
     *                the type of gis to search , if null : search for all place
     *                type.
     * @param countryCode
     *                Limit the search to the specified countryCode. If null or
     *                wrong it would be ignored (no check is done)
     * @throws An
     *                 {@link IllegalArgumentException} if the query is null or
     *                 empty string
     */
    public FulltextQuery(String query, Pagination pagination, Output output,
	    Class<? extends GisFeature> placeType, String countryCode) {
	super(pagination, output);
	Assert.notNull(query, "Query must not be empty");
	
	Assert.isTrue(!"".equals(query.trim()), "Query must not be empty");
	if (query.length() > FulltextQuery.QUERY_MAX_LENGTH) {
	    throw new IllegalArgumentException("query is limited to "
		    + FulltextQuery.QUERY_MAX_LENGTH + "characters");
	}
	this.query = query.trim();
	withPlaceType(placeType);
	limitToCountryCode(countryCode);
    }

    /**
     * @param query
     *                The text to search
     * @throws An
     *                 {@link IllegalArgumentException} if the query is null or
     *                 an empty string
     */
    public FulltextQuery(String query) {
	super();
	Assert.notNull(query, "Query must not be null");
	Assert.isTrue(!"".equals(query.trim()), "Query must not be empty");
	this.query = query.trim(); 
    }

    /**
     * @return The searched text for this FullTextQuery
     */
    public String getQuery() {
	return query;
    }

    /**
     * @param countryCode
     *                the countryCode to set. Limit the query to the specified
     *                countrycode, if the country code is null, it will be
     *                ignored. If null or invalid, it will be ignored (no check
     *                is done)
     */
    public FulltextQuery limitToCountryCode(String countryCode) {
	this.countryCode = countryCode;
	return this;
    }

    /**
     * @return the countryCode of the country that the query will be restricted
     *         to
     */
    public String getCountryCode() {
	return countryCode;
    }

    /**
     * @return Wether the output will be indented
     * @see Output#isIndented()
     */
    public boolean isOutputIndented() {
	return output.isIndented();
    }

    private boolean isNumericQuery() {
	try {
	    Integer.parseInt(this.query);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    /**
     * @return the placeType : it limits the search to an object of a specifict
     *         class
     */
    public Class<? extends GisFeature> getPlaceType() {
	return this.placeType;
    }

    /**
     * @param placeType
     *                The placeType to set, if null, search for all placetype
     * @return The current query to chain methods
     */
    public FulltextQuery withPlaceType(Class<? extends GisFeature> placeType) {
	this.placeType = placeType;
	return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	String asString = "FullTextQuery '" + this.query
		+ "' for ";
	if (this.placeType == null) {
	    asString += "all placeType";
	} else {
	    asString += this.placeType.getSimpleName();
	}
	asString += " with " + getOutput() + " and " + pagination +" for countrycode "+countryCode;
	return asString;
    }

    
    
    /**
     *  Enable the spellchecking for this query
     * @return The current query to chain methods
     */
    public FulltextQuery withSpellChecking(){
    	this.spellchecking = true;
    	return this;
    }
    
    /**
     *  Disable the spellchecking for this query
     * @return The current query to chain methods
     */
    public FulltextQuery withoutSpellChecking(){
    	this.spellchecking = false;
    	return this;
    }
    
    /**
     *  Wether the spellchecking is enabled for this query
     * @return The current query to chain methods
     */
    public boolean hasSpellChecking(){
    	return this.spellchecking; 
    }

    /**
     * @return A query string for the specified parameter (starting with '?')
     *         the name of the parameters are defined in {@link Constants}
     */
    public String toQueryString() {
	return ClientUtils.toQueryString(parameterize(), false);
    }

    /**
     * @return A Representation of all the needed parameters
     */
    public ModifiableSolrParams parameterize() {
	ModifiableSolrParams parameters = new ModifiableSolrParams();
	
	parameters.set(Constants.INDENT_PARAMETER, isOutputIndented() ? "on"
		: "off");
	parameters.set(Constants.ECHOPARAMS_PARAMETER, "none");
	parameters.set(Constants.START_PARAMETER, String
		.valueOf(getFirstPaginationIndex() - 1));// sub
	// 1
	// because
	// solr
	// start
	// at 0
	parameters.set(Constants.ROWS_PARAMETER, String.valueOf(getPagination()
		.getMaxNumberOfResults()));
	if (getOutputFormat() == OutputFormat.ATOM) {
	    parameters.set(Constants.STYLESHEET_PARAMETER,
		    Constants.ATOM_STYLESHEET);
	} else if (getOutputFormat() == OutputFormat.GEORSS) {
	    parameters.set(Constants.STYLESHEET_PARAMETER,
		    Constants.GEORSS_STYLESHEET);
	}
	parameters.set(Constants.OUTPUT_FORMAT_PARAMETER, getOutputFormat()
		.getParameterValue());
	// force Medium style if ATOM or Geo RSS
	if (getOutputFormat() == OutputFormat.ATOM
		|| getOutputFormat() == OutputFormat.GEORSS) {
	    parameters.set(Constants.FL_PARAMETER, OutputStyle.MEDIUM
		    .getFieldList(getOutput().getLanguageCode()));
	} else {
	    parameters.set(Constants.FL_PARAMETER, getOutput().getFields());
	}
	boolean isAdvancedQuery = (this.countryCode!=null || this.placeType != null);
	boolean isNumericQuery = isNumericQuery();
	StringBuffer query ;
	if (isAdvancedQuery){
	    if (isNumericQuery){
		 query = new StringBuffer(String.format(NESTED_QUERY_NUMERIC_TEMPLATE,getQuery()));
	    } else {
		query = new StringBuffer(String.format(NESTED_QUERY_TEMPLATE,getQuery()));
		
	    }
	    parameters.set(Constants.QT_PARAMETER, Constants.SolrQueryType.advanced
			.toString());
	    if (this.countryCode != null) {
		    query.append(" AND ").append(FullTextFields.COUNTRYCODE.getValue()+":"+countryCode);

		}
	    if (this.placeType != null) {
		    query.append(" AND ").append(FullTextFields.PLACETYPE.getValue()+":"+placeType.getSimpleName())
			    .append(" ");
		}
	    parameters.set(Constants.QUERY_PARAMETER, query.toString());
	}  else if (isNumericQuery) {
	    parameters.set(Constants.QT_PARAMETER, Constants.SolrQueryType.standard
			.toString());
	    parameters.set(Constants.QUERY_PARAMETER, getQuery());
	} else {
		    // we overide the query type
		    parameters.set(Constants.QT_PARAMETER,
			    Constants.SolrQueryType.standard.toString());
		    parameters.set(Constants.QUERY_PARAMETER, getQuery());
	}

	
	
	
	
	
	if (SpellCheckerConfig.enabled && this.hasSpellChecking()&& !isNumericQuery){
		parameters.set(Constants.SPELLCHECKER_ENABLED_PARAMETER,"true");
		if(isAdvancedQuery){
		    parameters.set(Constants.SPELLCHECKER_QUERY_PARAMETER, getQuery());
		}
		parameters.set(Constants.SPELLCHECKER_COLLATE_RESULTS_PARAMETER,SpellCheckerConfig.collateResults);
		parameters.set(Constants.SPELLCHECKER_NUMBER_OF_SUGGESTION_PARAMETER,SpellCheckerConfig.numberOfSuggestion);
		parameters.set(Constants.SPELLCHECKER_DICTIONARY_NAME_PARAMETER,SpellCheckerConfig.spellcheckerDictionaryName.toString());
	}

	return parameters;
    }

    @Override
    public int getMaxLimitResult() {
	return FulltextServlet.DEFAULT_MAX_RESULTS;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result
		+ ((countryCode == null) ? 0 : countryCode.hashCode());
	result = prime * result
		+ ((placeType == null) ? 0 : placeType.hashCode());
	result = prime * result + ((query == null) ? 0 : query.hashCode());
	result = prime * result + (spellchecking ? 1231 : 1237);
	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final FulltextQuery other = (FulltextQuery) obj;
	if (countryCode == null) {
	    if (other.countryCode != null)
		return false;
	} else if (!countryCode.equals(other.countryCode))
	    return false;
	if (placeType == null) {
	    if (other.placeType != null)
		return false;
	} else if (!placeType.equals(other.placeType))
	    return false;
	if (query == null) {
	    if (other.query != null)
		return false;
	} else if (!query.equals(other.query))
	    return false;
	if (spellchecking != other.spellchecking)
	    return false;
	return true;
    }

}
