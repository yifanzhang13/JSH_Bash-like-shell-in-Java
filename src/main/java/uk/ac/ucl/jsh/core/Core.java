package uk.ac.ucl.jsh.core;

import java.io.OutputStream;
import java.nio.file.Path;

public interface Core {
    Path getCurrentDirectory();
    String getLineSeparator();
    void setOutputStream(OutputStream outputStream);
    void setCurrentDirectory(Path path);
}
