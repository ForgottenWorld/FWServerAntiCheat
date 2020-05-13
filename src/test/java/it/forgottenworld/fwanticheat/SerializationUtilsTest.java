package it.forgottenworld.fwanticheat;

import junit.framework.TestCase;

import java.io.IOException;

public class SerializationUtilsTest extends TestCase {

    public void testSerialize() throws IOException, ClassNotFoundException {
        String string = "test";
        assertEquals(SerializationUtils.deserialize(SerializationUtils.serialize(string)), string);
    }

    public void testDeserialize() throws IOException, ClassNotFoundException {
        String string = "test";
        assertEquals(SerializationUtils.deserialize(SerializationUtils.serialize(string)),string);
    }
}