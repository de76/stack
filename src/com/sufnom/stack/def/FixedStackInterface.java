package com.sufnom.stack.def;

import com.sufnom.stack.core.StackInterface;

public class FixedStackInterface {
    public static final int blockSize = 4096;
    public final StackInterface stackInterface;

    public FixedStackInterface(String stackName){
        stackInterface = new StackInterface(stackName);
    }

    //returns 4k block data
    public byte[] readBlock(long blockPointer){
        return stackInterface.readBlock(blockPointer, blockSize);
    }

    public void updateBlock(long blockPointer, byte[] data) throws Exception{
        if (data.length > blockSize){
            throw new Exception("Block Stack Overflowed");
        }
        byte[] restByteCleaner = new byte[blockSize - data.length];
        stackInterface.updateBlock(blockPointer, data);
        if (restByteCleaner.length > 0)
            stackInterface.updateBlock(blockPointer+data.length, restByteCleaner);
    }

    /*
    TODO Optimizations needed the array fill should not
    be here, rather it should fill while writing the block
    */
    public long insert(byte[] data) throws Exception{
        if (data.length > blockSize){
            throw new Exception("Block Stack Overflowed");
        }
        byte[] fixedData = new byte[blockSize];
        for (int i = 0; i < data.length; i++){
            fixedData[i] = data[i];
        }
        return stackInterface.insert(fixedData);
    }
}
