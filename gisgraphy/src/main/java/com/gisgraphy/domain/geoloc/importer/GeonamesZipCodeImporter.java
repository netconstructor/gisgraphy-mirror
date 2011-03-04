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
package com.gisgraphy.domain.geoloc.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.FlushMode;
import org.springframework.beans.factory.annotation.Required;

import com.gisgraphy.domain.geoloc.entity.Adm;
import com.gisgraphy.domain.geoloc.entity.City;
import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.entity.ZipCode;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FulltextQuery;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.repository.IAdmDao;
import com.gisgraphy.domain.repository.ICityDao;
import com.gisgraphy.domain.repository.IGisFeatureDao;
import com.gisgraphy.domain.repository.ISolRSynchroniser;
import com.gisgraphy.domain.repository.IZipCodeDao;
import com.gisgraphy.domain.valueobject.FulltextResultsDto;
import com.gisgraphy.domain.valueobject.GISSource;
import com.gisgraphy.domain.valueobject.NameValueDTO;
import com.gisgraphy.domain.valueobject.SolrResponseDto;
import com.gisgraphy.helper.GeolocHelper;
import com.vividsolutions.jts.geom.Point;

/**
 * Import the zipcode from a Geonames dump file.
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public class GeonamesZipCodeImporter extends AbstractImporterProcessor {

    private IGisFeatureDao gisFeatureDao;

    private IAdmDao admDao;

    private IFullTextSearchEngine fullTextSearchEngine;

    private ISolRSynchroniser solRSynchroniser;

    private ICityDao cityDao;

    private IZipCodeDao zipCodeDao;

    protected long generatedFeatureId = 0;

    protected final long featureIdIncrement = 2000000;


    protected int[] accuracyToDistance = { 50000, 50000, 40000, 10000, 10000, 5000, 3000 };

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#processData
     * (java.lang.String)
     */
    @Override
    protected void processData(String line) {
	String[] fields = line.split("\t");

	/*
	 * line table has the following fields :
	 * --------------------------------------------------- 0 country code :
	 * 1 postal code 2 place name 3 admin name1 4 admin code1 5 admin name2
	 * 6 admin code2 7 admin name3 8 admin code3 9 latitude 10 longitude 11
	 * accuracy accuracy
	 * 
	 * Accuracy is an integer, the higher the better : 1 : estimated as
	 * average from numerically neigbouring postal codes 3 : same postal
	 * code, other name 4 : place name from geonames db 6 : postal code area
	 * centroid
	 */

	// check that the csv file line is in a correct format
	checkNumberOfColumn(fields);

	String countryCode = null;
	String code = null;
	String name = null;
	String adm1Name = null;
	int accuracy = 0;
	Point zipPoint = null;

	if (!isEmptyField(fields, 0, true)) {
	    countryCode = fields[0];
	}

	if (!isEmptyField(fields, 1, true)) {
	    code = fields[1];
	}

	if (!isEmptyField(fields, 2, true)) {
	    name = fields[2];
	}

	if (!isEmptyField(fields, 3, false)) {
	    adm1Name = fields[3];
	}

	if (!isEmptyField(fields, 11, false)) {
	    accuracy = new Integer(fields[11]);
	}

	// Location
	if (!isEmptyField(fields, 10, true) && !isEmptyField(fields, 9, true)) {
	    zipPoint = GeolocHelper.createPoint(new Float(fields[10]), new Float(fields[9]));
	}

	Long featureId = findFeature(fields, zipPoint, getAccurateDistance(accuracy));
	if (featureId != null) {
	    addAndSaveZipCodeToFeature(code, featureId);
	} else {
	    addNewEntityAndZip(fields);
	}
    }

    protected Long findFeature(String[] fields,  Point zipPoint,int maxDistance) {
	String query;
	boolean extendedsearch;
	if (fields[3] != null) {
	    query = fields[2] + " " + fields[3];
	    extendedsearch = true;
	} else {
	    query = fields[2];
	    extendedsearch = false;
	}
	FulltextResultsDto results = doAFulltextSearch(query,fields[0]);
	if (results.getResults().size() == 0) {
	    if (extendedsearch) {
		// do a basic search
		results = doAFulltextSearch(query, fields[0]);
		if (results.getResultsSize() == 0) {
		    // oops, no results
		    return null;
		} else if (results.getResultsSize() == 1) {
		    // we found the one!
		    return results.getResults().get(0).getFeature_id();
		} else {
		    // more than one match iterate and calculate distance and
		    // take the nearest
		    new Integer(fields[11]);
		    return findNearest(zipPoint, maxDistance, query, results);
		}
	    } else {
		// no features matches!
		return null;

	    }
	} else if (results.getResults().size() == 1) {
	    // we found the one!
	    return results.getResults().get(0).getFeature_id();
	} else {
	    // more than one match, take the nearest
	    return findNearest(zipPoint, maxDistance, query, results);
	}

    }

    protected Long findNearest(Point zipPoint, int maxDistance, String query, FulltextResultsDto results) {
	Long nearestFeatureId = null;
	double nearestDistance = 0;
	logger.error("More than one city match " + query);
	for (SolrResponseDto dto : results.getResults()) {
	    Point dtoPoint = GeolocHelper.createPoint(new Float(dto.getLng()), new Float(dto.getLat()));
	    if (nearestFeatureId == null) {
		nearestFeatureId = dto.getFeature_id();
		nearestDistance = GeolocHelper.distance(zipPoint, dtoPoint);
	    } else {
		double distance = GeolocHelper.distance(zipPoint, dtoPoint);
		if (distance > getAccurateDistance(maxDistance)) {
		    logger.debug(dto.getFeature_id() + " is too far and is not candidate");
		} else {
		    if (distance < nearestDistance) {
			logger.debug(dto.getFeature_id() + "is nearest than " + nearestFeatureId);
			nearestFeatureId = dto.getFeature_id();
			nearestDistance = distance;
		    }
		}

	    }
	}
	return nearestFeatureId;
    }

    protected int getAccurateDistance(int accuracyLevel) {
	return accuracyToDistance[Math.max(accuracyLevel, accuracyToDistance.length - 1)];
    }

    protected void addNewEntityAndZip(String[] fields) {
	City city = new City();
	city.setFeatureId(generatedFeatureId++);
	city.setName(fields[2]);
	// Location
	if (!isEmptyField(fields, 9, true) && !isEmptyField(fields, 10, true)) {
	    city.setLocation(GeolocHelper.createPoint(new Float(fields[9]), new Float(fields[10])));
	}
	city.setFeatureClass("P");
	city.setFeatureCode("PPL");
	city.setSource(GISSource.GEONAMES_ZIP);
	city.setCountryCode(fields[0]);
	setAdmCodesWithCSVOnes(fields, city);
	Adm adm;
	if (importerConfig.isTryToDetectAdmIfNotFound()) {
	    adm = this.admDao.suggestMostAccurateAdm(fields[0], fields[4], fields[6], fields[8], null, city);
	    logger.debug("suggestAdm=" + adm);
	} else {
	    adm = this.admDao.getAdm(fields[0], fields[4], fields[6], fields[8], null);
	}

	city.setAdm(adm);
	setAdmCodesWithLinkedAdmOnes(adm, city, importerConfig.isSyncAdmCodesWithLinkedAdmOnes());
	setAdmNames(adm, city);
	city.addZipCode(new ZipCode(fields[1]));

	cityDao.save(city);
    }

    protected void addAndSaveZipCodeToFeature(String code, Long featureId) {
	GisFeature feature = gisFeatureDao.getByFeatureId(featureId);
	if (feature == null) {
	    logger.error("can not add zip code " + code + " to " + featureId + ", because the feature doesn't exists");
	}
	ZipCode zipCode = new ZipCode(code);
	if (feature.getZipCodes() != null && !feature.getZipCodes().contains(zipCode)) {
	    feature.addZipCode(zipCode);
	    gisFeatureDao.save(feature);
	} else {
	    logger.warn("the zipcode " + code + " already exists for feature " + featureId);
	}
    }

    protected FulltextResultsDto doAFulltextSearch(String query, String countryCode) {
	FulltextQuery fulltextQuery = new FulltextQuery(query);
	fulltextQuery.limitToCountryCode(countryCode);
	fulltextQuery.withPlaceTypes(com.gisgraphy.fulltext.Constants.CITY_AND_CITYSUBDIVISION_PLACETYPE);

	FulltextResultsDto results = fullTextSearchEngine.executeQuery(fulltextQuery);
	return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldBeSkiped
     * ()
     */
    @Override
    public boolean shouldBeSkipped() {
	return !importerConfig.isGeonamesImporterEnabled();
    }

    protected void initFeatureIdGenerator() {
	generatedFeatureId = gisFeatureDao.getMaxFeatureId();
	generatedFeatureId = generatedFeatureId + featureIdIncrement;
    }

    private void setAdmNames(Adm adm, GisFeature gisFeature) {
	if (adm == null) {
	    return;
	}
	Adm admTemp = adm;
	do {
	    if (admTemp.getLevel() == 1) {
		gisFeature.setAdm1Name(admTemp.getName());
	    } else if (admTemp.getLevel() == 2) {
		gisFeature.setAdm2Name(admTemp.getName());
	    } else if (admTemp.getLevel() == 3) {
		gisFeature.setAdm3Name(admTemp.getName());
	    } else if (admTemp.getLevel() == 4) {
		gisFeature.setAdm4Name(admTemp.getName());
	    }
	    admTemp = admTemp.getParent();
	} while (admTemp != null);

    }

    private void setAdmCodesWithLinkedAdmOnes(Adm adm, GisFeature gisFeature, boolean syncAdmCodesWithLinkedAdmOnes) {

	if (syncAdmCodesWithLinkedAdmOnes) {
	    // reset adm code because we might link to an adm3 and adm4 code
	    // have
	    // been set
	    setAdmCodesToNull(gisFeature);
	    if (adm != null) {
		if (adm.getAdm1Code() != null) {
		    gisFeature.setAdm1Code(adm.getAdm1Code());
		}
		if (adm.getAdm2Code() != null) {
		    gisFeature.setAdm2Code(adm.getAdm2Code());
		}
		if (adm.getAdm3Code() != null) {
		    gisFeature.setAdm3Code(adm.getAdm3Code());
		}
		if (adm.getAdm4Code() != null) {
		    gisFeature.setAdm4Code(adm.getAdm4Code());
		}
	    }

	}
    }

    private void setAdmCodesToNull(GisFeature gisFeature) {
	gisFeature.setAdm1Code(null);
	gisFeature.setAdm2Code(null);
	gisFeature.setAdm3Code(null);
	gisFeature.setAdm4Code(null);
    }

    private void setAdmCodesWithCSVOnes(String[] fields, GisFeature gisFeature) {
	logger.debug("in setAdmCodesWithCSVOnes");
	if (!isEmptyField(fields, 4, false)) {
	    gisFeature.setAdm1Code(fields[4]);
	}
	if (!isEmptyField(fields, 6, false)) {
	    gisFeature.setAdm2Code(fields[6]);
	}
	if (!isEmptyField(fields, 8, false)) {
	    gisFeature.setAdm3Code(fields[8]);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#
     * shouldIgnoreFirstLine()
     */
    @Override
    protected boolean shouldIgnoreFirstLine() {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#
     * shouldIgnoreComments()
     */
    @Override
    protected boolean shouldIgnoreComments() {
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#
     * setCommitFlushMode()
     */
    @Override
    protected void setCommitFlushMode() {
	this.gisFeatureDao.setFlushMode(FlushMode.COMMIT);
	this.cityDao.setFlushMode(FlushMode.COMMIT);
	this.admDao.setFlushMode(FlushMode.COMMIT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#flushAndClear
     * ()
     */
    @Override
    protected void flushAndClear() {
	this.gisFeatureDao.flushAndClear();
	this.cityDao.flushAndClear();
	this.admDao.flushAndClear();
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#
     * getNumberOfColumns()
     */
    @Override
    protected int getNumberOfColumns() {
	return 12;
    }

    /**
     * @param cityDao
     *            The CityDao to set
     */
    @Required
    public void setCityDao(ICityDao cityDao) {
	this.cityDao = cityDao;
    }

    /**
     * @param gisFeatureDao
     *            The GisFeatureDao to set
     */
    @Required
    public void setGisFeatureDao(IGisFeatureDao gisFeatureDao) {
	this.gisFeatureDao = gisFeatureDao;
    }

    /**
     * @param admDao
     *            the admDao to set
     */
    @Required
    public void setAdmDao(IAdmDao admDao) {
	this.admDao = admDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#setup()
     */
    @Override
    public void setup() {
	super.setup();
	initFeatureIdGenerator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#tearDown()
     */
    @Override
    protected void tearDown() {
	super.tearDown();
	super.tearDown();
	if (!solRSynchroniser.commit()) {
	    logger.warn("The commit in tearDown of " + this.getClass().getSimpleName() + " has failed, the uncommitted changes will be commited with the auto commit of solr in few minuts");
	}
	solRSynchroniser.optimize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#getFiles()
     */
    @Override
    protected File[] getFiles() {
	return ImporterHelper.listCountryFilesToImport(importerConfig.getGeonamesZipCodeDir());
    }

    /**
     * @param solRSynchroniser
     *            the solRSynchroniser to set
     */
    @Required
    public void setSolRSynchroniser(ISolRSynchroniser solRSynchroniser) {
	this.solRSynchroniser = solRSynchroniser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.IGeonamesProcessor#rollback()
     */
    public List<NameValueDTO<Integer>> rollback() {
	List<NameValueDTO<Integer>> deletedObjectInfo = new ArrayList<NameValueDTO<Integer>>();
	// we first reset subClass
	int deletedgis = zipCodeDao.deleteAll();
	logger.warn("deleting zipCodes...");
	// we don't want to remove adm because some feature can be linked again
	if (deletedgis != 0) {
	    deletedObjectInfo.add(new NameValueDTO<Integer>(GisFeature.class.getSimpleName(), deletedgis));
	}
	resetStatus();
	return deletedObjectInfo;
    }

    @Required
    public void setZipCodeDao(IZipCodeDao zipCodeDao) {
	this.zipCodeDao = zipCodeDao;
    }

    @Required
    public void setFullTextSearchEngine(IFullTextSearchEngine fullTextSearchEngine) {
        this.fullTextSearchEngine = fullTextSearchEngine;
    }

}
