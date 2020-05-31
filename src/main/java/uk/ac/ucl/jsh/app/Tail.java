package uk.ac.ucl.jsh.app;

import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Tail extends AbstractApp{
    private int tailLine = 10;
    private String tailArg = "";

    public Tail() {}

    @Override
    public void run() throws RuntimeException {
        try {
            if (tailArg.isBlank() || tailArg.isEmpty()) {
                processStdin();
            } else {
                processFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        exit();
    }

    private void processStdin() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getInputStream()));
        process(bufferedReader);
    }

    private void processFile() throws IOException {
        File tailFile = outputFile();
        if (tailFile.exists()) {
            Charset encoding = StandardCharsets.UTF_8;
            Path filePath = Paths.
                    get(IntelligentPath.getPath(tailArg, jshCore.getCurrentDirectory()).toAbsolutePath().toString());

            process(Files.newBufferedReader(filePath, encoding));
        } else {
            throw new RuntimeException("tail: " + tailArg + " does not exist");
        }
    }

    private void process(BufferedReader reader) throws IOException {
        ArrayList<String> storage = new ArrayList<>();
        PrintStream pt = new PrintStream(getOutputStream());
        String line;
        while ((line = reader.readLine()) != null) {
            storage.add(line);
        }

        int index = 0;
        if (tailLine > storage.size()) {
            index = 0;
        } else {
            index = storage.size() - tailLine;
        }
        for (int i = index; i < storage.size(); i++) {
            pt.print(storage.get(i)+System.getProperty("line.separator"));
            pt.flush();
        }
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        if (args.length == 0) {
            return;
        }

        if (args[0].equals("-n")) {
            try{
                tailLine = Integer.parseInt(args[1]);
            } catch (NumberFormatException e){
                throw new RuntimeException("Number Format Exception");
            }
            if (args.length == 3) {
                tailArg = args[2];
            }
        } else {
            tailArg = args[0];
        }

        if (args.length > 3) {
            throw new RuntimeException("tail: illegal arguments");
        }
    }

    private File outputFile(){
        return IntelligentPath.getPath(tailArg, jshCore.getCurrentDirectory()).toFile();
    }
}
