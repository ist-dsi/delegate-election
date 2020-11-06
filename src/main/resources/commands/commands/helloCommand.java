package commands;

import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;

public class helloCommand extends BaseCommand {

    @Command
    @Usage("Say Hello")
    public String main() {
        return "Hello World!";
    }

}