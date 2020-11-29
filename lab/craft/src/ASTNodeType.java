public enum ASTNodeType {
    Programm,   //程序主入口，根节点

    IntDeclaration, //整形变量声明  int

    ExpressionStmt, //表达式语句 int age = 10;
    AssignmentStmt, //赋值语句  age = 10;

    Primary,        //基础表达式

    Multiplicative, //乘法表达式 2 * 2
    Additive,      //加法表达式 2 + 2

    Identifier, //标识符   todo:ccg，补充demo

    IntLiteral  //整形字面量
}
