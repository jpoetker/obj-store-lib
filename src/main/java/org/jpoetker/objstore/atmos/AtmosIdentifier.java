package org.jpoetker.objstore.atmos;

import org.jpoetker.objstore.Identifier;

interface AtmosIdentifier extends Identifier {

	String getResourcePath(String context);
}
