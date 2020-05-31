package uk.ac.ucl.jsh.core.parser;

import java.util.ArrayList;

public class ASTNode {
    private final String value;
    private final String type;
    private ArrayList<ASTNode> children;
    private String redirectType = null;
    private Boolean isQuoted = false;

    public ASTNode(){
        value = null;
        type = null;
    }

    public ASTNode(String value, String type){
        this.value = value;
        this.type = type;
        this.children = new ArrayList<>();
    }

    public ASTNode(String type){
        this.value = null;
        this.type = type;
        this.children = new ArrayList<>();
    }

    void addChild(ASTNode node){
        this.children.add(node);
        this.children.indexOf(node);
    }

    void setQuoted(boolean isQuoted) { this.isQuoted = isQuoted; }

    Boolean isQuoted() {return isQuoted; }

    String getRedirectType() { return this.redirectType; }

    void setRedirectType(String type) {this.redirectType = type;}

    void addChildFront(ASTNode node) {this.children.add(0, node); }

    ASTNode getChild(Integer childNumber){return this.children.get(childNumber);}

    ArrayList<ASTNode> getAllChildren(){return this.children;}

    String getValue(){
        return this.value;
    }

    String getType(){
        return this.type;
    }
}
