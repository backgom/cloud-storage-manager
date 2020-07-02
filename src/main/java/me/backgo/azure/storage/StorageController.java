package me.backgo.azure.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import me.backgo.azure.component.FileManager;

@Slf4j
@RestController
public class StorageController {
    
    @Autowired
    FileManager fileManager;

    @PostMapping(value = "/upload-file")
    public void uploadFile(@RequestPart(name = "files") List<MultipartFile> files) {
        try {
			fileManager.upload(files);
		} catch (IOException e) {
			log.error("Error occured to uploading files.");
		}
    }

    @GetMapping(value = "/download")
    public void downloadFile(HttpServletResponse response, @RequestParam(value = "fileName") String fileName) {
        OutputStream os;

        try {
            os = response.getOutputStream();

            int dataSize = fileManager.download(os, fileName);

            String mimeType = URLConnection.guessContentTypeFromName(fileName);
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + fileName + "\""));
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength(dataSize);

            os.flush();
            os.close();
        } catch (UnsupportedEncodingException e) {
            log.error("Error occured encoding the file name.");
        } catch (IOException e) {
            log.error("Error occured downloading the file.");
        }
    }

    @PostMapping(value = "/delete")
    public void deleteFile(@RequestParam(value = "fileName") String fileName) {
        try {
            fileManager.delete(fileName);
        } catch (FileNotFoundException e) {
            log.warn("File is not founded.");
        }
    }
    
    @GetMapping(value = "/rename")
    public void renameFile(@RequestParam(value = "sourceFileName") String sourceFileName, @RequestParam(value = "targetFileName") String targetFileName) {
        try {
			fileManager.rename(sourceFileName, targetFileName);
		} catch (FileAlreadyExistsException e) {
            log.error("Duplicate file name on target.");
        } catch (FileNotFoundException e) {
            log.error("File is not founded.");
        }
    }

}