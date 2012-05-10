package org.jpoetker.objstore;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

public interface ObjectStore {

	/**
	 * Creates a new object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param data An input stream containing the contents of the object.
	 *        The stream will not be closed at the end of the request.
	 * @param length The length of the stream in bytes.
	 * @param mimeType The MimeType of the object being stored.
	 * 		  If set to null, will default to "application/octet-stream"
	 * @returns Identifier of the newly created object.
	 * 
	 * @throws ObjectStorageException if the request fails
	 */
	Identifier createObject(UserContext userContext, InputStream data, long length, String mimeType) throws ObjectStorageException;
	
	/**
	 * Creates a new object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param data An input stream containing the contents of the object.
	 *        The stream will not be closed at the end of the request.
	 * @param length The length of the stream in bytes.
	 * @param mimeType TODO
	 * @param metadata A collection of Metadata to be associated with the object
	 * @returns Identifier of the newly created object.
	 * 
	 * @throws ObjectStorageException if the request fails
	 */
	Identifier createObject(UserContext userContext, InputStream data, long length, String mimeType, Collection<Metadata> metadata) throws ObjectStorageException;
	
	Identifier createObject(UserContext userContext, InputStream data, long length, String mimeType, Metadata ... metadata) throws ObjectStorageException;
	
	/**
	 * Creates a new object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param data An input stream containing the contents of the object.
	 *        The stream will not be closed at the end of the request.
	 * @param length The length of the stream in bytes.
	 * @param mimeType TODO
	 * @param acl A set of Grant objects to make up the access control list for this object.
	 * 		  May be null to use the default acl.
	 * @returns Identifier of the newly created object.
	 * 
	 * @throws ObjectStorageException if the request fails
	 */
	Identifier createObject(UserContext userContext, InputStream data, long length, String mimeType, Set<Grant> acl) throws ObjectStorageException;
	
	/**
	 * Creates a new object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param data An input stream containing the contents of the object.
	 *        The stream will not be closed at the end of the request.
	 * @param length The length of the stream in bytes.
	 * @param mimeType TODO
	 * @param acl A set of Grant objects to make up the access control list for this object.
	 * 		  May be null to use the default acl.
	 * @param metadata A collection of Metadata to be associated with the object
	 * @returns Identifier of the newly created object.
	 * 
	 * @throws ObjectStorageException if the request fails
	 */
	Identifier createObject(UserContext userContext, InputStream data, long length, String mimeType, Set<Grant> acl, Collection<Metadata> metadata) throws ObjectStorageException;
	
	/**
	 * Updates an object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param id The ID of the object being updated
	 * @param data The InputStream containing the new contents for the object
	 * @param length The length of the stream in bytes
	 * @param mimeType TODO
	 * @throws ObjectStorageException if the request fails
	 */
	void updateObject(UserContext userContext, Identifier id, InputStream data, long length, String mimeType) throws ObjectStorageException;
	
	/**
	 * Updates an object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param id The ID of the object being updated
	 * @param data The InputStream containing the new contents for the object
	 * @param length The length of the stream in bytes
	 * @param mimeType TODO
	 * @param metadata The collection of metadata to be set on the object
	 * @throws ObjectStorageException if the request fails
	 */
	void updateObject(UserContext userContext, Identifier id, InputStream data, long length, String mimeType, Collection<Metadata> metadata) throws ObjectStorageException;
	
	void updateObject(UserContext userContext, Identifier id, InputStream data, long length, String mimeType, Metadata ... metadata) throws ObjectStorageException;
	
	/**
	 * Updates an object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param id The ID of the object being updated
	 * @param data The InputStream containing the new contents for the object
	 * @param length The length of the stream in bytes
	 * @param mimeType TODO
	 * @param acl A Set of Grant objects that make up the new access control list for the object.
	 *        May be <code>null</code> in which case the ACL for the object will remain unchanged.
	 * @throws ObjectStorageException if the request fails
	 */
	void updateObject(UserContext userContext, Identifier id, InputStream data, long length, String mimeType, Set<Grant> acl) throws ObjectStorageException;
	
	/**
	 * Updates an object in the cloud.
	 * 
	 * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param id The ID of the object being updated
	 * @param data The InputStream containing the new contents for the object
	 * @param length The length of the stream in bytes
	 * @param mimeType TODO
	 * @param acl A Set of Grant objects that make up the new access control list for the object.
	 *        May be <code>null</code> in which case the ACL for the object will remain unchanged.
	 * @param metadata The collection of metadata to be set on the object
	 * @throws ObjectStorageException if the request fails
	 */
	void updateObject(UserContext userContext, Identifier id, InputStream data, long length, String mimeType, Set<Grant> acl, Collection<Metadata> metadata) throws ObjectStorageException;
	
	/**
     * Writes the Metadata into the object. If the tag does not exist, it is 
     * created and set to the corresponding value. If the tag exists, the 
     * existing value is replaced.
     * 
     * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
     * @param id the Identifier of the object to update
     * @param metadata Metadata to write to the object.
     * 
	 * @throws ObjectStorageException if the request fails
     */
    void setMetadata(UserContext userContext, Identifier id, Collection<Metadata> metadata) throws ObjectStorageException;
    
    void setMetadata(UserContext userContext, Identifier id, Metadata ... metadata) throws ObjectStorageException;
    
    /**
     * Fetches all user metadata for the object.
     * 
     * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param id the identifier of the object whose user metadata is being fetched
	 * 
	 * @return a collection of Metadata
	 * 
	 * @throws ObjectStorageException if the request fails
     */
    Collection<Metadata> getUserMetadata(UserContext userContext, Identifier id) throws ObjectStorageException;
    
    /**
     * Fetches the specified user metadata for the object.
     * 
     * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
	 * @param id the identifier of the object whose user metadata is being fetched
	 * @param metadatatags The metadata to look for on the object
	 * 
	 * @return a collection of Metadata
	 * 
	 * @throws ObjectStorageException if the request fails
     */
    Collection<Metadata> getUserMetadata(UserContext userContext, Identifier id, Collection<MetadataTag> metadatatags) throws ObjectStorageException;
    Collection<Metadata> getUserMetadata(UserContext userContext, Identifier id, MetadataTag ... metadatatags) throws ObjectStorageException;
    
    /**
     * Fetches all system metadata for the object.
     * 
     * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
     * @param id the identifier of the object whose system metadata to fetch.
     * 
     * @return The list of system metadata for the object.
     * 
     * @throws ObjectStorageException if the request fails
     */
    public Collection<Metadata> getSystemMetadata(UserContext userContext, Identifier id);
    
    /**
     * Fetches the system metadata for the object.
     * 
     * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
     * @param id the identifier of the object whose system metadata to fetch.
     * @param tags A list of system metadata tags to fetch. Optional. Default
     *            value is null to fetch all system metadata.
     *            
     * @return The list of system metadata for the object.
     * 
     * @throws ObjectStorageException if the request fails
     */
    public Collection<Metadata> getSystemMetadata(UserContext userContext, Identifier id, Collection<MetadataTag> tags);
    public Collection<Metadata> getSystemMetadata(UserContext userContext, Identifier id, MetadataTag ... tags);
    
    /**
     * Deletes an object from the cloud.
     * 
     * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
     * @param id the identifier of the object to delete.
     * 
	 * @throws ObjectStorageException if the request fails
     */
    void deleteObject(UserContext userContext, Identifier id) throws ObjectStorageException;
    
    /**
     * Reads an object's content and returns an InputStream to read the content.
     * 
     * Reminder: Close the InputStream when you have completed you operation so that the
     * HTTPConnection can be released.
     * 
     * @param userContext Contains the information required for authenticating 
	 * 		  to the object store for this request
     * @param id the identifier of the object to read
     * 
     * @return an InputStrem for reading the object data
     * 
     * @throws ObjectStorageException if the request fails
     */
    InputStream readObject(UserContext userContext, Identifier id);
    
    /**
     * List all the objects for a given metadata tag
     * @param tag
     * @param limit the number of records to return
     * @param continuationToken a token from a previous request that returned a subset of results
     * 		  this is used for paging through the result set
     * @return
     */
    QueryResults<Identifier> listObjects(UserContext userContext, String tag, int limit, String continuationToken);
    
    /**
     * List all the objects for a given metadata tag, include the metadata in the results.
     * 
     */
    QueryResults<ObjectInfo> listObjectsWithMetadata(UserContext userContext, String tag, int limit, String continuationToken);
    
    QueryResults<ObjectInfo> listObjectsWithMetadata(UserContext userContext, String tag, Collection<String> userMetadataTags, int limit, String continuatinoToken);
    QueryResults<ObjectInfo> listObjectsWithMetadata(UserContext userContext, String tag, Collection<String> userMetadataTags, Collection<String> systemMetadataTags, int limit, String continuatinoToken);
}
