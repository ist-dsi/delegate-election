package core;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DegreeDegreeYearPK implements Serializable {

    @Column(name = "degree_year_pk_degree_name")
    private String degreeName;

    @Column(name = "degree_year_pk_degree_year")
    private int degreeYear;

    @Column(name = "degree_year_pk_calendar_year")
    private int calendarYear;

    DegreeDegreeYearPK() {

    }

    public DegreeDegreeYearPK(String degreeName, int degreeYear, int calendarYear) {
        this.degreeName = degreeName;
        this.degreeYear = degreeYear;
        this.calendarYear = calendarYear;
    }

    public String getDegreeName() {
        return degreeName;
    }

    public int getDegreeYear() {
        return degreeYear;
    }

    public int getCalendarYear() {
        return calendarYear;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DegreeDegreeYearPK)) {
            return false;
        }
        DegreeDegreeYearPK pk = (DegreeDegreeYearPK) o;
        if (degreeName.equals(pk.getDegreeName()) && degreeYear == pk.getDegreeYear() && calendarYear == pk.getDegreeYear()) {
            return true;
        }
        return false;
    }

}
