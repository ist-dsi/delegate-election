package adapter;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import core.ApplicationPeriod;
import core.DegreeYear;
import core.ElectionPeriod;
import core.Period;
import core.Period.PeriodType;

public class DegreeYearHistoryAdapter implements JsonSerializer<DegreeYear> {

    @Override
    public JsonElement serialize(DegreeYear degree, Type arg1, JsonSerializationContext arg2) {
        final JsonObject degreeYearJson = new JsonObject();
        final JsonArray periodsJson = new JsonArray();

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        degreeYearJson.addProperty("degreeName", degree.getDegreeName());
        degreeYearJson.addProperty("degreeYear", degree.getDegreeYear());
        degreeYearJson.addProperty("degreeType", degree.getType());

        final List<Period> periods = new ArrayList<Period>(degree.getInactivePeriods());
        if (degree.getActivePeriod() != null) {
            periods.add(degree.getActivePeriod());
        }

        periods.sort(new Comparator<Period>() {
            @Override
            public int compare(Period firstPeriod, Period secondPeriod) {
                return firstPeriod.getStart().atStartOfDay().toInstant(ZoneOffset.UTC)
                        .compareTo(secondPeriod.getStart().atStartOfDay().toInstant(ZoneOffset.UTC));
//                return 0;
            }
        });

        Collections.reverse(periods);

        for (final Period p : periods) {
            if (p.getStart().isAfter(LocalDate.now())) {
                continue;
            }
            final JsonObject periodJson = new JsonObject();
            periodJson.addProperty("academicYear", p.getDegreeYear().getCalendarYear() + "/"
                    + (p.getDegreeYear().getCalendarYear() + 1));
            periodJson.addProperty("periodType", p.getType().toString());
            periodJson.addProperty("periodId", p.getId());
            periodJson.addProperty("start", p.getStart().format(dtf));
            periodJson.addProperty("end", p.getEnd().format(dtf));

            if (p.getType().equals(PeriodType.Application)) {
                periodJson.addProperty("info", ((ApplicationPeriod) p).getCandidateCount());
            } else if (p.getType().equals(PeriodType.Election)) {
                periodJson.addProperty("info", ((ElectionPeriod) p).getVotes().size());
            }

            periodsJson.add(periodJson);
        }

        degreeYearJson.add("periods", periodsJson);
        return degreeYearJson;
    }
}
