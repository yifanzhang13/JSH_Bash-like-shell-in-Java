package uk.ac.ucl.jsh.app;


import uk.ac.ucl.jsh.core.Core;

import java.io.InputStream;
import java.io.OutputStream;

public interface App extends Runnable {
    void setArgs(String[] args) throws RuntimeException;
    void run() throws RuntimeException;
    void setInputStream(InputStream inputStream);
    void setOutputStream(OutputStream outputStream);
    InputStream getInputStream();
    OutputStream getOutputStream();
    void exit();
    void injectCore(Core core);
    void disallowCloseOut();
    void lockOutputStream();
    boolean isOutputStreamLock();
}
