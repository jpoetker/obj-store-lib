package org.jpoetker.objstore;

public class Grant {
    private Grantee grantee;
    private Permission permission;

    public Grant(Grantee grantee, Permission permission) {
        super();
        this.grantee = grantee;
        this.permission = permission;
    }

    public Grantee getGrantee() {
        return grantee;
    }

    public Permission getPermission() {
        return permission;
    }

    public String toAclHeaderString() {
        StringBuilder buff = new StringBuilder(grantee.getSubject());
        buff.append("=");
        buff.append(permission.toString());

        return buff.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((grantee == null) ? 0 : grantee.hashCode());
        result = prime * result
                + ((permission == null) ? 0 : permission.hashCode());
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
        Grant other = (Grant) obj;
        if (grantee == null) {
            if (other.grantee != null)
                return false;
        } else if (!grantee.equals(other.grantee))
            return false;
        if (permission == null) {
            if (other.permission != null)
                return false;
        } else if (!permission.equals(other.permission))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Grant [grantee=" + grantee + ", permission=" + permission + "]";
    }

}
