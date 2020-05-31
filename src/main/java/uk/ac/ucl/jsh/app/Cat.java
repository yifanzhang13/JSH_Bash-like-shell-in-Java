package uk.ac.ucl.jsh.app;
import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;

public class Cat extends AbstractApp {
    private String[] arguments;

    @Override
    public void run() throws RuntimeException {
        try {
            if (arguments.length != 0) {
                outputFiles();
            } else {
                outputStdIn();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        exit();
    }

    private void outputStdIn() throws IOException {
        getInputStream().transferTo(getOutputStream());
    }

    private void outputFiles() throws  IOException{
        for (String arg : arguments) {
            File curFile = IntelligentPath.getPath(arg, jshCore.getCurrentDirectory()).toFile();
            FileInputStream fileInputStream = new FileInputStream(curFile);
            fileInputStream.transferTo(getOutputStream());
            getOutputStream().flush();
            fileInputStream.close();
        }
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        arguments = args;
    }
}
