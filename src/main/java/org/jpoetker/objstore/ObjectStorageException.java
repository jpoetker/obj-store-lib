package org.jpoetker.objstore;

public class ObjectStorageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ObjectStorageException() {
		super();
	}

	public ObjectStorageException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ObjectStorageException(String arg0) {
		super(arg0);
	}

	public ObjectStorageException(Throwable arg0) {
		super(arg0);
	}

}
