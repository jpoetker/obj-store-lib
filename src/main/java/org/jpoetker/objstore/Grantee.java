package org.jpoetker.objstore;

public class Grantee {
	public enum Type {USER, GROUP}
	
	private String subject;
	private Type type;
	private String description;
	
	public Grantee(String subject, Type type) {
		this(subject, type, null);
	}
	
	public Grantee(String subject, Type type, String description) {
		super();
		this.subject = subject;
		this.type = type;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubject() {
		return subject;
	}

	public Type getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Grantee other = (Grantee) obj;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Grantee [subject=" + subject + ", type=" + type
				+ ", description=" + description + "]";
	}
	
}
