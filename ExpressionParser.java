import java.util.*;

class Token {
    String lexeme;
    String tokenType;

    Token(String lexeme, String tokenType) {
        this.lexeme = lexeme;
        this.tokenType = tokenType;
    }
}

class SymbolTable {
    Map<String, String> table;

    SymbolTable() {
        table = new HashMap<>();
    }

    void insert(String lexeme, String tokenType) {
        table.put(lexeme, tokenType);
    }

    String lookup(String lexeme) {
        return table.get(lexeme);
    }
}

class TreeNode {
    String value;
    List<TreeNode> children;

    TreeNode(String value) {
        this.value = value;
        this.children = new ArrayList<>();
    }

    void addChild(TreeNode child) {
        this.children.add(child);
    }
}

public class ExpressionParser {
    static String input;
    static int index;
    static SymbolTable symbolTable;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter an expression (or type 'exit' to quit): ");
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break; // Exit the loop if the user types 'exit'
            }

            index = 0;
            symbolTable = new SymbolTable();

            try {
                TreeNode parseTree = parseExpression(); // Start parsing the expression
                if (index < input.length()) {
                    throw new RuntimeException("Unexpected input at index " + index);
                }

                System.out.println("Parse Tree:");
                visualizeTree(parseTree, 0); // Visualize the parse tree

                System.out.println("\nSymbol Table:");
                symbolTable.table.forEach((k, v) -> System.out.println(k + " -> " + v));

                // Check if the input string can be accepted
                boolean isValid = isValidInput(parseTree);
                System.out.println("\nInput accepted based on Grammar: " + isValid);
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    // Parses an expression (e.g., addition/subtraction)
    static TreeNode parseExpression() {
        TreeNode node = parseTerm(); // Parse the first term
        while (index < input.length()) {
            char currentChar = input.charAt(index);
            if (currentChar == '+' || currentChar == '-') {
                index++; // Move to the next character
                TreeNode opNode = new TreeNode(String.valueOf(currentChar));
                TreeNode rightNode = parseTerm(); // Parse the term after the operator
                opNode.addChild(node); // Add the left term
                opNode.addChild(rightNode); // Add the right term
                node = opNode; // Update the node to the operator node

                symbolTable.insert(String.valueOf(currentChar), "Operator");
            } else {
                break;
            }
        }
        return node;
    }

    // Parses a term (e.g., multiplication/division)
    static TreeNode parseTerm() {
        TreeNode node = parseFactor(); // Parse the first factor
        while (index < input.length()) {
            char currentChar = input.charAt(index);
            if (currentChar == '*' || currentChar == '/') {
                index++; // Move to the next character
                TreeNode opNode = new TreeNode(String.valueOf(currentChar));
                TreeNode rightNode = parseFactor(); // Parse the factor after the operator
                opNode.addChild(node); // Add the left factor
                opNode.addChild(rightNode); // Add the right factor
                node = opNode; // Update the node to the operator node

                symbolTable.insert(String.valueOf(currentChar), "Operator");
            } else {
                break;
            }
        }
        return node;
    }

    // Parses a factor (e.g., numbers or expressions within parentheses)
    static TreeNode parseFactor() {
        if (index >= input.length()) {
            throw new RuntimeException("Unexpected end of input");
        }

        char currentChar = input.charAt(index);

        if (Character.isDigit(currentChar)) {
            StringBuilder number = new StringBuilder();
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                number.append(input.charAt(index));
                index++;
            }

            String numStr = number.toString();
            TreeNode node = new TreeNode(numStr); // Use numerical value directly as node value
            symbolTable.insert(numStr, "Number");
            return node;

        } else if (currentChar == '(') {
            index++; // Move past '('
            TreeNode node = parseExpression(); // Parse the inner expression
            if (index < input.length() && input.charAt(index) == ')') {
                index++; // Move past ')'
                return node;
            } else {
                throw new RuntimeException("Expected closing parenthesis, found '" + currentChar + "'");
            }
        } else {
            throw new RuntimeException("Unexpected character '" + currentChar + "'");
        }
    }

    // Visualizes the parse tree in a top-down manner with proper indentation
    static void visualizeTree(TreeNode node, int depth) {
        if (node == null) {
            return;
        }
        // Print the current node with indentation based on the depth
        for (int i = 0; i < depth; i++) {
            System.out.print("  "); // Add spacing for depth
        }
        System.out.println(node.value); // Print numerical value directly

        // Recursively visualize each child node
        for (TreeNode child : node.children) {
            visualizeTree(child, depth + 1);
        }
    }

    // Check if the input string can be accepted based on the grammar
    static boolean isValidInput(TreeNode parseTree) {
        // The input is considered valid if it's a parse tree with only operators and numbers
        // and if the number of operators is one less than the number of numbers
        int numOperators = countOperators(parseTree);
        int numNumbers = countNumbers(parseTree);
        return numOperators == numNumbers - 1;
    }

    // Count the number of operators in the parse tree
    static int countOperators(TreeNode node) {
        if (node == null) {
            return 0;
        }
        int count = node.value.equals("+") || node.value.equals("-") || node.value.equals("*") || node.value.equals("/") ? 1 : 0;
        for (TreeNode child : node.children) {
            count += countOperators(child);
        }
        return count;
    }

    // Count the number of numbers in the parse tree
    static int countNumbers(TreeNode node) {
        if (node == null) {
            return 0;
        }
        int count = Character.isDigit(node.value.charAt(0)) ? 1 : 0; // Check if the value starts with a digit
        for (TreeNode child : node.children) {
            count += countNumbers(child);
        }
        return count;
    }
}
