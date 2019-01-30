package tech.qiwang;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.requests.OffsetFetchRequest;
import org.apache.kafka.common.requests.OffsetFetchResponse;

import java.util.*;

public class KafkaConsumerDemo {

    public static void main(String[] args) throws InterruptedException {

        Properties props = new Properties();
        /* 定义kakfa 服务的地址，不需要将所有broker指定上 */
        props.put("bootstrap.servers", "127.0.0.1:9092");
        /* 制定consumer group */
        String groupId = "test";
        props.put("group.id", groupId);
        /* 是否自动确认offset */
        props.put("enable.auto.commit", "true");
        /* 自动确认offset的时间间隔 */
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        /* key的序列化类 */
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        /* value的序列化类 */
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        /* 定义consumer */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        /* 消费者订阅的topic, 可同时订阅多个 */
        String topic = "test0";
        List<String> topics = Arrays.asList(topic);
        consumer.subscribe(topics);

        /* 读取数据，读取超时时间为100ms */
        while (true) {
            Thread.sleep(100);
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
                System.out.println( "  KafkaTimestampType:" + record.timestampType().name + " Delay times: " + (System.currentTimeMillis() - record.timestamp()) );
            }

            Map<Integer, Long> endOffsetMap = new HashMap<>();
            Map<Integer, Long> commitOffsetMap = new HashMap<>();
            List<TopicPartition> topicPartitions = new ArrayList<>();
            List<PartitionInfo> partitionsFor = consumer.partitionsFor("test0");
            for (PartitionInfo partitionInfo : partitionsFor) {
                TopicPartition topicPartition = new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
                topicPartitions.add(topicPartition);
            }

            //查询log size
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
            for (TopicPartition partitionInfo : endOffsets.keySet()) {
                endOffsetMap.put(partitionInfo.partition(), endOffsets.get(partitionInfo));
            }
            for (Integer partitionId : endOffsetMap.keySet()) {
                System.out.println(String.format("at %s, topic:%s, partition:%s, logSize:%s", System.currentTimeMillis(), topic, partitionId, endOffsetMap.get(partitionId)));
            }

            //查询消费offset
            for (TopicPartition topicAndPartition : topicPartitions) {
                OffsetAndMetadata committed = consumer.committed(topicAndPartition);
                commitOffsetMap.put(topicAndPartition.partition(), committed.offset());
            }

            //累加lag
            long lagSum = 0l;
            if (endOffsetMap.size() == commitOffsetMap.size()) {
                for (Integer partition : endOffsetMap.keySet()) {
                    long endOffSet = endOffsetMap.get(partition);
                    long commitOffSet = commitOffsetMap.get(partition);
                    long diffOffset = endOffSet - commitOffSet;
                    lagSum += diffOffset;
                    System.out.println("Topic:" + topic + ", groupID:" + groupId + ", partition:" + partition + ", endOffset:" + endOffSet + ", commitOffset:" + commitOffSet + ", diffOffset:" + diffOffset);
                }
                System.out.println("Topic:" + topic + ", groupID:" + groupId + ", LAG:" + lagSum);
            } else {
                System.out.println("this topic partitions lost");
            }
        }
    }

}
