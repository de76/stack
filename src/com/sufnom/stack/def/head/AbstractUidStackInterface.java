package com.sufnom.stack.def.head;

public abstract class AbstractUidStackInterface {
    public abstract boolean updateData(String uid, byte[] data);
    public abstract byte[] getData(String uid);
    public abstract boolean insertData(String uid, byte[] data);
}
