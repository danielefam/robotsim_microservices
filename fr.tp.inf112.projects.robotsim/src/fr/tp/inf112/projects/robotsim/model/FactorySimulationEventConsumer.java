package fr.tp.inf112.projects.robotsim.model;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import fr.tp.inf112.projects.robotsim.app.RemoteSimulatorController;
import fr.tp.inf112.projects.robotsim.notifier.SimulationServiceUtils;



public class FactorySimulationEventConsumer {
	private final KafkaConsumer<String, String> consumer;
	private final RemoteSimulatorController controller;
	private static final Logger LOGGER = Logger.getLogger(FactorySimulationEventConsumer.class.getName());
	
	public FactorySimulationEventConsumer(final RemoteSimulatorController controller) {
		this.controller = controller;
		final Properties props = SimulationServiceUtils.getDefaultConsumerProperties();
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class);
		this.consumer = new KafkaConsumer<>(props);
		final String topicName =
				SimulationServiceUtils.getTopicName(controller.getCanvas());
		this.consumer.subscribe(Collections.singletonList(topicName));
	}
	
	public void consumeMessages() throws JsonMappingException, JsonProcessingException {
		try {
			do {
				final ConsumerRecords<String, String> records =
						consumer.poll(Duration.ofMillis(100));
				LOGGER.finer("before for");
				for (final ConsumerRecord<String, String> record : records) {
					LOGGER.finer("inside for");
					LOGGER.fine("Received JSON Factory text '" + record.value() + "'.");
					controller.setCanvas(record.value());
				}
			} while (controller.isAnimationRunning());
		}
		finally {
			consumer.close();
		}
	}
	
	
}
