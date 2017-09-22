package com.sufnom.stack;

@SuppressWarnings("FieldCanBeLocal")
public class StackRequest {
    private byte[] rawRequest;

    public StackRequest(byte[] rawRequest){
        this.rawRequest = rawRequest;
    }

    private String target;  // stack-name           // 54 bytes
    private String command; // insert update get    // 6 bytes
    private String type;    // fsi esi dsi          // 4 bytes
    private byte[] response;
    public void processRequest(){
        byte[] targetRaw = new byte[54],
                commandRaw = new byte[6],
                typeRaw = new byte[4],
                dataBytes = new byte[rawRequest.length - 64];
        System.arraycopy(rawRequest, 0, targetRaw, 0, 54);
        System.arraycopy(rawRequest, 54, commandRaw, 0, 6);
        System.arraycopy(rawRequest, 60, typeRaw, 0, 4);
        target = new String(targetRaw);
        command = new String(commandRaw);
        type = new String(typeRaw);
        System.arraycopy(rawRequest,64,dataBytes, 0, dataBytes.length);
        response = StackTerminal.getSession()
                .processCommand(command,target,type,dataBytes);
    }

    public byte[] getResponse(){
        return response;
    }
}
