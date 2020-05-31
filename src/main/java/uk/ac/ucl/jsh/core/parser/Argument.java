package uk.ac.ucl.jsh.core.parser;

import uk.ac.ucl.jsh.Jsh;
import uk.ac.ucl.jsh.core.AppNotFoundException;
import uk.ac.ucl.jsh.core.ExecutorFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class Argument {
    private boolean isReplaceable;
    private boolean isQuoted;
    private String argumentString;
    public List<Pair<Integer, List<List<Command>>>> replaced;

    public Argument() {
        replaced = new LinkedList<>();
        isQuoted = false;
    }

    public Argument(String argumentString) {
        this.argumentString = argumentString;
        replaced = new LinkedList<>();
        isQuoted = false;
    }

    public boolean isQuoted() {
        return isQuoted;
    }

    public void setQuoted(boolean isQuoted) {
        this.isQuoted = isQuoted;
    }

    public boolean isReplaceable() {
        return isReplaceable;
    }

    public void setReplaceable(boolean replaceable) {
        isReplaceable = replaceable;
    }

    public String getArgumentString() {
        return argumentString;
    }

    public void setArgumentString(String argumentString) {
        this.argumentString = argumentString;
    }

    public static List<String> processAndExtendArguments(List<Argument> original, Jsh jsh) throws IOException, AppNotFoundException, ExecutionException, InterruptedException {

        List<String> newArguments = new LinkedList<>();

        for (Argument argument : original) {
            if (!argument.isQuoted()) {
                newArguments.addAll(processGlobbing(argument, jsh));
                continue;
            }

            if (argument.isReplaceable()) {
                Jsh newJsh = new Jsh();
                newJsh.getJshCore().setCurrentDirectory(jsh.getJshCore().getCurrentDirectory());

                PipedInputStream inputStream = new PipedInputStream();

                Scanner scanner = new Scanner(inputStream);
                StringBuilder stringBuilder = new StringBuilder();

                int current = 0;
                for (Pair<Integer, List<List<Command>>> replacedUnit : argument.replaced) {
                    if (replacedUnit.getKey() >= argument.getArgumentString().length()) {
                        stringBuilder.append(argument.getArgumentString());
                    } else {
                        stringBuilder.append(argument.getArgumentString(), current, replacedUnit.getKey() + 1);
                    }
                    Future first = ExecutorFactory.getExecutorService().submit(() -> {
                        try {
                            PipedOutputStream outputStream = new PipedOutputStream(inputStream);
                            newJsh.eval(replacedUnit.getValue(), outputStream);
                            outputStream.close();
                        } catch (Exception e) {
                            throw new RuntimeException("Substitution failed");
                        }
                    });

                    first.get();

                    Future<String> readAll = ExecutorFactory.getExecutorService().submit(() -> scannerReadAll(scanner));
                    String sub = readAll.get();
                    stringBuilder.append(sub);

                    current = replacedUnit.getKey() + 1;
                }

                if (current < argument.getArgumentString().length()) {
                    stringBuilder.append(argument.getArgumentString().substring(current));
                }

                newArguments.add(stringBuilder.toString());
                continue;
            }

            newArguments.add(argument.getArgumentString());
        }

        return newArguments;
    }

    private static String scannerReadAll(Scanner scanner) {
        StringBuilder stringBuilder = new StringBuilder();


        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
            stringBuilder.append(" ");
        }

        return stringBuilder.toString().trim();
    }

    private static List<String> processGlobbing(Argument argument, Jsh jsh) throws IOException {
        List<String> result = new ArrayList<>();

        int maxDepth = argument.getArgumentString().split("/").length;
        Stream<Path> pathStream = Files.walk(jsh.getJshCore().getCurrentDirectory(), maxDepth);
        int currentNameCount = jsh.getJshCore().getCurrentDirectory().getNameCount();
        String glob;
        if (argument.getArgumentString().startsWith("/")) {
            glob = String.format("glob:**%s", argument.getArgumentString());
        } else {
            glob = String.format("glob:**/%s", argument.getArgumentString());
        }
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);

        pathStream.skip(1).forEach((path) -> {
            if (pathMatcher.matches(path) && !path.getFileName().toString().startsWith(".")) {
                result.add(path.subpath(currentNameCount, path.getNameCount()).toString());
            }
        });

        if (result.isEmpty()) {
            result.add(argument.getArgumentString());
        }

        return result;
    }
}