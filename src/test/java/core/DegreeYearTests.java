package core;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.Degree.DegreeType;
import core.Period.PeriodType;

public class DegreeYearTests {

//    @Autowired
//    CalendarDAO calendarDAO;
//
//    @Autowired
//    PeriodDAO periodDAO;

    Calendar calendar;
    Degree degree;
    DegreeYear firstDegreeYear;

    @Before
    public void setUp() {
        calendar = new Calendar(1);
        degree = new Degree("", "", "1", DegreeType.Bachelor.toString(), calendar);
        calendar.addDegree(degree);
        degree.initDegreeYears();
        firstDegreeYear = degree.getDegreeYear(1);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getLastPeriodNoPeriodTest() {
        firstDegreeYear.addPeriod(new ApplicationPeriod(LocalDate.now(), LocalDate.now(), firstDegreeYear));

        assertEquals("Não existe último periodo!", null, firstDegreeYear.getLastPeriod(LocalDate.now()));
    }

    @Test
    public void getLastPeriodSinglePeriodTest() {
        Period period = new ApplicationPeriod(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), firstDegreeYear);
        firstDegreeYear.addPeriod(period);

        assertEquals("Periodo não foi encontrado.", period.getStart(), firstDegreeYear.getLastPeriod(LocalDate.now()).getStart());
    }

    @Test
    public void getLastPeriodTwoPeriodsTest() {
        Period firstPeriod = new ApplicationPeriod(LocalDate.now().minusDays(4), LocalDate.now().minusDays(3), firstDegreeYear);
        firstPeriod.setId(1);
        Period secondPeriod = new ApplicationPeriod(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Periodo não foi encontrado.", secondPeriod.getStart(),
                firstDegreeYear.getLastPeriod(LocalDate.now()).getStart());
    }

    @Test
    public void getNextPeriodNoPeriodTest() {
        firstDegreeYear
                .addPeriod(new ApplicationPeriod(LocalDate.now().minusDays(1), LocalDate.now().minusDays(1), firstDegreeYear));

        assertEquals("Não existe próximo periodo!", null, firstDegreeYear.getNextPeriod(LocalDate.now()));
    }

    @Test
    public void getNextPeriodSinglePeriodTest() {
        Period period = new ApplicationPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), firstDegreeYear);

        firstDegreeYear.addPeriod(period);

        assertEquals("Periodo não foi encontrado.", period.getStart(), firstDegreeYear.getNextPeriod(LocalDate.now()).getStart());
    }

    @Test
    public void getNextPeriodTwoPeriodsTest() {
        Period firstPeriod = new ApplicationPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), firstDegreeYear);
        firstPeriod.setId(1);
        Period secondPeriod = new ApplicationPeriod(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Periodo diferente obtido.", firstPeriod.getStart(),
                firstDegreeYear.getNextPeriod(LocalDate.now()).getStart());
    }

    @Test
    public void addConflictingPeriodsTest() {
        Period firstPeriod = new ApplicationPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), firstDegreeYear);
        firstPeriod.setId(1);
        Period secondPeriod = new ApplicationPeriod(LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Periodo diferente obtido.", 1, firstDegreeYear.getInactivePeriods().size());
    }

    @Test
    public void getPeriodActiveOnDateTest() {
        Period firstPeriod = new ApplicationPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), firstDegreeYear);
        firstPeriod.setId(1);
        Period secondPeriod = new ApplicationPeriod(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Devia ter periodo activo!", firstPeriod,
                firstDegreeYear.getPeriodActiveOnDate(LocalDate.now().plusDays(1)));
    }

    @Test
    public void getPeriodActiveOnDateNoPeriodTest() {
        Period firstPeriod = new ApplicationPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), firstDegreeYear);
        firstPeriod.setId(1);
        Period secondPeriod = new ApplicationPeriod(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Sem periodo activo na data!", null, firstDegreeYear.getPeriodActiveOnDate(LocalDate.now()));
    }

    @Test
    public void getCandidatesTest() {
        Period firstPeriod = new ApplicationPeriod(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), firstDegreeYear);
        firstPeriod.setId(1);
        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.setActivePeriod(firstPeriod);
        firstPeriod.addCandidate(new Student("One", "one", "", "", ""));

        Period secondPeriod = new ApplicationPeriod(LocalDate.now(), LocalDate.now().plusDays(4), firstDegreeYear);
        secondPeriod.setId(2);
        firstDegreeYear.addPeriod(secondPeriod);
        firstDegreeYear.setActivePeriod(secondPeriod);
        secondPeriod.setCandidates(firstPeriod.getCandidates());
        secondPeriod.addCandidate(new Student("Two", "two", "", "", ""));

        assertEquals("Devia ter dois candidatos!", 2, firstDegreeYear.getCandidates().size());
    }

    @Test
    public void getNoCandidatesTest() {
        Period firstPeriod = new ApplicationPeriod(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), firstDegreeYear);
        firstPeriod.setId(1);
        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.setActivePeriod(firstPeriod);
        firstPeriod.addCandidate(new Student("One", "one", "", "", ""));
        firstPeriod.setInactive();

        Period secondPeriod = new ApplicationPeriod(LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), firstDegreeYear);
        secondPeriod.setId(2);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Não devia ter candidatos!", 0, firstDegreeYear.getCandidates().size());
    }

    @Test
    public void setDateNoConflictTest() {
        LocalDate firstStart = LocalDate.now().plusDays(3);
        LocalDate firstEnd = LocalDate.now().plusDays(5);
        Period firstPeriod = new ApplicationPeriod(firstStart, firstEnd, firstDegreeYear);
        firstPeriod.setId(1);

        LocalDate secondStart = LocalDate.now().plusDays(8);
        LocalDate secondEnd = LocalDate.now().plusDays(10);
        Period secondPeriod = new ApplicationPeriod(secondStart, secondEnd, firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Foi encontrado periodo em conflicto.", false,
                firstDegreeYear.setDate(firstEnd.plusDays(1), secondStart.minusDays(1), PeriodType.Election));
    }

    @Test
    public void setDateSingleConflictTest() {
        LocalDate firstStart = LocalDate.now().plusDays(3);
        LocalDate firstEnd = LocalDate.now().plusDays(5);
        Period firstPeriod = new ApplicationPeriod(firstStart, firstEnd, firstDegreeYear);
        firstPeriod.setId(1);

        LocalDate secondStart = LocalDate.now().plusDays(8);
        LocalDate secondEnd = LocalDate.now().plusDays(10);
        Period secondPeriod = new ApplicationPeriod(secondStart, secondEnd, firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Não foi encontrado periodo em conflicto.", true,
                firstDegreeYear.setDate(firstStart.minusDays(1), firstStart.plusDays(1), PeriodType.Application));
        assertEquals("Inicio nao alterado", firstStart.minusDays(1), firstPeriod.getStart());
        assertEquals("Fim nao alterado", firstStart.plusDays(1), firstPeriod.getEnd());
    }

    @Test
    public void setDateDoubleConflictTest() {
        LocalDate firstStart = LocalDate.now().plusDays(3);
        LocalDate firstEnd = LocalDate.now().plusDays(5);
        Period firstPeriod = new ApplicationPeriod(firstStart, firstEnd, firstDegreeYear);
        firstPeriod.setId(1);

        LocalDate secondStart = LocalDate.now().plusDays(8);
        LocalDate secondEnd = LocalDate.now().plusDays(10);
        Period secondPeriod = new ApplicationPeriod(secondStart, secondEnd, firstDegreeYear);
        secondPeriod.setId(2);

        firstDegreeYear.addPeriod(firstPeriod);
        firstDegreeYear.addPeriod(secondPeriod);

        assertEquals("Não foi encontrado periodo em conflicto.", true,
                firstDegreeYear.setDate(firstStart.minusDays(1), firstEnd.plusDays(1), PeriodType.Application));
        assertEquals("Inicio nao alterado", firstStart.minusDays(1), firstPeriod.getStart());
        assertEquals("Fim nao alterado", firstEnd.plusDays(1), firstPeriod.getEnd());
    }

}
