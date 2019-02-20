package me.nikolyukin.cwReflection;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class Serialization {
    void serialize(Object object, OutputStream out) {
        try (ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(out))) {
            Class<?> objectClass = object.getClass();
            for (Field field : objectClass.getDeclaredFields()) {
                objectOut.writeObject(field);
                Class<?> fieldType = field.getType();
                switch (fieldType.getName()) {
                    case "byte":
                        objectOut.writeByte(field.getByte(object));
                        break;
                    case "char":
                        objectOut.writeChar(field.getChar(object));
                        break;
                    case "short":
                        objectOut.writeShort(field.getShort(object));
                        break;
                    case "int":
                        objectOut.writeInt(field.getInt(object));
                        break;
                    case "long":
                        objectOut.writeLong(field.getLong(object));
                        break;
                    case "float":
                        objectOut.writeFloat(field.getFloat(object));
                        break;
                    case "double":
                        objectOut.writeDouble(field.getDouble(object));
                        break;
                    case "boolean":
                        objectOut.writeBoolean(field.getBoolean(object));
                        break;
                    default:
                        objectOut.writeObject(field.get(object));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    T deserialize(InputStream in, Class<T> class) {
    }
}
