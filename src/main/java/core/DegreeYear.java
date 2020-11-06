package core;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import core.Period.PeriodType;

@Entity
@Table(name = "DegreeYear")
public class DegreeYear {

    @EmbeddedId
    private DegreeDegreeYearPK degreeDegreeYearPK;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "degree_name", referencedColumnName = "degree_pk_degree_name", insertable = true,
                    updatable = false),
            @JoinColumn(name = "calendar_year", referencedColumnName = "degree_pk_calendar_year", insertable = true,
                    updatable = false) })
    private Degree degree;

    @OneToMany(mappedBy = "degreeYear", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Period> periods;

    @OneToMany(mappedBy = "studentpk.degreeYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Student> students;

    private boolean studentsLoaded;

    DegreeYear() {
        this.studentsLoaded = false;
        this.students = new HashSet<>();
        this.periods = new HashSet<>();
    }

    public DegreeYear(int year, Degree d) {
        this();
        this.degreeDegreeYearPK = new DegreeDegreeYearPK(d.getAcronym(), year, d.getYear());
        this.degree = d;
    }

    public int getDegreeYear() {
        return degreeDegreeYearPK.getDegreeYear();
    }

    public String getDegreeName() {
        return degree.getName();
    }

    public int getCalendarYear() {
        return degreeDegreeYearPK.getCalendarYear();
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    public Degree getDegree() {
        return degree;
    }

    public Set<Student> getCandidates() {
        final Period activePeriod = getActivePeriod();
        if (activePeriod != null) {
            return activePeriod.getCandidates();
        }

        return new HashSet<Student>();
    }

    public void setActivePeriod(Period period) {
        if (periods.contains(period)) {
            if (getActivePeriod() != null) {
                getActivePeriod().setInactive();
            }
            period.setActive();
        }
    }

    public void addPeriod(Period period) {
        if (getActivePeriod() != null && period.getStart().isBefore(getActivePeriod().getEnd())) {
            return;
        }

        for (final Period p : periods) {
            if (period.conflictsWith(p)) {
                return;
            }
        }
        periods.add(period);
    }

    public void removePeriod(Period p) {
        periods.remove(p);
    }

    public Set<Period> getInactivePeriods() {
        final Set<Period> inactive = new HashSet<Period>();
        for (final Period p : periods) {
            if (!p.isActive()) {
                inactive.add(p);
            }
        }
        return inactive;
    }

    public Period getActivePeriod() {
        for (final Period p : periods) {
            if (p.isActive()) {
                return p;
            }
        }
        return null;
    }

    public ApplicationPeriod getCurrentApplicationPeriod() {
        ApplicationPeriod period = null;
        Period active = getActivePeriod();
        if (active != null && active.getType().equals(PeriodType.Application)) {
            return (ApplicationPeriod) active;
        }
        for (final Period p : periods) {
            if (p.getType().equals(PeriodType.Application) && p.getStart().isAfter(LocalDate.now())
                    && (period == null || p.getStart().isBefore(period.getStart()))) {
                period = (ApplicationPeriod) p;
            }
        }
        if (period != null) {
            return period;
        }
        for (final Period p : periods) {
            if (p.getType().equals(PeriodType.Application) && (period == null || p.getStart().isAfter(period.getStart()))) {
                period = (ApplicationPeriod) p;
            }
        }
        return period;
    }

    public ElectionPeriod getCurrentElectionPeriod() {
        ElectionPeriod period = null;
        Period active = getActivePeriod();
        if (active != null && active.getType().equals(PeriodType.Election)) {
            return (ElectionPeriod) active;
        }
        for (final Period p : periods) {
            if (p.getType().equals(PeriodType.Election) && p.getStart().isAfter(LocalDate.now())
                    && (period == null || p.getStart().isBefore(period.getStart()))) {
                period = (ElectionPeriod) p;
            }
        }
        if (period != null) {
            return period;
        }
        for (final Period p : periods) {
            if (p.getType().equals(PeriodType.Election) && (period == null || p.getStart().isAfter(period.getStart()))) {
                period = (ElectionPeriod) p;
            }
        }
        return period;
    }

    public Period getNextPeriod() {
        Period period = null;
        for (final Period p : periods) {
            if (p.getStart().isAfter(LocalDate.now()) && (period == null || p.getStart().isBefore(period.getStart()))) {
                period = p;
            }
        }
        return period;
    }

    public boolean setDate(LocalDate start, LocalDate end, PeriodType periodType) {
        final LocalDate now = LocalDate.now();
        Period newPeriod = null;
        //Changing the past is impossible, the end can't happen before the start
        if (end.isBefore(now) || end.isBefore(start)) {
            return true;
        }
        //Change cannot conflict with more than one period
        for (final Period p : periods) {
            //No conflict
            if (p.getEnd().isBefore(start) || p.getStart().isAfter(end)) {
                continue;
            } else {
                //Two conflicts - do nothing
                if (newPeriod != null) {
                    return true;
                } else {
                    newPeriod = p;
                }
            }
        }

        if (newPeriod == null) {
            return false;
        }
        //Types should match
        if (newPeriod.getType() != periodType) {
            return true;
        }
        if (newPeriod.getStart().isAfter(now) && start.isAfter(now)) {
            newPeriod.setStart(start);
        }
        if (newPeriod.getEnd().isAfter(now) && end.isAfter(now) && end.isAfter(newPeriod.getStart())) {
            newPeriod.setEnd(end);
        }
        return true;
    }

    public Period getPeriodActiveOnDate(LocalDate date) {
        for (final Period p : periods) {
            if (!p.getStart().isAfter(date) && !p.getEnd().isBefore(date)) {
                return p;
            }
        }
        return null;
    }

    public boolean hasPeriodBetweenDates(LocalDate first, LocalDate second, Period period) {
        for (final Period p : periods) {
            if (p.getId() == period.getId()) {
                continue;
            }
            if (p.conflictsWith(first, second)) {
                return true;
            }
        }
        return false;
    }

    public Period addPeriod(LocalDate start, LocalDate end, String periodType) {
        if (start.isAfter(end)) {
            return null;
        }
//Debug
//        if (start.isBefore(LocalDate.now()) || end.isBefore(LocalDate.now())) {
//            return null;
//        }
        if (getActivePeriod() != null && start.isBefore(getActivePeriod().getEnd())) {
            return null;
        }

        for (final Period p : periods) {
            if (p.conflictsWith(start, end)) {
                return null;
            }
        }
        Period p = null;
        if (periodType.equals(PeriodType.Application.toString())) {
            p = new ApplicationPeriod(start, end, this);
            periods.add(p);
        }
        if (periodType.equals(PeriodType.Election.toString())) {
            p = new ElectionPeriod(start, end, this);
            periods.add(p);
        }
        /***
         * DEBUG
         */
        if (start.isBefore(LocalDate.now()) && end.isAfter(LocalDate.now())) {
            p.setActive();
        }
        return p;
    }

    public String getType() {
        return degree.getType();
    }

    public Period getLastPeriod(LocalDate now) {
        Period last = null;
        for (Period p : periods) {
            if (p.getEnd().isBefore(now)) {
                if (last == null) {
                    last = p;
                } else if (p.getEnd().isAfter(last.getEnd())) {
                    last = p;
                }
            }
        }
        return last;
    }

    public Period getNextPeriod(LocalDate now) {
        Period next = null;
        for (Period p : periods) {
            if (!p.getStart().isBefore(now)) {
                if (next == null) {
                    next = p;
                } else if (p.getStart().isBefore(next.getStart())) {
                    next = p;
                }
            }
        }
        return next;
    }

    public boolean isStudentsLoaded() {
        return studentsLoaded;
    }

    public void setStudentsLoaded(boolean loaded) {
        studentsLoaded = loaded;
    }

    public void addStudent(Student newStudent) {
        students.add(newStudent);
    }
}
