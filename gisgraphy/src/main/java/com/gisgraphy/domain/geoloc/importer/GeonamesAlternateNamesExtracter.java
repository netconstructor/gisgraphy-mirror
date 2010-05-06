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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.gisgraphy.domain.repository.IOpenStreetMapDao;
import com.gisgraphy.domain.repository.OpenStreetMapDao;
import com.gisgraphy.domain.valueobject.Constants;
import com.gisgraphy.domain.valueobject.NameValueDTO;

/**
 * Extract the alternateNames into separate files : one for country, one for adm1 and one for adm2
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public class GeonamesAlternateNamesExtracter extends AbstractImporterProcessor {

    public static final String ALTERNATE_NAMES_COUNTRY_FILENAME = "alternateNames-country.txt";

    public static final String ALTERNATE_NAMES_ADM2_FILENAME = "alternateNames-adm2.txt";

    public static final String ALTERNATE_NAMES_ADM1_FILENAME = "alternateNames-adm1.txt";
    
    public static final String ALTERNATE_NAMES_FEATURES_FILENAME = "alternateNames-features.txt";

    private File adm1file;

    private File adm2file;

    private File countryFile;
    
    private File featuresFile;

    

    private OutputStreamWriter adm1fileOutputStreamWriter;

    private OutputStreamWriter adm2fileOutputStreamWriter;

    private OutputStreamWriter countryfileOutputStreamWriter;
    
    private OutputStreamWriter featuresfileOutputStreamWriter;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm:ss");

    private StringBuffer sb = new StringBuffer();

    @Autowired
    private IOpenStreetMapDao openStreetMapDao;


    /**
     * Default Constructor
     */
    public GeonamesAlternateNamesExtracter() {
	super();

    }

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
	 * ----------------------------------------- 0 : alternateNameId : 1 :
	 * geonameid : 2 : isolanguage : iso 639-2 or 3 or or 'post' 3 :
	 * alternate name 4 : isPreferredName 5 : isShortName
	 */

	// isEmptyField(fields,0,true);
	// isEmptyField(fields,1,true);
	if (!isEmptyField(fields, 1, false)) {
	    // fields = ImporterHelper.virtualizeADMD(fields);
	    if (lineIsAnAlternateNameForCountry(fields[1])) {
		writeAlternateName(countryfileOutputStreamWriter,line);
	    } else if (lineIsAnnAlternateNameForAdm1(fields[1])) {
		writeAlternateName(adm1fileOutputStreamWriter,line);
	    } else if (lineIsAnAlternatNameForAdm2(fields[1])) {
		writeAlternateName(adm2fileOutputStreamWriter,line);
	    }else {
		writeAlternateName(featuresfileOutputStreamWriter,line);
	    }
	} else {
	    logger.info("geonameid is null for geonames alternateNameId" + fields[0]);
	}
    }
    
  

    private boolean lineIsAnAlternatNameForAdm2(String geonameid) {
	return false;
    }

    private boolean lineIsAnnAlternateNameForAdm1(String geonameid) {
	return false;
    }

    private boolean lineIsAnAlternateNameForCountry(String geonameid) {
	return false;
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#shouldBeSkiped()
     */
    @Override
    public boolean shouldBeSkipped() {
	return !importerConfig.isGeonamesImporterEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#tearDown()
     */
    @Override
    protected void tearDown() {
	super.tearDown();
	closeOutputStreams();
    }

   

    

    private void writeAlternateName(OutputStreamWriter outputStreamWriter, String line) {
	if (outputStreamWriter != null) {
		try {
		    outputStreamWriter.write(line);
		    flushAndClear();
		} catch (IOException e) {
		    throw new RuntimeException(
			    "an error has occurred when writing in adm4 file",
			    e);
		}
	    }
    }


    private void closeOutputStreams() {
	if (adm1fileOutputStreamWriter != null) {
	    try {
		adm1fileOutputStreamWriter.close();
	    } catch (IOException e) {
		throw new RuntimeException("can not close adm1 outputStream", e);
	    }
	}
	if (adm2fileOutputStreamWriter != null) {
	    try {
		adm2fileOutputStreamWriter.close();
	    } catch (IOException e) {
		throw new RuntimeException("can not close adm2 outputStream", e);
	    }
	}
	if (countryfileOutputStreamWriter != null) {
	    try {
		countryfileOutputStreamWriter.close();
	    } catch (IOException e) {
		throw new RuntimeException("can not close country outputStream", e);
	    }
	}
	
	if (featuresfileOutputStreamWriter != null) {
	    try {
		featuresfileOutputStreamWriter.close();
	    } catch (IOException e) {
		throw new RuntimeException("can not close features outputStream", e);
	    }
	}
    }

   
    private OutputStreamWriter getWriter(File file)
	    throws FileNotFoundException {
	OutputStream o = null;
	OutputStreamWriter w = null;
	try {
	    if (file.exists()) {
		checkWriteRights(file);
		if (!file.delete()){
			 throw new RuntimeException("The file "+file.getAbsolutePath()+" exists but we can not delete it, to recreate it");    
		}
		return w;
	    } 
		o = new BufferedOutputStream(new FileOutputStream(file));
		w = new OutputStreamWriter(o, Constants.CHARSET);
		return w;
	} catch (UnsupportedEncodingException e) {
	    logger.warn("UnsupportedEncodingException for " + Constants.CHARSET
		    + " : Can not extract Alternate names");
	    return null;
	}

    }

    private void initFiles() {
	adm1file = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_ADM1_FILENAME);
	adm2file = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_ADM2_FILENAME);
	countryFile = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_COUNTRY_FILENAME);
	featuresFile = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_FEATURES_FILENAME);
	try {
	    adm1fileOutputStreamWriter = getWriter(adm1file);
	    adm2fileOutputStreamWriter = getWriter(adm2file);
	    countryfileOutputStreamWriter = getWriter(countryFile);
	    featuresfileOutputStreamWriter = getWriter(featuresFile);
	} catch (FileNotFoundException e) {
	    closeOutputStreams();
	    throw new RuntimeException(
		    "An error has occurred during creation of outpuStream : "
			    + e.getMessage(), e);
	}
    }

    /**
     * 

    /**
     * @param file
     */
    private void checkWriteRights(File file) {
	if (!file.canWrite()) {
	    throw new RuntimeException(
		    "you must have write rights in order to export adm in file "
			    + file.getAbsolutePath());
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#setup()
     */
    @Override
    public void setup() {
	super.setup();
	initFiles();
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
	return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#flushAndClear()
     */
    @Override
    protected void flushAndClear() {
	if (adm1fileOutputStreamWriter != null) {
	    try {
		adm1fileOutputStreamWriter.flush();
	    } catch (IOException e) {
		closeOutputStreams();
		throw new RuntimeException("can not flush adm1file : "
			+ e.getMessage(), e);
	    }
	}
	if (adm2fileOutputStreamWriter != null) {
	    try {
		adm2fileOutputStreamWriter.flush();
	    } catch (IOException e) {
		closeOutputStreams();
		throw new RuntimeException("can not flush adm2file : "
			+ e.getMessage(), e);
	    }
	}
	if (countryfileOutputStreamWriter != null) {
	    try {
		countryfileOutputStreamWriter.flush();
	    } catch (IOException e) {
		closeOutputStreams();
		throw new RuntimeException("can not flush countryfile : "
			+ e.getMessage(), e);
	    }
	}
	if (featuresfileOutputStreamWriter != null) {
	    try {
		featuresfileOutputStreamWriter.flush();
	    } catch (IOException e) {
		closeOutputStreams();
		throw new RuntimeException("can not flush featuresfile : "
			+ e.getMessage(), e);
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#getNumberOfColumns()
     */
    @Override
    protected int getNumberOfColumns() {
	return 6;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.AbstractImporterProcessor#getFiles()
     */
    @Override
    protected File[] getFiles() {
	if (importerConfig.isImportGisFeatureEmbededAlternateNames()) {
	    logger
		    .info("ImportGisFeatureEmbededAlternateNames = true, we do not need to extract alternatenames from "
			    + importerConfig.getAlternateNamesFileName());
	    return new File[0];
	}
	File[] files = new File[1];
	files[0] = new File(importerConfig.getGeonamesDir()
		+ importerConfig.getAlternateNamesFileName());
	return files;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.geoloc.importer.IGeonamesProcessor#rollback()
     */
    public List<NameValueDTO<Integer>> rollback() {
	List<NameValueDTO<Integer>> deletedObjectInfo = new ArrayList<NameValueDTO<Integer>>();
	adm1file = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_ADM1_FILENAME);
	deleteFile(adm1file, deletedObjectInfo);
	adm2file = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_ADM2_FILENAME);
	deleteFile(adm2file, deletedObjectInfo);
	countryFile = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_COUNTRY_FILENAME);
	deleteFile(countryFile, deletedObjectInfo);
	featuresFile = new File(importerConfig.getGeonamesDir()
		+ ALTERNATE_NAMES_FEATURES_FILENAME);
	deleteFile(featuresFile, deletedObjectInfo);
	resetStatus();
	return deletedObjectInfo;
    }

    private void deleteFile(File file,
	    List<NameValueDTO<Integer>> deletedObjectInfo) {
	if (file.delete()) {
	    deletedObjectInfo.add(new NameValueDTO<Integer>(file.getName(), 1));
	    logger.info("File " + file.getName() + " has been deleted");
	} else {
	    deletedObjectInfo.add(new NameValueDTO<Integer>(file.getName(), 0));
	    logger.info("File " + file.getName() + " has not been deleted");
	}
    }

}