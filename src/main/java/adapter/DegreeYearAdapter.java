package adapter;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import core.Degree;
import core.DegreeYear;
import core.Period.PeriodType;

public class DegreeYearAdapter implements JsonSerializer<Degree>, JsonDeserializer<DegreeChange> {

    @Override
    public JsonElement serialize(Degree degree, Type arg1, JsonSerializationContext arg2) {
        final JsonObject degreeObject = new JsonObject();

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

        degreeObject.addProperty("degree", degree.getAcronym());
        degreeObject.addProperty("degreeName", degree.getName());
        degreeObject.addProperty("degreeId", degree.getId());
        degreeObject.addProperty("academicYear", degree.getYear() + "/" + (degree.getYear() + 1));
        degreeObject.addProperty("degreeType", degree.getType());

        final JsonArray years = new JsonArray();
        final Map<Integer, DegreeYear> sortedDegrees = new HashMap<Integer, DegreeYear>();
        for (final DegreeYear degreeYear : degree.getYears()) {
            sortedDegrees.put(degreeYear.getDegreeYear(), degreeYear);
        }
        for (final Integer i : sortedDegrees.keySet()) {
            final DegreeYear degreeYear = sortedDegrees.get(i);
            final JsonObject yearObject = new JsonObject();
            final JsonObject applicationObject = new JsonObject();
            final JsonObject electionObject = new JsonObject();

            yearObject.addProperty("degreeYear", degreeYear.getDegreeYear());
            final LocalDate now = LocalDate.now();
            if (degreeYear.getCurrentApplicationPeriod() != null) {
                applicationObject.addProperty("applicationPeriodId", degreeYear.getCurrentApplicationPeriod().getId());
                applicationObject.addProperty("applicationPeriodStart", degreeYear.getCurrentApplicationPeriod().getStart()
                        .format(dtf));
                applicationObject.addProperty("applicationPeriodEnd",
                        degreeYear.getCurrentApplicationPeriod().getEnd().format(dtf));
                applicationObject.addProperty("candidateCount", degreeYear.getCurrentApplicationPeriod().getCandidateCount());
                if (degreeYear.getCurrentApplicationPeriod().getEnd().isBefore(now)) {
                    applicationObject.addProperty("state", "passado");
                } else if (degreeYear.getCurrentApplicationPeriod().getStart().isAfter(now)) {
                    applicationObject.addProperty("state", "futuro");
                } else {
                    applicationObject.addProperty("state", "presente");
                }
            }

            if (degreeYear.getCurrentElectionPeriod() != null) {
                electionObject.addProperty("electionPeriodId", degreeYear.getCurrentElectionPeriod().getId());
                electionObject.addProperty("electionPeriodStart", degreeYear.getCurrentElectionPeriod().getStart().format(dtf));
                electionObject.addProperty("electionPeriodEnd", degreeYear.getCurrentElectionPeriod().getEnd().format(dtf));
                electionObject.addProperty("voteCount", degreeYear.getCurrentElectionPeriod().getVotes().size());
                if (degreeYear.getCurrentElectionPeriod().getEnd().isBefore(now)) {
                    electionObject.addProperty("state", "passado");
                } else if (degreeYear.getCurrentElectionPeriod().getStart().isAfter(now)) {
                    electionObject.addProperty("state", "futuro");
                } else {
                    electionObject.addProperty("state", "presente");
                }
            }

            yearObject.add("applicationPeriod", applicationObject);
            yearObject.add("electionPeriod", electionObject);
            years.add(yearObject);
        }
        degreeObject.add("years", years);

        return degreeObject;
    }

    @Override
    public DegreeChange deserialize(JsonElement degreeElement, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final JsonObject degreeObject = degreeElement.getAsJsonObject();

        final String degreeId = degreeObject.get("degreeId").getAsString();
        final DegreeChange degree = new DegreeChange(degreeId);

        final JsonArray years = degreeObject.get("years").getAsJsonArray();

        for (final JsonElement yearElement : years) {
            final JsonObject yearObject = yearElement.getAsJsonObject();

            final int year = yearObject.get("degreeYear").getAsInt();

            final Set<PeriodChange> periods = new HashSet<PeriodChange>();

            final JsonObject applicationPeriodObject = yearObject.getAsJsonObject("applicationPeriod");
            if (applicationPeriodObject.has("applicationPeriodId")
                    || (applicationPeriodObject.has("applicationPeriodStart") && applicationPeriodObject
                            .has("applicationPeriodEnd"))) {
                int id = Integer.MIN_VALUE;
                if (applicationPeriodObject.has("applicationPeriodId")) {
                    id = applicationPeriodObject.get("applicationPeriodId").getAsInt();
                }
                final LocalDate start = LocalDate.parse(applicationPeriodObject.get("applicationPeriodStart").getAsString(), dtf);
                final LocalDate end = LocalDate.parse(applicationPeriodObject.get("applicationPeriodEnd").getAsString(), dtf);
                final PeriodChange applicationPeriod = new PeriodChange(PeriodType.Application, id, start, end);
                periods.add(applicationPeriod);
            }

            final JsonObject electionPeriodObject = yearObject.getAsJsonObject("electionPeriod");
            if (electionPeriodObject.has("electionPeriodId")
                    || (electionPeriodObject.has("electionPeriodStart") && electionPeriodObject.has("electionPeriodEnd"))) {
                int id = Integer.MIN_VALUE;
                if (electionPeriodObject.has("electionPeriodId")) {
                    id = electionPeriodObject.get("electionPeriodId").getAsInt();
                }
                final LocalDate start = LocalDate.parse(electionPeriodObject.get("electionPeriodStart").getAsString(), dtf);
                final LocalDate end = LocalDate.parse(electionPeriodObject.get("electionPeriodEnd").getAsString(), dtf);
                final PeriodChange electionPeriod = new PeriodChange(PeriodType.Election, id, start, end);
                periods.add(electionPeriod);
            }

            degree.addYear(year, periods);

        }

        return degree;

    }
}
