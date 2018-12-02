package common;
import java.io.Serializable;

public interface FileDTO extends Serializable {
    public String getFileName();

    public int getFileSize();
    
    public String getFileOwner();
    
    public String getFilePermission();
}
