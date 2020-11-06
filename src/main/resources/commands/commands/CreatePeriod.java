package commands;

import java.time.format.DateTimeFormatter;

import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.springframework.beans.factory.BeanFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import endpoint.Controller;

public class CreatePeriod extends BaseCommand {

    @Command
    @Usage("Creates a Period of the specified type in the specified dates")
    public String main(InvocationContext context, @Usage("Application or Election") @Option(names = { "type", "t" }) String type,
            @Usage("Start Date") @Option(names = { "start", "s" }) String start, @Usage("End Date") @Option(
                    names = { "end", "e" }) String end,
            @Usage("Degree Acronym") @Option(names = { "acronym", "a" }) String acronym, @Usage("Degree Year") @Option(names = {
                    "year", "y" }) int year) {
        BeanFactory bf = (BeanFactory) context.getAttributes().get("spring.beanfactory");
        if (type == null || !(type.equals("Application") || type.equals("Election"))) {
            return "Type must be Application or Election";
        }
        if (start == null || end == null) {
            return "Start and End Dates must follow the format dd/MM/yyyy";
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Controller c = bf.getBean(Controller.class);
//        try {
//            LocalDate startDate = LocalDate.parse(start, dtf);
//            LocalDate endDate = LocalDate.parse(end, dtf);
//            return c.addPeriod2(type.toUpperCase(), startDate, endDate, acronym, year);
//        } catch (DateTimeParseException dtpe) {
//            return "Start and End Dates must follow the format dd/MM/yyyy";
//        }
        JsonObject json = new JsonObject();
        json.addProperty("degreeId", acronym);
        json.addProperty("degreeYear", year);
        json.addProperty("end", end);
        json.addProperty("periodType", type.toUpperCase());
        json.addProperty("start", start);
        return c.command(new Gson().toJson(json));

    }
}
