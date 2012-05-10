package org.jpoetker.objstore.atmos.parser;

import org.jpoetker.objstore.Identifier;
import org.jpoetker.objstore.ObjectInfo;
import org.jpoetker.objstore.QueryResults;
import org.jpoetker.objstore.atmos.AtmosResponse;

public interface QueryResponseProcessor {
	
	public QueryResults<Identifier> parseObjectIdentifiers(AtmosResponse resposne);
	
	public QueryResults<ObjectInfo> parseObjectInfo(AtmosResponse response);
}
