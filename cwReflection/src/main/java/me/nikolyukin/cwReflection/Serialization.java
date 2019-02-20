package me.nikolyukin.cwReflection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

public class Serialization {
    public static void serialize(@NotNull Object object, OutputStream out) throws IOException, IllegalAccessException {
        try (var objectOut = new ObjectOutputStream(new BufferedOutputStream(out))) {
            Class<?> objectClass = object.getClass();
            for (Field field : objectClass.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                String s = fieldType.getName();
                if ("byte".equals(s)) {
                    objectOut.writeByte(field.getByte(object));
                } else if ("char".equals(s)) {
                    objectOut.writeChar(field.getChar(object));
                } else if ("short".equals(s)) {
                    objectOut.writeShort(field.getShort(object));
                } else if ("int".equals(s)) {
                    objectOut.writeInt(field.getInt(object));
                } else if ("long".equals(s)) {
                    objectOut.writeLong(field.getLong(object));
                } else if ("float".equals(s)) {
                    objectOut.writeFloat(field.getFloat(object));
                } else if ("double".equals(s)) {
                    objectOut.writeDouble(field.getDouble(object));
                } else if ("boolean".equals(s)) {
                    objectOut.writeBoolean(field.getBoolean(object));
                } else {
                    objectOut.writeObject(field.get(object));
                }
            }
        }
    }

    public static <T> T deserialize(InputStream in, @NotNull Class<T> objectClass)
        throws IOException, InstantiationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        T result = null;
        result = objectClass.getConstructor().newInstance();
        try (var objectIn = new ObjectInputStream(new BufferedInputStream(in))) {
            for (Field field : objectClass.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                switch (fieldType.getName()) {
                    case "byte":
                        field.setByte(result, objectIn.readByte());
                        break;
                    case "char":
                        field.setChar(result, objectIn.readChar());
                        break;
                    case "short":
                        field.setShort(result, objectIn.readShort());
                        break;
                    case "int":
                        field.setInt(result, objectIn.readInt());
                        break;
                    case "long":
                        field.setLong(result, objectIn.readLong());
                        break;
                    case "float":
                        field.setFloat(result, objectIn.readFloat());
                        break;
                    case "double":
                        field.setDouble(result, objectIn.readDouble());
                        break;
                    case "boolean":
                        field.setBoolean(result, objectIn.readBoolean());
                        break;
                    default:
                        field.set(result, objectIn.readObject());
                        break;
                }
            }
        }
        return result;
    }
}
