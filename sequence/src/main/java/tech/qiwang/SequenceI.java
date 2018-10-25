package tech.qiwang;

import cn.hutool.core.util.IdUtil;


/**
 * https://www.cnblogs.com/haoxinyue/p/5208136.html
 */
public interface SequenceI {

    Boolean init(long start);

    /**
     * 采用MongoDB的ID生成策略，这样前四位会隐藏时间信息
     * @return
     */
    default String stringUid(){
        return IdUtil.objectId();
    }

    /**
     * 连续性自增数字
     * @return
     */
    Long numberSequence();


    /**
     * CurrentTimeMillis + Server标记 + 时间段内自增数
     * @return
     */
    default Long snowflakeId(long workerId, long dcId) {
        return IdUtil.createSnowflake(workerId, dcId).nextId();
    }

}
