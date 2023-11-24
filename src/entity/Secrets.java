package entity;

import java.io.Serializable;

public class Secrets implements Serializable {

    private String fileName;
    private String fileSecretKey;
    private byte[] fileArray;

    public Secrets( String fileName, String fileSecretKey, byte[] fileArray){

        this.fileName=fileName;
        this.fileSecretKey=fileSecretKey;
        this.fileArray=fileArray;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSecretKey() {
        return fileSecretKey;
    }

    public byte[] getFileArray() {
        return fileArray;
    }
}
