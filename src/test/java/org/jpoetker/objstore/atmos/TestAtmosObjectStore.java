package org.jpoetker.objstore.atmos;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.jpoetker.objstore.Identifier;
import org.jpoetker.objstore.ObjectInfo;
import org.jpoetker.objstore.QueryResults;
import org.jpoetker.objstore.UserContext;
import org.jpoetker.objstore.atmos.parser.QueryResponseProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestAtmosObjectStore {

	private AtmosObjectStore objectStore;
	private HttpClient mockHttpClient;
	private UserContext userContext;
	private HttpResponse mockHttpResponse;
	private StatusLine mock200Status;
	
	@Before
	public void setup() throws Exception {
		mockHttpClient = mock(HttpClient.class);
		
		objectStore = new AtmosObjectStore();
		objectStore.setHost("localhost");
		objectStore.setHttpClient(mockHttpClient);
		
		userContext = new UserContext("testuid", TestAtmosRequest.testSecret);
		
		mockHttpResponse = mock(HttpResponse.class);
		
		when(mockHttpClient.execute((HttpUriRequest) anyObject())).thenReturn(mockHttpResponse);
		
		mock200Status = mock(StatusLine.class);
		when(mock200Status.getStatusCode()).thenReturn(new Integer(200));
		when(mock200Status.getReasonPhrase()).thenReturn("OK");
	}
	
	@Test
	public void testConfiguration() throws Exception {
		assertThat(objectStore.getHost(), is("localhost"));
		assertThat(objectStore.getPort(), is(80));
		assertThat(objectStore.getScheme(), is("http"));
		assertThat(objectStore.getContextRoot(), is("/rest"));
		assertThat(objectStore.getHttpClient(), is(mockHttpClient));
	}
	
	@Test
	public void testIllegalArgumentWhenInputStreamIsNull() throws Exception {
		IllegalArgumentException ex = null;
		
		try {
			objectStore.createObject(userContext, null, 120, "application/octet-stream");
		} catch (IllegalArgumentException e) {
			ex = e;
		}
		assertNotNull("Expected an IllegalArgumentException to be thrown", ex);
	}
	
	@Test
	public void testIllegalLenghtOfStream() throws Exception {
		IllegalArgumentException ex = null;
		InputStream in = mock(InputStream.class);
		
		try {
			objectStore.createObject(userContext, in, -120, "application/octet-stream");
		} catch (IllegalArgumentException e) {
			ex = e;
		}
		assertNotNull("Expected an IllegalArgumentException to be thrown when length is less than 0", ex);
		
		ex = null;
		try {
			objectStore.createObject(userContext, in, 0, "application/octet-stream");
		} catch (IllegalArgumentException e) {
			ex = e;
		}
		assertNotNull("Expected an IllegalArgumentException to be thrown when length is  0", ex);
		
		ex = null;
		
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200Status);
		when(mockHttpResponse.getFirstHeader("location")).thenReturn(new BasicHeader("location", "/rest/objects/00000000000000000000000000000000000000000000"));
		
		try {
			Identifier id = objectStore.createObject(userContext, in, 10, "application/octet-stream");
			assertEquals("00000000000000000000000000000000000000000000", id.toString());
		} catch (IllegalArgumentException e) {
			ex = e;
		}
		assertNull("Should not have caught an exception", ex);
	}
	
	@Test
	public void testUserContextRequired() throws Exception {
		IllegalArgumentException iax = null;
		InputStream in = mock(InputStream.class);
		
		try {
			objectStore.createObject(null, in, 100, null);
		} catch (IllegalArgumentException e) {
			iax = e;
		}
		assertNotNull("Expected an IllegalArgumentException when passing a null UserContext", iax);
		
		iax = null;
		try {
			UserContext userContext = new UserContext(null, "secret");
			objectStore.createObject(userContext, in, 100, null);
		} catch (IllegalArgumentException e) {
			iax = e;
		}
		assertNotNull("Expected an IllegalArgumentException when passing a UserContext with invalid uid", iax);
		iax = null;
		try {
			UserContext userContext = new UserContext("uid", null);
			objectStore.createObject(userContext, in, 100, null);
		} catch (IllegalArgumentException e) {
			iax = e;
		}
		assertNotNull("Expected an IllegalArgumentException when passing a UserContext with invalid shared secret", iax);
		
	}
	

	@Test
	public void testCreateObject() throws Exception {
		InputStream mockInputStream = mock(InputStream.class);
		// pretend that everything worked well
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200Status);
		when(mockHttpResponse.getFirstHeader("location")).thenReturn(new BasicHeader("location", "/rest/objects/00000000000000000000000000000000000000000000"));
		
		Identifier id = objectStore.createObject(userContext, mockInputStream, 100, "application/pdf"); // because the httpclient is a mock object - nothing will get read from the stream
		assertEquals("00000000000000000000000000000000000000000000", id.toString());
		
		verify(mockHttpClient, times(1)).execute((HttpUriRequest) Matchers.any(HttpPost.class));
		verify(mockHttpResponse, times(1)).getStatusLine();
	}

	
	@Test
	public void testUpdateObject() throws Exception {
		InputStream mockInputStream = mock(InputStream.class);
		// pretend that everything worked well
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200Status);
		when(mockHttpResponse.getFirstHeader("location")).thenReturn(new BasicHeader("location", "/rest/objects/00000000000000000000000000000000000000000000"));
		
		objectStore.updateObject(userContext, new AtmosObjectId("00000000000000000000000000000000000000000000"), mockInputStream, 100, "application/pdf"); // because the httpclient is a mock object - nothing will get read from the stream
		
		verify(mockHttpClient, times(1)).execute((HttpUriRequest) Matchers.any(HttpPut.class));
		verify(mockHttpResponse, times(1)).getStatusLine();
	}
	
	@Test
	public void testListObjectsWithMetadata() throws Exception {
		QueryResponseProcessor mockProcessor = mock(QueryResponseProcessor.class);
		when(mockProcessor.parseObjectInfo(Matchers.any(AtmosResponse.class))).thenReturn(new QueryResults<ObjectInfo>(null, null));
		
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200Status);
		when(mockHttpClient.execute(Matchers.any(HttpGet.class))).thenAnswer(new Answer<HttpResponse>() {

			@Override
			public HttpResponse answer(InvocationOnMock invocation)
					throws Throwable {
				HttpGet get = (HttpGet) invocation.getArguments()[0];
				
				assertThat(get.getFirstHeader("x-emc-tags").getValue(), is("test-tag"));
				assertThat(get.getFirstHeader("x-emc-limit").getValue(), is("100"));
				assertThat(get.getFirstHeader("x-emc-token").getValue(), is("ctoken"));
				assertThat(get.getFirstHeader("x-emc-user-tags").getValue(), is("metadata-test-tag,metadata-test-tag-2"));
				assertThat(get.getFirstHeader("x-emc-system-tags").getValue(), is("metadata-test-tag,metadata-test-tag-2"));
				assertThat(get.getFirstHeader("x-emc-include-meta").getValue(), is("1"));

				return mockHttpResponse;
			}
			
		});
		objectStore.setQueryResponseProcessor(mockProcessor);
		
		Collection<String> tags = new LinkedList<String>();
		tags.add("metadata-test-tag");
		tags.add("metadata-test-tag-2");
		
		objectStore.listObjectsWithMetadata(userContext, "test-tag", tags, tags, 100, "ctoken");
		
		verify(mockHttpResponse, atLeast(1)).getStatusLine();
		verify(mockHttpClient, times(1)).execute(Matchers.any(HttpGet.class));
		verify(mockProcessor, times(1)).parseObjectInfo(Matchers.any(AtmosResponse.class));
		
	}
	
	@Test
	public void testListObjectsWithMetadataShortForm() throws Exception {
		QueryResponseProcessor mockProcessor = mock(QueryResponseProcessor.class);
		when(mockProcessor.parseObjectInfo(Matchers.any(AtmosResponse.class))).thenReturn(new QueryResults<ObjectInfo>(null, null));
		
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200Status);
		when(mockHttpClient.execute(Matchers.any(HttpGet.class))).thenAnswer(new Answer<HttpResponse>() {

			@Override
			public HttpResponse answer(InvocationOnMock invocation)
					throws Throwable {
				HttpGet get = (HttpGet) invocation.getArguments()[0];
				
				assertThat(get.getFirstHeader("x-emc-tags").getValue(), is("test-tag"));
				assertThat(get.getFirstHeader("x-emc-limit"), nullValue());
				assertThat(get.getFirstHeader("x-emc-token"), nullValue());
				assertThat(get.getFirstHeader("x-emc-user-tags"), nullValue());
				assertThat(get.getFirstHeader("x-emc-system-tags"), nullValue());
				assertThat(get.getFirstHeader("x-emc-include-meta").getValue(), is("1"));

				return mockHttpResponse;
			}
			
		});
		objectStore.setQueryResponseProcessor(mockProcessor);
		
		objectStore.listObjectsWithMetadata(userContext, "test-tag", 0, null);
		
		verify(mockHttpResponse, atLeast(1)).getStatusLine();
		verify(mockHttpClient, times(1)).execute(Matchers.any(HttpGet.class));
		verify(mockProcessor, times(1)).parseObjectInfo(Matchers.any(AtmosResponse.class));
		
	}
}
