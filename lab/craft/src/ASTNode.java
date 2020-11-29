import java.util.List;

/**
 * AST节点
 * 属性包括AST的类型、文本值、下级子节点和父节点
 */
public interface ASTNode {

    ASTNode getParent();

    List<ASTNode> getChildren();

    ASTNodeType getType();

    String getText();



}
