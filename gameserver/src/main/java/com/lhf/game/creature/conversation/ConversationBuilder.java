package com.lhf.game.creature.conversation;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class ConversationBuilder {
    private Scanner input;
    private ConversationTree tree = null;

    public ConversationBuilder() {
        this.input = new Scanner(System.in);
    }

    public Boolean yesOrNo() {
        System.out.println("yes or no?");
        String validation_response = this.input.nextLine().toLowerCase();
        if (validation_response.equalsIgnoreCase("yes") || validation_response.equalsIgnoreCase("no")) {
            if (validation_response.equalsIgnoreCase("yes")) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            System.err.println("Invalid response, restarting from last prompt.");
            return null;
        }

    }

    private ConversationPattern buildPattern() {
        String regex = null;
        String example = null;
        boolean ignoreCase = false;
        ConversationPattern pattern = null;
        do {
            try {
                System.out.println("What regex would you like to try?");
                regex = this.input.nextLine();
                System.out.println("What's an example of that?");
                example = this.input.nextLine();
                System.out.println("Would you like to ignore casing?");
                ignoreCase = this.yesOrNo();
                pattern = new ConversationPattern(example, regex, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println("Try again!");
                regex = null;
            }
        } while (regex == null);
        System.out.println(pattern.toString());
        return pattern;
    }

    private ConversationTreeNode buildNodeBody(ConversationTreeNode node) {
        System.out.println("Do you want to add more body?");
        boolean addBody = this.yesOrNo();
        while (addBody) {
            System.out.println("What more do you want to add?");
            node.addBody(this.input.nextLine());
            System.out.println(node.toString());
            System.out.println("Do you want to add more body?");
            addBody = this.yesOrNo();
        }
        return node;
    }

    private ConversationTreeNode buildNodePrompts(ConversationTreeNode node) {
        System.out.println("Do you want to add more prompts?");
        boolean addPrompts = this.yesOrNo();
        while (addPrompts) {
            System.out.println("What more do you want to add?");
            node.addPrompt(this.input.nextLine());
            System.out.println(node.toString());
            System.out.println("Do you want to add more prompts?");
            addPrompts = this.yesOrNo();
        }
        return node;
    }

    private ConversationTreeNode selectValidNode() {
        try {
            System.out.println("Select a Node:");
            SortedMap<Integer, ConversationTreeNode> indexmap = new TreeMap<>();
            Integer i = 1;
            for (ConversationTreeNode node : this.tree.getNodes().values()) {
                indexmap.put(i, node);
                System.out.println(i.toString() + " : " + node.toString());
                i++;
            }
            ConversationTreeNode nextNode = indexmap.get(this.input.nextInt());
            this.input.nextLine();
            if (nextNode == null) {
                throw new IllegalArgumentException("Not a valid node");
            }
            return nextNode;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("You cannot select that as a node, exiting subroutine.");
            throw e;
        }
    }

    private ConversationTreeNode addNodeToTree(ConversationTreeNode node) {
        if (this.tree != null) {
            System.out.println(this.tree.toMermaid(false));
            try {
                ConversationTreeNode prevNode = this.selectValidNode();
                System.out.println("Now to build out how it will connect");
                ConversationPattern link = this.buildPattern();
                this.tree.addNode(prevNode.getNodeID(), link, node);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println("You cannot select that as a node, exiting subroutine.");
                throw e;
            }

        } else {
            System.err.println("The tree is not yet made!");
            System.err.println("Create tree?");
            if (this.yesOrNo()) {
                this.tree = new ConversationTree(node);
            }
        }
        return node;
    }

    public ConversationTreeNode buildNode(ConversationTreeNode node) {
        if (node == null) {
            System.out.println("What do you want in the body of the node?");
            node = new ConversationTreeNode(this.input.nextLine());
        }
        System.out.println(node.toString());
        System.out.println("Are you done with this node?");
        while (!this.yesOrNo()) {
            node = this.buildNodeBody(node);
            node = this.buildNodePrompts(node);
            System.out.println("Are you done with this node?");
        }
        this.addNodeToTree(node);
        return node;
    }

    public ConversationTree buildTree() {
        if (this.tree == null) {
            ConversationTreeNode start = this.buildNode(null);
            if (start != null) {
                this.tree = new ConversationTree(start);
                return this.tree;
            }
        }
        return this.tree;
    }

    public void buildGreetings() {
        System.out.println("Would you like to add greetings");
        while (this.yesOrNo()) {
            ConversationPattern greeting = this.buildPattern();
            this.tree.addGreeting(greeting);
            System.out.println("Would you like to add another greeting?");
        }
    }

    private ConversationTree nameTree() {
        System.out.println("Name the tree");
        if (this.tree == null) {
            this.buildTree();
        }
        System.out.print("The tree's current name is: ");
        System.out.println(this.tree.getTreeName());
        System.out.println("Do you want to change it?");
        if (this.yesOrNo()) {
            System.out.println("What name do you want to use?");
            String name = this.input.nextLine();
            this.tree.setTreeName(name);
        } else {
            System.out.println("Keeping the name as-is");
        }
        return this.tree;
    }

    private ConversationTree writeTree() {
        System.out.println("Write the tree");
        if (this.tree != null) {
            ConversationManager manager = new ConversationManager();
            this.nameTree();
            if (!manager.convoTreeToFile(this.tree)) {
                System.err.println("An error writing the file occured");
            }
        } else {
            System.err.println("No tree to write!");
        }
        return this.tree;
    }

    private ConversationTree loadTree() {
        System.out.println("Load the tree");
        ConversationManager manager = new ConversationManager();
        System.out.println("What is the name of the tree?");
        String name = this.input.nextLine();
        try {
            this.tree = manager.convoTreeFromFile(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.tree = null;
        }
        return this.tree;
    }

    public void menu() {
        int response = -1;
        while (response != 0) {
            try {
                System.out.println("0: Exit");
                System.out.println("1: Print mermaid");
                System.out.println("2: Build tree");
                System.out.println("3: Build new node");
                System.out.println("4: Modify existing node");
                System.out.println("5: Add greeting");
                System.out.println("6: Write the tree");
                System.out.println("7: Load a tree");
                response = this.input.nextInt();
                this.input.nextLine();
                switch (response) {
                    case 0:
                        System.out.println("You selected to exit");
                        break;
                    case 1:
                        System.out.println("Printing mermaid...");
                        if (this.tree != null) {
                            System.out.println(this.tree.toMermaid(false));
                        } else {
                            System.out.println("Tree not made");
                        }
                        break;
                    case 2:
                        System.out.println("Building tree...");
                        if (this.buildTree() == null) {
                            System.err.println("Error building tree!");
                        }
                        break;
                    case 3:
                        System.out.println("Building New node...");
                        if (this.buildNode(null) == null) {
                            System.err.println("Error building new node!");
                        }
                        break;
                    case 4:
                        System.out.println("Modify existing node...");
                        if (this.buildNode(this.selectValidNode()) == null) {
                            System.err.println("Error modifying existing node!");
                        }
                        break;
                    case 5:
                        System.out.println("Add greeting");
                        this.buildGreetings();
                        break;
                    case 6:
                        System.out.println("Writing the tree");
                        if (this.writeTree() == null) {
                            System.err.println("Error writing tree");
                        }
                        break;
                    case 7:
                        System.out.println("Load from a named file");
                        if (this.loadTree() == null) {
                            System.err.println("Error loading tree");
                        }
                        break;
                    default:
                        System.out.println("Bad command, retrying...");
                        break;
                }
            } catch (Exception e) {
                System.err.println("Something bad happened!");
            }
        }
        System.out.println("Exiting...");
    }

    public static void main(String[] args) {
        System.out.println("hello world");
        ConversationBuilder builder = new ConversationBuilder();
        builder.menu();
    }
}
