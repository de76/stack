package com.sufnom.stack.def;

import com.sufnom.stack.core.StackInterface;
import com.sufnom.sys.Config;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static com.sufnom.Main.debug;

public class ZedIndexer {
    private static final String TAG = "index.zed";
    private static final String RAM_DRIVE_PATH = Config.getSession().getValue(Config.KEY_STACK_RAM_PATH);
    public static final int MAX_KEY_LENGTH = 50;

    public StackInterface rawIndexStackInterface;
    public StackInterface fastSSdIndexStackInterface;

    public StackInterface fastRamIndexStackInterface;

    public String rawIndexFileName;
    public String fastIndexFileName;

    public ZedIndexer(String indexPrefix){
        rawIndexFileName = indexPrefix + ".raw." + TAG;
        fastIndexFileName = indexPrefix + ".fast." + TAG;

        rawIndexStackInterface = new StackInterface(rawIndexFileName);
        fastSSdIndexStackInterface = new StackInterface(fastIndexFileName);
        fastRamIndexStackInterface = new StackInterface(RAM_DRIVE_PATH, fastIndexFileName);
    }

    public void insertIndex(String key, long targetBlockId) throws Exception{
        try {
            // 4 byte block length
            // 8 byte targetBlockId
            // + lengthOf(key)
            int sumOfKey = sumOf(key);
            byte[] keyArr = key.getBytes();
            int keyLength = keyArr.length;
            if (keyLength > MAX_KEY_LENGTH){
                throw new Exception("Key Index Overflow");
            }
            int rawBufferLength = 4 + 8 + keyLength;
            ByteBuffer rawBuffer = ByteBuffer.allocate(rawBufferLength);
            rawBuffer.position(0);
            rawBuffer.putInt(rawBufferLength);
            rawBuffer.position(4);
            rawBuffer.putLong(targetBlockId);
            rawBuffer.position(12);
            rawBuffer.put(keyArr);
            long blockId = rawIndexStackInterface.insert(rawBuffer.array());
            // 8 byte long + 4 byte sum of key
            ByteBuffer indexBuffer = ByteBuffer.allocate(12);
            indexBuffer.position(0);
            indexBuffer.putLong(blockId);
            indexBuffer.position(8);
            indexBuffer.putInt(sumOfKey);
            fastSSdIndexStackInterface.insert(indexBuffer.array());
            fastRamIndexStackInterface.insert(indexBuffer.array());
            if (debug) System.out.println("Inserted : blockId = " + blockId + ", key = " + key + " , sum = " + sumOfKey);
        }
        catch (Exception e){e.printStackTrace();}
    }

    private boolean isKeyMatches(long targetBlockPointer, String key){
        try {
            String originalData = getKey(targetBlockPointer);
            if (originalData == null) return false;
            boolean status = originalData.trim().equals(key.trim());
            if (debug) System.out.println("Matching : " + key + " " + originalData + " status : " + status);
            return status;
        }
        catch (Exception e){e.printStackTrace();}
        return false;
    }

    private long getParentBlockId(long blockId){
        try {
            RandomAccessFile file = rawIndexStackInterface.getRandomAccessFile();
            file.seek(blockId +4);

            ByteBuffer byteBufferBody = ByteBuffer.allocate(8);
            byte[] rawParentBlockId = new byte[8];
            file.read(rawParentBlockId);
            byteBufferBody.position(0);
            byteBufferBody.put(rawParentBlockId);
            byteBufferBody.position(0);
            long parentBlockId = byteBufferBody.getLong();

            byteBufferBody.clear();
            return parentBlockId;
        }
        catch (Exception e){e.printStackTrace();}
        return 0;
    }

    //Ref
    //http://www.kdgregory.com/index.php?page=java.byteBuffer
    private String getKey(long blockId){
        try {
            RandomAccessFile file = rawIndexStackInterface.getRandomAccessFile();
            file.seek(blockId);
            byte[] lengthRawData = new byte[4];

            file.read(lengthRawData);
            ByteBuffer byteBufferHeader = ByteBuffer.allocate(4);
            byteBufferHeader.put(lengthRawData);
            byteBufferHeader.position(0);
            int length = byteBufferHeader.getInt();

            byte[] rawStringData = new byte[length - 4];
            long strPosition = blockId + 12;
            file.seek(strPosition);
            file.read(rawStringData);

            String data = new String(rawStringData);
            byteBufferHeader.clear();
            return data;
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }

    public long getTargetBlockId(String key){
        long dataPointer = 0;
        long sumPointerInData = 0;
        ByteBuffer bufferOfPointedSum = ByteBuffer.allocate(4);
        ByteBuffer bufferOfBlockId = ByteBuffer.allocate(8);
        byte[] sumIntRaw = new byte[4];
        byte[] blockIdRaw = new byte[8];
        long maxPossiblePointer;

        int pointedSumInt = 0;
        long blockId = 0;
        long parentBlockId = 0;

        int sumOfKey = sumOf(key);
        boolean isFinallyMatched = false;

        RandomAccessFile file = fastRamIndexStackInterface.getRandomAccessFile();
        try {
            maxPossiblePointer = file.length();
        }
        catch (Exception e){
            e.printStackTrace();
            return -1;
        }
        while (true){
            try {
                sumPointerInData = dataPointer + 8;
                file.seek(sumPointerInData);
                file.read(sumIntRaw);
                bufferOfPointedSum.position(0);
                bufferOfPointedSum.put(sumIntRaw);
                bufferOfPointedSum.position(0);
                pointedSumInt = bufferOfPointedSum.getInt();
                if (pointedSumInt == sumOfKey){
                    if (debug)System.out.println("Sum Matched : " + pointedSumInt);
                    //A Possible Match
                    //But Not exact
                    file.seek(dataPointer);
                    file.read(blockIdRaw);
                    bufferOfBlockId.position(0);
                    bufferOfBlockId.put(blockIdRaw);
                    bufferOfBlockId.position(0);
                    blockId = bufferOfBlockId.getLong();
                    if (debug) System.out.println("Matched block Id : " + blockId);
                    isFinallyMatched = isKeyMatches(blockId, key);
                    if (isFinallyMatched){
                        parentBlockId = getParentBlockId(blockId);
                        break;
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                break;
            }
            dataPointer += 12;
            if ( dataPointer >= maxPossiblePointer)
                return -1;
        }
        bufferOfBlockId.clear();
        bufferOfPointedSum.clear();
        return parentBlockId;
    }

    public static int sumOf(String s){
        int sum = 0;
        char[] data = s.toCharArray();
        for(int x=0; x<data.length ;x++)
            sum += data[x];    //This is enough to get the sum.
        return sum;
    }
}
