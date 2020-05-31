package uk.ac.ucl.jsh;

import uk.ac.ucl.jsh.app.App;
import uk.ac.ucl.jsh.core.*;
import uk.ac.ucl.jsh.core.parser.Argument;
import uk.ac.ucl.jsh.core.parser.Command;
import uk.ac.ucl.jsh.core.parser.IParser;
import uk.ac.ucl.jsh.core.parser.Parser;
import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Jsh {
    private Core jshCore;

    public Jsh() {
        jshCore = new JshCore();
    }

    public Core getJshCore() {
        return jshCore;
    }

    public void eval(List<List<Command>> commandsList, OutputStream output) throws Exception {
        jshCore.setOutputStream(output);
        for (int i = 0; i < commandsList.size(); i ++) {
            IPipeline pipeline = makePipeline(commandsList.get(i));
            if (i + 1 < commandsList.size()) {
                pipeline.lockEnd();
            }
            pipeline.setOutputStream(output);
            pipeline.run();
        }
    }

    private IPipeline makePipeline(List<Command> commands) throws Exception {
        IAbstractAppFactory appFactory = FactoryProvider.getAppFactory();

        IPipeline pipeline = new Pipeline();
        for (Command command : commands) {
            Command.preprocessCommand(command);
            List<String> appArgs = Argument.processAndExtendArguments(command.getArguments(), this);
            if (command.getCommandType().isEmpty() || command.getCommandType().isBlank()) {
                if (appArgs.size() < 1) {
                    throw new RuntimeException("Command Type Missing");
                }

                command.setCommandType(appArgs.get(0));
                appArgs = appArgs.subList(1, appArgs.size());
            }

            App app = appFactory.create(command.getCommandType(), getJshCore());

            if (command.getInputRedirect() != null) {
                File inputFile = new File(IntelligentPath.getPath(command.getInputRedirect().getArgumentString(), jshCore.getCurrentDirectory()).toAbsolutePath().toString());
                try {
                    app.setInputStream(new FileInputStream(inputFile));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Input Redirect File Not Found");
                }
            }

            if (command.getOutputRedirect() != null) {
                File outputFile = new File(IntelligentPath.getPath(command.getOutputRedirect().getArgumentString(), jshCore.getCurrentDirectory()).toAbsolutePath().toString());

                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }
                app.setOutputStream(new FileOutputStream(outputFile));
                app.lockOutputStream();
            }

            pipeline.append(app, appArgs.toArray(String[]::new));
        }
        return pipeline;
    }


    public void eval(String cmdline, OutputStream output) throws Exception {
        if (cmdline.isEmpty() || cmdline.isBlank()) {
            output.write('\n');
            return;
        }

        jshCore.setOutputStream(output);

        IParser parser = new Parser();
        List<List<Command>> commandsList = parser.parse(cmdline);
        eval(commandsList, output);
    }

    public static void main(String[] args) throws Exception {
        Jsh jsh = new Jsh();
        if (args.length > 0) {
            if (args.length != 2) {
                System.out.println("jsh: wrong number of arguments");
                return;
            }
            if (!args[0].equals("-c")) {
                System.out.println("jsh: " + args[0] + ": unexpected argument");
            }
            try {
                jsh.eval(args[1], System.out);
            } catch (Exception e) {
                ExecutorFactory.getExecutorService().shutdownNow();
            }
        } else {
            System.out.println("Welcome to JSH!");
            Scanner input = new Scanner(System.in);
            try {
                while (true) {
                    String prompt = jsh.jshCore.getCurrentDirectory().toAbsolutePath().toString() + "> ";
                    System.out.print(prompt);
                    try {
                        String cmdline = input.nextLine();
                        if (cmdline.trim().equals("exit")) break;
                        jsh.eval(cmdline, System.out);
                    } catch (PanicPipeLineException e) {
                        ExecutorFactory.getExecutorService().shutdownNow();
                        ExecutorFactory.reset();
                    } catch (Exception e) {
                        System.out.println("jsh: " + e.getMessage());
                    }
                }
            } finally {
                input.close();
            }
        }
        ExecutorFactory.getExecutorService().shutdownNow();
    }
}
