package com.io.github.cawodevelopment.fileuploads;

import com.io.github.cawodevelopment.fileuploads.dto.FileRequest;
import com.io.github.cawodevelopment.fileuploads.dto.FileResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @GetMapping
    public ResponseEntity<Page<FileResponse>> getAllFiles(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fileService.getAllFiles(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFileById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fileService.getFileById(id));
    }

    @PostMapping
    public ResponseEntity<FileResponse> createFile(@Valid FileRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fileService.createFile(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileResponse> updateFileById(@PathVariable Long id, @Valid FileRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fileService.updateFileById(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> deleteFileById(@PathVariable Long id) {
        fileService.deleteFileById(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
