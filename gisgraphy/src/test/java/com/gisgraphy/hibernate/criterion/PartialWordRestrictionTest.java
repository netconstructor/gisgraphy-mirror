package com.gisgraphy.hibernate.criterion;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projection;
import org.hibernate.transform.Transformers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.gisgraphy.domain.geoloc.entity.OpenStreetMap;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.AbstractIntegrationHttpSolrTestCase;
import com.gisgraphy.domain.geoloc.service.geoloc.street.StreetType;
import com.gisgraphy.domain.repository.IOpenStreetMapDao;
import com.gisgraphy.helper.GeolocHelper;
import com.gisgraphy.helper.StringHelper;
import com.gisgraphy.hibernate.projection.ProjectionBean;
import com.gisgraphy.hibernate.projection._OpenstreetmapDTO;
import com.gisgraphy.test._DaoHelper;
import com.vividsolutions.jts.geom.MultiLineString;


public class PartialWordRestrictionTest extends AbstractIntegrationHttpSolrTestCase{
    
    private _DaoHelper testDao;

    private IOpenStreetMapDao openStreetMapDao;
    
    private OpenStreetMap createOpenStreetMap() {
	OpenStreetMap streetOSM = new OpenStreetMap();
	String[] wktLineStrings={"LINESTRING (30.001 30.001, 40 40)"};
	MultiLineString shape = GeolocHelper.createMultiLineString(wktLineStrings);
	streetOSM.setShape(shape);
	streetOSM.setGid(1L);
	streetOSM.setOneWay(false);
	streetOSM.setStreetType(StreetType.FOOTWAY);
	streetOSM.setName("Champs-Elysées");
	streetOSM.setLocation(GeolocHelper.createPoint(30.001F, 40F));
	
	return StringHelper.updateOpenStreetMapEntityForIndexation(streetOSM);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testPartialWordRestriction() {
	OpenStreetMap streetOSM = createOpenStreetMap();
	openStreetMapDao.save(streetOSM);
	assertNotNull(openStreetMapDao.get(streetOSM.getId()));
	
	
	int numberOfLineUpdated = openStreetMapDao.updateTS_vectorColumnForStreetNameSearch();
	assertEquals("It should have 2 lines updated : one for partial and one for fulltext",2, numberOfLineUpdated);
	
	
	HibernateCallback hibernateCallback = new HibernateCallback() {

	    public Object doInHibernate(Session session)
		    throws PersistenceException {

		Criteria testCriteria = session.createCriteria(OpenStreetMap.class);
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("name");
		fieldList.add("gid");
		
		Projection projection = ProjectionBean.fieldList(fieldList, true);
		testCriteria.setProjection(projection).add(
			new PartialWordSearchRestriction(OpenStreetMap.PARTIALSEARCH_VECTOR_COLUMN_NAME, "elys"))//case sensitive accent
			.setResultTransformer(
				Transformers.aliasToBean(_OpenstreetmapDTO.class));

		List<_OpenstreetmapDTO> results = testCriteria.list();
		return results;
	    }
	};

	List<_OpenstreetmapDTO> streets = (List<_OpenstreetmapDTO>) testDao
		.testCallback(hibernateCallback);
	assertEquals(
		"According to the fulltext restriction, it should have a result (it should be case insensitive,accent insensitive and '-' insensitive",
		1, streets.size());
	
	assertEquals(
		"According to the fulltext restriction, the result is incorrect",
		streetOSM.getGid(), streets.get(0).getGid());
	
	HibernateCallback hibernateCallbackminusSign = new HibernateCallback() {

	    public Object doInHibernate(Session session)
		    throws PersistenceException {

		Criteria testCriteria = session.createCriteria(OpenStreetMap.class);
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("name");
		fieldList.add("gid");
		
		Projection projection = ProjectionBean.fieldList(fieldList, true);
		testCriteria.setProjection(projection).add(
			new PartialWordSearchRestriction(OpenStreetMap.PARTIALSEARCH_VECTOR_COLUMN_NAME, "mps ely"))
			.setResultTransformer(
				Transformers.aliasToBean(_OpenstreetmapDTO.class));

		List<_OpenstreetmapDTO> results = testCriteria.list();
		return results;
	    }
	};

	 streets = (List<_OpenstreetmapDTO>) testDao
		.testCallback(hibernateCallbackminusSign);
	assertEquals(
		"According to the fulltext restriction, it should have a result (it should be '-' insensitive",
		1, streets.size());
	
	assertEquals(
		"According to the fulltext restriction, the result is incorrect",
		streetOSM.getGid(), streets.get(0).getGid());
	
	HibernateCallback hibernateCallbackZeroResult = new HibernateCallback() {

	    public Object doInHibernate(Session session)
		    throws PersistenceException {

		Criteria testCriteria = session.createCriteria(OpenStreetMap.class);
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("name");
		fieldList.add("gid");
		
		Projection projection = ProjectionBean.fieldList(fieldList, true);
		testCriteria.setProjection(projection).add(
			new PartialWordSearchRestriction(OpenStreetMap.PARTIALSEARCH_VECTOR_COLUMN_NAME, "elysees champs"))//wrong word
			.setResultTransformer(
				Transformers.aliasToBean(_OpenstreetmapDTO.class));

		List<_OpenstreetmapDTO> results = testCriteria.list();
		return results;
	    }
	};

	 streets = (List<_OpenstreetmapDTO>) testDao
		.testCallback(hibernateCallbackZeroResult);
	assertEquals(
		"According to the fulltext restriction, it should not have result ",
		0, streets.size());
	

    }
    
    @Required
    public void setOpenStreetMapDao(IOpenStreetMapDao openStreetMapDao) {
        this.openStreetMapDao = openStreetMapDao;
    }

    @Required
    public void setTestDao(_DaoHelper testDao) {
	this.testDao = testDao;
    }

}
