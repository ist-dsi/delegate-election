package adapter;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import core.DegreeYear;

public class DegreeAdapter implements JsonSerializer<DegreeYear> {

    @Override
    public JsonElement serialize(DegreeYear degree, Type arg1, JsonSerializationContext arg2) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        JsonObject degreeJson = new JsonObject();
        JsonObject periodJson = new JsonObject();

        degreeJson.addProperty("id", degree.getDegree().getId());
        degreeJson.addProperty("name", degree.getDegreeName());
        degreeJson.addProperty("academicYear", degree.getCalendarYear() + "/" + (degree.getCalendarYear() + 1));
        degreeJson.addProperty("curricularYear", degree.getDegreeYear());

        if (degree.getActivePeriod() != null) {
            periodJson.addProperty("type", degree.getActivePeriod().getType().toString());
            periodJson.addProperty("start", degree.getActivePeriod().getStart().format(dtf));
            periodJson.addProperty("end", degree.getActivePeriod().getEnd().format(dtf));
        }
        degreeJson.add("currentPeriod", periodJson);

        return degreeJson;
    }

}
