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
package com.gisgraphy.domain.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleProjection;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernatespatial.GeometryUserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.entity.ZipCode;
import com.gisgraphy.domain.geoloc.entity.ZipCodesAware;
import com.gisgraphy.domain.geoloc.entity.event.EventManager;
import com.gisgraphy.domain.geoloc.entity.event.GisFeatureDeleteAllEvent;
import com.gisgraphy.domain.geoloc.entity.event.GisFeatureDeletedEvent;
import com.gisgraphy.domain.geoloc.entity.event.GisFeatureStoredEvent;
import com.gisgraphy.domain.geoloc.entity.event.PlaceTypeDeleteAllEvent;
import com.gisgraphy.domain.geoloc.importer.ImporterConfig;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.FullTextFields;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.IsolrClient;
import com.gisgraphy.domain.valueobject.Constants;
import com.gisgraphy.domain.valueobject.GisFeatureDistance;
import com.gisgraphy.helper.GeolocHelper;
import com.gisgraphy.helper.IntrospectionHelper;
import com.gisgraphy.hibernate.criterion.DistanceRestriction;
import com.gisgraphy.hibernate.criterion.ProjectionOrder;
import com.gisgraphy.hibernate.criterion.ResultTransformerUtil;
import com.gisgraphy.hibernate.projection.ProjectionBean;
import com.gisgraphy.hibernate.projection.SpatialProjection;
import com.ibm.icu.lang.UCharacter.JoiningType;
import com.vividsolutions.jts.geom.Point;

/**
 * Generic Dao for Gis Object (java-5 meaning) It suppose that the PK is of type
 * long because its goal is to be used with class gisfeatures and class that
 * extends GisFeature. if it is note the case. it is possible to create an other
 * inteface<br>
 * it adds some method to the GenericDao in order to acess GIS objects
 * 
 * @see GenericDao
 * @param <T>
 *                the type of the object the Gis Dao apply
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public class GenericGisDao<T extends GisFeature> extends
	GenericDao<T, java.lang.Long> implements IGisDao<T> {

    public static final Type GEOMETRY_TYPE = new CustomType(
	    GeometryUserType.class, null);

    public static final int MAX_FULLTEXT_RESULTS = 100;

    @Autowired
    @Qualifier("solrClient")
    private IsolrClient solrClient;

    private EventManager eventManager;

    /**
     * Constructor
     * 
     * @param persistentClass
     *                The specified Class for the GenericGisDao
     */
    public GenericGisDao(final Class<T> persistentClass) {
	super(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getNearestAndDistanceFrom(com.gisgraphy.domain.geoloc.entity.GisFeature,
     *      double, int, int)
     */
    public List<GisFeatureDistance> getNearestAndDistanceFromGisFeature(
	    final GisFeature gisFeature, final double distance,
	    final int firstResult, final int maxResults) {
	return getNearestAndDistanceFrom(gisFeature.getLocation(), gisFeature
		.getId(), distance, firstResult, maxResults, persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getNearestAndDistanceFrom(com.gisgraphy.domain.geoloc.entity.GisFeature,
     *      double)
     */
    public List<GisFeatureDistance> getNearestAndDistanceFromGisFeature(
	    GisFeature gisFeature, double distance) {
	return getNearestAndDistanceFrom(gisFeature.getLocation(), gisFeature
		.getId(), distance, -1, -1, persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getNearestAndDistanceFrom(com.vividsolutions.jts.geom.Point,
     *      double)
     */
    public List<GisFeatureDistance> getNearestAndDistanceFrom(Point point,
	    double distance) {
	return getNearestAndDistanceFrom(point, 0L, distance, -1, -1,
		persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getNearestAndDistanceFrom(com.vividsolutions.jts.geom.Point,
     *      double, int, int)
     */
    public List<GisFeatureDistance> getNearestAndDistanceFrom(Point point,
	    double distance, int firstResult, int maxResults) {
	return getNearestAndDistanceFrom(point, 0L, distance, firstResult,
		maxResults, persistentClass);
    }

    /**
     * base method for all findNearest* /**
     * 
     * @param point
     *                The point from which we want to find GIS Object
     * @param distance
     *                distance The radius in meters
     * @param pointId
     *                the id of the point that we don't want to be include, it
     *                is used to not include the gisFeature from which we want
     *                to find the nearest
     * @param firstResult
     *                the firstResult index (for pagination), numbered from 1,
     *                if < 1 : it will not be taken into account
     * @param maxResults
     *                The Maximum number of results to retrieve (for
     *                pagination), if <= 0 : it will not be taken into acount
     * @param requiredClass
     *                the class of the object to be retireved
     * @return A List of GisFeatureDistance with the nearest elements or an
     *         emptylist (never return null), ordered by distance.<u>note</u>
     *         the specified gisFeature will not be included into results
     * @see GisFeatureDistance
     * @return a list of gisFeature (never return null but an empty list)
     */
    @SuppressWarnings("unchecked")
    protected List<GisFeatureDistance> getNearestAndDistanceFrom(
	    final Point point, final Long pointId, final double distance,
	    final int firstResult, final int maxResults,
	    final Class<? extends GisFeature> requiredClass) {
	Assert.notNull(point);
	return (List<GisFeatureDistance>) this.getHibernateTemplate().execute(
		new HibernateCallback() {

		    public Object doInHibernate(Session session)
			    throws PersistenceException {
			Criteria criteria = session
				.createCriteria(requiredClass);
			
			if (maxResults > 0) {
			    criteria = criteria.setMaxResults(maxResults);
			}
			if (firstResult >= 1) {
			    criteria = criteria.setFirstResult(firstResult - 1);
			}
			criteria = criteria.add(new DistanceRestriction(point,
				distance));
			List<String> fieldList = IntrospectionHelper
				.getFieldsAsList(requiredClass);
			ProjectionList projections = ProjectionBean.fieldList(
				fieldList,true).add(
				SpatialProjection.distance_sphere(point,GisFeature.LOCATION_COLUMN_NAME).as(
					"distance"));
			
			criteria.setProjection(projections);
			if (pointId != 0) {
			    // remove The From Point
			    criteria = criteria.add(Restrictions.not(Restrictions.idEq(pointId)));
			}
			criteria.addOrder(new ProjectionOrder("distance"));

			criteria.setCacheable(true);
			List<Object[]> queryResults = criteria.list();
			
			String[] aliasList = (String[]) ArrayUtils
				.add(
					IntrospectionHelper
						.getFieldsAsArray(requiredClass),
					"distance");
			
			int idPropertyIndexInAliasList=0;
			for (int i=0;i<aliasList.length;i++){
			    if (aliasList[i]=="id"){
				idPropertyIndexInAliasList = i;
				break;
			    }
			}
			
			
			boolean hasZipCodesProperty = ZipCodesAware.class.isAssignableFrom(requiredClass);
			Map<Long, List<String>> idToZipCodesMap = null;
			if (hasZipCodesProperty && queryResults.size()>0){
			List<Long> ids = new ArrayList<Long>();
			for (Object[] tuple: queryResults){
			    ids.add((Long)tuple[idPropertyIndexInAliasList]);
			}
			String zipCodeQuery = "SELECT code as code,gisfeature as id FROM "+ZipCode.class.getSimpleName().toLowerCase() +" zip where zip.gisfeature in (:ids)" ;
			Query qry = session.createSQLQuery(zipCodeQuery).addScalar("code", Hibernate.STRING).addScalar("id", Hibernate.LONG);
			qry.setCacheable(true);

			qry.setParameterList("ids", ids);
			List<Object[]> zipCodes = (List<Object[]>) qry.list();
			
			if (zipCodes.size() > 0) {
			    idToZipCodesMap = new HashMap<Long, List<String>>();
			    for (Object[] zipCode : zipCodes){
				Long idFromZipcode = (Long) zipCode[1];
				List<String> zipCodesFromMap  = idToZipCodesMap.get(idFromZipcode);
				if (zipCodesFromMap == null){
				    List<String> zipCodesToAdd = new ArrayList<String>();
				    idToZipCodesMap.put(idFromZipcode, zipCodesToAdd);
				    zipCodesFromMap = zipCodesToAdd;
				} 
				zipCodesFromMap.add((String)zipCode[0]);
			    }
			}
			
			}
			List<GisFeatureDistance> results = ResultTransformerUtil
			.transformToGisFeatureDistance(
					aliasList,
				queryResults,idToZipCodesMap);
			return results;
		    }
		});

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#findByName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<T> listByName(final String name) {
	Assert.notNull(name);
	return (List<T>) this.getHibernateTemplate().execute(
		new HibernateCallback() {

		    public Object doInHibernate(Session session)
			    throws PersistenceException {
			String queryString = "from "
				+ persistentClass.getSimpleName()
				+ " as c where c.name= ?";

			Query qry = session.createQuery(queryString);
			qry.setCacheable(true);

			qry.setParameter(0, name);
			List<T> results = (List<T>) qry.list();
			if (results == null) {
			    results = new ArrayList<T>();
			}
			return results;
		    }
		});
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getByFeatureId(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    public T getByFeatureId(final Long featureId) {
	Assert.notNull(featureId);
	return (T) this.getHibernateTemplate().execute(new HibernateCallback() {

	    public Object doInHibernate(Session session)
		    throws PersistenceException {
		String queryString = "from " + persistentClass.getSimpleName()
			+ " as g where g.featureId= ?";

		Query qry = session.createQuery(queryString);
		qry.setCacheable(true);

		qry.setParameter(0, featureId);
		T result = (T) qry.uniqueResult();

		return result;
	    }
	});
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getDirty()
     */
    @SuppressWarnings("unchecked")
    public List<T> getDirties() {
	return (List<T>) this.getHibernateTemplate().execute(
		new HibernateCallback() {

		    public Object doInHibernate(final Session session)
			    throws PersistenceException {
			final String queryString = "from "
				+ persistentClass.getSimpleName()
				+ " as g where g.featureCode ='"
				+ ImporterConfig.DEFAULT_FEATURE_CODE
				+ "' or g.location=? or g.featureClass='"
				+ ImporterConfig.DEFAULT_FEATURE_CLASS + "'";

			final Query qry = session.createQuery(queryString);
			qry.setParameter(0, GeolocHelper.createPoint(0F, 0F),
				GEOMETRY_TYPE);
			qry.setCacheable(true);

			List<T> result = (List<T>) qry.list();
			if (result == null) {
			    result = new ArrayList<T>();
			}

			return result;
		    }
		});
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.GenericDao#save(java.lang.Object)
     */
    @Override
    public T save(T GisFeature) {
	T savedgisFeature = super.save(GisFeature);
	GisFeatureStoredEvent CreatedEvent = new GisFeatureStoredEvent(
		GisFeature);
	eventManager.handleEvent(CreatedEvent);
	return savedgisFeature;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.GenericDao#remove(java.lang.Object)
     */
    @Override
    public void remove(T gisFeature) {
	super.remove(gisFeature);
	GisFeatureDeletedEvent gisFeatureDeletedEvent = new GisFeatureDeletedEvent(
		gisFeature);
	eventManager.handleEvent(gisFeatureDeletedEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getByFeatureIds(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public List<T> listByFeatureIds(final List<Long> ids) {
	if (ids == null || ids.size() == 0) {
	    return new ArrayList<T>();
	}
	return (List<T>) this.getHibernateTemplate().execute(
		new HibernateCallback() {

		    public Object doInHibernate(final Session session)
			    throws PersistenceException {
			final String queryString = "from "
				+ persistentClass.getSimpleName()
				+ " as g where g.featureId in (:ids)";

			final Query qry = session.createQuery(queryString);
			qry.setParameterList("ids", ids);
			qry.setCacheable(true);

			List<T> result = (List<T>) qry.list();
			if (result == null) {
			    result = new ArrayList<T>();
			}

			return result;
		    }
		});
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getFromText(java.lang.String,
     *      boolean)
     */
    public List<T> listFromText(String name, boolean includeAlternateNames) {
	return listFromText(name, includeAlternateNames, persistentClass);
    }

    /**
     * Do a full text search for the given name. The search will be case,
     * iso-latin, comma-separated insensitive<br>
     * search for 'saint-André', 'saint-Andre', 'SaInT-andré', 'st-andré', etc
     * will return the same results. <u>note</u> : search for zipcode too
     * Polymorphism is not supported, e.g : if clazz=GisFeature : the results
     * will only be of that type and no other feature type (e.g : City that
     * extends gisFeature...etc) will be returned. The results will be sort by
     * relevance.
     * 
     * @param name
     *                The name to search for
     * @param includeAlternateNames
     *                Wether we search in the alternatenames too
     * @param clazz
     *                specify the features we want to search for, if null : no
     *                restriction is apply
     * @return a list of gisFeatures of type of the class for the given text.
     *         the max list size is {@link GenericGisDao#MAX_FULLTEXT_RESULTS};
     * @see IGisFeatureDao#listAllFeaturesFromText(String, boolean)
     */
    protected List<T> listFromText(String name, boolean includeAlternateNames,
	    Class<T> clazz) {
	logger.debug("getFromText " + name);
	// Set up a simple query
	// Check for a null or empty string query
	if (name == null || name.length() == 0) {
	    return new ArrayList<T>();
	}

	SolrQuery query = new SolrQuery();
	String namefield = FullTextFields.ALL_NAME.getValue();
	if (!includeAlternateNames) {
	    namefield = FullTextFields.NAME.getValue();
	}
	String queryString = "(" + namefield + ":\"" + name + "\" OR "
		+ FullTextFields.ZIPCODE.getValue() + ":\"" + name + "\")";
	if (clazz != null) {
	    queryString += " AND placetype:" + persistentClass.getSimpleName();
	}
	query.setQuery(queryString);
	query.setQueryType(Constants.SolrQueryType.advanced.toString());
	query.setFields(FullTextFields.FEATUREID.getValue());
	query.setRows(MAX_FULLTEXT_RESULTS);

	QueryResponse results = null;
	try {
	    results = solrClient.getServer().query(query);
	} catch (SolrServerException e) {
	    throw new RuntimeException(e);
	}

	List<Long> ids = new ArrayList<Long>();
	for (SolrDocument doc : results.getResults()) {
	    ids.add((Long) doc.getFieldValue(FullTextFields.FEATUREID
		    .getValue()));
	}
	// log
	List<T> gisFeatureList = this.listByFeatureIds(ids);
	if (logger.isDebugEnabled()) {
	    logger.debug("search on " + name + " returns "
		    + gisFeatureList.size());
	    for (GisFeature gisFeature : gisFeatureList) {
		logger.debug("search on " + name + " returns "
			+ gisFeature.getName());
	    }
	}

	return gisFeatureList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.GenericDao#deleteAll(java.util.List)
     */
    @Override
    public void deleteAll(List<T> list) {
	super.deleteAll(list);
	GisFeatureDeleteAllEvent gisFeatureDeleteAllEvent = new GisFeatureDeleteAllEvent(
		list);
	eventManager.handleEvent(gisFeatureDeleteAllEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.GenericDao#deleteAll()
     */
    @Override
    public int deleteAll() {
	int numberOfGisDeleted = super.deleteAll();
	PlaceTypeDeleteAllEvent placeTypeDeleteAllEvent = new PlaceTypeDeleteAllEvent(
		this.getPersistenceClass());
	eventManager.handleEvent(placeTypeDeleteAllEvent);
	return numberOfGisDeleted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gisgraphy.domain.repository.IGisDao#getEager(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    public T getEager(final Long id) {
	Assert.notNull(id, "Can not retrieve an Ogject with a null id");
	T returnValue = null;
	try {
	    return (T) this.getHibernateTemplate().execute(
		    new HibernateCallback() {

			public Object doInHibernate(Session session)
				throws PersistenceException {
			    String queryString = "from "
				    + persistentClass.getSimpleName()
				    + " o where o.id=" + id;

			    Query qry = session.createQuery(queryString);
			    qry.setCacheable(true);
			    GisFeature feature = (GisFeature) qry
				    .uniqueResult();

			    feature.getAdm().getAdm1Code();
			    feature.getAlternateNames().size();
			    return feature;

			}
		    });
	} catch (Exception e) {
	    logger.info("could not retrieve object of type "
		    + persistentClass.getSimpleName() + " with id " + id, e);
	}
	return returnValue;
    }
    
    

    @Required
    public void setEventManager(EventManager eventManager) {
	this.eventManager = eventManager;
    }
    
    
    /* (non-Javadoc)
     * @see com.gisgraphy.domain.repository.IGisDao#createGISTIndexForLocationColumn()
     */
    public void createGISTIndexForLocationColumn() {
	 this.getHibernateTemplate().execute(
			 new HibernateCallback() {

			    public Object doInHibernate(Session session)
				    throws PersistenceException {
				session.flush();
				logger.info("will create GIST index for  "+persistentClass.getSimpleName());
				String createIndex = "CREATE INDEX locationIndex"+persistentClass.getSimpleName()+" ON "+persistentClass.getSimpleName().toLowerCase()+" USING GIST (location)";  
				Query createIndexQuery = session.createSQLQuery(createIndex);
				createIndexQuery.executeUpdate();
				return null;
			    }
			});
    }

}
