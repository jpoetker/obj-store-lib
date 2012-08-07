package org.jpoetker.objstore.atmos.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpEntity;
import org.jpoetker.objstore.Metadata;
import org.jpoetker.objstore.ObjectInfo;
import org.jpoetker.objstore.QueryResults;
import org.jpoetker.objstore.atmos.AtmosResponse;
import org.jpoetker.objstore.atmos.AtmosStorageException;

public class QueryResponseParserStreamImpl implements QueryResponseParser {
	private static final String LISTABLE_TAG_NAME = "Listable";
	private static final String VALUE_TAG_NAME = "Value";
	private static final String NAME_TAG_NAME = "Name";
	private static final String OBJECT_ID_TAG_NAME = "ObjectID";
	private static final String USER_METADATA_LIST_TAG_NAME = "UserMetadataList";
	private static final String SYSTEM_METADATA_LIST_TAG_NAME = "SystemMetadataList";
	private static final String METADATA_TAG_NAME = "Metadata";
	private static final String OBJECT_TAG_NAME = "Object";
	private static final String EMC_NAMESPACE_URI = "http://www.emc.com/cos/";
	
	private XMLInputFactory xmlInputFactory;
	
	public QueryResponseParserStreamImpl() {
		super();
		this.xmlInputFactory = XMLInputFactory.newFactory();
	}
	
	@Override
	public QueryResults<String> parseObjectIdentifiers(AtmosResponse response) {
		Collection<String> identifiers = new LinkedList<String>();
		HttpEntity body = response.getEntity();

		if (body != null) {
			try {
				// Constructs a filtered stream reader
				// The filter will only return the text nodes of the ObjectId elements :)
				XMLStreamReader xmlReader = xmlInputFactory.createFilteredReader(
						xmlInputFactory.createXMLStreamReader(body.getContent()),
						new StringFilter());
				
				while(xmlReader.hasNext() && (xmlReader.getEventType() != XMLStreamReader.END_DOCUMENT)) {
					identifiers.add(xmlReader.getText().trim());
					xmlReader.next();
				}
				
			} catch (IllegalStateException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (XMLStreamException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (IOException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} 
		}
		identifiers = (identifiers.size() > 0) ? identifiers : null;
		return new QueryResults<String>(identifiers, response.getContinuationToken());
	}

	@Override
	public QueryResults<ObjectInfo> parseObjectInfo(AtmosResponse response) {
		Collection<ObjectInfo> objectInfos = new LinkedList<ObjectInfo>();
		HttpEntity body = response.getEntity();

		if (body != null) {
			try {
				// Constructs a filtered stream reader
				ObjectInfoFilter objectInfoFilter = new ObjectInfoFilter();
				XMLStreamReader xmlReader = xmlInputFactory.createFilteredReader(
						xmlInputFactory.createXMLStreamReader(body.getContent()),
						objectInfoFilter);
				
				ObjectInfo currentObj = new ObjectInfo();
				String name = null, value = null;
				boolean listable = false;
				Collection<Metadata> metadata = new LinkedList<Metadata>();
				
				while(xmlReader.hasNext() && (xmlReader.getEventType() != XMLStreamReader.END_DOCUMENT)) {
					if (xmlReader.getEventType() == XMLStreamReader.END_ELEMENT)
					{
						QName element = xmlReader.getName();
						if (OBJECT_TAG_NAME.equals(element.getLocalPart()) && (currentObj.getId() != null)) {
							objectInfos.add(currentObj);
							currentObj = new ObjectInfo();
						} else if (METADATA_TAG_NAME.equals(element.getLocalPart())) {
							metadata.add(new Metadata(name, value, listable));
							name = null;
							value = null;
							listable = false;
						} else if (SYSTEM_METADATA_LIST_TAG_NAME.equals(element.getLocalPart())) {
							currentObj.setSystemMetadata(metadata);
							metadata = new LinkedList<Metadata>();
						} else if (USER_METADATA_LIST_TAG_NAME.equals(element.getLocalPart())) {
							currentObj.setUserMetadata(metadata);
							metadata = new LinkedList<Metadata>();
						}
						
					} else if (xmlReader.getEventType() == XMLStreamReader.CHARACTERS) {
						if (OBJECT_ID_TAG_NAME.equals(objectInfoFilter.currentValueTag)) {
							currentObj.setId(xmlReader.getText().trim());
						} else if (NAME_TAG_NAME.equals(objectInfoFilter.currentValueTag)) {
							name = xmlReader.getText();
						} else if (VALUE_TAG_NAME.equals(objectInfoFilter.currentValueTag)) {
							value = xmlReader.getText();
						} else if (LISTABLE_TAG_NAME.equals(objectInfoFilter.currentValueTag)) {
							listable = Boolean.parseBoolean(xmlReader.getText());
						}
					}
					xmlReader.next();
				}
				
			} catch (IllegalStateException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (XMLStreamException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (IOException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} 
		}
		
		objectInfos = (objectInfos.size() > 0) ? objectInfos : null;
		return new QueryResults<ObjectInfo>(objectInfos, response.getContinuationToken());
	}

	private static class StringFilter implements StreamFilter {
		boolean acceptNextTextElement = false;
		
		@Override
		public boolean accept(XMLStreamReader stream) {
			if (stream.isStartElement()) {
				if (OBJECT_ID_TAG_NAME.equals(stream.getName().getLocalPart()) && 
				   (EMC_NAMESPACE_URI.equals(stream.getName().getNamespaceURI()))) 
				{
					acceptNextTextElement = true;
				}
			} else if (stream.isCharacters() && acceptNextTextElement) {
				acceptNextTextElement = false;
				return true;
			}
			return false;
		}
		
	}
	
	private static class ObjectInfoFilter implements StreamFilter {
	
		String currentValueTag = null;

		@Override
		public boolean accept(XMLStreamReader stream) {
			if (stream.isStartElement()) {
				if ((EMC_NAMESPACE_URI.equals(stream.getName().getNamespaceURI())) &&
					(valueIn(stream.getName().getLocalPart(), OBJECT_ID_TAG_NAME, NAME_TAG_NAME, VALUE_TAG_NAME, LISTABLE_TAG_NAME)))
				{
					currentValueTag = stream.getName().getLocalPart();
				}
			} else if (stream.isCharacters() && (currentValueTag != null)) {
				return true;
			} else if (stream.isEndElement()) {
				currentValueTag = null;
				if ((EMC_NAMESPACE_URI.equals(stream.getName().getNamespaceURI())) &&
					(valueIn(stream.getName().getLocalPart(), OBJECT_TAG_NAME, METADATA_TAG_NAME, SYSTEM_METADATA_LIST_TAG_NAME, QueryResponseParserStreamImpl.USER_METADATA_LIST_TAG_NAME)))
				{ 
					return true;
				}
			}
			return false;
		}
		
		private boolean valueIn(String value, String ... possibilities) {
			for (String string : possibilities) {
				if (value.equals(string)) {
					return true;
				}
			}
			return false;
		}
	}
}
