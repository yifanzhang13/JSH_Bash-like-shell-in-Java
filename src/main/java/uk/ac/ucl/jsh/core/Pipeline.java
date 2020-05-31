package uk.ac.ucl.jsh.core;

import uk.ac.ucl.jsh.app.App;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class Pipeline implements IPipeline {

    private List<App> apps;
    private ExecutorService executorService = ExecutorFactory.getExecutorService();
    public Pipeline() {
        apps = new ArrayList<>();
    }

    @Override
    public void run() throws RuntimeException {
        for (App app : apps) {
            Future future = executorService.submit(app);
            try {
                future.get();
            } catch (Exception e) {
                throw new PanicPipeLineException("");
            }
        }
    }

    @Override
    public void append(App app, String[] args) throws Exception {
        if (app.getInputStream() == null) {
            InputStream inputStream;

            if (apps.size() == 0) {
                inputStream = System.in;
            } else {
                inputStream = new PipedInputStream((PipedOutputStream) apps.get(apps.size() - 1).getOutputStream());
            }
            app.setInputStream(inputStream);
        }

        if (app.getOutputStream() == null) {
            app.setOutputStream(new PipedOutputStream());
        }

        app.setArgs(args);
        apps.add(app);
    }
    @Override
    public void setOutputStream(OutputStream outputStream) {
        App app = apps.get(apps.size() - 1);
        if (!app.isOutputStreamLock()) {
            apps.get(apps.size() - 1).setOutputStream(outputStream);
        }
    }

    @Override
    public void lockEnd() {
        apps.get(apps.size() - 1).disallowCloseOut();
    }


}
