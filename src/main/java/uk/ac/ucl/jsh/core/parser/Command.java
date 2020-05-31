package uk.ac.ucl.jsh.core.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Command {
    private String commandType;
    private List<Argument> arguments;
    private Argument inputRedirect;
    private Argument outputRedirect;

    public Command(String commandType) {
        this.commandType = commandType;
        arguments = new LinkedList<>();
    }

    public static void preprocessCommand(Command command) {
        List<Argument> temporal = new ArrayList<>();
        List<Argument> processedArguments = new ArrayList<>();

        temporal.add(new Argument(command.getCommandType()));
        temporal.addAll(command.arguments);

        int i = 0;
        while (i < temporal.size()) {
            if (temporal.get(i).getArgumentString().length() == 1) {
                if (temporal.get(i).getArgumentString().equals("<")) {
                    if (i + 1 >= temporal.size() || command.getInputRedirect() != null) {
                        throw new RuntimeException("Illegal redirect");
                    }

                    command.setInputRedirect(temporal.get(i + 1));
                    i += 2;
                    continue;
                } else if (temporal.get(i).getArgumentString().equals(">")) {
                    if (i + 1 >= temporal.size() || command.getOutputRedirect() != null) {
                        throw new RuntimeException("Illegal redirect");
                    }

                    command.setOutputRedirect(temporal.get(i + 1));
                    i += 2;
                    continue;
                }
            }
            if (temporal.get(i).getArgumentString().startsWith("<") && !temporal.get(i).isQuoted()) {
                Argument redirect = new Argument(temporal.get(i).getArgumentString().substring(1));
                command.setInputRedirect(redirect);
                i += 1;
                continue;
            } else if (temporal.get(i).getArgumentString().startsWith(">") && !temporal.get(i).isQuoted()) {
                Argument redirect = new Argument(temporal.get(i).getArgumentString().substring(1));
                command.setOutputRedirect(redirect);
                i += 1;
                continue;
            }

            processedArguments.add(temporal.get(i));
            i += 1;
        }

        command.setCommandType(processedArguments.get(0).getArgumentString());
        command.setArguments(processedArguments.subList(1, processedArguments.size()));
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public Argument getInputRedirect() {
        return inputRedirect;
    }

    public void setInputRedirect(Argument inputRedirect) {
        this.inputRedirect = inputRedirect;
    }

    public Argument getOutputRedirect() {
        return outputRedirect;
    }

    public void setOutputRedirect(Argument outputRedirect) {
        this.outputRedirect = outputRedirect;
    }
}
