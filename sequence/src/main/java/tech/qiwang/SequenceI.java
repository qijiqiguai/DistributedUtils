package tech.qiwang;

public interface SequenceI {

    void config();

    /**
     * 字符串型的唯一ID，最简单的生成方式即用UUID来实现
     * @return
     */
    String stringUid();

    /**
     * 连续性的数字序列号
     * @return
     */
    Long numberSequence();


    /**
     * CurrentTimeMillis + Server标记 + 时间段内自增数
     * @return
     */
    String timeBasedUid();

}
