package endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import core.Calendar;
import core.Degree;
import core.DegreeBean;
import core.DegreeYear;
import core.Student;

@Component
public class AccessTokenHandler {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenHandler.class);

    @Value("${dataclient.uri}")
    private String base_domain;

    @Value("${dataclient.serverId}")
    private String clientId;

    @Value("${dataclient.serverSecret}")
    private String clientSecret;

    private String accessToken;

    @PostConstruct
    private void setupNewAccessToken() {
        if (base_domain == null || clientId == null || clientSecret == null) {
            throw new RuntimeException("Exception reading from .yml at AccessTokenHandler");
        }

        if (accessToken != null) {
            return;
        }

        RestTemplate t = new RestTemplate();
        String infoUrl =
                base_domain + "/oauth/access_token?client_id=" + clientId + "&client_secret=" + clientSecret
                        + "&grant_type=client_credentials";
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<String>(requestHeaders);
        HttpEntity<String> response = t.exchange(infoUrl, HttpMethod.POST, requestEntity, String.class);
        JsonObject json = new JsonParser().parse(response.getBody()).getAsJsonObject();
        accessToken = json.get("access_token").getAsString();
        logger.info("Setup access token {}", accessToken);
    }

    public Degree[] getDegrees() {
        RestTemplate t = new RestTemplate();
        DegreeBean[] degreesBeans = t.getForObject(base_domain + "/api/fenix/v1/degrees?lang=pt-PT", DegreeBean[].class);
        return Arrays.stream(degreesBeans).distinct().map(bean -> new Degree(bean.getName(), bean.getAcronym(), bean.getId(), bean.getType())).toArray(Degree[]::new);
    }

    public Set<StudentDto> getStudents(String degreeId, int degreeYear) {
        final RestTemplate t = new RestTemplate();

        try {
            StudentDto[] students = t.getForObject(base_domain + "/api/fenix/v1/degrees/" + degreeId + "/students?curricularYear=" + degreeYear
                            + "&access_token=" + accessToken, StudentDto[].class);
            return Stream.of(students).collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Error getting students", e);
            return null;
        }
    }

    public String setDelegates(String degreeAcronym, String istId, Integer degreeYear) {
        OAuth2Authentication auth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails authDetails = (OAuth2AuthenticationDetails) auth.getDetails();

        RestTemplate t = new RestTemplate();
        String infoUrl =
                base_domain + "/api/fenix/v1/degrees/" + degreeAcronym + "/delegates?access_token=" + authDetails.getTokenValue();
        JsonObject json = new JsonObject();
        json.addProperty("delegateId", istId);
        json.addProperty("curricularYear", degreeYear.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Requested-With", "spring-rest-template");
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");

        HttpEntity<String> requestEntity = new HttpEntity<String>(json.toString(), headers);
        HttpEntity<String> responseEntity = t.exchange(infoUrl, HttpMethod.PUT, requestEntity, String.class);
        return responseEntity.getBody();
    }

    public void init(Calendar calendar) {
        final RestTemplate t = new RestTemplate();
        final Degree[] c = getDegrees();

        for (final Degree element : c) {
            if (element.getType().equals(Degree.DegreeType.Bachelor.toString())
                    || element.getType().equals(Degree.DegreeType.Integrated.toString())
                    || element.getType().equals(Degree.DegreeType.Master.toString())) {
                element.setCalendar(calendar);
                element.setKey();
                calendar.getDegrees().add(element);
            }
        }

        for (final Degree d : calendar.getDegrees()) {
            d.initDegreeYears();
        }
    }
    
    @Transactional
    public void initStudents(DegreeYear degreeYear, Degree degree) {
        Set<StudentDto> degreeYearStudents = getStudents(degree.getId(), degreeYear.getDegreeYear());
        Set<String> currentUsernames = degreeYear.getStudents().stream().map(Student::getUsername).collect(Collectors.toSet());
        logger.info("Init {} students for degree {} of year {}", degreeYearStudents.size(), degree.getAcronym(), degreeYear.getDegreeYear());
        List<Student> sts = new ArrayList<>();
        for(StudentDto studentDto : degreeYearStudents) {
            if (!currentUsernames.contains(studentDto.getUsername())) {
                sts.add(new Student(studentDto.getName(), studentDto.getUsername(), null, null, null));
            }
        }

        for (Student student : sts) {
            student.setDegreeYear(degreeYear);
            degreeYear.getStudents().add(student);
        }
    }
}
