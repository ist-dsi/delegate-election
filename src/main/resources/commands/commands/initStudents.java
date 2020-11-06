package commands;

import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.springframework.beans.factory.BeanFactory;

import core.util.ScheduledTasks;
import endpoint.Controller;

public class initStudents extends BaseCommand {

    @Command
    @Usage("Initializes Students for Last Year")
    public void main(InvocationContext context) throws Exception {
        BeanFactory bf = (BeanFactory) context.getAttributes().get("spring.beanfactory");
        ScheduledTasks st = bf.getBean(ScheduledTasks.class);
        st.retrieveStudents();
    }
}
