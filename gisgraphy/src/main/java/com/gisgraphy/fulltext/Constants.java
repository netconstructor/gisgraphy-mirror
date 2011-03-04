package com.gisgraphy.fulltext;

import com.gisgraphy.domain.geoloc.entity.Adm;
import com.gisgraphy.domain.geoloc.entity.City;
import com.gisgraphy.domain.geoloc.entity.CitySubdivision;
import com.gisgraphy.domain.geoloc.service.fulltextsearch.OutputStyleHelper;

public class Constants {
	
		/**
		 * convenence placetype for only city
		 */
		public final static Class[] ONLY_CITY_PLACETYPE = new Class[]{City.class};
		/**
		 * convenence placetype for only adm
		 */
		public final static Class[] ONLY_ADM_PLACETYPE = new Class[]{Adm.class};
		
		 /**
		 * convenence placetype for city and citySubdivision
		 */
		public final static Class[] CITY_AND_CITYSUBDIVISION_PLACETYPE = new Class[] {City.class,CitySubdivision.class};

}
