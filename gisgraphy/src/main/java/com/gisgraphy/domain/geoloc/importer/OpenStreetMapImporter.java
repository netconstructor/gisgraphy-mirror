/**
 * 
 */
package com.gisgraphy.domain.geoloc.importer;

import java.io.File;
import java.util.List;

import com.gisgraphy.domain.geoloc.entity.OpenStreetMap;
import com.gisgraphy.domain.geoloc.service.geoloc.street.StreetType;
import com.gisgraphy.domain.repository.OpenStreetMapDao;
import com.gisgraphy.domain.valueobject.NameValueDTO;
import com.gisgraphy.helper.GeolocHelper;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Import the street from an (pre-processed) openStreet map data file .
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public class OpenStreetMapImporter extends AbstractImporterProcessor {
    
    public static Long GeneratedId = 0L;
    
    private OpenStreetMapDao openStreetMapDao;

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#flushAndClear()
     */
    @Override
    protected void flushAndClear() {
	openStreetMapDao.flushAndClear();

    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#getFiles()
     */
    @Override
    protected File[] getFiles() {
	return ImporterHelper.listCountryFilesToImport(importerConfig.getOpenStreetMapDir());
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#getNumberOfColumns()
     */
    @Override
    protected int getNumberOfColumns() {
	return 9;
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#processData(java.lang.String)
     */
    @Override
    protected void processData(String line) throws GeonamesProcessorException {
	String[] fields = line.split("\t");

	//
	// Line table has the following fields :
	// --------------------------------------------------- 
	//0: id; 1 name; 2 location; 3 length ;4 countrycode; 5 : gid ;
	//6 type; 7 oneway; 8 : shape;
	//
	checkNumberOfColumn(fields);
	OpenStreetMap street = new OpenStreetMap();
	
	// set name
	if (!isEmptyField(fields, 1, false)) {
	    street.setName(fields[1].trim());
	}

	if (!isEmptyField(fields, 2, false)) {
	    try {
		Point location = (Point) GeolocHelper.convertFromHEXEWKBToGeometry(fields[2]);
		street.setLocation(location);
	    } catch (RuntimeException e) {
		logger.warn("can not parse location for "+fields[1]+" : "+e);
	    }
	}
	
	if (!isEmptyField(fields, 3, false)) {
	    street.setLength(new Double(fields[3].trim()));
	}
	
	if (!isEmptyField(fields, 4, false)) {
	    street.setCountryCode(fields[4].trim());
	}
	
		GeneratedId= GeneratedId+1;
	        street.setGid(new Long(GeneratedId));
	
	if (!isEmptyField(fields, 6, false)) {
	    StreetType type;
	    try {
		type = StreetType.valueOf(fields[6].toUpperCase());
		street.setStreetType(type);
	    } catch (Exception e) {
		logger.warn("can not determine streetType for "+fields[1]+" : "+e);
	    }
	    
	}
	
	if (!isEmptyField(fields, 7, false)) {
	    boolean oneWay = false;
	    try {
		oneWay  = Boolean.valueOf(fields[7]);
		street.setOneWay(oneWay);
	    } catch (Exception e) {
		logger.warn("can not determine streetType for "+fields[1]+" : "+e);
	    }
	    
	}
	
	if (!isEmptyField(fields, 8, true)) {
	    try {
		street.setShape((MultiLineString)GeolocHelper.convertFromHEXEWKBToGeometry(fields[8]));
	    } catch (RuntimeException e) {
		logger.warn("can not parse shape for "+fields[1] +" : "+e);
	    }
	    
	}
		
	openStreetMapDao.save(street);

    }
    
   
    
    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldBeSkiped()
     */
    @Override
    protected boolean shouldBeSkipped() {
	return !importerConfig.isOpenstreetmapImporterEnabled();
    }
    
   


    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#setCommitFlushMode()
     */
    @Override
    protected void setCommitFlushMode() {
	openStreetMapDao.flushAndClear();

    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldIgnoreComments()
     */
    @Override
    protected boolean shouldIgnoreComments() {
	return true;
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldIgnoreFirstLine()
     */
    @Override
    protected boolean shouldIgnoreFirstLine() {
	return false;
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.IGeonamesProcessor#rollback()
     */
    public List<NameValueDTO<Integer>> rollback() {
	// TODO osm 
	return null;
    }
    
    /**
     * @param openStreetMapDao the openStreetMapDao to set
     */
    public void setOpenStreetMapDao(OpenStreetMapDao openStreetMapDao) {
        this.openStreetMapDao = openStreetMapDao;
    }

   
    
}
