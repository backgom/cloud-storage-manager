package me.backgo.azure.component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import javax.annotation.PostConstruct;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AzureBlobManager implements FileManager {

    @Value("${storage.azure-connection-string}")
    private String connectStr;

    @Value("${storage.azure-container-name}")
    private String containerName;

    BlobServiceClient blobServiceClient;
    BlobContainerClient containerClient;
    
    @PostConstruct
    public void init() {
        // Create a BlobServiceClient object which will be used to get a container client
        this.blobServiceClient = new BlobServiceClientBuilder().connectionString(this.connectStr).buildClient();
        // Get a container client object
        this.containerClient = this.blobServiceClient.getBlobContainerClient(this.containerName);

        log.info("Initializing Azure Blob SDK");
    }

	@Override
	public void upload(List<MultipartFile> files) throws IOException {
		for (MultipartFile file : files) {
            InputStream is = null;

            try {
                is = file.getInputStream();
                long fileSize = file.getSize();
                String fileName = file.getOriginalFilename();

                String normalizedFileName = Normalizer.normalize(fileName, Form.NFC);
                
                // Get a reference to a blob
                BlobClient blobClient = containerClient.getBlobClient(normalizedFileName);

                // Duplicate file name handling
                int prefixNumber = 0;
                while (blobClient.exists()) {
                    prefixNumber++;
                    String newFileName = prefixNumber + "_" + normalizedFileName;
                    blobClient = containerClient.getBlobClient(newFileName);
                }

                // Upload the blob
                blobClient.upload(is, fileSize);

			} catch (IOException e) {
				log.error("Error occurred uploading the file to storage.");
            } finally {
                if(is != null) {
                    is.close();
                }
            }
        }
		
	}

	@Override
	public int download(OutputStream os, String fileName) {
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        int dataSize = (int) blobClient.getProperties().getBlobSize();

        blobClient.download(os);

        return dataSize;
	}

	@Override
	public void delete(String fileName) throws FileNotFoundException {
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        if( !blobClient.exists() ) {
            throw new FileNotFoundException();
        }

        blobClient.delete();
	}

	@Override
	public void rename(String source, String target) throws FileNotFoundException, FileAlreadyExistsException {
		BlobClient sourceBlobClient = containerClient.getBlobClient(source);

        if (!sourceBlobClient.exists()) {
            throw new FileNotFoundException();
        }
    
        BlobClient targetBlobClient = containerClient.getBlobClient(target);
                       
        if (targetBlobClient.exists()) {
            throw new FileAlreadyExistsException(target);
        }

        InputStream sourceInputStream = sourceBlobClient.openInputStream();
        targetBlobClient.upload(sourceInputStream, sourceBlobClient.getProperties().getBlobSize());
        sourceBlobClient.delete();
	}
    
}