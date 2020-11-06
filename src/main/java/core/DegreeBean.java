package core;

import javax.persistence.Column;

/**
 * Created by Sérgio Silva (hello@fenixedu.org).
 */
public class DegreeBean {

    private String name;
    private String id;
    private String acronym;
    private String type;

    public DegreeBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DegreeBean)) {
            return false;
        }

        DegreeBean that = (DegreeBean) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        if (getAcronym() != null ? !getAcronym().equals(that.getAcronym()) : that.getAcronym() != null) {
            return false;
        }
        return getType() != null ? getType().equals(that.getType()) : that.getType() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        result = 31 * result + (getAcronym() != null ? getAcronym().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DegreeBean{" + "name='" + name + '\'' + ", id='" + id + '\'' + ", acronym='" + acronym + '\'' + ", type='" + type
                + '\'' + '}';
    }
}
