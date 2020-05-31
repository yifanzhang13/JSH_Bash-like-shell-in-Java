package uk.ac.ucl.jsh.app;

import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

public class Find extends AbstractApp{
    private String[] arguments;


    @Override
    public void run() throws RuntimeException {
        try {
            if (arguments[0].equals("-name")){
                process(jshCore.getCurrentDirectory(), arguments[1]);
            } else {
                process(IntelligentPath.getPath(arguments[0], jshCore.getCurrentDirectory()), arguments[2]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            exit();
        }
    }

    private void process(Path path, String pattern) throws IOException {
        if (!path.toFile().exists()) {
            throw new RuntimeException("Path does not exist");
        }

        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(String.format("glob:**/%s", pattern));
        Stream<Path> pathStream = Files.walk(path);
        int nameCount = jshCore.getCurrentDirectory().getNameCount();

        pathStream.skip(1).forEach((p) -> {
            if (p.toFile().isFile()) {
                if ((pathMatcher.matches(p)) && (path.equals(jshCore.getCurrentDirectory()))) {
                    writeOutputStream("./");
                    writeOutputStreamLn(p.subpath(nameCount, p.getNameCount()).toString());
                }else if((pathMatcher.matches(p))){
                    writeOutputStreamLn(p.subpath(nameCount, p.getNameCount()).toString());
                }
            }
        });
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        arguments = args;
        if (arguments.length<2){
            throw new RuntimeException("find: wrong number of arguments");
        }
    }
}
