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
package com.gisgraphy.domain.geoloc.entity;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.gisgraphy.domain.geoloc.service.geoloc.street.StreetType;

/**
 * Represents a {@link Street}.
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Street extends GisFeature {

    private boolean oneWay;
    private StreetType streetType;
    private Double length;
    
    /**
     * Override the gisFeature value.<br>
     * Default to true;<br>
     * If this field is set to false, then the object won't be synchronized with
     * the fullText search engine
     */
    @Override
    @Transient
    public boolean isFullTextSearchable() {
	return true;
    }

    /**
     * Constructor that populate the {@link Street} with the gisFeature fields<br>
     * 
     * @param gisFeature
     *                The gisFeature from which we want to populate the
     *                {@link Street}
     */
    public Street(GisFeature gisFeature) {
	super(gisFeature);
    }

    /**
     * Needed by CGLib
     */
    public Street() {
	super();
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public StreetType getStreetType() {
        return streetType;
    }

    public void setStreetType(StreetType streetType) {
        this.streetType = streetType;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

}
