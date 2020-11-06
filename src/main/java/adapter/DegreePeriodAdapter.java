package adapter;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import core.DegreeYear;
import core.Period;

public class DegreePeriodAdapter implements JsonSerializer<DegreeYear> {

    @Override
    public JsonElement serialize(DegreeYear degree, Type arg1, JsonSerializationContext arg2) {
        final JsonObject degreeJson = new JsonObject();

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        final Period period = degree.getNextPeriod();

        if (period != null) {
            degreeJson.addProperty("start", period.getStart().format(dtf));
            degreeJson.addProperty("end", period.getEnd().format(dtf));
        } else {
            degreeJson.addProperty("start", "");
            degreeJson.addProperty("end", "");
        }

        return degreeJson;
    }
}
