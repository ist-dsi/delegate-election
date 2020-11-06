package endpoint;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.when;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import core.ApplicationPeriod;
import core.Calendar;
import core.CalendarDAO;
import core.Degree;
import core.Degree.DegreeType;
import core.DegreeDAO;
import core.DegreeYear;
import core.ElectionPeriod;
import core.Period;
import core.Period.PeriodType;
import core.PeriodDAO;
import core.Student;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = App.class)
//@WebIntegrationTest
public class UserRestServiceTests {

    @Autowired
    CalendarDAO calendarRepository;

    @Autowired
    DegreeDAO degreeRepository;

    @Autowired
    PeriodDAO periodRepository;

    @Autowired
    WebApplicationContext webApplicationContext;

    private Student studentOne;
    private Student studentTwo;
    private Degree degreeOne;
    private Degree degreeTwo;
    private Student studentFour;
    private Student studentThree;

    private DegreeYear firstDegreeYear;

    private DegreeYear secondDegreeYear;

    private Student studentSix;

    private Student studentFive;
    private ApplicationPeriod applicationPeriod;
    private ElectionPeriod electionPeriod;

    private ElectionPeriod electionPeriodPast;

    private ApplicationPeriod applicationPeriodPast;

    private DegreeYear thirdDegreeYear;

    private Student studentOneTwo;

    private ApplicationPeriod pastApplicationPeriod;

    @Before
    public void setUp() {
        calendarRepository.deleteAll();

        Calendar calendar = new Calendar(1);
        calendarRepository.save(calendar);

        degreeOne = new Degree("Degree1", "deg1", "1", DegreeType.Bachelor.toString(), calendar);
        calendar.addDegree(degreeOne);
        degreeTwo = new Degree("Degree2", "deg2", "2", DegreeType.Master.toString(), calendar);
        calendar.addDegree(degreeTwo);
        degreeOne.initDegreeYears();
        degreeTwo.initDegreeYears();

        firstDegreeYear = degreeOne.getDegreeYear(1);
        secondDegreeYear = degreeOne.getDegreeYear(2);
        thirdDegreeYear = degreeTwo.getDegreeYear(1);

        studentOne = new Student("Student1", "id1", "email1@email.com", "", "");
        studentOneTwo = new Student("Student1", "id1", "email1@email.com", "", "");
        studentTwo = new Student("Student2", "id2", "email2@email.com", "", "");
        studentThree = new Student("Student3", "id3", "email3@email.com", "", "");
        studentFour = new Student("Student4", "id4", "email4@email.com", "", "");
        studentFive = new Student("Student5", "id5", "email5@email.com", "", "");
        studentSix = new Student("Student6", "id6", "email6@email.com", "", "");

        studentOne.setDegreeYear(firstDegreeYear);
        firstDegreeYear.addStudent(studentOne);
        studentFive.setDegreeYear(firstDegreeYear);
        firstDegreeYear.addStudent(studentFive);

        studentSix.setDegreeYear(secondDegreeYear);
        secondDegreeYear.addStudent(studentSix);
        studentTwo.setDegreeYear(secondDegreeYear);
        secondDegreeYear.addStudent(studentTwo);
        studentFour.setDegreeYear(secondDegreeYear);
        secondDegreeYear.addStudent(studentFour);

        studentOneTwo.setDegreeYear(thirdDegreeYear);
        thirdDegreeYear.addStudent(studentOneTwo);

        firstDegreeYear.setStudentsLoaded(true);
        secondDegreeYear.setStudentsLoaded(true);
        thirdDegreeYear.setStudentsLoaded(true);

        applicationPeriod = new ApplicationPeriod(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), firstDegreeYear);
        electionPeriod = new ElectionPeriod(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), secondDegreeYear);
        pastApplicationPeriod =
                new ApplicationPeriod(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), thirdDegreeYear);

        firstDegreeYear.addPeriod(applicationPeriod);
        firstDegreeYear.setActivePeriod(applicationPeriod);

        secondDegreeYear.addPeriod(electionPeriod);
        secondDegreeYear.setActivePeriod(electionPeriod);

        thirdDegreeYear.addPeriod(pastApplicationPeriod);

        applicationPeriod.addCandidate(studentOne);

        calendarRepository.save(calendar);

        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    private void fakeUser(String username) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JsonObject principal = new JsonObject();
        principal.addProperty("username", username);
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(principal, auth));
    }

    @Test
    public void studentWithNoDegreesTest() {
        when().get("/students/{istId}/degrees", studentThree.getUsername()).then().assertThat().statusCode(200).body("",
                hasSize(0));
    }

    @Test
    public void studentWithOneDegreesTest() {
        when().get("/students/{istId}/degrees", studentTwo.getUsername()).then().assertThat().statusCode(200).body("id",
                hasItem(secondDegreeYear.getDegree().getId()));
    }

    @Test
    public void studentWithTwoDegreesTest() {
        when().get("/students/{istId}/degrees", studentOne.getUsername()).then().assertThat().statusCode(200).body("", hasSize(2))
                .body("id", hasItems(thirdDegreeYear.getDegree().getId(), firstDegreeYear.getDegree().getId()));
    }

    @Test
    public void nonVotingStudentTest() {
        MockMvcResponse response = when().get("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(),
                secondDegreeYear.getDegree().getId());
        response.then().assertThat().statusCode(200);
        assertEquals("Resposta não devia ter conteúdo.", "\"\"", response.getBody().asString());
    }

    @Test
    public void voteTest() {
        fakeUser(studentTwo.getUsername());
        given().body(studentFour.getUsername())
                .post("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), firstDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));

        when().get("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), secondDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));
    }

    @Test
    public void duplicateVoteTest() {
        fakeUser(studentTwo.getUsername());
        given().body(studentFour.getUsername())
                .post("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), firstDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));

        fakeUser(studentTwo.getUsername());
        given().body(studentFour.getUsername())
                .post("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), firstDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));

        when().get("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), secondDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));
    }

    @Test
    public void candidateStudentTest() {
        when().get("/degrees/{degreeId}/years/{year}/candidates", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear()).then().assertThat().statusCode(200)
                .body("username", hasItem(studentOne.getUsername()));
    }

    @Test
    public void nonCandidateStudentTest() {
        when().get("/degrees/{degreeId}/years/{year}/candidates", secondDegreeYear.getDegree().getId(),
                secondDegreeYear.getDegreeYear()).then().assertThat().statusCode(200).body("", hasSize(0));
    }

    @Test
    public void addCandidateStudentTest() {
        fakeUser(studentFive.getUsername());

        MockMvcResponse response = given().body(studentFive.getUsername()).post("/degrees/{degreeId}/years/{year}/candidates",
                firstDegreeYear.getDegree().getId(), firstDegreeYear.getDegreeYear());
        response.then().assertThat().statusCode(200);
        assertEquals("Resposta não devia ter conteúdo.", "\"success\"", response.getBody().asString());

        when().get("/degrees/{degreeId}/years/{year}/candidates", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear()).then().assertThat().statusCode(200)
                .body("username", hasItems(studentOne.getUsername(), studentFive.getUsername()));
    }

    @Test
    public void checkCandidateStudentTest() {
        when().get("/degrees/{degreeId}/years/{year}/candidates/{istId}", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear(), studentOne.getUsername()).then().assertThat().statusCode(200)
                .body("username", equalTo(studentOne.getUsername()));
    }

    @Test
    public void checkNonCandidateStudentTest() {
        MockMvcResponse response = when().get("/degrees/{degreeId}/years/{year}/candidates/{istId}",
                thirdDegreeYear.getDegree().getId(), thirdDegreeYear.getDegreeYear(), studentOne.getUsername());
        response.then().assertThat().statusCode(200);
        assertEquals("Resposta não devia ter conteúdo.", "\"\"", response.getBody().asString());
    }

    @Test
    public void removeCandidateStudentTest() {
        when().delete("/degrees/{degreeId}/years/{year}/candidates/{istId}", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear(), studentOne.getUsername()).then().assertThat().statusCode(200);

        when().get("/degrees/{degreeId}/years/{year}/candidates", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear()).then().assertThat().statusCode(200).body("", hasSize(0));
    }

    @Test
    public void removeNonCandidateStudentTest() {
        when().delete("/degrees/{degreeId}/years/{year}/candidates/{istId}", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear(), studentTwo.getUsername()).then().assertThat().statusCode(200);

        when().get("/degrees/{degreeId}/years/{year}/candidates", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear()).then().assertThat().statusCode(200).body("", hasSize(1));

    }

    @Test
    public void findOneStudentByUsenameTest() {
        when().get("/degrees/{degreeId}/years/{year}/students/?begins={start}", thirdDegreeYear.getDegree().getId(),
                thirdDegreeYear.getDegreeYear(), studentOne.getUsername()).then().assertThat().statusCode(200)
                .body("", hasSize(1)).body("username", hasItem(studentOne.getUsername()));
    }

    @Test
    public void findOneStudentByNameTest() {
        when().get("/degrees/{degreeId}/years/{year}/students/?begins={start}", thirdDegreeYear.getDegree().getId(),
                thirdDegreeYear.getDegreeYear(), studentOne.getName()).then().assertThat().statusCode(200).body("", hasSize(1))
                .body("username", hasItem(studentOne.getUsername()));
    }

    @Test
    public void findNoStudentsTest() {
        when().get("/degrees/{degreeId}/years/{year}/students/?begins={start}", secondDegreeYear.getDegree().getId(),
                secondDegreeYear.getDegreeYear(), studentOne.getUsername() + "a").then().assertThat().statusCode(200)
                .body("", hasSize(0));
    }

    /**
     * Manager
     */

    @Test
    public void getStudentInTwoDegreeYearsTest() {
        fakeUser("ist173833");
        given().body(degreeOne.getId()).when().get("/students/{istId}", studentOne.getUsername()).then().assertThat()
                .statusCode(200).body("username", equalTo(studentOne.getUsername())).body("name", equalTo(studentOne.getName()));
    }

    @Test
    public void getStudentTest() {
        fakeUser("ist173833");
        given().body(degreeOne.getId()).when().get("/students/{istId}", studentTwo.getUsername()).then().assertThat()
                .statusCode(200).body("username", equalTo(studentTwo.getUsername())).body("name", equalTo(studentTwo.getName()));
    }

    @Test
    public void getDegreeYearVotesTest() {
        fakeUser(studentTwo.getUsername());
        given().body(studentFour.getUsername())
                .post("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), firstDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));

        fakeUser("ist173833");
        when().get("/degrees/{degreeId}/years/{year}/votes", degreeOne.getId(), secondDegreeYear.getDegreeYear()).then()
                .assertThat().statusCode(200).body(studentFour.getUsername(), equalTo(1));
    }

    @Test
    public void getFirstDegreeYearPeriodsTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fakeUser("ist173833");
        given().body(degreeOne.getId()).when()
                .get("/degrees/{degreeId}/years/{year}/periods", degreeOne.getId(), firstDegreeYear.getDegreeYear()).then()
                .assertThat().statusCode(200).body("applicationStart", equalTo(applicationPeriod.getStart().format(dtf)))
                .body("applicationEnd", equalTo(applicationPeriod.getEnd().format(dtf))).body("electionStart", equalTo(""))
                .body("electionEnd", equalTo(""));
    }

    @Test
    public void getPeriodCandidatesTest() {
        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getActivePeriod();

        fakeUser("ist173833");
        when().get("/periods/{periodId}/candidates", period.getId()).then().assertThat().statusCode(200).body("", hasSize(1))
                .body("username", hasItem(studentOne.getUsername()));
    }

    @Test
    public void getVotedStudentsTest() {
        fakeUser(studentTwo.getUsername());
        given().body(studentFour.getUsername())
                .post("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), firstDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), secondDegreeYear.getCalendarYear())
                .getDegreeYear(secondDegreeYear.getDegreeYear()).getActivePeriod();

        fakeUser("ist173833");
        when().get("/periods/{periodId}/candidates", period.getId()).then().assertThat().statusCode(200).body("", hasSize(1))
                .body("username", hasItem(studentFour.getUsername()));
    }

    @Test
    public void getVotesTest() {
        fakeUser(studentTwo.getUsername());
        given().body(studentFour.getUsername())
                .post("/students/{istId}/degrees/{degreeId}/votes", studentTwo.getUsername(), firstDegreeYear.getDegree().getId())
                .then().assertThat().statusCode(200).body("username", equalTo(studentFour.getUsername()));

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), secondDegreeYear.getCalendarYear())
                .getDegreeYear(secondDegreeYear.getDegreeYear()).getActivePeriod();

        fakeUser("ist173833");
        when().get("/periods/{periodId}/votes", period.getId()).then().assertThat().statusCode(200)
                .body(studentFour.getUsername(), equalTo(1));
    }

    @Test
    public void checkPedagogicoRoleTest() {
        fakeUser("ist173833");
        when().get("/roles").then().assertThat().statusCode(200).body("aluno", equalTo(false)).body("pedagogico", equalTo(true));
    }

    @Test
    public void checkStudentRoleTest() {
        fakeUser(studentTwo.getUsername());
        when().get("/roles").then().assertThat().statusCode(200).body("aluno", equalTo(true)).body("pedagogico", equalTo(false));
    }

    @Test
    public void getHistoryTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JsonArray array = new JsonArray();

        JsonObject json = new JsonObject();
        json.addProperty("degreeId", degreeTwo.getId());
        JsonArray years = new JsonArray();
        JsonObject yearObject = new JsonObject();
        yearObject.addProperty("degreeYear", thirdDegreeYear.getDegreeYear());
        JsonObject applicationObject = new JsonObject();
        applicationObject.addProperty("applicationPeriodStart", LocalDate.now().plusDays(6).format(dtf));
        applicationObject.addProperty("applicationPeriodEnd", LocalDate.now().plusDays(7).format(dtf));
        JsonObject electionObject = new JsonObject();
        electionObject.addProperty("electionPeriodStart", LocalDate.now().minusDays(1).format(dtf));
        electionObject.addProperty("electionPeriodEnd", LocalDate.now().plusDays(1).format(dtf));

        yearObject.add("applicationPeriod", applicationObject);
        yearObject.add("electionPeriod", electionObject);
        years.add(yearObject);
        json.add("years", years);
        array.add(json);

        fakeUser("ist173833");
        given().body(new Gson().toJson(array)).when().put("/periods").then().assertThat().statusCode(200);

        fakeUser("ist173833");
        when().get("/degrees/{degreeId}/years/{year}/history", degreeTwo.getId(), thirdDegreeYear.getDegreeYear()).peek().then()
                .assertThat().statusCode(200).body("periods", hasSize(2)).body("periods[0].info", equalTo(0))
                .body("periods[0].periodType", equalTo(electionPeriod.getType().toString())).body("periods[1].info", equalTo(0))
                .body("periods[1].periodType", equalTo(applicationPeriod.getType().toString()));
    }

    @Test
    public void getPeriodsTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

        fakeUser("ist173833");
        when().get("/periods").then().assertThat().statusCode(200).body("", hasSize(2))
                .body("years.applicationPeriod.applicationPeriodStart",
                        hasItem(hasItem(applicationPeriod.getStart().format(dtf))))
                .body("years.applicationPeriod.applicationPeriodEnd", hasItem(hasItem(applicationPeriod.getEnd().format(dtf))))
                .body("years.electionPeriod.electionPeriodStart", hasItem(hasItem(electionPeriod.getStart().format(dtf))))
                .body("years.electionPeriod.electionPeriodEnd", hasItem(hasItem(electionPeriod.getEnd().format(dtf))));
    }

    @Test
    public void createPeriodTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JsonObject json = new JsonObject();
        json.addProperty("degreeId", degreeOne.getId());
        json.addProperty("degreeYear", firstDegreeYear.getDegreeYear());
        json.addProperty("periodType", PeriodType.Election.toString());
        json.addProperty("start", LocalDate.now().plusDays(3).format(dtf));
        json.addProperty("end", LocalDate.now().plusDays(5).format(dtf));

        fakeUser("ist173833");
        given().body(new Gson().toJson(json)).when().post("/periods").then().assertThat().statusCode(200);

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentElectionPeriod();

        if (period == null) {
            fail("Periodo não foi criado!");
        }

        assertEquals(LocalDate.now().plusDays(3).format(dtf), period.getStart().format(dtf));
        assertEquals(LocalDate.now().plusDays(5).format(dtf), period.getEnd().format(dtf));
    }

//    @Test
//    public void createPeriodInThePastTest() {
//        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        JsonObject json = new JsonObject();
//        json.addProperty("degreeId", degreeTwo.getId());
//        json.addProperty("degreeYear", thirdDegreeYear.getDegreeYear());
//        json.addProperty("periodType", PeriodType.Election.toString());
//        json.addProperty("start", LocalDate.now().minusDays(1).format(dtf));
//        json.addProperty("end", LocalDate.now().format(dtf));
//
//        fakeUser("ist173833");
//        given().body(new Gson().toJson(json)).when().post("/periods").then().assertThat().statusCode(200);
//
//        Period period = degreeRepository.findByIdAndYear(degreeTwo.getId(), thirdDegreeYear.getCalendarYear())
//                .getDegreeYear(thirdDegreeYear.getDegreeYear()).getCurrentElectionPeriod();
//
//        if (period != null) {
//            fail("Periodo foi criado!");// Não é suposto poder criar periodos a iniciar no passado.
//        }
//    }

    @Test
    public void createPeriodBadDatesTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JsonObject json = new JsonObject();
        json.addProperty("degreeId", degreeTwo.getId());
        json.addProperty("degreeYear", thirdDegreeYear.getDegreeYear());
        json.addProperty("periodType", PeriodType.Election.toString());
        json.addProperty("start", LocalDate.now().format(dtf));
        json.addProperty("end", LocalDate.now().minusDays(1).format(dtf));

        fakeUser("ist173833");
        given().body(new Gson().toJson(json)).when().post("/periods").then().assertThat().statusCode(200);

        Period period = degreeRepository.findByIdAndYear(degreeTwo.getId(), thirdDegreeYear.getCalendarYear())
                .getDegreeYear(thirdDegreeYear.getDegreeYear()).getCurrentElectionPeriod();

        if (period != null) {
            fail("Periodo foi criado!");// Não é suposto poder criar periodos que terminam antes de comecar
        }
    }

    @Test
    public void bulkPeriodUpdateTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JsonArray array = new JsonArray();

        JsonObject json = new JsonObject();
        json.addProperty("degreeId", degreeOne.getId());
        JsonArray years = new JsonArray();
        JsonObject yearObject = new JsonObject();
        yearObject.addProperty("degreeYear", firstDegreeYear.getDegreeYear());
        JsonObject applicationObject = new JsonObject();
        applicationObject.addProperty("applicationPeriodStart", LocalDate.now().minusDays(2).format(dtf));
        applicationObject.addProperty("applicationPeriodEnd", LocalDate.now().plusDays(5).format(dtf));
        JsonObject electionObject = new JsonObject();
        electionObject.addProperty("electionPeriodStart", LocalDate.now().plusDays(6).format(dtf));
        electionObject.addProperty("electionPeriodEnd", LocalDate.now().plusDays(7).format(dtf));

        yearObject.add("applicationPeriod", applicationObject);
        yearObject.add("electionPeriod", electionObject);
        years.add(yearObject);
        json.add("years", years);
        array.add(json);

        fakeUser("ist173833");
        given().body(new Gson().toJson(array)).when().put("/periods").then().assertThat().statusCode(200);

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentApplicationPeriod();

        Period electionPeriod = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentElectionPeriod();

        if (electionPeriod == null) {
            fail("Periodo não foi criado!");
        }

        assertEquals(LocalDate.now().minusDays(1).format(dtf), period.getStart().format(dtf));
        assertEquals(LocalDate.now().plusDays(5).format(dtf), period.getEnd().format(dtf));

        assertEquals(LocalDate.now().plusDays(6).format(dtf), electionPeriod.getStart().format(dtf));
        assertEquals(LocalDate.now().plusDays(7).format(dtf), electionPeriod.getEnd().format(dtf));
    }

    @Test
    public void periodUpdateTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JsonObject json = new JsonObject();
        json.addProperty("start", LocalDate.now().minusDays(1).format(dtf));
        json.addProperty("end", LocalDate.now().plusDays(5).format(dtf));

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentApplicationPeriod();

        fakeUser("ist173833");
        given().body(new Gson().toJson(json)).when().put("/periods/{periodId}", period.getId()).then().assertThat()
                .statusCode(200);

        period = periodRepository.findById(period.getId());

        if (period == null) {
            fail("Periodo não foi criado!");
        }

        assertEquals(LocalDate.now().minusDays(1).format(dtf), period.getStart().format(dtf));
        assertEquals(LocalDate.now().plusDays(5).format(dtf), period.getEnd().format(dtf));
    }

    @Test
    public void periodDeleteTest() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JsonObject json = new JsonObject();
        json.addProperty("degreeId", degreeOne.getId());
        json.addProperty("degreeYear", firstDegreeYear.getDegreeYear());
        json.addProperty("periodType", PeriodType.Election.toString());
        json.addProperty("start", LocalDate.now().plusDays(3).format(dtf));
        json.addProperty("end", LocalDate.now().plusDays(5).format(dtf));

        fakeUser("ist173833");
        given().body(new Gson().toJson(json)).when().post("/periods").then().assertThat().statusCode(200);

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentElectionPeriod();

        fakeUser("ist173833");
        when().delete("/periods/{periodId}", period.getId()).then().assertThat().statusCode(200);

        period = periodRepository.findById(period.getId());

        if (period != null) {
            fail("Periodo não foi apagado!");
        }
    }

    @Test
    public void inProgressPeriodDeleteTest() {
        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentApplicationPeriod();

        fakeUser("ist173833");
        when().delete("/periods/{periodId}", period.getId()).then().assertThat().statusCode(200);

        period = periodRepository.findById(period.getId());

        if (period == null) {
            fail("Periodo foi apagado!");
        }
    }

    @Test
    public void manuallyAddExistingCandidateTest() {
        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentApplicationPeriod();

        fakeUser("ist173833");
        when().post("/periods/{periodId}/candidates/{istId}", period.getId(), studentFive.getUsername()).then().assertThat()
                .statusCode(200);

        when().get("/degrees/{degreeId}/years/{year}/candidates", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear()).then().assertThat().statusCode(200)
                .body("username", hasItem(studentFive.getUsername()));
    }

    @Test
    public void manuallyAddCandidateStudentTest() {
        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentApplicationPeriod();

        fakeUser("ist173833");
        when().post("/periods/{periodId}/candidates/{istId}", period.getId(), studentTwo.getUsername()).then().assertThat()
                .statusCode(200);

        when().get("/degrees/{degreeId}/years/{year}/candidates", firstDegreeYear.getDegree().getId(),
                firstDegreeYear.getDegreeYear()).then().assertThat().statusCode(200)
                .body("username", hasItem(studentTwo.getUsername()));
    }

    // Falha porque o aluno está em dois degreeYears do mesmo ano.
    @Test
    public void selfProposedTest() {
        JsonObject json = new JsonObject();
        JsonArray students = new JsonArray();
        students.add(new JsonParser().parse(studentOne.getUsername()));
        json.add("usernames", students);

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), firstDegreeYear.getCalendarYear())
                .getDegreeYear(firstDegreeYear.getDegreeYear()).getCurrentApplicationPeriod();

        fakeUser("ist173833");
        given().body(new Gson().toJson(json)).when()
                .post("/periods/{periodId}/selfProposed", period.getId(), studentOne.getUsername()).then().assertThat()
                .statusCode(200).body(studentOne.getUsername(), equalTo(true));
    }

    @Test
    public void notSelfProposedTest() {
        JsonObject json = new JsonObject();
        JsonArray students = new JsonArray();
        students.add(new JsonParser().parse(studentFour.getUsername()));
        json.add("usernames", students);

        Period period = degreeRepository.findByIdAndYear(degreeOne.getId(), secondDegreeYear.getCalendarYear())
                .getDegreeYear(secondDegreeYear.getDegreeYear()).getCurrentElectionPeriod();

        fakeUser("ist173833");
        given().body(new Gson().toJson(json)).when()
                .post("/periods/{periodId}/selfProposed", period.getId(), studentFour.getUsername()).then().assertThat()
                .statusCode(200).body(studentFour.getUsername(), equalTo(false));
    }

}
