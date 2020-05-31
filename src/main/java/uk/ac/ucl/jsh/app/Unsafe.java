package uk.ac.ucl.jsh.app;

public class Unsafe extends AbstractApp {
    private App coreApp;

    public Unsafe(App app) {
        coreApp = app;
    }

    @Override
    public void run() {
        try {
            coreApp.run();
            exit();
        } catch (Exception ignored) { }
    }

    @Override
    public void setArgs(String[] args) {
        try {
            coreApp.setArgs(args);
        } catch (Exception ignored) { }
    }
}
