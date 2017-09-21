package com.sufnom.stack.def;

import com.sufnom.stack.core.StackInterface;

import java.nio.ByteBuffer;

public class DynamicStackInterface {
    public final StackInterface stackInterface;

    public DynamicStackInterface(String stackName){
        stackInterface = new StackInterface(stackName);
    }

    public byte[] readBlock(long blockPointer){
        byte[] blockSizeBytes = stackInterface.readBlock(blockPointer, 4);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(blockSizeBytes);
        buffer.position(0);
        int blockSize = buffer.getInt();
        buffer.clear();
        return stackInterface.readBlock(blockPointer, blockSize);
    }

    public long insert(byte[] data) throws Exception{
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.position(0);
        buffer.putInt(data.length);
        long stackBlockId = stackInterface.insert(buffer.array());
        stackInterface.insert(data);
        buffer.clear();
        return stackBlockId;
    }
}
