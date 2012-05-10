package org.jpoetker.objstore.atmos;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHeader;
import org.jpoetker.objstore.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestAtmosResponse {
	private static final String BAD_REQUEST_XML = "<Error>\n" +
	"\t<Code>1002</Code>\n" +
	"\t<Message>One or more arguments in the request was invalid.</Message>\n" +
	"</Error>";
	private HttpResponse mockHttpResponse;
	private StatusLine mock200;
	
	@Before
	public void setUp() throws Exception {
		mockHttpResponse = mock(HttpResponse.class);
		
		mock200 = mock(StatusLine.class);
		when(mock200.getStatusCode()).thenReturn(new Integer(200));
		when(mock200.getReasonPhrase()).thenReturn("OK");
	}

	@Test
	public void testGetObjectId() throws Exception {
		final String id = "12345678901234567890123456789012345678900000";
		
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200);
		when(mockHttpResponse.getFirstHeader("location")).thenReturn(new BasicHeader("location", "/rest/objects/" + id));
		
		AtmosResponse response = new AtmosResponse(mockHttpResponse);
		assertEquals(id, response.getObjectId().toString());
		verify(mockHttpResponse, times(1)).getFirstHeader("location");
	}
	
	@Test
	public void testGetMetaDataNonListableOnly() throws Exception {
	
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200);
		when(mockHttpResponse.getFirstHeader("x-emc-meta")).thenReturn(new BasicHeader("x-emc-meta", "name1=value1,name2=value2"));
		
		AtmosResponse response = new AtmosResponse(mockHttpResponse);
		Collection<Metadata> metadata = response.getMetadata();
		
		assertEquals(2, metadata.size());
		String[] names = {"name1", "name2"};
		String[] values = {"value1", "value2"};
		
		int i = 0;
		for (Metadata meta : metadata) {
			assertEquals(names[i], meta.getName());
			assertEquals(values[i], meta.getValue());
			assertTrue(!meta.isListable());
			i++;
		}
	}
	
	@Test
	public void testGetMetaDataListableOnly() throws Exception {
	
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200);
		when(mockHttpResponse.getFirstHeader("x-emc-listable-meta")).thenReturn(new BasicHeader("x-emc-listable-meta", "name1=value1,name2=value2"));
		
		AtmosResponse response = new AtmosResponse(mockHttpResponse);
		Collection<Metadata> metadata = response.getMetadata();
		
		assertEquals(2, metadata.size());
		String[] names = {"name1", "name2"};
		String[] values = {"value1", "value2"};
		
		int i = 0;
		for (Metadata meta : metadata) {
			assertEquals(names[i], meta.getName());
			assertEquals(values[i], meta.getValue());
			assertTrue(meta.isListable());
			i++;
		}
	}
	
	@Test
	public void testGetMetaDataMixed() throws Exception {
	
		when(mockHttpResponse.getStatusLine()).thenReturn(mock200);
		when(mockHttpResponse.getFirstHeader("x-emc-meta")).thenReturn(new BasicHeader("x-emc-meta", "name1=value1,name2=value2"));
		when(mockHttpResponse.getFirstHeader("x-emc-listable-meta")).thenReturn(new BasicHeader("x-emc-listable-meta", "name3=value3,name4=value4"));
		
		AtmosResponse response = new AtmosResponse(mockHttpResponse);
		Collection<Metadata> metadata = response.getMetadata();
		
		assertEquals(4, metadata.size());
		String[] names = {"name1", "name2", "name3", "name4"};
		String[] values = {"value1", "value2", "value3", "value4"};
		
		int i = 0;
		for (Metadata meta : metadata) {
			assertEquals(names[i], meta.getName());
			assertEquals(values[i], meta.getValue());
			assertTrue(( i > 1) ? meta.isListable() : !meta.isListable());
			i++;
		}
	}
	
	@Test
	public void testBadRequest() throws Exception {
		AtmosStorageException aex = null;
		
		StatusLine mock400 = mock(StatusLine.class);
		when(mock400.getStatusCode()).thenReturn(new Integer(400));
		when(mock400.getReasonPhrase()).thenReturn("Bad Request.");
		
		HttpEntity mockEntity = mock(HttpEntity.class);
		when(mockEntity.getContent()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation)
					throws Throwable {
				return new ByteArrayInputStream(BAD_REQUEST_XML.getBytes("UTF-8"));
			}
			
		});
		
		when(mockHttpResponse.getStatusLine()).thenReturn(mock400);
		when(mockHttpResponse.getEntity()).thenReturn(mockEntity);
		
		try {
			new AtmosResponse(mockHttpResponse);
		} catch (AtmosStorageException e) {
			aex = e;
		}
		
		verify(mockHttpResponse, times(1)).getStatusLine();
		verify(mock400, atLeast(1)).getStatusCode();
		verify(mockHttpResponse, times(1)).getEntity();
		verify(mockEntity, times(1)).getContent();
		
		assertNotNull(aex);
		assertEquals(new Integer(400), aex.getHttpCode());
		assertEquals(new Integer(1002), aex.getAtmosCode());
		assertEquals("One or more arguments in the request was invalid.", aex.getMessage());
	}
	
}
