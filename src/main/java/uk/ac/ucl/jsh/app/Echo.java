package uk.ac.ucl.jsh.app;

public class Echo extends AbstractApp {
    private String[] arguments;

    public Echo() {}

    @Override
    public void run() throws RuntimeException {
        String result = String.join(" ", arguments);
        writeOutputStreamLn(result);

        exit();
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        arguments = args.clone();
    }

}
