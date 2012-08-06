package org.jpoetker.objstore.atmos.auth;

/**
 * SimpleAuthenticationCredentialProvider can be used when your application has one uid/shared secret
 * for the entire application.
 * 
 * @author poetker_j
 *
 */
public class SimpleAuthenticationCredentialProvider implements AuthenticationCredentialProvider {
	private String userId;
	private String sharedSecret;
	
	public SimpleAuthenticationCredentialProvider() {
		this(null, null);
	}
	
	public SimpleAuthenticationCredentialProvider(String uid, String sharedSecret) {
		super();
		this.userId = uid;
		this.sharedSecret = sharedSecret;
	}
	
	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sharedSecret == null) ? 0 : sharedSecret.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		SimpleAuthenticationCredentialProvider other = (SimpleAuthenticationCredentialProvider) obj;
		if (sharedSecret == null) {
			if (other.sharedSecret != null)
				return false;
		} else if (!sharedSecret.equals(other.sharedSecret))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

}
