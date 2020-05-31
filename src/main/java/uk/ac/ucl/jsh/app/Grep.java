package uk.ac.ucl.jsh.app;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grep extends AbstractApp {
    private String[] arguments;

    private List<String> matchFiles(Pattern pattern) throws IOException {
        List<String> matched = new ArrayList<>();
        int fileNums = arguments.length - 1;
        Path[] filePathArray = new Path[fileNums];

        Path currentDir = jshCore.getCurrentDirectory();
        for (int i = 0; i < fileNums; i++) {
            Path filePath = currentDir.resolve(arguments[i + 1]);
            if (Files.notExists(filePath) || Files.isDirectory(filePath)) {
                throw new IOException("file is a path or does not exist");
            }
            filePathArray[i] = filePath;
        }

        for (int j = 0; j < filePathArray.length; j++) {
            Charset encoding = StandardCharsets.UTF_8;
            BufferedReader reader = Files.newBufferedReader(filePathArray[j], encoding);
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    matched.add(line);
                }
            }
        }

        return matched;
    }

    private List<String> matchStdin(Pattern pattern) throws IOException {
        List<String> result = new ArrayList<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getInputStream()));

        String line;
        while (true) {
            if ((line = bufferedReader.readLine()) == null) break;
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                result.add(line);
            }
        }

        return result;
    }


    @Override
    public void run() throws RuntimeException {
        Pattern grepPattern = Pattern.compile(arguments[0]);
        List<String> matched = new ArrayList<>();
        try {
            if (arguments.length - 1 > 0) {
                matched.addAll(matchFiles(grepPattern));
            } else {
                matched.addAll(matchStdin(grepPattern));
            }

            for (String result : matched) {
                writeOutputStreamLn(result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            exit();
        }
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        arguments = args;

        if (arguments.length < 1) {
            throw new RuntimeException("grep: wrong number of arguments");
        }
    }
}