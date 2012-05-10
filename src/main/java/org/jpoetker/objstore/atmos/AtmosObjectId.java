package org.jpoetker.objstore.atmos;


public class AtmosObjectId implements AtmosIdentifier {
	private static final long serialVersionUID = 6110744595044207521L;
	private String id;
	
	public AtmosObjectId(String id) {
		super();
		this.id = id;
	}
	
	@Override
	public String getResourcePath(String context) {
		return context + "/objects/" + id;
	}
	
	@Override
	public String toString() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AtmosObjectId other = (AtmosObjectId) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
