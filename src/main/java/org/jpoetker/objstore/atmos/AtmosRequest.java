package org.jpoetker.objstore.atmos;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.AbstractHttpMessage;
import org.jpoetker.objstore.Grant;
import org.jpoetker.objstore.Grantee;
import org.jpoetker.objstore.Metadata;
import org.jpoetker.objstore.MetadataTag;
import org.jpoetker.objstore.ObjectStorageException;
import org.jpoetker.objstore.UserContext;

class AtmosRequest {
	private static final String X_EMC_SYSTEM_TAGS_HEADER = "x-emc-system-tags";
	private static final String X_EMC_USER_TAGS_HEADER = "x-emc-user-tags";
	private static final String X_EMC_INCLUDE_META_HEADER = "x-emc-include-meta";
	private static final String X_EMC_LIMIT_HEADER = "x-emc-limit";
	private static final String X_EMC_TOKEN_HEADER = "x-emc-token";
	private static final String X_EMC_UID_HEADER = "x-emc-uid";
	private static final String X_EMC_SIGNATURE_HEADER = "x-emc-signature";
	private static final String X_EMC_GROUPACL_HEADER = "x-emc-groupacl";
	private static final String X_EMC_USERACL_HEADER = "x-emc-useracl";
	private static final String X_EMC_TAGS_HEADER = "x-emc-tags";
	private static final String X_EMC_META_HEADER = "x-emc-meta";
	private static final String X_EMC_LISTABLE_META_HEADER = "x-emc-listable-meta";
	private static final String DATE_HEADER = "Date";
	private static final String CONTENT_RANGE_HEADER = "Content-Range";
	private static final String RANGE_HEADER = "Range";
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private static final String UTF_8 = "UTF-8";
	
	private Map<String, String> headers;
	private URL url;
	private byte[] secret;
	
	public AtmosRequest(URL url, UserContext userContext) {
		super();

		try {
			secret = Base64.decodeBase64(userContext.getSharedSecret().getBytes(UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new ObjectStorageException("Could not decode shared secret");
		}
		this.url = url;
		this.headers = new HashMap<String, String>();
		this.headers.put(X_EMC_UID_HEADER, userContext.getUid());
	}
	
	/**
	 * Sets the content type header to the specified value.
	 * 
	 * If <code>null</code> is passed, this method will set the content type header 
	 * to the default mime type "application/octet-stream"
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		headers.put(CONTENT_TYPE_HEADER, (contentType != null) ? contentType : DEFAULT_MIME_TYPE);
	}
	
	public void setMetadata(Collection<Metadata> metadata) {
		if ((metadata != null) && (!metadata.isEmpty())) {
			StringBuilder listable = new StringBuilder();
			StringBuilder nonListable = new StringBuilder();
			
			for (Metadata meta : metadata) {
				// select the correct buffer for the metadata
				StringBuilder buff = (meta.isListable()) ? listable : nonListable;
				// append the comma if we've already got data
				if (buff.length() > 0) {
					buff.append(", ");
				}
				// append the key value pair for the metadata
				buff.append(meta.toKeyValueString());
			}
			
			// set the headers values only if we've have data
			if (listable.length() > 0) {
				headers.put(X_EMC_LISTABLE_META_HEADER, listable.toString());
			}
			if (nonListable.length() > 0) {
				headers.put(X_EMC_META_HEADER, nonListable.toString());
			}
		}
	}
	
	public void setMetadataTags(Collection<MetadataTag> tags) {
		if ((tags != null) && (!tags.isEmpty())) {
			StringBuilder taglist = new StringBuilder();
			
			for (MetadataTag metadataTag : tags) {
				if (taglist.length() > 0) {
					taglist.append(",");
				}
				taglist.append(metadataTag.getName());
			}
			headers.put(X_EMC_TAGS_HEADER, taglist.toString());
		}
	}
	
	public void setAcl(Set<Grant> acl) {
		if ((acl != null) && (!acl.isEmpty())) {
			StringBuilder userGrants = new StringBuilder();
			StringBuilder groupGrants = new StringBuilder();
			
			for (Grant grant : acl) {
				StringBuilder buff = (grant.getGrantee().getType() == Grantee.Type.USER) ? userGrants : groupGrants;
				
				if (buff.length() > 0) {
					buff.append(",");
				}
				buff.append(grant.toAclHeaderString());
			}
			headers.put(X_EMC_USERACL_HEADER, userGrants.toString());
			headers.put(X_EMC_GROUPACL_HEADER, groupGrants.toString());
		}
	}
	
	public void setTag(String tag) {
		if (tag != null) {
			headers.put(X_EMC_TAGS_HEADER, tag);
		}
	}
	
	public void setLimit(int limit) {
		if (limit > 0) {
			headers.put(X_EMC_LIMIT_HEADER, Integer.toString(limit));
		}
	}
	
	public void setContinuationToken(String continuationToken) {
		if (continuationToken != null) {
			headers.put(X_EMC_TOKEN_HEADER, continuationToken);
		}
	}
	/**
	 * Creates a POST request with no body.
	 * 
	 * @return
	 */
	public HttpPost createPost() {
		return createPost(null, 0);
	}
	
	public HttpPost createPost(InputStream data, long length) {
		return createRequest(HttpPost.class, data, length);
	}
	
	public HttpPut createPut(InputStream data, long length) {
		return createRequest(HttpPut.class, data, length);
	}
	
	public HttpGet createGet() {
		return createRequest(HttpGet.class);
	}
	
	public HttpDelete createDelete() {
		return createRequest(HttpDelete.class);
	}
	
	public void setIncludeMetadata(boolean value) {
		if (value) {
			headers.put(X_EMC_INCLUDE_META_HEADER, "1");
		} else {
			headers.remove(X_EMC_INCLUDE_META_HEADER);
		}
	}
	
	public void setUserMetaTags(Collection<String> tags) {
		setTags(X_EMC_USER_TAGS_HEADER, tags);
	}
	
	public void setSystemMetaTags(Collection<String> tags) {
		setTags(X_EMC_SYSTEM_TAGS_HEADER, tags);
	}
	
	private void setTags(String headerName, Collection<String> tags) {
		if ((tags != null) && (!tags.isEmpty())) {
			headers.put(headerName, join(tags, ","));
		}
	}
	
	private <T extends HttpRequestBase> T createRequest(Class<T> clss) {
		try {
			T req = clss.newInstance();
			
			req.setURI(url.toURI());
			
			signRequest(req.getMethod());
			
			applyHeaders(req);
			
			return req;
		} catch (InvalidKeyException e) {
			throw new AtmosStorageException("Invalid shared secret", e);
		} catch (NoSuchAlgorithmException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} catch (IllegalStateException e) {
			throw new AtmosStorageException(e);
		} catch (IOException e) {
			throw new AtmosStorageException(e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (InstantiationException e) {
			throw new AtmosStorageException(e);
		} catch (IllegalAccessException e) {
			throw new AtmosStorageException(e);
		}
	}
	
	private <T extends HttpEntityEnclosingRequestBase> T createRequest(Class<T> clss, InputStream data, long contentLength) {
		T req = createRequest(clss);
		req.setEntity(createEntity(data, contentLength));
		return req; 
	}
	
	private HttpEntity createEntity(InputStream data, long contentLength) {
		return (data != null) ? new InputStreamEntity(data, contentLength) : new ByteArrayEntity( new byte[0] );
	}
	
	private void applyHeaders( AbstractHttpMessage request) {
        for( String headerName : headers.keySet() ) {
            request.addHeader( headerName, headers.get( headerName ) );
        }
    }

	void createDateHeader() {
		DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		
		dateFormat.setTimeZone(timezone);
		headers.put(DATE_HEADER, dateFormat.format(new Date()));
	}
	
	void signRequest(String method) throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		StringBuilder buff = new StringBuilder();
		
		buff.append(method).append("\n");
		
		if (headers.containsKey(CONTENT_TYPE_HEADER)) {
			buff.append(headers.get(CONTENT_TYPE_HEADER).toLowerCase());
		}
		// line feed even if no content type header exists (which it will)
		buff.append("\n");
		
		// Range headers..
		if (headers.containsKey(RANGE_HEADER)) {
			buff.append(headers.get(RANGE_HEADER));
		} else if (headers.containsKey(CONTENT_RANGE_HEADER)) {
			buff.append(headers.get(CONTENT_RANGE_HEADER));
		}
		// Even if the range headers were not set, we still need a break
		buff.append("\n");
		
		// if the data header has not been set - we need to do so
		if (!headers.containsKey(DATE_HEADER)) {
			createDateHeader();
		}
		buff.append(headers.get(DATE_HEADER)).append("\n");
		buff.append(URLDecoder.decode(url.getPath(), UTF_8).toLowerCase());
		if (url.getQuery() != null) {
			buff.append("?").append(url.getQuery());
		}
		buff.append("\n");
		
		// 'x-emc' headers must be hashed in alphabetic order and the 
		// values stripped of whitespace
		SortedSet<String> keys = new TreeSet<String>();
		Map<String, String> xEmcHeaders = new HashMap<String, String>();
		
		for (String key : headers.keySet()) {
			if (key.indexOf("x-emc") == 0) {
				String lowerKey = key.toLowerCase();
				keys.add(lowerKey);
				xEmcHeaders.put(lowerKey, headers.get(key).replace("\n", ""));
			}
		}
		int initial = buff.length();
		for (String key : keys) {
			if (buff.length() > initial) {
				buff.append("\n");
			}
			buff.append(key).append(':').append(normalizeSpace(xEmcHeaders.get(key)));
		}
		
		String hash = signHash(buff.toString());
		
		headers.put(X_EMC_SIGNATURE_HEADER, hash);
	}

	
	private String normalizeSpace(String str) {
		StringBuilder buff = new StringBuilder(str);
		int i=0;
		while(i<buff.length()) {
			if ((buff.charAt(i) == ' ') && (i+1 < buff.length())) {
				if (buff.charAt(i+1) == ' ') {
					buff.deleteCharAt(i);
				} else {
					i++;
				}
			} else {
				i++;
			}	
		}
		return buff.toString().trim();
	}
	
	/**
     * Generates an HMAC-SHA1 signature of the given input string using the
     * shared secret key.
     * 
     * @param input the string to sign
     * 
     * @return the HMAC-SHA1 signature in Base64 format
     * 
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws UnsupportedEncodingException
     */
	protected String signHash(String hash) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA1");
		mac.init(key);
		
		byte[] hashedBytes = mac.doFinal(hash.getBytes(UTF_8));
		
		return new String(Base64.encodeBase64(hashedBytes), UTF_8);
	}

	
	Map<String, String> getHeaders() {
		return headers;
	}

	private static String join(Collection<String> values, String with) {
		if (values == null) return "";
		
		StringBuilder buff = new StringBuilder();
		
		for (String string : values) {
			if (buff.length() > 0) {
				buff.append(with);
			}
			buff.append(string);
		}
		return buff.toString();
	}
}
