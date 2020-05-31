package uk.ac.ucl.jsh.core.parser;

import java.util.List;

public interface IParser {
    List<List<Command>> parse(String commandString);
}
