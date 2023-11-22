package entity;

import java.io.Serializable;

public class History implements Serializable {
    private String fileName;
    private String timeStamp;
    private String encrypt;


    public  History(String fileName, String timeStamp, String encrypt){
        this.fileName=fileName;
        this.timeStamp=timeStamp;
        this.encrypt=encrypt;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getEncrypt() {
        return encrypt;
    }
}
