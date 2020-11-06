package core;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "period_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "period")
public abstract class Period implements Serializable {

    public enum PeriodType {
        Application {
            @Override
            public String toString() {
                return "APPLICATION";
            }
        },
        Election {
            @Override
            public String toString() {
                return "ELECTION";
            }
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "period_id")
    private int id;

    /*@Column(name = "degree_name")
    private String degreeName;
    
    @Column(name = "degree_year")
    private int degree_Year;
    
    @Column(name = "calendar_year")
    private int calendarYear;*/

    @Column(name = "start")
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate start;

    @Column(name = "end")
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate end;

    @ManyToMany(mappedBy = "applicationPeriod", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected Set<Student> candidates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "degree_name", referencedColumnName = "degree_year_pk_degree_name", insertable = true,
                    updatable = false),
            @JoinColumn(name = "degree_year", referencedColumnName = "degree_year_pk_degree_year", insertable = true,
                    updatable = false),
            @JoinColumn(name = "calendar_year", referencedColumnName = "degree_year_pk_calendar_year", insertable = true,
                    updatable = false) })
    protected DegreeYear degreeYear;

    @Column(name = "active")
    private boolean active;

    Period() {

    }

    // This constructor is only for temporary Periods.
    public Period(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public Period(LocalDate start, LocalDate end, DegreeYear degreeYear) {
//        if (end.isBefore(start) || start.isBefore(LocalDate.now())) {
//            throw new IllegalArgumentException();
//        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException();
        }
        //this.periodPK = new PeriodPK(degreeYear.getDegreeName(), degreeYear.getDegreeYear(), degreeYear.getCalendarYear());
        this.start = start;
        this.end = end;
        /* this.degreeName = degreeYear.getDegreeName();
         this.degree_Year = degreeYear.getDegreeYear();
         this.calendarYear = degreeYear.getCalendarYear();*/
        this.degreeYear = degreeYear;
        this.active = false;
        this.candidates = new HashSet<Student>();
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public boolean conflictsWith(Period p) {
        if (end.isBefore(p.getStart()) || start.isAfter(p.getEnd())) {
            return false;
        }
        return true;
    }

    public boolean conflictsWith(LocalDate otherStart, LocalDate otherEnd) {
        if (end.isBefore(otherStart) || start.isAfter(otherEnd)) {
            return false;
        }
        return true;
    }

    public DegreeYear getDegreeYear() {
        return degreeYear;
    }

    public void setDegreeYear(DegreeYear degreeYear) {
        this.degreeYear = degreeYear;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive() {
        this.active = true;
    }

    public void setInactive() {
        this.active = false;
    }

    public Set<Student> getCandidates() {
        candidates.size();
        return this.candidates;
    }

    public void setCandidates(Set<Student> candidates) {
        this.candidates = new HashSet<Student>(candidates);
        for (final Student s : candidates) {
            s.addPeriod(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Period)) {
            return false;
        }
        Period p = null;
        if (o instanceof ApplicationPeriod) {
            p = (ApplicationPeriod) o;
        } else {
            p = (ElectionPeriod) o;
        }
//        if (this.degree_Year == p.degree_Year && this.calendarYear == p.calendarYear && this.degreeName.equals(p.degreeName)
//                && this.start.equals(p.start) && this.end.equals(p.end)) {
//            return true;
//        } else {
//            return false;
//        }
        return p.getId() == getId();
    }

    @Override
    public int hashCode() {
//        int year = this.degree_Year;
//        int calendar = this.calendarYear;
//        int degreeName = this.degreeName.hashCode();
//        int start = this.start.hashCode();
//        int end = this.end.hashCode();
//        return year * calendar * degreeName * start * end;
        return getId();
        //Forcing equals to be called
//        return 0;
    }

    public int getId() {
        return id;
    }

    abstract public PeriodType getType();

    public void addCandidate(Student s) {
        // Só se pode adicionar candidatos a periodos activos
        if (!isActive()) {
            return;
        }
        getCandidates().add(s);
        s.addPeriod(this);
    }

    public void removeCandidates(Student s) {
        if (getCandidates().contains(s)) {
            getCandidates().remove(s);
        }
        s.removePeriod(this);
    }

    @PrePersist
    @PreUpdate
    public void checkActivation() throws Exception {
//        if (!start.isAfter(LocalDate.now()) && !end.isBefore(LocalDate.now())) {
//            if (!degreeYear.isStudentsLoaded()) {
//                degreeYear.initStudents();
//            }
//            degreeYear.setActivePeriod(this);
//            Period lastPeriod = degreeYear.getLastPeriod(LocalDate.now());
//            if (lastPeriod != null) {
//                setCandidates(lastPeriod.getCandidates());
//            }
//        }
    }

    // Isto apenas serve para os Unit Tests (já que estes não vão à db.. mas deviam)
    // Pode ser removido se passar a existir uma lista de periodos no DegreeYear
    void setId(int id) {
        this.id = id;
    }

}
