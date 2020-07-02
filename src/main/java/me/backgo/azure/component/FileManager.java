package me.backgo.azure.component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * FileManager
 */
public interface FileManager {
    public void upload(List<MultipartFile> files) throws IOException;

    public int download(OutputStream os, String fileName);
    
    public void delete(String fileName) throws FileNotFoundException;

    public void rename(String source, String target) throws FileNotFoundException, FileAlreadyExistsException;

}