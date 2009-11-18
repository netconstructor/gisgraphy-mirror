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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


/**
 * Interface that describe usefull function to manage the database 
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public interface IDatabaseHelper {

    /**
     * @param file the file to execute, it will be read as an utf-8 file
     * @param continueOnError if an error occured, the process will go on if this value is true, if not it will throw an exception
     * @throws Exception in case of error during execution, or if the file is null or does not exist
     * @return true if no error or false if an error occured and continueOnError is true)
     */
    public boolean execute(final File file, boolean continueOnError) throws Exception;
    
    /**
     * This method will drop all the tables related to Geonames
     * 
     */
    public void DropGeonamesTables();
    
    /**
     * This method will recreate all the tables related to Geonames
     * 
     */
    public void CreateGeonamesTables();
    
  

}