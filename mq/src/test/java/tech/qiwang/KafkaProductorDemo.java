package tech.qiwang;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class KafkaProductorDemo {

    public static void main(String[] args) throws InterruptedException, ExecutionException {


        Properties properties = new Properties();

        properties.put("bootstrap.servers", "127.0.0.1:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");


        KafkaProducer kafkaProducer = new KafkaProducer(properties);

        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 1000000000; i++) {
            long currentTime = System.currentTimeMillis();
            ProducerRecord<String,String> producerRecord = new ProducerRecord<>("test0", "keykey","hello:" + currentTime);
            kafkaProducer.send(producerRecord).get();
            System.out.println("Message: " + currentTime);
            Thread.sleep(random.nextInt(10)*100);
        }

        kafkaProducer.close();

        System.out.println("product end");
    }
}
