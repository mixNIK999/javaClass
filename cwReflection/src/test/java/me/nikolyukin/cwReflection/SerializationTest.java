package me.nikolyukin.cwReflection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SerializationTest {
    private Structure base = new Structure();

    private static class Structure {
        private byte byteField = 1;
        private char charField = 2;
        private short shortField = 3;
        private int intField = 4;
        private long longField = 5;
        private float floatField = 6;
        private double doubleField = 7.1;
        private boolean booleanField = true;
        private String stringField = "test";
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }
            Structure structure = (Structure) obj;
            return byteField == structure.byteField &&
                charField == structure.charField &&
                shortField == structure.shortField &&
                intField == structure.intField &&
                longField == structure.longField &&
                floatField == structure.floatField &&
                doubleField == structure.doubleField &&
                booleanField == structure.booleanField &&
                stringField.equals(structure.stringField);
        }
    }

    @Test
    void serializeDeserialize() throws IOException {
        try (var out = new ByteArrayOutputStream(1000)) {
            Serialization.serialize(base, out);
            byte[] data = out.toByteArray();
            try(var in = new ByteArrayInputStream(data)) {
                assertEquals(base, Serialization.deserialize(in, Structure.class));
            }
        }

    }
}