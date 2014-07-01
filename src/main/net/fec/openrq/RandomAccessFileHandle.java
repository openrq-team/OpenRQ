package net.fec.openrq;

import java.io.RandomAccessFile;

/**
 * Created by philip on 6/25/2014.
 */
public class RandomAccessFileHandle {
    RandomAccessFile handle;
    String name;

    public RandomAccessFile getHandle() {
        return handle;
    }

    public String getName() {
        return name;
    }

    public RandomAccessFileHandle(RandomAccessFile raf, String name) {
        this.handle = raf;
        this.name = name;
    }


}
