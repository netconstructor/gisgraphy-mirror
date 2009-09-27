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

import org.junit.Assert;
import org.junit.Test;


public class ImporterConfigTest {
    
    @Test
    public void testSetOpenStreetMapDirShouldAddFileSeparatorIfItDoesnTEndsWithFileSeparator(){
	String OpenStreetMapDir = "Test";
	ImporterConfig importerConfig = new ImporterConfig();
	importerConfig.setOpenStreetMapDir(OpenStreetMapDir);
	Assert.assertTrue("setOpenStreetMapDir should add File separator",importerConfig.getOpenStreetMapDir().endsWith(File.separator));
	Assert.assertEquals(OpenStreetMapDir+File.separator,importerConfig.getOpenStreetMapDir());
    }
    
    @Test
    public void testSetGeonamesDirShouldAddFileSeparatorIfItDoesnTEndsWithFileSeparator(){
	String geonamesDir = "Test";
	ImporterConfig importerConfig = new ImporterConfig();
	importerConfig.setGeonamesDir(geonamesDir);
	Assert.assertTrue("setGeonamesDir should add File separator",importerConfig.getGeonamesDir().endsWith(File.separator));
	Assert.assertEquals(geonamesDir+File.separator,importerConfig.getGeonamesDir());
    }
    
    @Test
    public void testIsGeonamesImporterShouldBeTrueByDefault(){
	ImporterConfig importerConfig = new ImporterConfig();
	Assert.assertTrue("Geonames importer should be enabled by default ",importerConfig.isGeonamesImporterEnabled());
    }
    
    @Test
    public void testIsOpenstreetmapImporterShouldBeTrueByDefault(){
	ImporterConfig importerConfig = new ImporterConfig();
	Assert.assertTrue("OpenStreetMap importer should be enabled by default ",importerConfig.isOpenstreetmapImporterEnabled());
    }

}
