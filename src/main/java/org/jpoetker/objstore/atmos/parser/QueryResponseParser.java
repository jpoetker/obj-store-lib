package org.jpoetker.objstore.atmos.parser;

import org.jpoetker.objstore.ObjectInfo;
import org.jpoetker.objstore.QueryResults;
import org.jpoetker.objstore.atmos.AtmosResponse;

public interface QueryResponseParser {
	
	public QueryResults<String> parseObjectIdentifiers(AtmosResponse resposne);
	
	public QueryResults<ObjectInfo> parseObjectInfo(AtmosResponse response);
}
