package com.gisgraphy.domain.geoloc.importer;

import junit.framework.Assert;

import org.junit.Test;

import com.gisgraphy.domain.repository.AbstractTransactionalTestCase;
import com.gisgraphy.domain.valueobject.ImporterStatusDto;

public class GeonamesDatabaseIndexerTest extends AbstractTransactionalTestCase {
    
    public GeonamesDatabaseIndexer geonamesDatabaseIndexer;
    
    @Test
    public void testProcess(){
	geonamesDatabaseIndexer.process();
	assertEquals("statusMessage should be null if the process is ok","", geonamesDatabaseIndexer.getStatusMessage());
	ImporterStatusDto status = new ImporterStatusDto(geonamesDatabaseIndexer);
	assertEquals(100, status.getPercent());
    }
    
    @Test
    public void testShouldBeSkiped(){
	ImporterConfig importerConfig = new ImporterConfig();
	GeonamesDatabaseIndexer geonamesDatabaseIndexerTobeSkipped = new GeonamesDatabaseIndexer();
	geonamesDatabaseIndexerTobeSkipped.setImporterConfig(importerConfig);
	
	importerConfig.setGeonamesImporterEnabled(false);
	Assert.assertTrue(geonamesDatabaseIndexerTobeSkipped.shouldBeSkipped());
	
	importerConfig.setGeonamesImporterEnabled(true);
	Assert.assertFalse(geonamesDatabaseIndexerTobeSkipped.shouldBeSkipped());
    }

    
    public void setGeonamesDatabaseIndexer(GeonamesDatabaseIndexer geonamesDatabaseIndexer) {
        this.geonamesDatabaseIndexer = geonamesDatabaseIndexer;
    }
    
    

}
