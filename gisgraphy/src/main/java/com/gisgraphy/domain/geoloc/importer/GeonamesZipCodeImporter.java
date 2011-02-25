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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.FlushMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import com.gisgraphy.domain.geoloc.entity.Adm;
import com.gisgraphy.domain.geoloc.entity.AlternateName;
import com.gisgraphy.domain.geoloc.entity.Country;
import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.entity.ZipCode;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IFullTextSearchEngine;
import com.gisgraphy.domain.repository.IAdmDao;
import com.gisgraphy.domain.repository.IAlternateNameDao;
import com.gisgraphy.domain.repository.ICityDao;
import com.gisgraphy.domain.repository.ICountryDao;
import com.gisgraphy.domain.repository.IGisDao;
import com.gisgraphy.domain.repository.IGisFeatureDao;
import com.gisgraphy.domain.repository.ISolRSynchroniser;
import com.gisgraphy.domain.valueobject.AlternateNameSource;
import com.gisgraphy.domain.valueobject.Constants;
import com.gisgraphy.domain.valueobject.FeatureCode;
import com.gisgraphy.domain.valueobject.GISSource;
import com.gisgraphy.domain.valueobject.NameValueDTO;
import com.gisgraphy.helper.GeolocHelper;

/**
 * Import the zipcode from a Geonames dump file.
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public class GeonamesZipCodeImporter extends AbstractImporterProcessor {

    private ICityDao cityDao;

    private IGisFeatureDao gisFeatureDao;

    private IAlternateNameDao alternateNameDao;

    private IAdmDao admDao;

    private ICountryDao countryDao;
    
    private IFullTextSearchEngine fullTextSearchEngine;


    private ISolRSynchroniser solRSynchroniser;

    @Autowired
    IGisDao<? extends GisFeature>[] iDaos;



    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#processData(java.lang.String)
     */
    @Override
    protected void processData(String line) {
	String[] fields = line.split("\t");

	/*
	 * line table has the following fields :
	 * --------------------------------------------------- 0 country code : 1
	 * postal code 2 place name 3 admin name1 4 admin code1 5 admin name2 6 admin code2
	 *  7 admin name3 8 admin code3  9 latitude 10 longitude 11 accuracy
	 * accuracy
	 */

	// check that the csv file line is in a correct format
	checkNumberOfColumn(fields);
	
	String country = null ;
	String code = null;
	String name = null;
	String adm1Name = null;
	String adm2Name = null;
	int accuracy = 0;
	

	if (!isEmptyField(fields, 0, true)) {
	    country = fields[0];
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
	
	if (!isEmptyField(fields, 5, false)) {
		adm2Name = fields[3];
	} 
	
	if (!isEmptyField(fields, 11, true)) {
	    accuracy = new Integer(fields[11]);
	} 
	

	//si zip deja present =>retrun
	
	//find city or subdivision by name and adm1 name verif distance
	//find city or subdivision by name and adm2 name verif distance
	//find city or subdivision by name
	
	/*si null=>ajoute gisfeature avec source geonames zip
	
	getgisfeature =>add zipcode
	*/
	
	/*if (featureCode_ != null) {
	    GisFeature featureObject = (GisFeature) featureCode_.getObject();
	    logger.debug(featureClass + "_" + featureCode
		    + " have an entry in " + FeatureCode.class.getSimpleName()
		    + " : " + featureObject.getClass().getSimpleName());
	    featureObject.populate(gisFeature);
		// zipcode
		String foundZipCode = findZipCode(fields);
		if (foundZipCode != null){
			featureObject.addZipCode(new ZipCode(foundZipCode));//TODO tests zip we should take embeded option into account
		}
	    this.gisFeatureDao.save(featureObject);
	} else {
	    logger.debug(featureClass + "_" + featureCode
		    + " have no entry in " + FeatureCode.class.getSimpleName()
		    + " and will be considered as a GisFeature");
	    this.gisFeatureDao.save(gisFeature);
	}
	// }*/

    }
    
    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldBeSkiped()
     */
    @Override
    public boolean shouldBeSkipped() {
	return !importerConfig.isGeonamesImporterEnabled();
    }

    private boolean isAlreadyUpdated(GisFeature feature) {
	if (feature.getModificationDate() != null) {
	    logger
		    .info(feature
			    + " has already been updated, it is probably a duplicate entry");
	    return true;
	}
	return false;
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

    private void setAdmCodesWithLinkedAdmOnes(Adm adm, GisFeature gisFeature,
	    boolean syncAdmCodesWithLinkedAdmOnes) {

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
	if (!isEmptyField(fields, 10, false)) {
	    gisFeature.setAdm1Code(fields[10]);
	}
	if (!isEmptyField(fields, 11, false)) {
	    gisFeature.setAdm2Code(fields[11]);
	}
	if (!isEmptyField(fields, 12, false)) {
	    gisFeature.setAdm3Code(fields[12]);
	}
	if (!isEmptyField(fields, 13, false)) {
	    gisFeature.setAdm4Code(fields[13]);
	}
    }

    private List<AlternateName> splitAlternateNames(String alternateNamesString,
	    GisFeature gisFeature) {
	String[] alternateNames = alternateNamesString.split(",");
	List<AlternateName> alternateNamesList = new ArrayList<AlternateName>();
	for (String name : alternateNames) {
	    AlternateName alternateName = new AlternateName();
	    alternateName.setName(name.trim());
	    alternateName.setSource(AlternateNameSource.EMBEDED);
	    alternateName.setGisFeature(gisFeature);
	    alternateNamesList.add(alternateName);
	}
	return alternateNamesList;
    }

    private String findZipCode(String[] fields) {
	logger.debug("try to detect zipCode for " + fields[1] + "[" + fields[0]
		+ "]");
	String zipCode = null;
	String[] alternateNames = fields[3].split(",");
	boolean found = false;
	Pattern patterncountry = null;
	Matcher matcherCountry = null;
	if (!isEmptyField(fields, 8, false)) {
	    Country country = countryDao.getByIso3166Alpha2Code(fields[8]);
	    if (country != null) {
		String regex = country.getPostalCodeRegex();
		if (regex != null) {
		    patterncountry = Pattern.compile(regex);
		    if (patterncountry == null) {
			logger.info("can not compile regexp" + regex);
			return null;
		    }
		} else {
		    logger.debug("regex=null for country " + country);
		    return null;
		}
	    } else {
		logger
			.warn("can not proces ZipCode because can not find country for "
				+ fields[8]);
		return null;
	    }

	} else {
	    logger.warn("can not proces ZipCode because can not find country ");
	}
	for (String element : alternateNames) {
	    matcherCountry = patterncountry.matcher(element);
	    if (matcherCountry.matches()) {
		if (found) {
		    logger
			    .info("There is more than one possible ZipCode for feature with featureid="
				    + fields[0] + ". it will be ignore");
		    return null;
		}
		try {
		    zipCode = element;
		    found = true;
		} catch (NumberFormatException e) {
		}

	    }
	}
	logger.debug("found " + zipCode + " for " + fields[1] + "[" + fields[0]
		+ "]");
	return zipCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldIgnoreFirstLine()
     */
    @Override
    protected boolean shouldIgnoreFirstLine() {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldIgnoreComments()
     */
    @Override
    protected boolean shouldIgnoreComments() {
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#setCommitFlushMode()
     */
    @Override
    protected void setCommitFlushMode() {
	this.cityDao.setFlushMode(FlushMode.COMMIT);
	this.gisFeatureDao.setFlushMode(FlushMode.COMMIT);
	this.alternateNameDao.setFlushMode(FlushMode.COMMIT);
	this.admDao.setFlushMode(FlushMode.COMMIT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#flushAndClear()
     */
    @Override
    protected void flushAndClear() {
	this.cityDao.flushAndClear();
	this.gisFeatureDao.flushAndClear();
	this.alternateNameDao.flushAndClear();
	this.admDao.flushAndClear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#getNumberOfColumns()
     */
    @Override
    protected int getNumberOfColumns() {
	return 12;
    }

    /**
     * @param cityDao
     *                The CityDao to set
     */
    @Required
    public void setCityDao(ICityDao cityDao) {
	this.cityDao = cityDao;
    }

    /**
     * @param alternateNameDao
     *                The alternateNameDao to set
     */
    @Required
    public void setAlternateNameDao(IAlternateNameDao alternateNameDao) {
	this.alternateNameDao = alternateNameDao;
    }

    /**
     * @param gisFeatureDao
     *                The GisFeatureDao to set
     */
    @Required
    public void setGisFeatureDao(IGisFeatureDao gisFeatureDao) {
	this.gisFeatureDao = gisFeatureDao;
    }

    /**
     * @param admDao
     *                the admDao to set
     */
    @Required
    public void setAdmDao(IAdmDao admDao) {
	this.admDao = admDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#setup()
     */
    @Override
    public void setup() {
	super.setup();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#tearDown()
     */
    @Override
    protected void tearDown() {
	super.tearDown();
	super.tearDown();
	if (!solRSynchroniser.commit()){
	    logger.warn("The commit in tearDown of "+this.getClass().getSimpleName()+" has failed, the uncommitted changes will be commited with the auto commit of solr in few minuts");
	}
	solRSynchroniser.optimize();
    }

    /**
     * @param countryDao
     *                The countryDao to set
     */
    @Required
    public void setCountryDao(ICountryDao countryDao) {
	this.countryDao = countryDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#getFiles()
     */
    @Override
    protected File[] getFiles() {
	return ImporterHelper.listCountryFilesToImport(importerConfig
		.getGeonamesDir());
    }

    /**
     * @param solRSynchroniser
     *                the solRSynchroniser to set
     */
    @Required
    public void setSolRSynchroniser(ISolRSynchroniser solRSynchroniser) {
	this.solRSynchroniser = solRSynchroniser;
    }

    /**
     * @param daos
     *                the iDaos to set
     */
    public void setIDaos(IGisDao<? extends GisFeature>[] daos) {
	iDaos = daos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.IGeonamesProcessor#rollback()
     */
    public List<NameValueDTO<Integer>> rollback() {
	List<NameValueDTO<Integer>> deletedObjectInfo = new ArrayList<NameValueDTO<Integer>>();
	// we first reset subClass
	for (IGisDao<? extends GisFeature> gisDao : iDaos) {
	    if (gisDao.getPersistenceClass() != GisFeature.class
		    && gisDao.getPersistenceClass() != Adm.class
		    && gisDao.getPersistenceClass() != Country.class) {
		logger.warn("deleting "
			+ gisDao.getPersistenceClass().getSimpleName() + "...");
		// we don't want to remove adm because some feature can be
		// linked again
		int deletedgis = gisDao.deleteAll();
		logger.warn(deletedgis+" "
			+ gisDao.getPersistenceClass().getSimpleName()
			+ " have been deleted");
		if (deletedgis != 0) {
		    deletedObjectInfo.add(new NameValueDTO<Integer>(
			    GisFeature.class.getSimpleName(), deletedgis));
		}
	    }
	}
	logger.warn("deleting gisFeature...");
	// we don't want to remove adm because some feature can be linked again
	int deletedgis = gisFeatureDao.deleteAllExceptAdmsAndCountries();
	logger.warn(deletedgis + " gisFeature have been deleted");
	if (deletedgis != 0) {
	    deletedObjectInfo.add(new NameValueDTO<Integer>(GisFeature.class
		    .getSimpleName(), deletedgis));
	}
	resetStatus();
	return deletedObjectInfo;
    }

}
