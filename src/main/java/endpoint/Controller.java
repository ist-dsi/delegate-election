package endpoint;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.security.oauth2.sso.EnableOAuth2Sso;
import org.springframework.cloud.security.oauth2.sso.OAuth2SsoConfigurerAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import adapter.DegreeAdapter;
import adapter.DegreeChange;
import adapter.DegreePeriodAdapter;
import adapter.DegreeYearAdapter;
import adapter.DegreeYearHistoryAdapter;
import adapter.HibernateProxyTypeAdapter;
import adapter.PeriodChange;
import adapter.StudentAdapter;
import core.ApplicationPeriod;
import core.Calendar;
import core.CalendarDAO;
import core.Degree;
import core.DegreeDAO;
import core.DegreeYear;
import core.ElectionPeriod;
import core.Period;
import core.Period.PeriodType;
import core.PeriodDAO;
import core.Student;
import core.StudentDAO;
import core.Vote;

@EnableOAuth2Sso
@RestController
public class Controller {

    @Autowired
    private AccessTokenHandler accessTokenHandler;

    @Autowired
    StudentDAO studentDAO;

    @Autowired
    DegreeDAO degreeDAO;

    @Autowired
    CalendarDAO calendarDAO;

    @Autowired
    PeriodDAO periodDAO;

    @Autowired
    Environment env;

    @Value("#{'${pedagogico.users}'.split(',')}")
    Set<String> userset;

    @RequestMapping(value = "/students/{istId}/degrees", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody String getStudentDegrees(@PathVariable String istId) {
        final Set<DegreeYear> studentDegrees =
                StreamSupport
                        .stream(studentDAO.findByUsername(istId, calendarDAO.findFirstByOrderByYearDesc().getYear())
                                .spliterator(), false).map(Student::getDegreeYear).collect(Collectors.toSet());
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
        gsonBuilder.registerTypeAdapter(DegreeYear.class, new DegreeAdapter());
        final Gson gson = gsonBuilder.create();
        return gson.toJson(studentDegrees);
    }

    @RequestMapping(value = "/students/{istId}/degrees/{degreeId}/votes", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getVote(@PathVariable String istId, @PathVariable String degreeId) {
        if (!getLoggedUsername().equals(istId)) {
            return new Gson().toJson("");
        }

        final Student student =
                StreamSupport
                        .stream(studentDAO.findByUsername(istId, calendarDAO.findFirstByOrderByYearDesc().getYear())
                                .spliterator(), false).filter(s -> s.getDegreeYear().getDegree().getId().equals(degreeId))
                        .collect(Collectors.toList()).get(0);

        // Desta forma é possível obter o voto do último periodo também.
        ElectionPeriod ePeriod = student.getDegreeYear().getCurrentElectionPeriod();
        if (ePeriod == null) {
            return new Gson().toJson("No election period.");
        }
        String voted = ePeriod.getVote(istId);
        if (voted == null) {
            return new Gson().toJson("Student hasn't voted.");
        }
        if (voted.isEmpty()) {
            return new Gson().toJson(null);
        }
        Student st =
                studentDAO.findByUsernameAndDegreeAndCalendarYear(voted, degreeId, calendarDAO.findFirstByOrderByYearDesc()
                        .getYear());
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        return gson.toJson(st);
    }

    @RequestMapping(value = "/students/{istId}/degrees/{degreeId}/votes", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String addVote(@PathVariable String istId, @PathVariable String degreeId, @RequestBody String vote) {
        if (!getLoggedUsername().equals(istId)) {
            return new Gson().toJson("");
        }

        final Student student =
                StreamSupport
                        .stream(studentDAO.findByUsername(istId, calendarDAO.findFirstByOrderByYearDesc().getYear())
                                .spliterator(), false).filter(s -> s.getDegreeYear().getDegree().getId().equals(degreeId))
                        .collect(Collectors.toList()).get(0);
        Student candidate;
        if (vote.equals("nil")) {
            candidate = null;
        } else {
            candidate =
                    StreamSupport
                            .stream(studentDAO.findByUsername(vote, calendarDAO.findFirstByOrderByYearDesc().getYear())
                                    .spliterator(), false).filter(s -> s.getDegreeYear().getDegree().getId().equals(degreeId))
                            .collect(Collectors.toList()).get(0);
        }

        //The active periods must be the same for both students
        final Period period = student.getDegreeYear().getActivePeriod();
        ElectionPeriod ePeriod;
        try {
            ePeriod = (ElectionPeriod) period;
            String voted = ePeriod.getVote(istId);
            if (voted != null) {
                return new Gson().toJson("Student has already voted.");
            }
            ePeriod.vote(student, candidate);
        } catch (final ClassCastException e) {
            // Se o cast não foi possivel, o periodo actual nao e de eleicao
            final Gson gson = new Gson();
            return new Gson().toJson("");
        }
        studentDAO.save(student);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        return gson.toJson(candidate);
    }

    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/candidates", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getCandidates(@PathVariable String degreeId, @PathVariable int year) {
        final Set<Student> candidates =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year)
                        .getCandidates();
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        return gson.toJson(candidates);
    }

    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/candidates", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String addCandidate(@PathVariable String degreeId, @PathVariable int year, @RequestBody String istId) {
        if (!getLoggedUsername().equals(istId) && !hasAccessToManagement()) {
            return new Gson().toJson("fail");
        }
        final Student applicant =
                StreamSupport
                        .stream(studentDAO.findByUsername(istId, calendarDAO.findFirstByOrderByYearDesc().getYear())
                                .spliterator(), false)
                        .filter(s -> s.getDegreeYear().getDegree().getId().equals(degreeId)
                                && s.getDegreeYear().getDegreeYear() == year).collect(Collectors.toList()).get(0);

        final Period period = applicant.getDegreeYear().getActivePeriod();
        try {
            final ApplicationPeriod aPeriod = (ApplicationPeriod) period;
            aPeriod.addCandidate(applicant);
        } catch (final ClassCastException e) {
            // Se o cast não foi possivel, o periodo actual nao e de condidatura
            final Gson gson = new Gson();
            return gson.toJson("fail");
        }
        studentDAO.save(applicant);
        //TODO Smarter responses
        final Gson gson = new Gson();
        return gson.toJson("success");
    }

    //Se a resposta for vazia, não se candidatou a este curso. Talvez seja mais simples retornar false ou true?
    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/candidates/{istId}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getCandidate(@PathVariable String degreeId, @PathVariable int year, @PathVariable String istId) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        final List<Student> s =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year)
                        .getCandidates().stream().filter(c -> c.getUsername().equals(istId)).collect(Collectors.toList());
        if (s.isEmpty()) {
            return new Gson().toJson("");
        }
        final Student candidate = s.get(0);

        return gson.toJson(candidate);
    }

    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/candidates/{istId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String removeCandidate(@PathVariable String degreeId, @PathVariable int year, @PathVariable String istId) {

        if (!getLoggedUsername().equals(istId)) {
            return new Gson().toJson("");
        }

        final List<Student> candidateList =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year)
                        .getCandidates().stream().filter(c -> c.getUsername().equals(istId)).collect(Collectors.toList());
        if (candidateList.size() < 1) {
            return new Gson().toJson("fail");
        }
        Student candidate = candidateList.get(0);
        ((ApplicationPeriod) candidate.getDegreeYear().getActivePeriod()).removeCandidates(candidate);
        studentDAO.save(candidate);
        final Gson g = new Gson();
        //TODO smarter Responses
        return g.toJson("Ok");
    }

    // Este Endpoint pode ser usado para obter todos os estudantes em que se pode votar.
    // Para cada estudante, devolve: nome, id e foto
    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/students", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getDegreeYearStudents(@PathVariable String degreeId, @PathVariable int year, @RequestParam(
            value = "begins", required = false) String start) {
        final Set<Student> students =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year)
                        .getStudents();
        Set<Student> filteredStudents = students;
        if (start != null) {
            filteredStudents =
                    students.stream()
                            .filter(s -> s.getName().toLowerCase().contains(start.toLowerCase())
                                    || s.getUsername().toLowerCase().equals(start.toLowerCase())).collect(Collectors.toSet());
        }
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        return gson.toJson(filteredStudents);
    }

    // TODO Test this!
    @RequestMapping(value = "/students", method = RequestMethod.GET)
    public @ResponseBody String findStudent(@RequestParam(value = "begins", required = false) String start) {

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        Iterable<Student> students = studentDAO.findByUsername(start, calendarDAO.findFirstByOrderByYearDesc().getYear());
        if (students.iterator().hasNext()) {
            Set<Student> result = new HashSet<Student>();
            result.add(students.iterator().next());
            return gson.toJson(result);
        }
        Set<Student> sts = studentDAO.findByName(start, calendarDAO.findFirstByOrderByYearDesc().getYear());
        return gson.toJson(sts);
    }

    /***************************** Manager API *****************************/
    @RequestMapping(value = "/students/{istId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody String getStudent(@PathVariable String istId, @RequestBody String degreeId) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        final Student student =
                studentDAO.findByUsernameAndDegreeAndCalendarYear(istId, degreeId, calendarDAO.findFirstByOrderByYearDesc()
                        .getYear());
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        return gson.toJson(student);
    }

    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/votes", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getVotes(@PathVariable String degreeId, @PathVariable int year) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        //Obtem todos os votos (aluno -> numero de votos)
        final Map<String, Long> voteCount =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year)
                        .getCurrentElectionPeriod().getVotes().stream()
                        .collect(Collectors.groupingBy(v -> v.getVoted(), Collectors.counting()));
        final Gson gson = new Gson();
        return gson.toJson(voteCount);
    }

    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/history", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getHistoy(@PathVariable String degreeId, @PathVariable int year) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        final DegreeYear degreeYear =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(DegreeYear.class, new DegreeYearHistoryAdapter()).create();
        return gson.toJson(degreeYear);
    }

    @RequestMapping(value = "/degrees/{degreeId}/years/{year}/periods", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getPeriods(@PathVariable String degreeId, @PathVariable int year) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        final DegreeYear degreeYear =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(DegreeYear.class, new DegreePeriodAdapter()).create();
        return gson.toJson(degreeYear);
    }

    @RequestMapping(value = "/periods", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody String getPeriods() {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        final Set<Degree> degrees =
                StreamSupport.stream(degreeDAO.findAll().spliterator(), false)
                        .filter(d -> d.getYear() == calendarDAO.findFirstByOrderByYearDesc().getYear())
                        .collect(Collectors.toSet());
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Degree.class, new DegreeYearAdapter()).create();
        final Gson gson = gsonBuilder.create();
        return gson.toJson(degrees);
    }

    // Permitir criar apenas periodos futuros
    @RequestMapping(value = "/periods", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody String addPeriod(@RequestBody String periodJson) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final JsonParser parser = new JsonParser();

        final JsonObject periodObject = (JsonObject) parser.parse(periodJson);
        final String degreeId = periodObject.get("degreeId").getAsString();
        final int year = periodObject.get("degreeYear").getAsInt();
        final String periodType = periodObject.get("periodType").getAsString();
        final LocalDate start = LocalDate.parse(periodObject.get("start").getAsString(), dtf);
        final LocalDate end = LocalDate.parse(periodObject.get("end").getAsString(), dtf);

        final DegreeYear degreeYear =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year);

        if (periodType.equals(PeriodType.Application.toString())) {
            final Period period = degreeYear.addPeriod(start, end, periodType);
            if (period != null) {
//                period.schedulePeriod(periodDAO, degreeDAO);
                periodDAO.save(period);
            }
        } else if (periodType.equals(PeriodType.Election.toString())) {
            final Period period = degreeYear.addPeriod(start, end, periodType);
            if (period != null) {
//                period.schedulePeriod(periodDAO, degreeDAO);
                periodDAO.save(period);
            }
        }

        return new Gson().toJson("ok");
    }

    // Permitir alterar apenas periodos futuros
    // Permitir criar
    @RequestMapping(value = "/periods", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody String updatePeriods(@RequestBody String periodsJson) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }
        System.out.println(periodsJson);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(DegreeChange.class, new DegreeYearAdapter()).create();
        final DegreeChange[] degrees = gson.fromJson(periodsJson, DegreeChange[].class);
        for (final DegreeChange degreeChange : degrees) {
            for (final Integer year : degreeChange.getPeriods().keySet()) {
                for (final PeriodChange change : degreeChange.getPeriods().get(year)) {
                    final DegreeYear degreeYear =
                            degreeDAO.findByIdAndYear(degreeChange.getDegreeId(),
                                    calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year);
                    if (degreeYear == null) {
                        continue;
                    }
                    final boolean periodExists = degreeYear.setDate(change.getStart(), change.getEnd(), change.getPeriodType());
                    // Se o periodo existe, tem de agendar novamente, pois uma ou ambas as datas foram alteradas.
                    if (periodExists) {
                        System.out.println("Period Exists");
                        Period period = degreeYear.getPeriodActiveOnDate(change.getStart());
                        if (period == null) {
                            period = degreeYear.getPeriodActiveOnDate(change.getEnd());
                        } else {
                            continue;
                        }
//                        period.unschedulePeriod(periodDAO, degreeDAO);
//                        period.schedulePeriod(periodDAO, degreeDAO);
                    } else {
                        System.out.println("Period Does Not Exist");
                        if (change.getPeriodType().toString().equals(PeriodType.Application.toString())) {
                            final Period period = new ApplicationPeriod(change.getStart(), change.getEnd(), degreeYear);
                            degreeYear.addPeriod(period);
//                            period.schedulePeriod(periodDAO, degreeDAO);
                            periodDAO.save(period);
                        } else if (change.getPeriodType().toString().equals(PeriodType.Election.toString())) {
                            final Period period = new ElectionPeriod(change.getStart(), change.getEnd(), degreeYear);
                            degreeYear.addPeriod(period);
//                            period.schedulePeriod(periodDAO, degreeDAO);
                            periodDAO.save(period);
                        }
                    }
                }
            }
        }
        calendarDAO.save(calendarDAO.findFirstByOrderByYearDesc());
        return new Gson().toJson("ok");
    }

    @RequestMapping(value = "/periods/{periodId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public @ResponseBody String removePeriods(@PathVariable int periodId) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        final Period period = periodDAO.findById(periodId);
        DegreeYear dy = period.getDegreeYear();

        if (period.getStart().isAfter(LocalDate.now())) {
            dy.removePeriod(period);
            periodDAO.delete(period);
            return new Gson().toJson("deleted period " + periodId);
        }
        return new Gson().toJson("didn't delete period " + periodId);
    }

    // recebe Json com 2 datas: start e end
    @RequestMapping(value = "/periods/{periodId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody String updatePeriod(@PathVariable int periodId, @RequestBody String dates) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final JsonParser parser = new JsonParser();

        final Period period = periodDAO.findById(periodId);
        final DegreeYear degreeYear = period.getDegreeYear();

        final JsonObject datesObject = (JsonObject) parser.parse(dates);
        final LocalDate newStart = LocalDate.parse(datesObject.get("start").getAsString(), dtf);
        final LocalDate newEnd = LocalDate.parse(datesObject.get("end").getAsString(), dtf);

        final LocalDate start = period.getStart();
        final LocalDate end = period.getEnd();

        if (newStart.isAfter(LocalDate.now()) && start.isAfter(LocalDate.now())
                && !degreeYear.hasPeriodBetweenDates(newStart, end, period)) {
            period.setStart(newStart);
        }

        if (newEnd.isAfter(LocalDate.now()) && end.isAfter(LocalDate.now()) && newEnd.isAfter(period.getStart())
                && !degreeYear.hasPeriodBetweenDates(start, newEnd, period)) {
            period.setEnd(newEnd);
        }

        periodDAO.save(period);
        return new Gson().toJson("ok");
    }

    @RequestMapping(value = "/periods/{periodId}/candidates", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String getCandidatesFromPeriod(@PathVariable int periodId) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }
        final Period period = periodDAO.findById(periodId);
        if (period == null) {
            return new Gson().toJson("No Period with that Id");
        }
        final Set<Student> candidates = period.getCandidates();
        //Necessário ir buscar estudantes não auto-propostos
        if (period.getType().toString().equals(PeriodType.Election.toString())) {
            final Set<Vote> votes = ((ElectionPeriod) period).getVotes();
            Student st = null;
            for (final Vote v : votes) {
                st =
                        studentDAO.findByUsernameAndDegreeAndCalendarYear(v.getVoted(), period.getDegreeYear().getDegree()
                                .getId(), period.getDegreeYear().getCalendarYear());
                if (st != null) {
                    candidates.add(st);
                }
                st = null;
            }
        }
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(Student.class, new StudentAdapter()).create();
        return gson.toJson(candidates);
    }

    @RequestMapping(value = "/periods/{periodId}/candidates/{istId}", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String addCandidateToPeriod(@PathVariable int periodId, @PathVariable String istId) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }
        final Period period = periodDAO.findById(periodId);
        if (period == null) {
            return new Gson().toJson("No Period with that Id");
        }

        final Set<Student> students = period.getDegreeYear().getStudents();
        for (final Student s : students) {
            if (s.getUsername().equals(istId) && !period.getCandidates().contains(s)) {
                period.addCandidate(s);
                studentDAO.save(s);
                return new Gson().toJson("ok");
            }
        }
        Iterable<Student> sts = studentDAO.findByUsername(istId, calendarDAO.findFirstByOrderByYearDesc().getYear());
        if (sts != null && sts.iterator().hasNext()) {
            Student s = sts.iterator().next();
            period.addCandidate(s);
            studentDAO.save(s);
        }
        return new Gson().toJson("ok?");
    }

    @RequestMapping(value = "/periods/{periodId}/selfProposed", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String selfPropposed(@PathVariable int periodId, @RequestBody String studentJson) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }
        Period period = periodDAO.findById(periodId);
        Set<Student> candidates = period.getCandidates();
        JsonParser parser = new JsonParser();
        JsonObject students = new JsonParser().parse(studentJson).getAsJsonObject();
        JsonArray array = students.get("usernames").getAsJsonArray();
        List<String> usernames = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            usernames.add(array.get(i).getAsString());
        }
        JsonObject result = new JsonObject();
        for (String s : usernames) {
            Iterator<Student> stIt = studentDAO.findByUsername(s, period.getDegreeYear().getCalendarYear()).iterator();
            if (stIt.hasNext()) {
                if (candidates.contains(stIt.next())) {
                    result.addProperty(s, true);
                } else {
                    result.addProperty(s, false);
                }
            }
        }
        return new Gson().toJson(result);
    }

    @RequestMapping(value = "/periods/{periodId}/votes", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody String periodVotes(@PathVariable int periodId) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("");
        }
        final Period period = periodDAO.findById(periodId);
        if (period == null) {
            return new Gson().toJson("Periodo com esse Id não existe.");
        }
        if (period.getType().equals(PeriodType.Application)) {
            return new Gson().toJson("Periodos de Candidaturas não têm votos.");
        }
        final ElectionPeriod ePeriod = (ElectionPeriod) period;
        final JsonObject result = new JsonObject();
        Map<String, Integer> votos = new HashMap<String, Integer>();
        for (Student st : ePeriod.getCandidates()) {
            votos.put(st.getUsername(), 0);
        }
        for (Vote v : ePeriod.getVotes()) {
            String voted = v.getVoted();
            if (votos.containsKey(voted)) {
                int nVotos = votos.get(voted);
                nVotos++;
                votos.put(voted, nVotos);
            } else {
                votos.put(voted, 1);
            }
        }
        if (votos.keySet().contains("")) {
            int i = votos.get("");
            votos.remove("");
            votos.put("branco", i);
        }
        for (String s : votos.keySet()) {
            result.addProperty(s, votos.get(s));
        }

        return new Gson().toJson(result);
    }

    @RequestMapping(value = "/roles", produces = "application/json; charset=utf-8")
    public @ResponseBody String roles() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username;
        final Map<String, Object> userDetails;
        if (auth instanceof OAuth2Authentication) {
            final OAuth2Authentication oauth = (OAuth2Authentication) auth;
            userDetails = (Map<String, Object>) oauth.getUserAuthentication().getDetails();
            username = (String) userDetails.get("username");
        } else {
            final JsonObject jo = (JsonObject) auth.getPrincipal();
            final JsonElement je = jo.get("username");
            username = je.getAsString();
        }

        final JsonObject result = new JsonObject();
        result.addProperty("pedagogico", hasAccessToManagement());
        Iterable<Student> students = null;
        students = studentDAO.findByUsername(username, calendarDAO.findFirstByOrderByYearDesc().getYear());
        result.addProperty("aluno", students.iterator().hasNext());
        return new Gson().toJson(result);
    }

    @RequestMapping(value = "/delegates/{degreeAcronym}/{istId}/{degreeYear}", method = RequestMethod.GET)
    public @ResponseBody String setDelegates(@PathVariable String degreeAcronym, @PathVariable String istId,
            @PathVariable Integer degreeYear) {
        if (!hasAccessToManagement()) {
            return new Gson().toJson("Unauthorized");
        }
        if (degreeAcronym != null && istId != null && degreeYear != null) {
            try {
                return accessTokenHandler.setDelegates(degreeAcronym, istId, degreeYear);
            } catch (Exception e) {
                e.printStackTrace();
                JsonObject json = new JsonObject();
                json.addProperty("error", e.getMessage());
                return json.toString();
            }
        } else {
            return new Gson().toJson("Wrong Parameters");
        }
    }

    /*****************************
     * Commands
     * 
     * @throws Exception
     *****************************/

    public String createCalendar() throws Exception {
        final LocalDate now = LocalDate.now();
        final Calendar lastCreatedCalendar = calendarDAO.findFirstByOrderByYearDesc();
        if (lastCreatedCalendar != null) {
            final int lastCreatedYear = lastCreatedCalendar.getYear();
            if (now.isBefore(LocalDate.of(lastCreatedYear + 1, 8, 31))) {
                return "Calendar of " + lastCreatedYear + "/" + (lastCreatedYear + 1)
                        + " already exists. Can only create a new Calendar after August";
            }
        }
        if (now.isBefore(LocalDate.of(now.getYear(), 8, 31))) {
            final Calendar c = new Calendar(now.getYear() - 1);
            accessTokenHandler.init(c);
            calendarDAO.save(c);
            return "Calendar of " + (now.getYear() - 1) + "/" + now.getYear() + " created.";
        } else {
            final Calendar c = new Calendar(now.getYear());
            accessTokenHandler.init(c);
            calendarDAO.save(c);
            return "Calendar of " + now.getYear() + "/" + (now.getYear() + 1) + " created.";
        }
    }

    public String command(String periodJson) {

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final JsonParser parser = new JsonParser();

        final JsonObject periodObject = (JsonObject) parser.parse(periodJson);
        final String degreeId = periodObject.get("degreeId").getAsString();
        final int year = periodObject.get("degreeYear").getAsInt();
        final String periodType = periodObject.get("periodType").getAsString();
        final LocalDate start = LocalDate.parse(periodObject.get("start").getAsString(), dtf);
        final LocalDate end = LocalDate.parse(periodObject.get("end").getAsString(), dtf);

        final DegreeYear degreeYear =
                degreeDAO.findByIdAndYear(degreeId, calendarDAO.findFirstByOrderByYearDesc().getYear()).getDegreeYear(year);

        if (periodType.equals(PeriodType.Application.toString())) {
            final Period period = degreeYear.addPeriod(start, end, periodType);
            if (period != null) {
//                period.schedulePeriod(periodDAO, degreeDAO);
                periodDAO.save(period);
            }
        } else if (periodType.equals(PeriodType.Election.toString())) {
            final Period period = degreeYear.addPeriod(start, end, periodType);
            if (period != null) {
//                period.schedulePeriod(periodDAO, degreeDAO);
                periodDAO.save(period);
            }
        }

        return new Gson().toJson("ok");
    }

    /***************************** OLD API *****************************/
    @RequestMapping("/user")
    public @ResponseBody String user() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final Gson gson = new Gson();

        if (auth instanceof OAuth2Authentication) {
            final OAuth2Authentication oauth = (OAuth2Authentication) auth;
            final Map<String, Object> userDetails = (Map<String, Object>) oauth.getUserAuthentication().getDetails();
            return gson.toJson(userDetails);
        } else {
            return gson.toJson(auth.getPrincipal());
        }
    }

    private String getLoggedUsername() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username;
        final Map<String, Object> userDetails;
        if (auth instanceof OAuth2Authentication) {
            final OAuth2Authentication oauth = (OAuth2Authentication) auth;
            userDetails = (Map<String, Object>) oauth.getUserAuthentication().getDetails();
            username = (String) userDetails.get("username");
        } else {
            final JsonObject jo = (JsonObject) auth.getPrincipal();
            final JsonElement je = jo.get("username");
            username = je.getAsString();
        }
        return username;
    }

    private boolean hasAccessToManagement() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username;
        final Map<String, Object> userDetails;
        if (auth instanceof OAuth2Authentication) {
            final OAuth2Authentication oauth = (OAuth2Authentication) auth;
            userDetails = (Map<String, Object>) oauth.getUserAuthentication().getDetails();
            username = (String) userDetails.get("username");
        } else {
            final JsonObject jo = (JsonObject) auth.getPrincipal();
            final JsonElement je = jo.get("username");
            username = je.getAsString();
        }
        return userset.contains(username);
    }

    /*********************************** CONFIG *****************************************/
    @Configuration
    protected static class SecurityConfiguration extends OAuth2SsoConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.logout().and().antMatcher("/**").authorizeRequests().antMatchers("/index.html", "/", "/login").permitAll().and()
                    .authorizeRequests()
                    .antMatchers("/home.html", "/user", "/user", "/students/**", "/degrees/**", "/periods/**, /roles/**")
                    .authenticated().and().csrf().csrfTokenRepository(csrfTokenRepository()).and()
                    .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
        }

        private Filter csrfHeaderFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    final CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (csrf != null) {
                        Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                        final String token = csrf.getToken();
                        if (cookie == null || token != null && !token.equals(cookie.getValue())) {
                            cookie = new Cookie("XSRF-TOKEN", token);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                    }
                    filterChain.doFilter(request, response);
                }
            };
        }

        private CsrfTokenRepository csrfTokenRepository() {
            final HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
            repository.setHeaderName("X-XSRF-TOKEN");
            return repository;
        }
    }
}