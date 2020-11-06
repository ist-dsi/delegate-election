package core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.Degree.DegreeType;

public class VoteTests {
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

    }
}
