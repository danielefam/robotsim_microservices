package fr.tp.inf112.projects.robotsim.app;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Area;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Door;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.Machine;
import fr.tp.inf112.projects.robotsim.model.Room;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class TestRobotSimSerializationJSON {
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(TestRobotSimSerializationJSON.class.getName());


    public TestRobotSimSerializationJSON() {
        objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType(PositionedShape.class.getPackageName())
        .allowIfSubType(Component.class.getPackageName())
        .allowIfSubType(BasicVertex.class.getPackageName())
        .allowIfSubType(ArrayList.class.getName())
        .allowIfSubType(LinkedHashSet.class.getName())
        .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }


    @Test
    public void testSerialization() throws JsonProcessingException {
    	
        final Factory myFactory = new Factory(200, 200, "Simple Test Puck Factory"); 
        String factoryAsJsonString = objectMapper.writeValueAsString(myFactory);
        LOGGER.info(factoryAsJsonString);
        final Factory roundTrip = objectMapper.readValue(factoryAsJsonString, Factory.class);
        LOGGER.info(roundTrip.toString());
    }
}
