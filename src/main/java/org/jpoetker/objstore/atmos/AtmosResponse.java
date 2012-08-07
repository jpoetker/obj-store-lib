package org.jpoetker.objstore.atmos;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jpoetker.objstore.Metadata;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AtmosResponse implements HttpResponse {
	private static final Pattern OBJECTID_EXTRACTOR = Pattern.compile("/\\w+/objects/([0-9a-f]{44})");
	private HttpResponse response;

	public AtmosResponse(HttpResponse response) {
		super();
		this.response = response;
		validate();
	}

	public String getObjectId() {
		String location = response.getFirstHeader("location").getValue();
		Matcher m = OBJECTID_EXTRACTOR.matcher(location);

		if (m.find()) {
			String id = m.group(1);
			return id;
		} else {
			throw new AtmosStorageException("Could not parse object id from " + location);
		}
	}

	public List<Metadata> getMetadata() {
		LinkedList<Metadata> meta = new LinkedList<Metadata>();

		readMetadata(meta, response.getFirstHeader("x-emc-meta"), false);
		readMetadata(meta, response.getFirstHeader("x-emc-listable-meta"), true);

		return meta;
	}

	/**
	 * Returns an <code>InputStream</code> from the entity for reading the
	 * response.
	 * 
	 * @throws IOException
	 * @throws IllegalStateException
	 * 
	 */
	public InputStream getInputStream() throws IllegalStateException, IOException {
		return new HttpInputStream();
	}

	public String getContinuationToken() {
		Header token = this.getFirstHeader("x-emc-token");
		return (token != null) ? token.getValue() : null;
	}

	private void readMetadata(List<Metadata> metadata, Header header, boolean listable) {
		if (header != null) {
			String list = header.getValue();
			if (list != null) {
				String[] attrs = list.split(",");
				for (String pair : attrs) {
					String[] splitpair = pair.split("=", 2);
					String name = splitpair[0];
					String value = splitpair[1];

					name = name.trim();

					metadata.add(new Metadata(name, value, listable));
				}
			}
		}
	}

	public void addHeader(Header arg0) {
		response.addHeader(arg0);
	}

	public void addHeader(String arg0, String arg1) {
		response.addHeader(arg0, arg1);
	}

	public boolean containsHeader(String arg0) {
		return response.containsHeader(arg0);
	}

	public Header[] getAllHeaders() {
		return response.getAllHeaders();
	}

	public HttpEntity getEntity() {
		return response.getEntity();
	}

	public Header getFirstHeader(String arg0) {
		return response.getFirstHeader(arg0);
	}

	public Header[] getHeaders(String arg0) {
		return response.getHeaders(arg0);
	}

	public Header getLastHeader(String arg0) {
		return response.getLastHeader(arg0);
	}

	public Locale getLocale() {
		return response.getLocale();
	}

	public HttpParams getParams() {
		return response.getParams();
	}

	public ProtocolVersion getProtocolVersion() {
		return response.getProtocolVersion();
	}

	public StatusLine getStatusLine() {
		return response.getStatusLine();
	}

	public HeaderIterator headerIterator() {
		return response.headerIterator();
	}

	public HeaderIterator headerIterator(String arg0) {
		return response.headerIterator(arg0);
	}

	public void removeHeader(Header arg0) {
		response.removeHeader(arg0);
	}

	public void removeHeaders(String arg0) {
		response.removeHeaders(arg0);
	}

	public void setEntity(HttpEntity arg0) {
		response.setEntity(arg0);
	}

	public void setHeader(Header arg0) {
		response.setHeader(arg0);
	}

	public void setHeader(String arg0, String arg1) {
		response.setHeader(arg0, arg1);
	}

	public void setHeaders(Header[] arg0) {
		response.setHeaders(arg0);
	}

	public void setLocale(Locale arg0) {
		response.setLocale(arg0);
	}

	public void setParams(HttpParams arg0) {
		response.setParams(arg0);
	}

	public void setReasonPhrase(String arg0) throws IllegalStateException {
		response.setReasonPhrase(arg0);
	}

	public void setStatusCode(int arg0) throws IllegalStateException {
		response.setStatusCode(arg0);
	}

	public void setStatusLine(ProtocolVersion arg0, int arg1, String arg2) {
		response.setStatusLine(arg0, arg1, arg2);
	}

	public void setStatusLine(ProtocolVersion arg0, int arg1) {
		response.setStatusLine(arg0, arg1);
	}

	public void setStatusLine(StatusLine arg0) {
		response.setStatusLine(arg0);
	}

	protected void validate() {

		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() > 299) {
			HttpEntity body = response.getEntity();

			if (body == null) {
				throw new AtmosStorageException(status.getReasonPhrase(), status.getStatusCode());
			}
			try {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse(body.getContent());

				String code = null;
				String message = null;

				NodeList nodes = doc.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node n = nodes.item(i);
					if (Node.ELEMENT_NODE == n.getNodeType()) {
						if ("Code".equals(n.getNodeName())) {
							code = n.getFirstChild().getNodeValue();
						} else if ("Message".equals(n.getNodeName())) {
							message = n.getFirstChild().getNodeValue();
						}
					}
				}
				if ((code == null) && (message == null)) {
					throw new AtmosStorageException(status.getReasonPhrase(), status.getStatusCode());
				}
				throw new AtmosStorageException(message, status.getStatusCode(), Integer.parseInt(code));
			} catch (ParserConfigurationException e) {
				throw new AtmosStorageException(status.getReasonPhrase(), status.getStatusCode(), e);
			} catch (IllegalStateException e) {
				throw new AtmosStorageException(status.getReasonPhrase(), status.getStatusCode(), e);
			} catch (SAXException e) {
				throw new AtmosStorageException(status.getReasonPhrase(), status.getStatusCode(), e);
			} catch (IOException e) {
				throw new AtmosStorageException(status.getReasonPhrase(), status.getStatusCode(), e);
			}
		}
	}

	class HttpInputStream extends InputStream {
		private InputStream in;

		public HttpInputStream() throws IllegalStateException, IOException {
			super();
			in = AtmosResponse.this.getEntity().getContent();
		}

		public int available() throws IOException {
			return in.available();
		}

		public void close() throws IOException {
			in.close();
			try {
				EntityUtils.consume(AtmosResponse.this.getEntity());
			} catch (Exception e) {
			}
		}

		public boolean equals(Object obj) {
			return in.equals(obj);
		}

		public int hashCode() {
			return in.hashCode();
		}

		public void mark(int readlimit) {
			in.mark(readlimit);
		}

		public boolean markSupported() {
			return in.markSupported();
		}

		public int read() throws IOException {
			return in.read();
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return in.read(b, off, len);
		}

		public int read(byte[] b) throws IOException {
			return in.read(b);
		}

		public void reset() throws IOException {
			in.reset();
		}

		public long skip(long n) throws IOException {
			return in.skip(n);
		}

		public String toString() {
			return in.toString();
		}
	}
}
