package uk.ac.ucl.jsh.core;

import uk.ac.ucl.jsh.app.*;

public class AppFactory implements IAbstractAppFactory {
    @Override
    public App create(String appName) throws AppNotFoundException{
        App app;
        switch (appName) {
            case "cd":
                app = new Cd();
                break;
            case "cat":
                app = new Cat();
                break;
            case "echo":
                app = new Echo();
                break;
            case "ls":
                app = new Ls();
                break;
            case "pwd":
                app = new Pwd();
                break;
            case "tail":
                app = new Tail();
                break;
            case "head":
                app = new Head();
                break;
            case "grep":
                app = new Grep();
                break;
            case "sed":
                app = new Sed();
                break;
            case "find":
                app = new Find();
                break;
            case "wc":
                app = new Wc();
                break;
            case "_ls":
                app = new Unsafe(new Ls());
                break;
            case "_cd":
                app = new Unsafe(new Cd());
                break;
            case "_cat":
                app = new Unsafe(new Cat());
                break;
            case "_echo":
                app = new Unsafe(new Echo());
                break;
            case "_pwd":
                app = new Unsafe(new Pwd());
                break;
            case "_tail":
                app = new Unsafe(new Tail());
                break;
            case "_head":
                app = new Unsafe(new Head());
                break;
            case "_grep":
                app = new Unsafe(new Grep());
                break;
            case "_sed":
                app = new Unsafe(new Sed());
                break;
            case "_find":
                app = new Unsafe(new Find());
                break;
            case "_wc":
                app = new Unsafe(new Wc());
                break;
            default:
                throw new AppNotFoundException("App Not Found");
        }

        return app;
    }

    @Override
    public App create(String appName, Core core) throws AppNotFoundException {
        App app = create(appName);
        app.injectCore(core);
        return app;
    }
}
