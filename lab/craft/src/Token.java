/**
 * 简单的Token
 */
public interface Token {

    /**
     * Token的类型
     * @return
     */
    TokenType getType();

    /**
     * Token的文本值
     * @return
     */
    String getText();
}
