package tech.qiwang;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import tech.qiwang.impl.RedisBasedSequence;

import java.util.HashSet;
import java.util.Set;

/**
 * 测试场景
 * 1：测试连续数字型ID
 * 2：测试随机字符串型ID
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringMain.class)
public class RedisBasedSequenceTest {

    private final String REDIS_KEY = "Sequence_Key";
    private final double COUNT = 1000000;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    public void basicOps() throws Exception {
        SequenceI sequence = new RedisBasedSequence(redisTemplate, REDIS_KEY);

//        numberSequence(sequence);

        uidSequence(sequence);
    }

    private void numberSequence(SequenceI sequence){
        // 测试设置初始值
        sequence.init(100);
        Assert.assertTrue( sequence.numberSequence()==101 );

        // 测试连续数字型序列号的连续性
        double numCount = COUNT;
        long startTime = System.currentTimeMillis();
        long last = 0;
        for(int i=0; i<numCount; i++){
            if(i == 0){
                last = sequence.numberSequence();
            }else {
                long thisNum = sequence.numberSequence();
                Assert.assertTrue(last+1==thisNum);
                last = thisNum;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("NumberSequence TimeCost: " + (endTime - startTime) + " MS" );
        System.out.println("NumberSequence AverageTimeCost: " + (double)(endTime - startTime)/numCount + " MS" );
    }

    private void uidSequence(SequenceI sequence) {
        // 测试随机字符串是否重复
        double strCount = COUNT;
        Set<String> sequenceSet = new HashSet<>();
        long startTime = System.currentTimeMillis();

        for(int i=0; i<strCount; i++){
            String uid = sequence.stringUid();
            sequenceSet.add(uid);
            System.out.println(uid);
        }
        Assert.assertTrue(strCount == sequenceSet.size() );

        long endTime = System.currentTimeMillis();
        System.out.println("UidSequence TimeCost: " + (endTime - startTime) + " MS" );
        System.out.println("UidSequence AverageTimeCost TimeCost: " + (double)(endTime - startTime)/strCount + " MS" );
    }
}
