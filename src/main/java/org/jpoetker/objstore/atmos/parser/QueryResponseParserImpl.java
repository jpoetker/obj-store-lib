package org.jpoetker.objstore.atmos.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.jpoetker.objstore.Metadata;
import org.jpoetker.objstore.ObjectInfo;
import org.jpoetker.objstore.QueryResults;
import org.jpoetker.objstore.atmos.AtmosResponse;
import org.jpoetker.objstore.atmos.AtmosStorageException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QueryResponseParserImpl implements QueryResponseParser {
	private DocumentBuilderFactory documentBuilderFactory;

	public QueryResponseParserImpl() {
		super();
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
	}

	@Override
	public QueryResults<String> parseObjectIdentifiers(AtmosResponse response) {
		Collection<String> identifiers = new LinkedList<String>();
		HttpEntity body = response.getEntity();

		if (body != null) {
			try {
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

				Document xml = documentBuilder.parse(body.getContent());

				NodeList objects = xml.getElementsByTagName("Object");
				for (int i = 0; i < objects.getLength(); i++) {
					Node objectNode = objects.item(i);
					NodeList objectNodeChildren = objectNode.getChildNodes();
					for (int j = 0; j < objectNodeChildren.getLength(); j++) {
						Node objectNodeChild = objectNodeChildren.item(j);
						if (isNamedElement(objectNodeChild, "ObjectID")) {
							identifiers.add(objectNodeChild.getFirstChild().getNodeValue());
							break;
						}
					}
				}

			} catch (ParserConfigurationException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (IllegalStateException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (SAXException e) {
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
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

				Document xml = documentBuilder.parse(body.getContent());

				// NodeList objects =
				// xml.getElementsByTagNameNS("http://www.emc.com/cos/",
				// "Object");
				NodeList objects = xml.getElementsByTagName("Object");
				for (int i = 0; i < objects.getLength(); i++) {
					ObjectInfo objectInfo = new ObjectInfo();
					Node objectNode = objects.item(i);
					NodeList objectNodeChildren = objectNode.getChildNodes();
					for (int j = 0; j < objectNodeChildren.getLength(); j++) {
						Node objectNodeChild = objectNodeChildren.item(j);
						if (isNamedElement(objectNodeChild, "ObjectID")) {
							objectInfo.setId(objectNodeChild.getFirstChild().getNodeValue());
						} else if (isNamedElement(objectNodeChild, "SystemMetadataList")) {
							objectInfo.setSystemMetadata(parseMetaData(objectNodeChild));
						} else if (isNamedElement(objectNodeChild, "UserMetadataList")) {
							objectInfo.setUserMetadata(parseMetaData(objectNodeChild));
						}
					}
					objectInfos.add(objectInfo);
				}
			} catch (ParserConfigurationException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (IllegalStateException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (SAXException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} catch (IOException e) {
				throw new AtmosStorageException(e.getMessage(), e);
			} finally {

			}

			objectInfos = (objectInfos.size() > 0) ? objectInfos : null;
			return new QueryResults<ObjectInfo>(objectInfos, response.getContinuationToken());
		}

		return null;
	}

	private Collection<Metadata> parseMetaData(Node container) {
		Collection<Metadata> metadata = new LinkedList<Metadata>();
		NodeList metadataNodes = container.getChildNodes();
		for (int x = 0; x < metadataNodes.getLength(); x++) {
			Node metaNode = metadataNodes.item(x);
			if (isNamedElement(metaNode, "Metadata")) {
				NodeList nvNodes = metaNode.getChildNodes();
				String name = null, value = null;
				boolean listable = false;
				for (int y = 0; y < nvNodes.getLength(); y++) {
					Node n = nvNodes.item(y);
					if (isNamedElement(n, "Name")) {
						name = n.getFirstChild().getNodeValue();
					} else if (isNamedElement(n, "Value")) {
						value = n.getFirstChild().getNodeValue();
					} else if (isNamedElement(n, "Listable")) {
						listable = Boolean.valueOf(n.getFirstChild().getNodeValue());
					}
				}
				metadata.add(new Metadata(name, value, listable));
			}
		}
		return metadata;
	}

	private static boolean isNamedElement(Node node, String elementName) {
		return ((Node.ELEMENT_NODE == node.getNodeType()) && elementName.equals(node.getNodeName()));
	}
}
