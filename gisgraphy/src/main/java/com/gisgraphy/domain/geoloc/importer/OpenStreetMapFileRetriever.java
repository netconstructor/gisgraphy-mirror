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
package com.gisgraphy.domain.geoloc.importer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.gisgraphy.helper.Untar;


/**
 * Retrieve The Geonames files from a server
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public class OpenStreetMapFileRetriever extends AbstractFileRetriever {

  

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractFileRetriever#getDownloadDirectory()
     */
    public String getDownloadDirectory() {
	return importerConfig.getOpenStreetMapDir();
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractFileRetriever#getDownloadBaseUrl()
     */
    public String getDownloadBaseUrl() {
	return importerConfig
	    .getOpenstreetMapDownloadURL();
    }
    
    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractFileRetriever#decompressFiles()
     */
    public void decompressFiles() throws IOException {
	File[] filesToUntar = ImporterHelper
		.listTarFiles(getDownloadDirectory());
	for (int i = 0; i < filesToUntar.length; i++) {
	    Untar untar = new Untar(filesToUntar[i].getAbsolutePath(),new File(getDownloadDirectory()));
	    untar.untar();
	}

	// for log purpose
	File[] filesToImport = ImporterHelper
		.listCountryFilesToImport(getDownloadDirectory());

	for (int i = 0; i < filesToImport.length; i++) {
	    logger.info("the files " + filesToImport[i].getName()
		    + " will be imported for openstreetMap");
	}
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractFileRetriever#shouldBeSkipped()
     */
    @Override
    public boolean shouldBeSkipped() {
	return !(importerConfig.isRetrieveFiles()  && importerConfig.isOpenstreetmapImporterEnabled());
    }

    /* (non-Javadoc)
     * @see com.gisgraphy.domain.geoloc.importer.AbstractFileRetriever#getFilesToDownload()
     */
    @Override
    List<String> getFilesToDownload() {
	return importerConfig.getOpenStreetMapDownloadFilesListFromOption();
    }

}
