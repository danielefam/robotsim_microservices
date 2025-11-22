package fr.tp.inf112.projects.robotsim.notifier;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.Factory;

public class SimulationServiceUtils {
	public static final String BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String GROUP_ID = "Factory-Simulation-Group";
	private static final String AUTO_OFFSET_RESET = "earliest";
	private static final String TOPIC = "simulation-topic-";
	
	public static String getTopicName(final Canvas factoryModel) {
		return TOPIC + factoryModel.getId();
	}
	
	public static Properties getDefaultConsumerProperties() {
		final Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
		return props;
	}
}