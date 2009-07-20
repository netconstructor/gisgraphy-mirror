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
package com.gisgraphy.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

/**
 * Utility class to untar files, file can be gziped or Bzip2
 * 
 *  @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 * 
 */
public class Untar {
    private String tarFileName;
    private File dest;

  
    /**
     * @param tarFileName the path to the file we want to untar
     * @param dest the pat where the file should be untar
     */
    public Untar(String tarFileName, File dest) {
	this.tarFileName = tarFileName;
	this.dest = dest;
    }

   /* public static void main(String args[]) {
	try {
	    Untar untar = new Untar("/home/dmasclet/street/done/csv/files.tar.bz2", new File("/home/dmasclet/decompress"));
	    untar.untar();
	} catch (Exception e) {
	}

    }*/

    private InputStream getDecompressedInputStream(final String name, final InputStream istream) throws IOException {
	if (name == null) {
	    throw new RuntimeException("fileName to decompress can not be null");
	}
	if (name.toLowerCase().endsWith("gzip")|| name.toLowerCase().endsWith("gz")) {
	    return new BufferedInputStream(new GZIPInputStream(istream));
	} else  if (name.toLowerCase().endsWith("bz2") || name.toLowerCase().endsWith("bzip2")) {
		final char[] magic = new char[] { 'B', 'Z' };
		for (int i = 0; i < magic.length; i++) {
		    if (istream.read() != magic[i]) {
			throw new RuntimeException("Invalid bz2 file." + name);
		    }
		}
		return new BufferedInputStream(new CBZip2InputStream(istream));
	    }
	else if (name.toLowerCase().endsWith("tar")){
	    return istream;
	}
	throw new RuntimeException("can only detect compression for extension tar, gzip, gz, bz2, or bzip2");
    }

    /**
     * process the untar
     * @throws IOException
     */
    public void untar() throws IOException {

	TarInputStream tin = null;
	try {
	    if (!dest.exists()) {
		dest.mkdir();
	    }

	    tin = new TarInputStream(getDecompressedInputStream(tarFileName, new FileInputStream(new File(tarFileName))));

	    TarEntry tarEntry = tin.getNextEntry();

	    while (tarEntry != null) {
		File destPath = new File(dest.toString() + File.separatorChar + tarEntry.getName());
		if (tarEntry.isDirectory()) {
		    destPath.mkdir();
		} else {
		    FileOutputStream fout = new FileOutputStream(destPath);
		    try {
			tin.copyEntryContents(fout);
		    } finally {
			fout.flush();
			fout.close();
		    }
		}
		tarEntry = tin.getNextEntry();
	    }
	} finally {
	    if (tin != null) {
		tin.close();
	    }
	}

    }
}