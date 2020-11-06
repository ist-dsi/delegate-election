package core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CalendarTests {

    Calendar calendar;

    @Before
    public void setUp() {
        calendar = new Calendar(1);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void addFirstDegreeTest() {
        calendar.addDegree(new Degree("Degree1", "D1", "01", "LICENCIATURA", calendar));

        Set<Degree> degrees = calendar.getDegrees();
        assertEquals("O curso não foi iniciado!", 1, degrees.size());
    }

    @Test
    public void addSecondDegreeTest() {
        calendar.addDegree(new Degree("Degree1", "D1", "01", "LICENCIATURA", calendar));
        calendar.addDegree(new Degree("Degree2", "D2", "02", "LICENCIATURA", calendar));

        Set<Degree> degrees = calendar.getDegrees();
        assertEquals("Um ou ambos os curso não foram iniciados!", 2, degrees.size());
    }

    @Test
    public void addDuplicateDegreeTest() {
        calendar.addDegree(new Degree("Degree1", "D1", "01", "LICENCIATURA", calendar));
        calendar.addDegree(new Degree("Degree1", "D1", "01", "LICENCIATURA", calendar));

        Set<Degree> degrees = calendar.getDegrees();
        assertEquals("O segundo curso foi iniciado!", 1, degrees.size());
    }

    @Test
    public void getNoDegreesTest() {
        try {
            Set<Degree> degrees = calendar.getDegrees();
            assertEquals("Não devia ter nenhum curso.", 0, degrees.size());
        } catch (NullPointerException e) {
            fail("Foi retornado null.");
        }
    }
}
