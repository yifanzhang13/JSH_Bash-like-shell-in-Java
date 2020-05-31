package uk.ac.ucl.jsh.core.parser;

import java.io.StringReader;
import java.util.*;

public class Parser implements IParser {

    public List<List<Command>> parse(String commandString) throws IllegalArgumentException {


        StringReader reader = new StringReader(commandString);
        Lexer lexer = new Lexer(reader);
        GeneratedParser generatedParser = new GeneratedParser(lexer);

        try{
            generatedParser.parse();
        }
        catch(Exception ignore){
            throw new IllegalArgumentException();
        }

        ASTNode root = generatedParser.getTree();
        List<List<Command>> commandsList = new ArrayList<>();

        for(ASTNode seq: root.getAllChildren()){
            List<ASTNode> pipeList = seq.getAllChildren();
            for(ASTNode pipe : pipeList) {
                commandsList.add(parseMulti(pipe));
            }
        }

        return commandsList;

    }


    private List<Command> parseMulti(ASTNode pipe){
        List<ASTNode> pipeUnits = pipe.getAllChildren();
        List<Command> commandList = new LinkedList<>();

        for(ASTNode pipes : pipeUnits) {
            Command tempCommand = parseRegular(pipes);
            commandList.add(tempCommand);
        }

        return commandList;
    }

    private void processRedirect(ASTNode redirectNode, Command result) {

        String redirectType = redirectNode.getRedirectType();

        if(redirectType.equals("OUT")){
            if(result.getOutputRedirect() != null) throw new DuplicateFormatFlagsException("Duplicate Redirect");
            result.setOutputRedirect(parseArgument(redirectNode.getChild(0)));
        }
        else if(redirectType.equals("IN")){
            if(result.getInputRedirect() != null) throw new DuplicateFormatFlagsException("Duplicate Redirect");
            result.setInputRedirect(parseArgument(redirectNode.getChild(0)));
        }
    }


    private Command parseRegular(ASTNode call){

        if(call.getAllChildren().size() < 1) {
            return null;
        }

        Command result = new Command("");

        for(ASTNode call_item: call.getAllChildren()) {
            if(call_item.getType().equals("redirection_list")){
                for(ASTNode redirection : call_item.getAllChildren()){
                    processRedirect(redirection, result);
                }
            }
            if(call_item.getType().equals("keyword")){
                result.addArgument(parseArgument(call_item.getChild(0)));
            }
            else if(call_item.getType().equals("atom_list")){
                for(ASTNode atom_item : call_item.getAllChildren()){
                    if(atom_item.getType().equals("argument")){
                        result.addArgument(parseArgument(atom_item));
                    }
                    if(atom_item.getType().equals("redirect")){
                        processRedirect(atom_item, result);
                    }
                }
            }
        }

        return result;
    }

    private Argument parseArgument(ASTNode argument) {
        Argument result = new Argument();

        StringBuilder argumentString = new StringBuilder();
        for(ASTNode atom : argument.getAllChildren()) {
            if (argument.isQuoted()) result.setQuoted(true);
            if (atom.getType().equals("backquote")) {
                List<List<Command>> backquoteResult = parse(atom.getValue());
                result.setReplaceable(true);
                result.setQuoted(true);
                if (argumentString.length() == 0) {
                    result.replaced.add(new Pair<>(0, backquoteResult));
                } else {
                    result.replaced.add(new Pair<>(argumentString.length() - 1, backquoteResult));
                }
            } else {
                argumentString.append(atom.getValue());
            }
        }
        result.setArgumentString(argumentString.toString());
        return result;
    }


}
