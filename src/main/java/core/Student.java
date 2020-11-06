package core;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "student")
public class Student {

    @Transient
    private String username;

    @EmbeddedId
    private StudentPK studentpk = null;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "student_period", joinColumns = { @JoinColumn(name = "username", referencedColumnName = "username"),
            @JoinColumn(name = "degree_name", referencedColumnName = "degree_name"),
            @JoinColumn(name = "degree_year", referencedColumnName = "degree_year"),
            @JoinColumn(name = "calendar_year", referencedColumnName = "calendar_year") }, inverseJoinColumns = { @JoinColumn(
            name = "period_id", referencedColumnName = "period_id") })
    private final Set<Period> applicationPeriod = new HashSet<Period>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumns({
//            @JoinColumn(name = "degree_name", referencedColumnName = "degree_year_pk_degree_name", insertable = true,
//                    updatable = false),
//            @JoinColumn(name = "degree_year", referencedColumnName = "degree_year_pk_degree_year", insertable = true,
//                    updatable = false),
//            @JoinColumn(name = "calendar_year", referencedColumnName = "degree_year_pk_calendar_year", insertable = true,
//                    updatable = false) })
//    private DegreeYear degreeYear;

    private String name;

    Student() {

    }

    public Student(String name, String username, String email, String photoType, String photoBytes) {
        this.name = name;
        this.username = username;
        this.studentpk = new StudentPK(username);
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return studentpk.getUsername();
    }

    public DegreeYear getDegreeYear() {
        return studentpk.getDegreeYear();
    }

    public void setDegreeYear(DegreeYear degreeYear) {
        if (studentpk == null) {
            this.studentpk = new StudentPK(username);
        }
        studentpk.setDegreeYear(degreeYear);
    }

    public void addPeriod(Period p) {
        this.applicationPeriod.add(p);
    }

    public void removePeriod(Period p) {
        if (this.applicationPeriod.contains(p)) {
            this.applicationPeriod.remove(p);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Student)) {
            return false;
        }
        Student s = (Student) o;
        if (s.getUsername().equals(this.getUsername())) {
            return true;
        }

        return false;
    }

	@Override
	public String toString() {
		return "Student [username=" + username + ", studentpk=" + studentpk + ", applicationPeriod=" + applicationPeriod
				+ ", name=" + name + "]";
	}
    
    

}
