package uk.ac.ucl.jsh.app;

import uk.ac.ucl.jsh.core.Core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public abstract class AbstractApp implements App {

    Core jshCore;
    private InputStream inputStream;
    private OutputStream outputStream;
    private OutputStreamWriter outputStreamWriter;
    private boolean outputStreamLock = false;
    private boolean isEnd = false;

    public void injectCore(Core core) {
        this.jshCore = core;
    }

    @Override
    public void lockOutputStream() {
        outputStreamLock = true;
    }

    @Override
    public boolean isOutputStreamLock() {
        return outputStreamLock;
    }

    void writeOutputStream(String content) throws RuntimeException {
        if (content.isEmpty()) {
            return;
        }

        try {
            outputStreamWriter.write(content);
            outputStreamWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error writing to outputstream");
        }
    }

    void writeOutputStreamLn(String content) {
        writeOutputStream(content + jshCore.getLineSeparator());
    }


    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        outputStreamWriter = new OutputStreamWriter(outputStream);
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public abstract void run() throws RuntimeException;
    public abstract void setArgs(String[] args) throws RuntimeException;

    @Override
    public void disallowCloseOut() {
        isEnd = true;
    }
    @Override
    public void exit() {
        try {
            if (!getOutputStream().equals(System.out) && !isEnd) {
                getOutputStream().close();
            }

            if (!getInputStream().equals(System.in)) {
                getInputStream().close();
            }

        } catch (IOException ignored) { }
    }
}
