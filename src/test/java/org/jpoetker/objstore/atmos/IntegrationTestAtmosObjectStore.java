package org.jpoetker.objstore.atmos;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.ResourceBundle;


import org.jpoetker.objstore.Metadata;
import org.jpoetker.objstore.ObjectStore;
import org.jpoetker.objstore.QueryResults;
import org.junit.Before;
import org.junit.Test;

/**
 * This class will run some tests against http://api.atmosonline.com,
 * but it requires an IntegrationTest.properties file that
 * contains 2 keys: uid=<uid of your atmos online user> and 
 * sharedSecet=<the shared secret for your account>
 * 
 * @author poetker_j
 *
 */
public class IntegrationTestAtmosObjectStore {
	private static final String loremIpsum = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, " +
			"sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut " +
			"wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut " +
			"aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate " +
			"velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et " +
			"accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis " +
			"dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue " +
			"nihil imperdiet doming id quod mazim placerat facer possim assum. Typi non habent claritatem " +
			"insitam; est usus legentis in iis qui facit eorum claritatem. Investigationes demonstraverunt " +
			"lectores legere me lius quod ii legunt saepius. Claritas est etiam processus dynamicus, qui " +
			"sequitur mutationem consuetudium lectorum. Mirum est notare quam littera gothica, quam nunc " +
			"putamus parum claram, anteposuerit litterarum formas humanitatis per seacula quarta decima et " +
			"quinta decima. Eodem modo typi, qui nunc nobis videntur parum clari, fiant sollemnes in futurum.";
	
	static ResourceBundle atmosOnlineProperties;
	
	static {
		atmosOnlineProperties = ResourceBundle.getBundle("IntegrationTest");
	}
	
	private ObjectStore storage;
	
	@Before
	public void setUp() throws Exception {
		storage = new AtmosObjectStore("api.atmosonline.com", atmosOnlineProperties.getString("uid"), atmosOnlineProperties.getString("sharedSecret"));
	}
	
	private InputStream createInputStream() throws UnsupportedEncodingException {
		return new ByteArrayInputStream(loremIpsum.getBytes("UTF-8"));
	}
	
	@Test
	public void testIt() throws Exception {
		
		// First create an object in the cloud
		InputStream in = createInputStream();		
		String id = storage.createObject(in, loremIpsum.getBytes("UTF-8").length, "text/plain", new Metadata("Test-Data", "Test", true));
		assertNotNull(id);
		System.out.println("Object created with ID: " + id.toString());
		in.close();
		
		// Read the object back from the cloud
		in = storage.readObject(id);
		byte[] data = new byte[loremIpsum.getBytes("UTF-8").length];
		// verify it is what we expect
		in.read(data);
		assertEquals(loremIpsum, new String(data));
		assertEquals(-1, in.read(data));
		in.close();
		in = null;
		
		// look at the user metadata
		Collection<Metadata> metadata = storage.getUserMetadata(id);
		assertEquals(1, metadata.size());
		Metadata meta = metadata.iterator().next();
		System.out.println(meta.toString());
		assertEquals("Test-Data", meta.getName());
		assertEquals("Test", meta.getValue());
		assertTrue(meta.isListable());
		
		// add some metadata
		storage.setMetadata(id, new Metadata("test-name", "test-value", false));
		
		// take another look at the metadata
		metadata = storage.getUserMetadata(id);
		assertEquals(2, metadata.size());
		for (Metadata m : metadata) {
			System.out.println(m.toString());
		}
		
		// take another look at the system metadata
		metadata = storage.getSystemMetadata(id);
		assertNotNull(metadata);
		assertTrue("No system metadata returned.", !metadata.isEmpty());
		for (Metadata m : metadata) {
			System.out.println(m.toString());
		}


		// update the data 
		InputStream upperIn = new ByteArrayInputStream(loremIpsum.toUpperCase().getBytes("UTF-8"));
		storage.updateObject(id, upperIn, loremIpsum.getBytes("UTF-8").length, "text/plain");
		upperIn.close();
		// read it back out
		in = storage.readObject(id);
		data = new byte[loremIpsum.getBytes("UTF-8").length];
		// verify it is as we expect
		in.read(data);
		assertEquals(loremIpsum.toUpperCase(), new String(data));
		assertEquals(-1, in.read(data));
		in.close();
		in = null;
		
		QueryResults<String> ids = storage.listObjects("Test-Data", 0, null);
		for (String i : ids.getResults()) {
			System.out.println(i.toString());
		}
		
		storage.listObjectsWithMetadata("Test-Data", 0, null);
		
		// remove the object from the cloud
		storage.deleteObject(id);
	}
}
