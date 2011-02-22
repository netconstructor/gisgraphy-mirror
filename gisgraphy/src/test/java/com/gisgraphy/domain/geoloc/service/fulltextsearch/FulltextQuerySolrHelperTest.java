package com.gisgraphy.domain.geoloc.service.fulltextsearch;

import static com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuerySolrHelper.NESTED_QUERY_INTEXT_WITHSTATE_TEMPLATE;
import static com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuerySolrHelper.NESTED_QUERY_NUMERIC_TEMPLATE;
import static com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuerySolrHelper.NESTED_QUERY_TEMPLATE;
import static com.gisgraphy.domain.valueobject.Pagination.paginate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

import com.gisgraphy.domain.geoloc.entity.Country;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.spell.SpellCheckerConfig;
import com.gisgraphy.domain.valueobject.Constants;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.Output.OutputStyle;
import com.gisgraphy.serializer.OutputFormat;
import com.gisgraphy.test.GeolocTestHelper;


public class FulltextQuerySolrHelperTest {
	 private  OutputStyleHelper outputStyleHelper = new OutputStyleHelper();
	 
    @Test
    public void testToQueryStringShouldreturnCorrectParamsForBasicQuery() {
	Country france = GeolocTestHelper.createCountryForFrance();
	Pagination pagination = paginate().from(3).to(10);
	Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode(
		"FR").withStyle(OutputStyle.SHORT).withIndentation();
	String searchTerm = "Saint-André";
	FulltextQuery fulltextQuery = new FulltextQuery(searchTerm,
		pagination, output, null, null);
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
		FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertEquals(outputStyleHelper.getFulltextFieldList(Output.OutputStyle.SHORT,"FR"), parameters
		.get(Constants.FL_PARAMETER));
	assertEquals("wrong indent parameter found", "on", parameters
		.get(Constants.INDENT_PARAMETER));
	assertEquals("wrong echoparams parameter found", "none", parameters
		.get(Constants.ECHOPARAMS_PARAMETER));
	assertEquals("wrong start parameter found", "2", parameters
		.get(Constants.START_PARAMETER));
	assertEquals("wrong rows parameter found", "8", parameters
		.get(Constants.ROWS_PARAMETER));
	assertEquals("wrong output format parameter found", OutputFormat.JSON
		.getParameterValue(), parameters
		.get(Constants.OUTPUT_FORMAT_PARAMETER));
	assertEquals("wrong query type parameter found",
		Constants.SolrQueryType.standard.toString(), parameters
			.get(Constants.QT_PARAMETER));
	assertEquals("wrong query parameter found ",searchTerm,
		parameters
			.get(Constants.QUERY_PARAMETER));
	assertNull("spellchecker query should not be set when standard query",parameters
		.get(Constants.SPELLCHECKER_QUERY_PARAMETER));   
	}
    
    @Test
    public void testToQueryStringShouldreturnCorrectParamsForBasicNumericQuery() {
	Pagination pagination = paginate().from(3).to(10);
	Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode(
		"FR").withStyle(OutputStyle.SHORT).withIndentation();
	String searchTerm = "1001";
	FulltextQuery fulltextQuery = new FulltextQuery(searchTerm,
		pagination, output, null, null);
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
		FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertEquals(outputStyleHelper.getFulltextFieldList(Output.OutputStyle.SHORT,"FR"), parameters
		.get(Constants.FL_PARAMETER));
	assertEquals("wrong indent parameter found", "on", parameters
		.get(Constants.INDENT_PARAMETER));
	assertEquals("wrong echoparams parameter found", "none", parameters
		.get(Constants.ECHOPARAMS_PARAMETER));
	assertEquals("wrong start parameter found", "2", parameters
		.get(Constants.START_PARAMETER));
	assertEquals("wrong rows parameter found", "8", parameters
		.get(Constants.ROWS_PARAMETER));
	assertEquals("wrong output format parameter found", OutputFormat.JSON
		.getParameterValue(), parameters
		.get(Constants.OUTPUT_FORMAT_PARAMETER));
	assertEquals("wrong query type parameter found",
		Constants.SolrQueryType.numeric.toString(), parameters
			.get(Constants.QT_PARAMETER));
	assertEquals("wrong query parameter found ",searchTerm,
		parameters
			.get(Constants.QUERY_PARAMETER));
	assertEquals("wrong query parameter",searchTerm,
	parameters
		.get(Constants.QUERY_PARAMETER));
	
	assertNull("spellchecker query should not be set when basic numeric query",parameters
		.get(Constants.SPELLCHECKER_QUERY_PARAMETER));
	}
    
    
    
    
    @Test
    public void testToQueryStringShouldreturnCorrectParamsForAdvancedNumericQuery() {
	Pagination pagination = paginate().from(3).to(10);
	    Output output = Output.withFormat(OutputFormat.JSON)
		    .withLanguageCode("FR").withStyle(OutputStyle.SHORT)
		    .withIndentation();
	    FulltextQuery fulltextQuery = new FulltextQuery("1001",
		    pagination, output, com.gisgraphy.fulltext.Constants.ONLY_CITY_PLACETYPE, "FR");
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
		FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertEquals(outputStyleHelper.getFulltextFieldList(Output.OutputStyle.SHORT,"FR"), parameters
		.get(Constants.FL_PARAMETER));
	assertEquals("wrong indent parameter found", "on", parameters
		.get(Constants.INDENT_PARAMETER));
	assertEquals("wrong echoparams parameter found", "none", parameters
		.get(Constants.ECHOPARAMS_PARAMETER));
	assertEquals("wrong start parameter found", "2", parameters
		.get(Constants.START_PARAMETER));
	assertEquals("wrong rows parameter found", "8", parameters
		.get(Constants.ROWS_PARAMETER));
	assertEquals("wrong output format parameter found", OutputFormat.JSON
		.getParameterValue(), parameters
		.get(Constants.OUTPUT_FORMAT_PARAMETER));
	assertEquals("wrong query type parameter found",
		Constants.SolrQueryType.advanced.toString(), parameters
			.get(Constants.QT_PARAMETER));
	assertTrue("wrong query parameter found '"+FullTextFields.PLACETYPE.getValue()+":' is expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
		parameters
			.get(Constants.QUERY_PARAMETER).contains(FullTextFields.PLACETYPE.getValue()+":"));


	assertTrue("wrong nested parameter found, actual : "+parameters
		.get(Constants.QUERY_PARAMETER),
	parameters
		.get(Constants.QUERY_PARAMETER).contains(String.format(NESTED_QUERY_NUMERIC_TEMPLATE, "1001")));
	
	
	assertTrue("wrong query parameter found '"+FullTextFields.COUNTRYCODE.getValue()+":' is expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
			parameters
				.get(Constants.QUERY_PARAMETER).contains(FullTextFields.COUNTRYCODE.getValue()+":"));
	assertNull("spellchecker query should not be set when advanced numeric query",parameters
		.get(Constants.SPELLCHECKER_QUERY_PARAMETER)); 
	}
    
    @Test
    public void testToQueryStringShouldreturnCorrectParamsWhenAllRequiredIsFalse() {
	Pagination pagination = paginate().from(1).to(10);
	    Output output = Output.withFormat(OutputFormat.JSON)
		    .withLanguageCode("FR").withStyle(OutputStyle.SHORT)
		    .withIndentation();
	    FulltextQuery fulltextQuery = new FulltextQuery("foo",
		    pagination, output, com.gisgraphy.fulltext.Constants.ONLY_CITY_PLACETYPE, "FR").withAllwordsRequired(false);
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
		FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertEquals(outputStyleHelper.getFulltextFieldList(Output.OutputStyle.SHORT,"FR"), parameters
		.get(Constants.FL_PARAMETER));
	assertEquals("wrong indent parameter found", "on", parameters
		.get(Constants.INDENT_PARAMETER));
	assertEquals("wrong echoparams parameter found", "none", parameters
		.get(Constants.ECHOPARAMS_PARAMETER));
	assertEquals("wrong start parameter found", "0", parameters
		.get(Constants.START_PARAMETER));
	assertEquals("wrong rows parameter found", "10", parameters
		.get(Constants.ROWS_PARAMETER));
	assertEquals("wrong output format parameter found", OutputFormat.JSON
		.getParameterValue(), parameters
		.get(Constants.OUTPUT_FORMAT_PARAMETER));
	assertEquals("wrong query type parameter found",
		Constants.SolrQueryType.advanced.toString(), parameters
			.get(Constants.QT_PARAMETER));
	assertTrue("wrong query parameter found '"+FullTextFields.PLACETYPE.getValue()+":' is expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
		parameters
			.get(Constants.QUERY_PARAMETER).contains(FullTextFields.PLACETYPE.getValue()+":"));


	assertTrue("wrong nested parameter found, actual : "+parameters
		.get(Constants.QUERY_PARAMETER),
	parameters
		.get(Constants.QUERY_PARAMETER).contains(String.format(NESTED_QUERY_INTEXT_WITHSTATE_TEMPLATE, "foo")));
	
	
	assertTrue("wrong query parameter found '"+FullTextFields.COUNTRYCODE.getValue()+":' is expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
			parameters
				.get(Constants.QUERY_PARAMETER).contains(FullTextFields.COUNTRYCODE.getValue()+":"));
	assertNotNull("spellchecker query should be set ",parameters
		.get(Constants.SPELLCHECKER_QUERY_PARAMETER));    }


    
    @Test
    public void testToQueryStringShouldreturnCorrectParamsForAdvancedNonNumeric() {
	Pagination pagination = paginate().from(3).to(10);
	Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode(
		"FR").withStyle(OutputStyle.SHORT).withIndentation();
	String searchTerm = "Saint-André";
	FulltextQuery fulltextQuery = new FulltextQuery(searchTerm,
		pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "fr");
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
		FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertEquals(outputStyleHelper.getFulltextFieldList(Output.OutputStyle.SHORT,"FR"), parameters
		.get(Constants.FL_PARAMETER));
	assertEquals("wrong indent parameter found", "on", parameters
		.get(Constants.INDENT_PARAMETER));
	assertEquals("wrong echoparams parameter found", "none", parameters
		.get(Constants.ECHOPARAMS_PARAMETER));
	assertEquals("wrong start parameter found", "2", parameters
		.get(Constants.START_PARAMETER));
	assertEquals("wrong rows parameter found", "8", parameters
		.get(Constants.ROWS_PARAMETER));
	assertEquals("wrong output format parameter found", OutputFormat.JSON
		.getParameterValue(), parameters
		.get(Constants.OUTPUT_FORMAT_PARAMETER));
	assertEquals("wrong query type parameter found",
		Constants.SolrQueryType.advanced.toString(), parameters
			.get(Constants.QT_PARAMETER));
	assertTrue("wrong nested parameter found actual : "+parameters
		.get(Constants.QUERY_PARAMETER),
		parameters
			.get(Constants.QUERY_PARAMETER).contains(String.format(NESTED_QUERY_TEMPLATE, searchTerm)));
	assertTrue("wrong query parameter found '"+FullTextFields.PLACETYPE.getValue()+":' expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
		parameters
			.get(Constants.QUERY_PARAMETER).contains(FullTextFields.PLACETYPE.getValue()+":"));
	assertTrue("wrong query parameter found '"+FullTextFields.COUNTRYCODE.getValue()+":' expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
			parameters
				.get(Constants.QUERY_PARAMETER).contains(FullTextFields.COUNTRYCODE.getValue()+":"));
	assertNotNull("spellchecker query should be set when numeric query",parameters
		.get(Constants.SPELLCHECKER_QUERY_PARAMETER));  
    }

    @Test
    public void testToQueryStringShouldreturnCorrectParamsForGeoRSS() {
	Pagination pagination = paginate().from(3).to(10);
	Output output = Output.withFormat(OutputFormat.GEORSS)
		.withLanguageCode("FR").withStyle(OutputStyle.SHORT)
		.withIndentation();
	FulltextQuery fulltextQuery = new FulltextQuery("Saint-André",
		pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "fr");
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
			FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertEquals("wrong field list", outputStyleHelper.getFulltextFieldList(Output.OutputStyle.MEDIUM,"FR"), parameters.get(Constants.FL_PARAMETER));
	assertEquals("wrong indent parameter found", "on", parameters
		.get(Constants.INDENT_PARAMETER));
	assertEquals("wrong echoparams parameter found", "none", parameters
		.get(Constants.ECHOPARAMS_PARAMETER));
	assertEquals("wrong start parameter found", "2", parameters
		.get(Constants.START_PARAMETER));
	assertEquals("wrong rows parameter found", "8", parameters
		.get(Constants.ROWS_PARAMETER));
	assertEquals("wrong output format parameter found", OutputFormat.GEORSS
		.getParameterValue(), parameters
		.get(Constants.OUTPUT_FORMAT_PARAMETER));
	assertEquals("wrong stylesheet", Constants.GEORSS_STYLESHEET,
		parameters.get(Constants.STYLESHEET_PARAMETER));
	assertEquals("wrong query type parameter found",
		Constants.SolrQueryType.advanced.toString(), parameters
			.get(Constants.QT_PARAMETER));
	assertTrue("wrong query parameter found '"+FullTextFields.PLACETYPE.getValue()+":' expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
		parameters
			.get(Constants.QUERY_PARAMETER).contains(FullTextFields.PLACETYPE.getValue()+":"));
	assertTrue("wrong query parameter found '"+FullTextFields.COUNTRYCODE.getValue()+":' expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
			parameters
				.get(Constants.QUERY_PARAMETER).contains(FullTextFields.COUNTRYCODE.getValue()+":"));
    }

    @Test
    public void testToQueryStringShouldreturnCorrectParamsForAtom() {
	Pagination pagination = paginate().from(3).to(10);
	Output output = Output.withFormat(OutputFormat.ATOM).withLanguageCode(
		"FR").withStyle(OutputStyle.SHORT).withIndentation();
	FulltextQuery fulltextQuery = new FulltextQuery("Saint-André",
		pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "fr");
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
			FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertEquals("wrong field list",outputStyleHelper.getFulltextFieldList(Output.OutputStyle.MEDIUM,"FR"), parameters.get(Constants.FL_PARAMETER));
	assertEquals("wrong indent parameter found", "on", parameters
		.get(Constants.INDENT_PARAMETER));
	assertEquals("wrong echoparams parameter found", "none", parameters
		.get(Constants.ECHOPARAMS_PARAMETER));
	assertEquals("wrong start parameter found", "2", parameters
		.get(Constants.START_PARAMETER));
	assertEquals("wrong rows parameter found", "8", parameters
		.get(Constants.ROWS_PARAMETER));
	assertEquals("wrong output format parameter found", OutputFormat.GEORSS
		.getParameterValue(), parameters
		.get(Constants.OUTPUT_FORMAT_PARAMETER));
	assertEquals("wrong stylesheet", Constants.ATOM_STYLESHEET, parameters
		.get(Constants.STYLESHEET_PARAMETER));
	assertEquals("wrong query type parameter found",
		Constants.SolrQueryType.advanced.toString(), parameters
			.get(Constants.QT_PARAMETER));
	assertTrue("wrong query parameter found '"+FullTextFields.PLACETYPE.getValue()+":' expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
		parameters
			.get(Constants.QUERY_PARAMETER).contains(FullTextFields.PLACETYPE.getValue()+":"));
	assertTrue("wrong query parameter found '"+FullTextFields.COUNTRYCODE.getValue()+":' expected in query but was "+parameters
			.get(Constants.QUERY_PARAMETER),
			parameters
				.get(Constants.QUERY_PARAMETER).contains(FullTextFields.COUNTRYCODE.getValue()+":"));
    }
    
    @Test
    public void testToQueryStringShouldreturnCorrectParamsForSpellChecking() {
    	boolean savedSpellCheckingActiveByDefaultValue = SpellCheckerConfig.activeByDefault;
    	boolean savedSpellCheckerConfigEnabled = SpellCheckerConfig.enabled;
    	try {
	SpellCheckerConfig.activeByDefault= true;
	SpellCheckerConfig.enabled = false;
	Pagination pagination = paginate().from(3).to(10);
	Output output = Output.withFormat(OutputFormat.ATOM).withLanguageCode(
		"FR").withStyle(OutputStyle.SHORT).withIndentation();
	FulltextQuery fulltextQuery = new FulltextQuery("Saint-André",
		pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "fr").withSpellChecking();
	// split parameters
	HashMap<String, String> parameters = GeolocTestHelper.splitURLParams(
			FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	// check parameters
	assertTrue("the fulltextquery should have spellchecking enabled even if spellchecker is disabled", fulltextQuery.hasSpellChecking());
	assertTrue("spellchecker should not be listed if spellchecker is disabled", !parameters
		.containsKey(Constants.SPELLCHECKER_ENABLED_PARAMETER));
	//active spellchecker and re test
	SpellCheckerConfig.enabled = true;
	fulltextQuery = new FulltextQuery("Saint-André",
			pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "fr").withSpellChecking();
	parameters = GeolocTestHelper.splitURLParams(
			FulltextQuerySolrHelper.toQueryString(fulltextQuery), "&");
	assertTrue("the fulltextquery should have spellchecking enabled when spellchecker is enabled", fulltextQuery.hasSpellChecking());
	assertEquals("spellchecker should be enabled", "true", parameters
			.get(Constants.SPELLCHECKER_ENABLED_PARAMETER));
	assertEquals("spellchecker should be enabled", String.valueOf(SpellCheckerConfig.collateResults), parameters
			.get(Constants.SPELLCHECKER_COLLATE_RESULTS_PARAMETER));
	assertEquals("spellchecker should be enabled",  String.valueOf(SpellCheckerConfig.numberOfSuggestion), parameters
			.get(Constants.SPELLCHECKER_NUMBER_OF_SUGGESTION_PARAMETER));
	assertEquals("spellchecker should be enabled", SpellCheckerConfig.spellcheckerDictionaryName.toString(), parameters
			.get(Constants.SPELLCHECKER_DICTIONARY_NAME_PARAMETER));
    	} catch (RuntimeException e) {
		fail(e.getMessage());
	} finally {
	    SpellCheckerConfig.activeByDefault = savedSpellCheckingActiveByDefaultValue;
	    SpellCheckerConfig.enabled=savedSpellCheckerConfigEnabled;
	}
    }


}
