package io.confluent.examples.clients.cloud.springboot;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import io.confluent.examples.clients.cloud.DataRecordAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static java.lang.System.out;

@Log4j2
@Component
@RequiredArgsConstructor
public class ProducerExample {

  private final KafkaTemplate<String, DataRecordAvro> producer;
  private final NewTopic topic;

  @EventListener(ApplicationStartedEvent.class)
  public void produce() {
    // Produce sample data
    final long numMessages = 10L;
    for (long i = 0L; i < numMessages; i++) {
      String key = "alice";
      DataRecordAvro record = new DataRecordAvro(i);

      log.info("Producing record: {}\t{}", key, record);
      producer.send(topic.name(), key, record).addCallback(
          result -> {
            final RecordMetadata m;
            if (result != null) {
              m = result.getRecordMetadata();
              log.info("Produced record to topic {} partition {} @ offset {}",
                       m.topic(),
                       m.partition(),
                       m.offset());
            }
          },
          exception -> log.error("Failed to produce to kafka", exception));
    }

    producer.flush();

    out.printf("10 messages were produced to topic %s%n", topic);

  }

}
