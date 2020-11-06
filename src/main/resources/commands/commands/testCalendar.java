package commands;

import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.springframework.beans.factory.BeanFactory;

import endpoint.Controller;

public class testCalendar extends BaseCommand {

    @Command
    @Usage("Creates Calendar")
    public String main(InvocationContext context) throws Exception {
        BeanFactory bf = (BeanFactory) context.getAttributes().get("spring.beanfactory");
        Controller tc = bf.getBean(Controller.class);
        return tc.createCalendar();
    }
}
