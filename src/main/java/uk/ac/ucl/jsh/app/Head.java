package uk.ac.ucl.jsh.app;

import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Head extends AbstractApp{
    private int headLine = 10;
    private String headArg = "";

    public Head() {
    }

    @Override
    public void run() throws RuntimeException {
        try {
            if (headArg.isEmpty() || headArg.isBlank()) {
                processStdin();
            } else {
                processFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException");
        } finally {
            exit();
        }
    }

    private void processFile() throws IOException {
        File headFile = outputFile(headArg);
        BufferedReader reader = Files.newBufferedReader(headFile.toPath(), StandardCharsets.UTF_8);
        for (int i=0;i<headLine;i++){
            String line = null;
            if ((line = reader.readLine()) != null){
                writeOutputStreamLn(line);
            }
        }
    }

    private void processStdin() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        int i = 0;
        String line;
        while (true) {
            if ((line = bufferedReader.readLine()) == null || i >= headLine) break;
            writeOutputStreamLn(line);
            i += 1;
        }
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        if (args.length == 3){
            if (args[0].equals("-n")) {
                try {
                    headLine = Integer.parseInt(args[1]);
                    headArg = args[2];
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e.getMessage() + "Number Format Exception");
                }
            } else {
                throw new RuntimeException("Bad Arguments");
            }
        } else if (args.length > 3) {
            throw new RuntimeException("head: wrong arguments");
        } else if (args.length == 1) {
            headArg = args[0];
        }
    }

    private File outputFile(String filePath){
        return new File(IntelligentPath.getPath(filePath, jshCore.getCurrentDirectory()).toAbsolutePath().toString());
    }
}
