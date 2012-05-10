package org.jpoetker.objstore.atmos;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.jpoetker.objstore.Grant;
import org.jpoetker.objstore.Grantee;
import org.jpoetker.objstore.Metadata;
import org.jpoetker.objstore.MetadataTag;
import org.jpoetker.objstore.Permission;
import org.jpoetker.objstore.UserContext;
import org.junit.Before;
import org.junit.Test;

public class TestAtmosRequest {
	public static String testSecret;
	
	private AtmosRequest request;
	private URL testUrl;
	
	static {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
			SecretKey key = keyGen.generateKey();
			testSecret = Base64.encodeBase64String(key.getEncoded());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp() throws Exception {
		UserContext userContext = new UserContext("userid", testSecret);
	    testUrl = new URL("http://localhost/rest/objects");
		request = new AtmosRequest(testUrl, userContext);
	}
	
	@Test
	public void testConstruction() throws Exception {
		Map<String, String> headers = request.getHeaders();
		assertNotNull(headers);
		assertThat(headers.get("x-emc-uid"), is("userid"));
	}
	
	@Test
	public void testListableMetaData() throws Exception {
		Collection<Metadata> metadata = new LinkedList<Metadata>();
		metadata.add(new Metadata("meta-data-name-1", "meta-data-value-1", true));
		metadata.add(new Metadata("meta-data-name-2", "meta-data-value-2", true));
		metadata.add(new Metadata("meta-data-name-3", "meta,data,value-commas", true));
		metadata.add(new Metadata("meta-data-name-4", "meta\ndata\nvalue-linebreak", true));
		
		request.setMetadata(metadata);
		
		Map<String, String> headers = request.getHeaders();
		assertThat(headers.get("x-emc-listable-meta"), is("meta-data-name-1=meta-data-value-1, " +
				"meta-data-name-2=meta-data-value-2, meta-data-name-3=metadatavalue-commas, meta-data-name-4=metadatavalue-linebreak"));
		assertNull(headers.get("x-emc-meta"));
	}
	
	@Test
	public void testNonListableMetaData() throws Exception {
		Collection<Metadata> metadata = new LinkedList<Metadata>();
		metadata.add(new Metadata("meta-data-name-1", "meta-data-value-1", false));
		metadata.add(new Metadata("meta-data-name-2", "meta-data-value-2", false));
		metadata.add(new Metadata("meta-data-name-3", "meta,data,value-commas", false));
		metadata.add(new Metadata("meta-data-name-4", "meta\ndata\nvalue-linebreak", false));
		
		request.setMetadata(metadata);
		
		Map<String, String> headers = request.getHeaders();
		assertThat(headers.get("x-emc-meta"), is("meta-data-name-1=meta-data-value-1, " +
				"meta-data-name-2=meta-data-value-2, meta-data-name-3=metadatavalue-commas, meta-data-name-4=metadatavalue-linebreak"));
		assertNull(headers.get("x-emc-listable-meta"));
	}
	
	@Test
	public void testMixedListableMetaData() throws Exception {
		Collection<Metadata> metadata = new LinkedList<Metadata>();
		metadata.add(new Metadata("meta-data-name-1", "meta-data-value-1", true));
		metadata.add(new Metadata("meta-data-name-2", "meta-data-value-2", false));
		metadata.add(new Metadata("meta-data-name-3", "meta,data,value-commas", true));
		metadata.add(new Metadata("meta-data-name-4", "meta\ndata\nvalue-linebreak", false));
		
		request.setMetadata(metadata);
		
		Map<String, String> headers = request.getHeaders();
		assertThat(headers.get("x-emc-listable-meta"), is("meta-data-name-1=meta-data-value-1, " +
				"meta-data-name-3=metadatavalue-commas"));
		assertThat(headers.get("x-emc-meta"), is("meta-data-name-2=meta-data-value-2, meta-data-name-4=metadatavalue-linebreak"));
	}
	
	@Test
	public void testAcl() throws Exception {
		Set<Grant> acl = new HashSet<Grant>();
		acl.add(new Grant(new Grantee("user", Grantee.Type.USER), Permission.FULL_CONTROL));
		acl.add(new Grant(new Grantee("read-user", Grantee.Type.USER), Permission.READ));
		acl.add(new Grant(new Grantee("write-user", Grantee.Type.USER), Permission.WRITE));
		acl.add(new Grant(new Grantee("group", Grantee.Type.GROUP), Permission.FULL_CONTROL));
		acl.add(new Grant(new Grantee("read-group", Grantee.Type.GROUP), Permission.READ));
		acl.add(new Grant(new Grantee("write-group", Grantee.Type.GROUP), Permission.WRITE));
		
		request.setAcl(acl);
		
		Map<String, String> headers = request.getHeaders();
		String useracl = headers.get("x-emc-useracl");
		assertNotNull(useracl);
		String[] userGrants = useracl.split(",");
		assertEquals(3, userGrants.length);
		
		
		String groupacl = headers.get("x-emc-groupacl");
		assertNotNull(groupacl);
		String[] groupGrants = groupacl.split(",");
		assertEquals(3, groupGrants.length);
		
		Arrays.sort(userGrants);
		Arrays.sort(groupGrants);
		
		for (Grant grant : acl) {
			if (grant.getGrantee().getType() == Grantee.Type.USER) {
				int loc = Arrays.binarySearch(userGrants, grant.toAclHeaderString());
				assertTrue(grant.toAclHeaderString() + " expected in " + userGrants, loc >= 0);
			} else {
				int loc = Arrays.binarySearch(groupGrants, grant.toAclHeaderString());
				assertTrue(grant.toAclHeaderString() + " expected in " + groupGrants, loc >= 0);
			}
		}
	}
	
	@Test
	public void testSignRequest() throws Exception {
		Set<Grant> acl = new HashSet<Grant>();
		acl.add(new Grant(new Grantee("jeff", Grantee.Type.USER), Permission.FULL_CONTROL));
		
		Collection<Metadata> metadata = new LinkedList<Metadata>();
		metadata.add(new Metadata("NAME", "the value  of the meta  data "));
		
		request.setContentType(null);
		request.setAcl(acl);
		request.setMetadata(metadata);
		request.createDateHeader();
		
		Map<String, String> headers = request.getHeaders();
		assertNotNull(headers.get("Date"));
		
		StringBuilder hashable = new StringBuilder("POST\n");
		hashable.append("application/octet-stream\n");
		hashable.append("\n");
		hashable.append(headers.get("Date")).append("\n");
		hashable.append(URLDecoder.decode(testUrl.getPath(), "UTF-8").toLowerCase()).append("\n");
		hashable.append("x-emc-groupacl:\n");
		hashable.append("x-emc-meta:NAME=the value of the meta data\n");
		hashable.append("x-emc-uid:userid\n");
		hashable.append("x-emc-useracl:jeff=FULL_CONTROL");
		
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec key = new SecretKeySpec(Base64.decodeBase64(testSecret.getBytes("UTF-8")), "HmacSHA1");
		mac.init(key);
		
		byte[] hashedBytes = mac.doFinal(hashable.toString().getBytes("ISO-8859-1"));
		
		request.signRequest("POST");
		
		assertEquals(new String(Base64.encodeBase64(hashedBytes), "UTF-8"), headers.get("x-emc-signature"));
	}
	
	@Test
	public void testSetMetaDataNullOrEmpty() {
		request.setMetadata(null);
		
		assertThat(request.getHeaders().get("x-emc-meta"), nullValue());
		assertThat(request.getHeaders().get("x-emc-meta-listable"), nullValue());
		
		Collection<Metadata> meta = Collections.emptyList();
		request.setMetadata(meta);
		
		assertThat(request.getHeaders().get("x-emc-meta"), nullValue());
		assertThat(request.getHeaders().get("x-emc-meta-listable"), nullValue());
	}
	
	@Test
	public void setMetaDataTags() {
		Collection<MetadataTag> tags = Arrays.asList(new MetadataTag("test-tag", false), new MetadataTag("test-tag-2", true));
		
		request.setMetadataTags(tags);
		
		assertThat(request.getHeaders().get("x-emc-tags"), is("test-tag,test-tag-2"));
	}
	
	@Test
	public void testSetMetaDataTagsNullOrEmpty() {
		request.setMetadataTags(null);
		
		assertThat(request.getHeaders().get("x-emc-tags"), nullValue());
		
		Collection<MetadataTag> meta = Collections.emptyList();
		request.setMetadataTags(meta);
		
		assertThat(request.getHeaders().get("x-emc-tags"), nullValue());
	}

}
