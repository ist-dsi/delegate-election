package core;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CalendarDegreePK implements Serializable {

    @Column(name = "degree_pk_degree_name")
    private final String degreeName;

    @Column(name = "degree_pk_calendar_year")
    private final int calendarYear;

    CalendarDegreePK() {
        degreeName = "";
        calendarYear = 0;
    }

    public CalendarDegreePK(String name, int year) {
        this.degreeName = name;
        this.calendarYear = year;
    }

    public String getName() {
        return degreeName;
    }

    public int getCalendarYear() {
        return calendarYear;
    }
}
