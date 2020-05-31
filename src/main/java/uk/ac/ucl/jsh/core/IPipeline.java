package uk.ac.ucl.jsh.core;

import uk.ac.ucl.jsh.app.App;
import java.io.OutputStream;

public interface IPipeline {
    void run() throws Exception;
    void append(App app, String[] args) throws Exception;
    void setOutputStream(OutputStream outputStream);
    void lockEnd();
}
