package org.jpoetker.objstore;

import java.util.Collection;

public class QueryResults<T> {
	private Collection<T> results;
	private String continuationToken;
	
	public QueryResults(Collection<T> results, String continuationToken) {
		super();
		this.results = results;
		this.continuationToken = continuationToken;
	}
	
	public Collection<T> getResults() {
		return results;
	}
	public void setResults(Collection<T> results) {
		this.results = results;
	}
	public String getContinuationToken() {
		return continuationToken;
	}
	public void setContinuationToken(String continuationToken) {
		this.continuationToken = continuationToken;
	}
	
}
