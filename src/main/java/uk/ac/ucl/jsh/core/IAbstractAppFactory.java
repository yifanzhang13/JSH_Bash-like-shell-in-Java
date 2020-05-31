package uk.ac.ucl.jsh.core;

import uk.ac.ucl.jsh.app.App;

public interface IAbstractAppFactory {
    App create(String appName) throws AppNotFoundException;
    App create(String appName, Core core) throws AppNotFoundException;
}
