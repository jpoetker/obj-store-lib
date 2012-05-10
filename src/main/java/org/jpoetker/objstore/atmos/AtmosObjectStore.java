package org.jpoetker.objstore.atmos;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.jpoetker.objstore.Grant;
import org.jpoetker.objstore.Identifier;
import org.jpoetker.objstore.Metadata;
import org.jpoetker.objstore.MetadataTag;
import org.jpoetker.objstore.ObjectInfo;
import org.jpoetker.objstore.ObjectStorageException;
import org.jpoetker.objstore.ObjectStore;
import org.jpoetker.objstore.QueryResults;
import org.jpoetker.objstore.UserContext;
import org.jpoetker.objstore.atmos.parser.QueryResponseProcessor;
import org.jpoetker.objstore.atmos.parser.QueryResponseProcessorStreamImpl;

public class AtmosObjectStore implements ObjectStore {
	
	private String host;
	private int port;
	private String scheme;
	private String contextRoot = "/rest";
	private HttpClient httpClient;
	private QueryResponseProcessor queryResponseProcessor;
	
	public AtmosObjectStore() {
		this(null);
	}
	
	/**
	 * Creates an AtmosObjectStore for the host, assuming http over port 80
	 * 
	 * @param host The Atmos server host name or IP Address
	 */
	public AtmosObjectStore(String host) {
		this(host, 80, "http");
	}
	
	/**
	 * Creates an AtmosObjectStore for the host and port supplied.
	 * 
	 * Assumes the protocol is http unless the port is 443, in which case
	 * this constructor will assume https.
	 * 
	 * @param host The Atmos server host name or IP Address
	 * @param port The Atmos server port
	 */
	public AtmosObjectStore(String host, int port) {
		this(host, port, (port == 443) ? "https" : "http");
	}
	
	/**
	 * Creates an AtmosObjectStore for the host, port, and scheme supplied
	 * 
	 * @param host The Atmos server host name or IP Address
	 * @param port The Atmos server port
	 * @param scheme "http" or "https"
	 */
	public AtmosObjectStore(String host, int port, String scheme) {
		this(host, port, scheme, null);
	}
	
	public AtmosObjectStore(String host, int port, String scheme, HttpClient httpClient) {
		this.host = host;
		this.port = port;
		this.scheme = (scheme != null) ? scheme.toLowerCase() : ((port == 443) ? "https" : "http");
		this.httpClient = httpClient;
		this.queryResponseProcessor = new QueryResponseProcessorStreamImpl();
	}
	
	@Override
	public Identifier createObject(UserContext userContext, InputStream data,
			long length, String mimeType) throws ObjectStorageException {
		return createObject(userContext, data, length, mimeType, null, null);
	}

	@Override
	public Identifier createObject(UserContext userContext, InputStream data,
			long length, String mimeType, Collection<Metadata> metadata)
			throws ObjectStorageException {
		return createObject(userContext, data, length, mimeType, null, metadata);
	}

	@Override
	public Identifier createObject(UserContext userContext, InputStream data,
			long length, String mimeType, Metadata... metadata) throws ObjectStorageException {
		return createObject(userContext, data, length, mimeType, null, Arrays.asList(metadata));
	}

	@Override
	public Identifier createObject(UserContext userContext, InputStream data,
			long length, String mimeType, Set<Grant> acl) throws ObjectStorageException {
		return createObject(userContext, data, length, mimeType, acl, null);
	}

	@Override
	public Identifier createObject(UserContext userContext, InputStream data,
			long length, String mimeType, Set<Grant> acl, Collection<Metadata> metadata)
			throws ObjectStorageException {
		AtmosResponse response = null;
		
		validateUserContext(userContext);
		validateInputStreamParamaters(data, length);
		
		try {
			URL url = buildUrl(getContextRoot() + "/objects", null);
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			
			request.setContentType(mimeType);
			request.setAcl(acl);
			request.setMetadata(metadata);
			
			response = execute( request.createPost(data, length));
			
			return response.getObjectId();
		} catch (MalformedURLException e) {
			throw new ObjectStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new ObjectStorageException("Invalid URL", e);
		} catch (ClientProtocolException e) {
			throw new ObjectStorageException(e.getMessage(), e);
		} catch (IOException e) {
			throw new ObjectStorageException(e.getMessage(), e);
		} finally {
			cleanup(response);
		}
	}

	@Override
	public void updateObject(UserContext userContext, Identifier id,
			InputStream data, long length, String mimeType) throws ObjectStorageException {
		updateObject(userContext, id, data, length, mimeType, null, null);
	}

	@Override
	public void updateObject(UserContext userContext, Identifier id,
			InputStream data, long length, String mimeType, Collection<Metadata> metadata)
			throws ObjectStorageException {
		updateObject(userContext, id, data, length, mimeType, null, metadata);
	}

	@Override
	public void updateObject(UserContext userContext, Identifier id,
			InputStream data, long length, String mimeType, Metadata... metadata)
			throws ObjectStorageException {
		updateObject(userContext, id, data, length, null, (metadata != null) ? Arrays.asList(metadata) : null);
		
	}

	@Override
	public void updateObject(UserContext userContext, Identifier id,
			InputStream data, long length, String mimeType, Set<Grant> acl)
			throws ObjectStorageException {
		updateObject(userContext, id, data, length, mimeType, acl, null);
		
	}

	@Override
	public void updateObject(UserContext userContext, Identifier id,
			InputStream data, long length, String mimeType,
			Set<Grant> acl, Collection<Metadata> metadata) throws ObjectStorageException {
		
		AtmosResponse response = null;
		
		validateUserContext(userContext);
		validateInputStreamParamaters(data, length);
		
		try {
			URL url = buildUrl(((AtmosIdentifier) id).getResourcePath(getContextRoot()), null);
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			
			request.setContentType(mimeType);
			request.setMetadata(metadata);
			request.setAcl(acl);
			
			response = execute(request.createPut(data, length));
		
		} catch (MalformedURLException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (ClientProtocolException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} catch (IOException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} finally {
			cleanup(response);
		}
	}

	@Override
	public void setMetadata(UserContext userContext, Identifier id,
			Collection<Metadata> metadata) throws ObjectStorageException {
		AtmosResponse response = null;
		
		try {
			URL url = buildUrl(((AtmosIdentifier) id).getResourcePath(getContextRoot()), "metadata/user");
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			
			request.setMetadata(metadata);
			
			response = execute (request.createPost());
		} catch (MalformedURLException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (ClientProtocolException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} catch (IOException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} finally {
			cleanup(response);
		}
	}

	@Override
	public void setMetadata(UserContext userContext, Identifier id,
			Metadata... metadata) {
		setMetadata(userContext, id, (metadata != null) ? Arrays.asList(metadata) : null);
	}

	@Override
	public Collection<Metadata> getUserMetadata(UserContext userContext, Identifier id) {
		Collection<MetadataTag> tags = Collections.emptyList();
		return getUserMetadata(userContext, id, tags);
	}

	@Override
	public Collection<Metadata> getUserMetadata(UserContext userContext,
			Identifier id, Collection<MetadataTag> metadatatags) {
		return getMetadata(userContext, id, metadatatags, "metadata/user");
	}

	@Override
	public Collection<Metadata> getUserMetadata(UserContext userContext,
			Identifier id, MetadataTag... metadatatags)
			throws ObjectStorageException {
		List<MetadataTag> tags;
		
		if (metadatatags != null) {
			tags = Arrays.asList(metadatatags);
		} else {
			tags = Collections.emptyList();
		}
		
		return getUserMetadata(userContext, id, tags);
	}

	@Override
	public Collection<Metadata> getSystemMetadata(UserContext userContext, Identifier id) {
		Collection<MetadataTag> tags = Collections.emptyList();
		return getSystemMetadata(userContext, id, tags);
	}

	@Override
	public Collection<Metadata> getSystemMetadata(UserContext userContext,
			Identifier id, Collection<MetadataTag> metadatatags) {
		return getMetadata(userContext, id, metadatatags, "metadata/system");
	}

	@Override
	public Collection<Metadata> getSystemMetadata(UserContext userContext,
			Identifier id, MetadataTag... metadatatags)
			throws ObjectStorageException {
		List<MetadataTag> tags;
		
		if (metadatatags != null) {
			tags = Arrays.asList(metadatatags);
		} else {
			tags = Collections.emptyList();
		}
		
		return getSystemMetadata(userContext, id, tags);
	}
	
	
	private Collection<Metadata>getMetadata(UserContext userContext, Identifier id, Collection<MetadataTag> metadatatags, String path) {
		AtmosResponse response = null;
		
		validateUserContext(userContext);
		
		try {
			URL url = buildUrl(((AtmosIdentifier) id).getResourcePath(getContextRoot()), path);
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			
			request.setMetadataTags(metadatatags);
			
			response = execute(request.createGet());
			
			return response.getMetadata();
		} catch (MalformedURLException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (IOException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} finally {
			cleanup(response);
		}
	}
	

	/**
	 * Deletes an object from the cloud.
	 * 
	 * @param id the identifier of the object to be deleted.
	 * @throws ObjectStorageException when things go wrong
	 */
	@Override
	public void deleteObject(UserContext userContext, Identifier id)
			throws ObjectStorageException {
		AtmosResponse response = null;
		
		validateUserContext(userContext);
		
		try {
			URL url = buildUrl(((AtmosIdentifier) id).getResourcePath(getContextRoot()), null);
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			
			response = execute(request.createDelete());
			
		} catch (MalformedURLException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (IOException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} finally {
			cleanup(response);
		}
	}

	/**
	 * Reads an object from the cloud and returns an InputStream to read the content.
	 * 
	 * Since the <code>InputStream</code> is linked to the HTTP Connection, developers
	 * must take care to close the <code>InputStream</code> when they are finished reading
	 * to relase the HTTP resources.
	 * 
	 * @param userContext the user context to use for the read operation
	 * @param id the <code>Identifier</code> of the object being read
	 * @return InputStream to the content of the object
	 * @throws ObjectStorageException when anything goes wrong 
	 */
	@Override
	public InputStream readObject(UserContext userContext, Identifier id) {
		AtmosResponse response = null;
		validateUserContext(userContext);
		
		try {
			URL url = buildUrl(((AtmosIdentifier) id).getResourcePath(getContextRoot()), null);
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			
			response = execute(request.createGet());
		
			return response.getInputStream();
		} catch (MalformedURLException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (IOException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		}
	}

	@Override
	public QueryResults<Identifier> listObjects(UserContext userContext, String tag, int limit, String continuationToken) {
		AtmosResponse response = null;
		
		validateUserContext(userContext);
		if (tag == null) throw new AtmosStorageException("Tag cannot be null");
		
		try {
			URL url = buildUrl(getContextRoot() + "/objects", null);
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			request.setTag(tag);
			request.setLimit(limit);
			request.setContinuationToken(continuationToken);
			request.setIncludeMetadata(true);
			response = execute(request.createGet());
			
			return queryResponseProcessor.parseObjectIdentifiers(response);
		} catch (MalformedURLException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (IOException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} finally {
			cleanup(response);
		}
	}
	@Override
	public QueryResults<ObjectInfo> listObjectsWithMetadata(UserContext userContext, String tag, int limit, String continuationToken) {
		return listObjectsWithMetadata(userContext, tag, null, null, limit, continuationToken);
	}
    
	@Override
    public QueryResults<ObjectInfo> listObjectsWithMetadata(UserContext userContext, String tag, Collection<String> userMetadataTags, int limit, String continuationToken) {
		return listObjectsWithMetadata(userContext, tag, userMetadataTags, null, limit, continuationToken);
	}
	
	@Override
    public QueryResults<ObjectInfo> listObjectsWithMetadata(UserContext userContext, String tag, Collection<String> userMetadataTags, Collection<String> systemMetadataTags, int limit, String continuationToken) {
		AtmosResponse response = null;
		
		validateUserContext(userContext);
		
		try {
			URL url = buildUrl(getContextRoot() + "/objects", null);
			
			AtmosRequest request = new AtmosRequest(url, userContext);
			request.setTag(tag);
			request.setLimit(limit);
			request.setContinuationToken(continuationToken);
			request.setIncludeMetadata(true);
			request.setUserMetaTags(userMetadataTags);
			request.setSystemMetaTags(systemMetadataTags);
			
			response = execute( request.createGet() );
			
			return queryResponseProcessor.parseObjectInfo(response);
		} catch (MalformedURLException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (URISyntaxException e) {
			throw new AtmosStorageException("Invalid URL", e);
		} catch (IOException e) {
			throw new AtmosStorageException(e.getMessage(), e);
		} finally {
			cleanup(response);
		}
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public HttpClient getHttpClient() {
		if (httpClient == null) {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			SchemeSocketFactory socketFactory = ("https".equals(this.scheme) ? SSLSocketFactory.getSocketFactory() : PlainSocketFactory.getSocketFactory());
			schemeRegistry.register(new Scheme(this.scheme, this.port, socketFactory));
			
			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
			cm.setMaxTotal(200);
			cm.setDefaultMaxPerRoute(200);
			httpClient = new DefaultHttpClient(cm, null);
		}
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Returns the context root of the REST API.
	 * 
	 * By default this is "/rest"
	 * 
	 * @return the context root of the REST API
	 */
	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}
	
	protected URL buildUrl(String resource, String query) throws URISyntaxException, MalformedURLException {
		URI uri = new URI(scheme, null, host, port, resource, query, null);
		return uri.toURL();
	}
	

	private void validateInputStreamParamaters(InputStream data, long length) {
		if (data == null) {
			throw new IllegalArgumentException("An InputStream is required");
		}
		if (length <= 0) {
			throw new IllegalArgumentException("The length of bytes in the stream must be greater than 0");
		}
	}
	
	private void validateUserContext(UserContext userContext) {
		if (userContext == null) {
			throw new IllegalArgumentException("A valid UserContext must be provided.");
		}
		if ((userContext.getUid() == null) || (userContext.getUid().trim().length() == 0)) {
			throw new IllegalArgumentException("A valid UID must be provided in the UserContext.");
		}
		if ((userContext.getSharedSecret() == null) || (userContext.getSharedSecret().trim().length() == 0)) {
			throw new IllegalArgumentException("A valid shared secret must be provided in the UserContext.");
		}
	}

	private AtmosResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException {
		return new AtmosResponse(getHttpClient().execute(request));
	}
	
	private void cleanup(AtmosResponse response) {
		if ((response != null) && (response.getEntity() != null)) {
			try {
				EntityUtils.consume(response.getEntity());
			} catch (IOException e) {
			}
		}
	}

	public QueryResponseProcessor getQueryResponseProcessor() {
		return queryResponseProcessor;
	}

	public void setQueryResponseProcessor(
			QueryResponseProcessor queryResponseProcessor) {
		this.queryResponseProcessor = queryResponseProcessor;
	}
}
