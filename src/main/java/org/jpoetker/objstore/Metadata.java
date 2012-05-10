package org.jpoetker.objstore;

import java.io.Serializable;

public class Metadata implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String value;
	private boolean listable;
	
	public Metadata(String name, String value) {
		this(name, value, false);
	}
	
	public Metadata(String name, String value, boolean listable) {
		super();
		this.name = name;
		this.value = value;
		this.listable = listable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isListable() {
		return listable;
	}

	public void setListable(boolean listable) {
		this.listable = listable;
	}

	public String toKeyValueString() {
		StringBuilder buff = new StringBuilder(name);
		buff.append("=");
		buff.append(value.replace(",", "").replace("\n", ""));
		return buff.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		if (listable) buff.append("[listable] ");
		buff.append(name).append(" : ").append(value);
		return buff.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (listable ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metadata other = (Metadata) obj;
		if (listable != other.listable)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
