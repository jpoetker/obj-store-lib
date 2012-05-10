package org.jpoetker.objstore;

public class UserContext {
	private String uid;
	private String sharedSecret;
	
	public UserContext(String uid, String sharedSecret) {
		super();
		this.uid = uid;
		this.sharedSecret = sharedSecret;
	}

	public String getUid() {
		return uid;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	@Override
	public String toString() {
		return "UserContext [uid=" + uid + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
		UserContext other = (UserContext) obj;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}
}
