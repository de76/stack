package com.sufnom.stack.core;

import com.sufnom.sys.Config;

import java.io.RandomAccessFile;

import static com.sufnom.Main.debug;

public class StackInterface {
    private static final String path = Config.getSession().getValue(Config.KEY_STACK_PATH);
    private RandomAccessFile randomAccessFile;

    public StackInterface(String stackName){
        try {
            String filePath = path + stackName;
            if (debug) System.out.println("Opening : " + filePath);
            randomAccessFile = new RandomAccessFile(filePath, "rw");
        }
        catch (Exception e){e.printStackTrace();}
    }

    public StackInterface(String path, String stackName){
        try {
            String filePath = path + stackName;
            if (debug) System.out.println("Opening : " + filePath);
            randomAccessFile = new RandomAccessFile(filePath, "rw");
        }
        catch (Exception e){e.printStackTrace();}
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    public byte[] readBlock(long blockPointer, int length){
        try {
            if (debug) System.out.println("Reading : blockPointer = " + blockPointer + ", length = " + length);
            randomAccessFile.seek(blockPointer);
            byte[] block = new byte[length];
            randomAccessFile.read(block);
            return block;
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }

    public void updateBlock(long blockPointer, byte[] data){
        try {
            if (debug) System.out.println("Update : blockPointer = " + blockPointer + ", length = " + data.length);
            randomAccessFile.seek(blockPointer);
            randomAccessFile.write(data);
        }
        catch (Exception e){e.printStackTrace();}
    }

    public long insert(byte[] data){
        try {
            if (debug) System.out.println("Insert : length = " + data.length);
            long pointer = randomAccessFile.length();
            randomAccessFile.seek(pointer);
            randomAccessFile.write(data);
            return pointer;
        }
        catch (Exception e){e.printStackTrace();}
        return 0;
    }
}
