package com.sufnom.stack.def.head;


import com.sufnom.stack.def.FixedStackInterface;
import com.sufnom.stack.def.ZedIndexer;

import static com.sufnom.Main.debug;

public abstract class UidStackInterface extends AbstractUidStackInterface {
    private FixedStackInterface stackInterface;
    private ZedIndexer indexer;

    public UidStackInterface(String STACK_NAME){
        stackInterface = new FixedStackInterface(STACK_NAME);
        indexer = new ZedIndexer(STACK_NAME);
    }

    @Override
    public boolean updateData(String uid, byte[] data){
        try {
            long blockId = indexer.getTargetBlockId(uid);
            if (blockId == -1){
                if (debug) System.out.println("blockId Not Found : " + uid);
                return false;
            }
            if (data.length > FixedStackInterface.blockSize)
                return false;
            stackInterface.updateBlock(blockId, data);
            return true;
        }
        catch (Exception e){e.printStackTrace();}
        return false;
    }

    @Override
    public byte[] getData(String uid){
        try {
            long blockId = indexer.getTargetBlockId(uid);
            if (blockId == -1){
                if (debug) System.out.println("blockId Not Found : " + uid);
                return null;
            }
            return stackInterface.readBlock(blockId);
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }

    @Override
    public boolean insertData(String uid, byte[] data){
        try {
            if (data.length > FixedStackInterface.blockSize)
                return false;
            long blockId = stackInterface.insert(data);
            indexer.insertIndex(uid, blockId);
            if (debug) System.out.println("Profile Inserted : uid = " + uid + ", blockId = " + blockId );
            return true;
        }
        catch (Exception e){e.printStackTrace();}
        return false;
    }
}