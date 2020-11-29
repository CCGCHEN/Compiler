import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * 四、简单的脚本解释器
 *
 * 运行脚本：
 * 命令行输入：java SimpleScript 进入一个REPL界面
 * 依次敲入命令，譬如：
 * 2+3;
 * int age = 10;
 * int b;
 * b = 10 * 2;
 * age = age + b;
 * exit();  //退出程序
 *
 *
 */
public class SimpleScript {
    private HashMap<String, Integer> variables = new HashMap<>();
    private static boolean verbose = false;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-v")) {
            verbose = true;
            System.out.println("verbose mode");
        }
        System.out.println("Simple script language!");

        SimpleParser parser = new SimpleParser();
        SimpleScript script = new SimpleScript();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String scriptText = "";
        System.out.println("\n>");

        while (true) {
            try {
                String line = reader.readLine().trim();
                if (line.equals("exit();")) {
                    System.out.println("bye");
                    break;
                }
                scriptText += line + "\n";
                if (line.endsWith(";")) {
                    ASTNode tree = parser.parse(scriptText);
                    if (verbose) {
                        parser.dumpAST(tree, "");
                    }

                    script.evaluate(tree, "");
                    System.out.print("\n>");   //提示符

                    scriptText = "";
                }
            } catch (Exception e) {

                System.out.println(e.getLocalizedMessage());
                System.out.print("\n>");   //提示符
                scriptText = "";
            }
        }
    }

    private Integer evaluate(ASTNode node, String indent) throws Exception {
        Integer result = null;
        if (verbose) {
            System.out.println(indent + "Calculating: " + node.getType());
        }
        switch (node.getType()) {
            case Programm:
                for (ASTNode child : node.getChildren()) {
                    result = evaluate(child, indent);
                }
                break;
            case Additive:
                ASTNode child1 = node.getChildren().get(0);
                Integer value1 = evaluate(child1, indent + "\t");
                ASTNode child2 = node.getChildren().get(1);
                Integer value2 = evaluate(child2, indent + "\t");
                if (node.getText().equals("+")) {
                    result = value1 + value2;
                } else {
                    result = value1 - value2;
                }
                break;
            case Multiplicative:
                child1 = node.getChildren().get(0);
                value1 = evaluate(child1, indent + "\t");
                child2 = node.getChildren().get(1);
                value2 = evaluate(child2, indent + "\t");
                if (node.getText().equals("*")) {
                    result = value1 * value2;
                } else {
                    result = value1 / value2;
                }
                break;
            case IntLiteral:
                result = Integer.valueOf(node.getText()).intValue();
                break;

            case Identifier:
                String varName = node.getText();
                if (variables.containsKey(varName)) {
                    Integer value = variables.get(varName);
                    if (value != null) {
                        result = value.intValue();
                    } else {
                        throw new Exception("variable " + varName + " has not been set any value");
                    }
                }
                else{
                    throw new Exception("unknown variable: " + varName);
                }
                break;

            case AssignmentStmt:
                varName = node.getText();
                if (!variables.containsKey(varName)){
                    throw new Exception("unknown variable: " + varName);
                }   //接着执行下面的代码
            case IntDeclaration:
                varName = node.getText();
                Integer varValue = null;
                if (node.getChildren().size() > 0) {
                    ASTNode child = node.getChildren().get(0);
                    result = evaluate(child, indent + "\t");
                    varValue = Integer.valueOf(result);
                }
                variables.put(varName, varValue);
                break;

            default:
        }

        if (verbose) {
            System.out.println(indent + " Result: " + result);
        } else if (indent.equals("")) {
            if (node.getType() == ASTNodeType.IntDeclaration
                    || node.getType() == ASTNodeType.AssignmentStmt) {
                System.out.println(node.getText() + " : " + result);
            } else if (node.getType() != ASTNodeType.Programm){
                System.out.println(result);
            }
        }
        return result;
    }
}
