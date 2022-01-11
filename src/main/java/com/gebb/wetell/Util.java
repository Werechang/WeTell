package com.gebb.wetell;

import com.gebb.wetell.dataclasses.DataClassFilter;

import java.io.*;

public class Util {
    public static <E> byte[] serializeObject(E object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(object);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public static <E> E deserializeObject(Class<E> type, byte[] stream) {
        ByteArrayInputStream bis = new ByteArrayInputStream(stream);
        try {
            ObjectInputStream is = new ObjectInputStream(bis);
            is.setObjectInputFilter(new DataClassFilter());
            Object o = is.readObject();
            // If cast fails return null
            if (type.isInstance(o)) {
                return type.cast(o);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
