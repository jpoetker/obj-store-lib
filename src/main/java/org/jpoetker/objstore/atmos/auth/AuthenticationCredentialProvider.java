package org.jpoetker.objstore.atmos.auth;

/**
 * This interface can be implemented to provide the user's uid and shared secret
 * to the AtmosObjectStore. 
 * 
 * Use this interface if you need to support multiple tenants within your applications.
 * 
 * @author poetker_j
 *
 */
public interface AuthenticationCredentialProvider {

	public String getUserId();
	
	public String getSharedSecret();
}
