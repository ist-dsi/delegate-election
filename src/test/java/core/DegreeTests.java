package core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.Degree.DegreeType;

public class DegreeTests {

    Calendar calendar;

    @Before
    public void setUp() {
        calendar = new Calendar(1);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void initMasterDegreeTest() {
        Degree degree = new Degree("", "", "1", DegreeType.Master.toString(), calendar);

        degree.initDegreeYears();

        Set<DegreeYear> years = degree.getYears();
        assertEquals("Numero errado de anos no mestrado", 2, years.size());
        for (DegreeYear degreeYear : years) {
            if (degreeYear.getDegreeYear() < 1 || degreeYear.getDegreeYear() > 2) {
                fail("Foi adicionado o ano curricular " + degreeYear.getDegreeYear() + " num mestrado!");
            }
        }
    }

    @Test
    public void initBachelorDegreeTest() {
        Degree degree = new Degree("", "", "1", DegreeType.Bachelor.toString(), calendar);

        degree.initDegreeYears();

        Set<DegreeYear> years = degree.getYears();
        assertEquals("Numero errado de anos no mestrado", 3, years.size());
        for (DegreeYear degreeYear : years) {
            if (degreeYear.getDegreeYear() < 1 || degreeYear.getDegreeYear() > 3) {
                fail("Foi adicionado o ano curricular " + degreeYear.getDegreeYear() + " numa licenciatura!");
            }
        }
    }

    @Test
    public void initIntegratedDegreeTest() {
        Degree degree = new Degree("", "", "1", DegreeType.Integrated.toString(), calendar);

        degree.initDegreeYears();

        Set<DegreeYear> years = degree.getYears();
        assertEquals("Numero errado de anos no mestrado", 5, years.size());
        for (DegreeYear degreeYear : years) {
            if (degreeYear.getDegreeYear() < 1 || degreeYear.getDegreeYear() > 5) {
                fail("Foi adicionado o ano curricular " + degreeYear.getDegreeYear() + " num mestrado integrado!");
            }
        }
    }

}
