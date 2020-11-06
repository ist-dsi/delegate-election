package core;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

@Embeddable
public class StudentPK implements Serializable {

    @Column(name = "username")
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "degree_name", referencedColumnName = "degree_year_pk_degree_name", insertable = true,
                    updatable = false),
            @JoinColumn(name = "degree_year", referencedColumnName = "degree_year_pk_degree_year", insertable = true,
                    updatable = false),
            @JoinColumn(name = "calendar_year", referencedColumnName = "degree_year_pk_calendar_year", insertable = true,
                    updatable = false) })
    private DegreeYear degreeYear;

    StudentPK() {
    }

    public StudentPK(String username) {
        this.username = username;
    }

    public StudentPK(String username, DegreeYear degreeYear) {
        this.username = username;
        this.degreeYear = degreeYear;
    }

    public void setDegreeYear(DegreeYear dy) {
        this.degreeYear = dy;
    }

    public String getUsername() {
        return username;
    }

    public DegreeYear getDegreeYear() {
        return degreeYear;
    }
}
