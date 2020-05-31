package uk.ac.ucl.jsh.app;

public class Pwd extends AbstractApp {
    public Pwd() {}

    @Override
    public void run() {
        writeOutputStreamLn(this.jshCore.getCurrentDirectory().toString());
        exit();
    }

    @Override
    public void setArgs(String[] args) {}
}
