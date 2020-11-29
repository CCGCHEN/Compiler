import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 三、实现一个简单的语法解析器。
 *
 * 能够学会间隙简单的表达式、变量声明和初始化语句、赋值语句
 */
public class SimpleParser {
    public static void main(String[] args) {
        SimpleParser parser = new SimpleParser();
        String script = null;
        ASTNode tree = null;

        try {
            script = "int age = 45 + 2; age = 20; age+10*2;";
            System.out.println("解析：" + script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //测试异常语法
        try {
            script = "2+3+;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //测试异常语法
        try {
            script = "2+3*;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + "\t");
        }
    }

    public ASTNode parse(String script) throws Exception{
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(script);
        ASTNode rootNode = prog(tokens);
        return rootNode;
    }

    private ASTNode prog(TokenReader tokens) throws Exception {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Programm, "pwd");

        while (tokens.peek() != null) {
            SimpleASTNode child = intDeclare(tokens);

            if (child == null) {
                //是否表达式语句
                child = expressionStatement(tokens);
            }
            if (child == null) {
                //是否赋值语句
                child = assignmentStatement(tokens);
            }

            if (child != null) {
                node.addChild(child);
            } else {
                throw new Exception("unknown statement");
            }
        }
        return node;
    }

    private SimpleASTNode assignmentStatement(TokenReader tokens) throws Exception{
        SimpleASTNode node = null;
        Token token = tokens.peek();    //预读，看看下面是不是标识符
        if (token != null && token.getType() == TokenType.Identifier) {
            token = tokens.read();
            node = new SimpleASTNode(ASTNodeType.AssignmentStmt, token.getText());
            token = tokens.peek();
            if (token != null && token.getType() == TokenType.Assignment) {
                tokens.read();
                SimpleASTNode child = additive(tokens);
                if (child == null) {
                    throw new Exception("invalid assignment statement, expecting an expression");
                } else {
                    node.addChild(child);
                    token = tokens.peek();
                    if (token != null && token.getType() == TokenType.SemiColon) {
                        tokens.read();
                    } else {
                        throw new Exception("invalid statement, expecting semicolon");
                    }
                }
            } else {
                tokens.unread();
                node = null;
            }
        }
        return node;
    }

    private SimpleASTNode additive(TokenReader tokens) throws Exception {
        SimpleASTNode child = multiplicative(tokens);   //应用add'规则
        SimpleASTNode node = child;
        if (child != null) {
            while (true) {
                Token token = tokens.peek();
                if (token != null && (token.getType() == TokenType.Plus || token.getType() == TokenType.Minus)) {
                    token = tokens.read();
                    SimpleASTNode child2 = multiplicative(tokens);
                    if (child2 != null) {
                        node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                        node.addChild(child);
                        node.addChild(child2);
                        child = node;
                    } else {
                        throw new Exception("invalid additive expression, expecting the right part.");
                    }
                } else {
                    break;
                }
            }
        }
        return node;
    }

    /**
     * 乘法表达式
     * @param tokens
     * @return
     */
    private SimpleASTNode multiplicative(TokenReader tokens) throws Exception{    SimpleASTNode child1 = primary(tokens);
        SimpleASTNode node = child1;

        while (true) {
            Token token = tokens.peek();
            if (token != null && (token.getType() == TokenType.Star || token.getType() == TokenType.Slash)) {
                token = tokens.read();
                SimpleASTNode child2 = primary(tokens);
                if (child2 != null) {
                    node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                    child1 = node;
                }else{
                    throw new Exception("invalid multiplicative expression, expecting the right part.");
                }
            } else {
                break;
            }
        }

        return node;
    }

    private SimpleASTNode primary(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        Token token = tokens.peek();
        if (token != null) {
            if (token.getType() == TokenType.IntLiteral) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
            } else if (token.getType() == TokenType.Identifier) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());
            } else if (token.getType() == TokenType.LeftParen) {
                tokens.read();
                node = additive(tokens);
                if (node != null) {
                    token = tokens.peek();
                    if (token != null && token.getType() == TokenType.RightParen) {
                        tokens.read();
                    } else {
                        throw new Exception("expecting right parenthesis");
                    }
                } else {
                    throw new Exception("expecting an additive expression inside parenthes");
                }
            }
        }
        return node;
    }

    private SimpleASTNode expressionStatement(TokenReader tokens) throws Exception {
        int pos = tokens.getPosition();
        SimpleASTNode node = additive(tokens);
        if (node != null) {
            Token token = tokens.peek();
            if (token != null && token.getType() == TokenType.SemiColon) {
                tokens.read();
            } else {
                node = null;
                tokens.setPosition(pos);//回溯
            }
        }
        return node;
    }

    private SimpleASTNode intDeclare(TokenReader tokens) throws Exception{
        SimpleASTNode node = null;
        Token token = tokens.peek();
        if (token != null && token.getType() == TokenType.Int) {
            token = tokens.read();
            if (tokens.peek().getType() == TokenType.Identifier) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
                token = tokens.peek();
                if (token != null && token.getType() == TokenType.Assignment) {
                    tokens.read();
                    SimpleASTNode child = additive(tokens);
                    if (child == null) {
                        throw new Exception("invalid variable initialization, expecting an expression");
                    } else {
                        node.addChild(child);
                    }
                }
            } else {
                throw new Exception("variable name expected");
            }
            if (node != null) {
                token = tokens.peek();
                if (token != null && token.getType() == TokenType.SemiColon) {
                    tokens.read();
                } else {
                    throw new Exception("invalid statement, expecting semicolon");
                }
            }
        }
        return node;
    }

    private class SimpleASTNode implements ASTNode {
        SimpleASTNode parent = null;
        List<ASTNode> children = new ArrayList<>();
        List<ASTNode> readonlyChildren = Collections.unmodifiableList(children);
        ASTNodeType nodeType = null;
        String text = null;

        public SimpleASTNode(ASTNodeType nodeType, String text) {
            this.nodeType = nodeType;
            this.text = text;
        }

        @Override
        public ASTNode getParent() {
            return parent;
        }

        @Override
        public List<ASTNode> getChildren() {
            return readonlyChildren;
        }

        @Override
        public ASTNodeType getType() {
            return nodeType;
        }

        @Override
        public String getText() {
            return text;
        }

        public void addChild(SimpleASTNode child) {
            children.add(child);
            child.parent = this;
        }
    }
}
