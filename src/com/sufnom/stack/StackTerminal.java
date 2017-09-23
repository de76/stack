package com.sufnom.stack;

import com.sufnom.stack.def.DynamicStackInterface;
import com.sufnom.stack.def.ExtendedStackInterface;
import com.sufnom.stack.def.FixedStackInterface;
import com.sufnom.stack.def.ZedIndexer;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class StackTerminal {
    private static final StackTerminal session = new StackTerminal();
    public static StackTerminal getSession() { return session; }

    public static final String COMMAND_INSERT = "insert";
    public static final String COMMAND_GET = "get";
    public static final String COMMAND_UPDATE = "update";

    public static final String TYPE_FIXED_STACK = "fsi";
    public static final String TYPE_EXTENDED_STACK = "esi";
    public static final String TYPE_DYNAMIC_STACK = "dsi";

    private HashMap<String, Object> connectedStacks = new HashMap<>();

    public byte[] processCommand(String command, String target, String type, byte[] dataBytes){
        Object stack = connectedStacks.get(target);
        if (stack == null){
            stack = connectToStack(target, type);
            if (stack == null) return "error".getBytes();
            connectedStacks.put(target, stack);
        }
        try {
            switch (type){
                case TYPE_FIXED_STACK:
                    return processFixedStack((FixedStackInterface)stack,
                            command, dataBytes);
                case TYPE_DYNAMIC_STACK:
                    return processDynamicStack((DynamicStackInterface)stack,
                            command,dataBytes);
                case TYPE_EXTENDED_STACK:
                    return processExtendedStack((ExtendedStackInterface)stack,
                            command,dataBytes);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return "error".getBytes();
        }
        return new byte[0];
    }

    private byte[] processFixedStack(FixedStackInterface stack,
                         String command, byte[] dataBytes) throws Exception{
        switch (command){
            case COMMAND_INSERT:
                return convertToBytes(stack.insert(dataBytes));
            case COMMAND_UPDATE:
                byte[] data = new byte[4096];
                System.arraycopy(dataBytes,8,data,0, data.length);
                stack.updateBlock(getBlockId(dataBytes), data);
                return "ok".getBytes();
            case COMMAND_GET:
                return stack.readBlock(getBlockId(dataBytes));
        }
        throw new Exception("Command Not Matched");
    }

    private byte[] convertToBytes(long blockId){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, blockId);
        byte[] result = buffer.array();
        buffer.clear();
        return result;
    }

    private long getBlockId(byte[] dataBlock){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        byte[] blockHead = new byte[8];
        System.arraycopy(dataBlock,0,blockHead,0,8);
        buffer.put(blockHead);
        long blockId = buffer.getLong(0);
        buffer.clear();
        return blockId;
    }

    private byte[] processExtendedStack(ExtendedStackInterface stack,
                        String command, byte[] dataBytes) throws Exception{
        byte[] rawUid = new byte[ZedIndexer.MAX_KEY_LENGTH];
        byte[] rawData = new byte[dataBytes.length - rawUid.length];
        System.arraycopy(dataBytes, 0, rawUid, 0, rawUid.length);
        System.arraycopy(rawData, rawUid.length, rawData, 0, rawData.length);
        String uid = new String(rawUid);
        switch (command){
            case COMMAND_INSERT:
                return stack.insertData(uid, rawData) ?
                        "ok".getBytes() : "error".getBytes();
            case COMMAND_GET:
                return stack.getData(uid);
            case COMMAND_UPDATE:
                return stack.updateData(uid, rawData) ?
                        "ok".getBytes() : "error".getBytes();
        }
        throw new Exception("Command Not Matched");
    }

    private byte[] processDynamicStack(DynamicStackInterface stack,
                       String command, byte[] dataBytes) throws Exception{
        switch (command){
            case COMMAND_INSERT:
                return convertToBytes(stack.insert(dataBytes));
            case COMMAND_GET:
                return stack.readBlock(getBlockId(dataBytes));
        }
        throw new Exception("Command Not Matched");
    }

    private Object connectToStack(String target, String type){
        switch (type){
            case TYPE_FIXED_STACK: return new FixedStackInterface(target);
            case TYPE_DYNAMIC_STACK: return new DynamicStackInterface(target);
            case TYPE_EXTENDED_STACK: return new ExtendedStackInterface(target);
        }
        return null;
    }
}
