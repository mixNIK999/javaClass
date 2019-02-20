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
    public static void serialize(@NotNull Object object, OutputStream out) throws IOException {
        try (var objectOut = new ObjectOutputStream(new BufferedOutputStream(out))) {
            Class<?> objectClass = object.getClass();
            for (Field field : objectClass.getDeclaredFields()) {
//                objectOut.writeObject(field);
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
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> T deserialize(InputStream in, @NotNull Class<T> objectClass) throws IOException {
        T result = null;
        try {
            result = objectClass.getConstructor().newInstance();
            try (var objectIn = new ObjectInputStream(new BufferedInputStream(in))) {
                for (Field field : objectClass.getDeclaredFields()) {
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
            } catch (IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }
}
