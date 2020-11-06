package core;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "degree")
public class Degree {

    public enum DegreeType {
        Bachelor {
            @Override
            public String toString() {
                return "Licenciatura Bolonha";
            }
        },
        Master {
            @Override
            public String toString() {
                return "Mestrado Bolonha";
            }
        },
        Integrated {
            @Override
            public String toString() {
                return "Mestrado Integrado";
            }
        }
    }

    @EmbeddedId
    @Column(name = "degree_id")
    private CalendarDegreePK calendarDegreePK;

    @ManyToOne(fetch = FetchType.LAZY)
    //  @Column(name = "Course_Calendar")
    @JoinColumn(name = "calendar_year", referencedColumnName = "year")
    private Calendar calendar;

    @OneToMany(mappedBy = "degree", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<DegreeYear> years = new HashSet<DegreeYear>();

    @Column(name = "name")
    private String name;
    @Column(name = "id")
    private String id;
    @Column(name = "acronym")
    private String acronym;
    @Column(name = "type")
    private String type;

    Degree() {

    }

    public Degree(String name, String acronym, String id, String type) {
        this.name = name;
        this.acronym = acronym;
        this.id = id;
        this.type = type;
    }

    public Degree(String name, String acronym, String id, String type, Calendar c) {
        this(name, acronym, id, type);
        this.calendarDegreePK = new CalendarDegreePK(name, c.getYear());
        this.calendar = c;
        this.years = new HashSet<DegreeYear>();
    }

    public void setCalendar(Calendar c) {
        this.calendar = c;
    }

    public void setKey() {
        this.calendarDegreePK = new CalendarDegreePK(acronym, calendar.getYear());
    }

    public void initDegreeYears() {
        if (type.equals(DegreeType.Master.toString())) {
            for (int i = 1; i <= 2; i++) {
                years.add(new DegreeYear(i, this));
            }
        } else if (type.equals(DegreeType.Bachelor.toString())) {
            for (int i = 1; i <= 3; i++) {
                years.add(new DegreeYear(i, this));
            }
        } else if (type.equals(DegreeType.Integrated.toString())) {
            for (int i = 1; i <= 5; i++) {
                years.add(new DegreeYear(i, this));
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return calendar.getYear();
    }

    public DegreeYear getDegreeYear(int year) {
        for (final DegreeYear d : years) {
            if (d.getDegreeYear() == year) {
                return d;
            }
        }
        return null;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Set<DegreeYear> getYears() {
        return years;
    }

//    public void addYear(int i) {
//        years.add(new DegreeYear(i, this));
//    }

    public String print() {
        return "Name: " + name + " Type: " + type + " Teste Integrado: " + type.equals("Mestrado Integrado");
    }

}
