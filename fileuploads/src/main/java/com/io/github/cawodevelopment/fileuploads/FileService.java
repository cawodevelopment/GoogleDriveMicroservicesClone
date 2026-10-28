package com.io.github.cawodevelopment.fileuploads;

import com.io.github.cawodevelopment.fileuploads.dto.FileRequest;
import com.io.github.cawodevelopment.fileuploads.dto.FileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Page<FileResponse> getAllFiles(Pageable pageable) {

    }

    public FileResponse getFileById(Long id) {

    }

    public FileResponse createFile(FileRequest request) {

    }

    public FileResponse updateFileById(Long id, FileRequest request) {

    }

    public void deleteFileById(Long id) {

    }
}
