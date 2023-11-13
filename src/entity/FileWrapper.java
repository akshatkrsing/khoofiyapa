package entity;

import java.io.File;

public class FileWrapper {
    String fileName;
    String filePath;
    public FileWrapper(File file){
        this.fileName = file.getName();
        this.filePath = file.getAbsolutePath();
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}
