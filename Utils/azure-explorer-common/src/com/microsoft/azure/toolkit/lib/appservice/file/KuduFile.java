package com.microsoft.azure.toolkit.lib.appservice.file;

import lombok.Data;

import java.util.Objects;

@Data
public class KuduFile implements AppServiceFile {
    private String name;
    private long size;
    private String mtime;
    private String crtime;
    private String mime;
    private String href;
    private String path;

    @Override
    public Type getType() {
        return Objects.equals("inode/directory", this.mime) ? Type.DIRECTORY : Type.FILE;
    }
}
