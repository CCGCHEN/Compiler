public interface TokenReader {
    /**
     * 返回Token流中的下一个Token,并从流中取出。
     * 如果流已经为空，则返回null
     * @return
     */
    Token read();

    /**
     * 返回Token流中的下一个Token,但不从流中取出；
     * 如果流已经为空，返回null
     * @return
     */
    Token peek();

    /**
     * Token流回退一步，恢复原来的Token.
     */
    void unread();

    /**
     * 获取Token流当前的读取位置。
     * @return
     */
    int getPosition();

    /**
     * 设置Token流当前的读取位置
     * @param position
     */
    void setPosition(int position);
}
