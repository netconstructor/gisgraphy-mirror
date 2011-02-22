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

import static com.gisgraphy.domain.valueobject.Pagination.paginate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.gisgraphy.domain.geoloc.service.fulltextsearch.spell.SpellCheckerConfig;
import com.gisgraphy.domain.valueobject.Output;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.domain.valueobject.Output.OutputStyle;
import com.gisgraphy.serializer.OutputFormat;

public class FulltextQueryTest {

   

    @Test
    public void testFulltextQueryStringPaginationOutputClassOfQextendsGisFeature() {
	Pagination pagination = paginate().from(2).to(7);
	Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode(
		"FR").withStyle(OutputStyle.FULL).withIndentation();
	FulltextQuery fulltextQuery = new FulltextQuery("text", pagination,
		output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "fr");
	assertEquals(pagination, fulltextQuery.getPagination());
	assertEquals(output, fulltextQuery.getOutput());
	Assert.assertArrayEquals(com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, fulltextQuery.getPlaceTypes());
	assertEquals("text", fulltextQuery.getQuery());
	assertTrue(fulltextQuery.isOutputIndented());
	assertEquals("fr", fulltextQuery.getCountryCode());
    }



    @Test
    public void testFulltextQueryString() {
	FulltextQuery fulltextQuery = new FulltextQuery("text");
	assertEquals("text", fulltextQuery.getQuery());
	assertEquals(Output.DEFAULT_OUTPUT, fulltextQuery.getOutput());
	assertEquals(Pagination.DEFAULT_PAGINATION, fulltextQuery
		.getPagination());
	assertNull(fulltextQuery.getPlaceTypes());
	assertEquals("text", fulltextQuery.getQuery());
    }

    @Test
    public void testFulltextQueryWithNullQueryThrows() {
	Pagination pagination = paginate().from(2).to(7);
	Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode(
		"FR").withStyle(OutputStyle.FULL);
	try {
	    new FulltextQuery(RandomStringUtils
		    .random(FulltextQuery.QUERY_MAX_LENGTH) + 1, pagination,
		    output,com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "FR");
	    fail("query must have a maximmum length of "
		    + FulltextQuery.QUERY_MAX_LENGTH);
	} catch (IllegalArgumentException e) {

	}

    }

    @Test
    public void testFulltextQueryWithTooLongQueryStringThrows() {
	Pagination pagination = paginate().from(2).to(7);
	Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode(
		"FR").withStyle(OutputStyle.FULL);
	try {
	    new FulltextQuery(null, pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "FR");
	    fail("Null query should throws");
	} catch (IllegalArgumentException e) {

	}
    }

    @Test
	public void testFulltextQueryWithEmptyQueryThrows() {
		Pagination pagination = paginate().from(2).to(7);
		Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode("FR").withStyle(OutputStyle.FULL);
		try {
			new FulltextQuery(" ", pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "FR");
			fail("empty query should throws");
		} catch (IllegalArgumentException e) {

		}

		try {
			new FulltextQuery(" ");
			fail("Empty query should throws");
		} catch (RuntimeException e) {

		}
	}
    @Test
	public void testFulltextQueryWithPaginationShouldTrim() {
		Pagination pagination = paginate().from(2).to(7);
		Output output = Output.withFormat(OutputFormat.JSON).withLanguageCode("FR").withStyle(OutputStyle.FULL);
			FulltextQuery query = new FulltextQuery(" t ", pagination, output, com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, "FR");
			Assert.assertEquals("t", query.getQuery());
	}
    
    @Test
	public void testFulltextQueryShouldTrim() {
			FulltextQuery query = new FulltextQuery(" t ");
			Assert.assertEquals("t", query.getQuery());
	}

    @Test
    public void testWithPaginationShouldBeSetToDefaultPaginationIfNull() {
	assertEquals(Pagination.DEFAULT_PAGINATION, new FulltextQuery("text")
		.withPagination(null).getPagination());
    }

    @Test
    public void testWithPaginationShouldSetThePagination() {
	assertEquals(5, new FulltextQuery("text").withPagination(
		paginate().from(5).to(23)).getPagination().getFrom());
	assertEquals(23, new FulltextQuery("text").withPagination(
		paginate().from(5).to(23)).getPagination().getTo());
    }

    @Test
    public void testWithOutputShouldBeSetToDefaultOutputIfNull() {
	assertEquals(Output.DEFAULT_OUTPUT, new FulltextQuery("text")
		.withOutput(null).getOutput());
    }

    @Test
    public void testWithOutputShouldSetTheOutput() {
	FulltextQuery fulltextQuery = new FulltextQuery("text");
	Pagination pagination = paginate().from(2).to(7);
	fulltextQuery.withPagination(pagination);
	assertEquals(pagination, fulltextQuery.getPagination());
    }

    @Test
    public void testWithPlaceTypeShouldBeSetToNullIfNull() {
	assertNull(new FulltextQuery("text").withPlaceTypes(null).getPlaceTypes());
    }

    public void testLimitToCountryCodeShouldSetTheCountryCode() {
	FulltextQuery fulltextQuery = new FulltextQuery("text")
		.limitToCountryCode("FR");
	assertEquals("FR", fulltextQuery.getCountryCode());
    }

    @Test
    public void testLimitToCountryCodeShouldBeSetToNull() {
	assertNull(new FulltextQuery("text").limitToCountryCode(null)
		.getCountryCode());
    }

    @Test
    public void testWithPlaceTypeShouldSetTheplaceType() {
	FulltextQuery fulltextQuery = new FulltextQuery("text");
	fulltextQuery.withPlaceTypes(com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE);
	assertEquals(com.gisgraphy.fulltext.Constants.ONLY_ADM_PLACETYPE, fulltextQuery.getPlaceTypes());
    }

    @Test
    public void testQueryShouldHaveSpellcheckingCorrectDefaultValue(){
    	boolean savedSpellCheckingValue = SpellCheckerConfig.activeByDefault;
	try {
	    FulltextQuery query = new FulltextQuery("test");
	    assertEquals(savedSpellCheckingValue, query.hasSpellChecking());
	    SpellCheckerConfig.activeByDefault = !SpellCheckerConfig.activeByDefault;
	    query = new FulltextQuery("test2");
	    assertEquals(SpellCheckerConfig.activeByDefault, query.hasSpellChecking());
	} catch (RuntimeException e) {

	} finally {
	    SpellCheckerConfig.activeByDefault = savedSpellCheckingValue;
	}
    }

    @Test
    public void testCleanQueryString(){
	FulltextQuery query = new FulltextQuery("{!dismax qf=population} paris");
	query.cleanQueryString();
	Assert.assertEquals("dismax qfpopulation paris", query.getQuery());
    }
    
    @Test
    public void testIsALlWordsRequiredShouldBeTrueByDefault(){
    	FulltextQuery query = new FulltextQuery("foo");
    	Assert.assertTrue(query.isAllwordsRequired());
    }
    
    @Test
    public void testwithAllWordsRequired(){
    	FulltextQuery query = new FulltextQuery("foo");
    	Assert.assertTrue(query.isAllwordsRequired());
    	query.withAllwordsRequired(false);
    	Assert.assertFalse(query.isAllwordsRequired());
    }
    
}
