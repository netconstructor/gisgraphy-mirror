package com.gisgraphy.geocoding;

public class GeocodingException extends RuntimeException {

    public GeocodingException() {
    }

    public GeocodingException(String message) {
	super(message);
    }

    public GeocodingException(Throwable cause) {
	super(cause);
    }

    public GeocodingException(String message, Throwable cause) {
	super(message, cause);
    }

}
