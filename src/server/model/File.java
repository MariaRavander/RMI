package server.model;

import common.*;
import server.integration.CatalogDAO;

public class File implements FileDTO {
    public final String name;
    public final Integer size;
    public final String owner;
    public final String permission;
    
    
    public File(String name, Integer size, String owner, String permission) {
        this.name = name;
        this.size = size;
        this.owner = owner;
        this.permission = permission;
    }

    @Override
    public String getFileName() {
        return this.name;
    }

    @Override
    public int getFileSize() {
        return this.size;
    }

    @Override
    public String getFileOwner() {
        return this.owner;
    }

    @Override
    public String getFilePermission() {
        return this.permission;
    }
}
