package adapter;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import core.ApplicationPeriod;
import core.Degree;
import core.DegreeYear;

public class ApplicationPeriodAdapter implements JsonSerializer<ApplicationPeriod>, JsonDeserializer<ApplicationPeriod> {

    @Override
    public JsonElement serialize(ApplicationPeriod period, Type arg1, JsonSerializationContext arg2) {
        JsonObject periodObject = new JsonObject();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//      periodObject.addProperty("id", oeriod.getId());
        periodObject.addProperty("degree", period.getDegreeYear().getDegree().getAcronym());
        periodObject.addProperty("degreeId", period.getDegreeYear().getDegree().getId());
        periodObject.addProperty("degreeYear", period.getDegreeYear().getDegreeYear());
        periodObject.addProperty("start", period.getStart().format(dtf));
        periodObject.addProperty("end", period.getEnd().format(dtf));
        periodObject.addProperty("type", period.getType().toString());
        periodObject.addProperty("candidateCount", period.getCandidateCount());

        return periodObject;
    }

    @Override
    public ApplicationPeriod deserialize(JsonElement periodElement, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        JsonObject periodObject = periodElement.getAsJsonObject();

//      String id = periodObject.get("id");
        LocalDate start = LocalDate.parse(periodObject.get("start").getAsString());
        LocalDate end = LocalDate.parse(periodObject.get("end").getAsString());
        int year = periodObject.get("degreeYear").getAsInt();
        String degreeId = periodObject.get("degreeId").getAsString();

        return new ApplicationPeriod(start, end, new DegreeYear(year, new Degree(null, null, degreeId, null, null)));
    }
}
