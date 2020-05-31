package uk.ac.ucl.jsh.core;

public class FactoryProvider {

    private static IAbstractAppFactory appFactory = new AppFactory();

    public static IAbstractAppFactory getAppFactory() {
        if (appFactory == null) {
            appFactory = new AppFactory();
        }

        return appFactory;
    }
}
