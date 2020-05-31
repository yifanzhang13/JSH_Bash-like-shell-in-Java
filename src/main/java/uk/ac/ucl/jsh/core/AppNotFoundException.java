package uk.ac.ucl.jsh.core;

public class AppNotFoundException extends RuntimeException {
    public AppNotFoundException(String message) {
        super(message);
    }
}
