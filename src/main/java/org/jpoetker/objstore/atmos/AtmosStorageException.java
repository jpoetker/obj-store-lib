package org.jpoetker.objstore.atmos;

import org.jpoetker.objstore.ObjectStorageException;

public class AtmosStorageException extends ObjectStorageException {

	private static final long serialVersionUID = 1L;
	private Integer httpCode = null;
	private Integer atmosCode = null;
	
	public AtmosStorageException() {
		super();
	}

	public AtmosStorageException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AtmosStorageException(String arg0) {
		super(arg0);
	}

	public AtmosStorageException(Throwable arg0) {
		super(arg0);
	}

	public AtmosStorageException(String message, int httpCode) {
		super(message);
		this.httpCode = new Integer(httpCode);
	}
	
	public AtmosStorageException(String message, int httpCode, Throwable t) {
		super(message, t);
		this.httpCode = new Integer(httpCode);
	}
	
	
	public AtmosStorageException(String message, int httpCode, int atmosCode) {
		super(message);
		this.httpCode = new Integer(httpCode);
		this.atmosCode = new Integer(atmosCode);
	}
	
	public AtmosStorageException(String message, int httpCode, int atmosCode, Throwable t) {
		super(message, t);
		this.httpCode = new Integer(httpCode);
		this.atmosCode = new Integer(atmosCode);
	}

	public Integer getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(Integer httpCode) {
		this.httpCode = httpCode;
	}

	public Integer getAtmosCode() {
		return atmosCode;
	}

	public void setAtmosCode(Integer atmosCode) {
		this.atmosCode = atmosCode;
	}
}
