package org.jpoetker.objstore;

import java.io.Serializable;
import java.util.Collection;

public class ObjectInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private Collection<Metadata> userMetadata;
	private Collection<Metadata> systemMetadata;

	public ObjectInfo() {
		this(null, null, null);
	}
	
	public ObjectInfo(String id, Collection<Metadata> userMetadata,
			Collection<Metadata> systemMetadata) {
		super();
		this.id = id;
		this.userMetadata = userMetadata;
		this.systemMetadata = systemMetadata;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Collection<Metadata> getUserMetadata() {
		return userMetadata;
	}

	public void setUserMetadata(Collection<Metadata> userMetadata) {
		this.userMetadata = userMetadata;
	}

	public Collection<Metadata> getSystemMetadata() {
		return systemMetadata;
	}

	public void setSystemMetadata(Collection<Metadata> systemMetadata) {
		this.systemMetadata = systemMetadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.toString().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectInfo other = (ObjectInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.toString().equals(other.id.toString()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (id == null) ? "?" : id.toString();
	}
	
	
}
