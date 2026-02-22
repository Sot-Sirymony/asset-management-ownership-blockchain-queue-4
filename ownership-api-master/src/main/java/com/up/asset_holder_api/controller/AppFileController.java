package com.up.asset_holder_api.controller;

import com.up.asset_holder_api.model.entity.FileUpload;
import com.up.asset_holder_api.model.response.FileUploadResponse;
import com.up.asset_holder_api.service.AppFileService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin

public class AppFileController {
    private final AppFileService appFileService;
    public AppFileController(AppFileService appFileService) {
        this.appFileService = appFileService;
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "upload image")
    public ResponseEntity<?> postImage(@RequestParam MultipartFile file) throws IOException {
        String fileName = appFileService.saveFile(file);
        String fileUrl = "http://165.22.99.91:8081/api/v1/files?fileName=" + fileName;
        FileUpload fileResponse = new FileUpload(fileName,fileUrl,file.getContentType(),file.getSize());
        FileUploadResponse<FileUpload> response = FileUploadResponse.<FileUpload>builder()
                .message("successfully uploaded file")
                .httpStatus(HttpStatus.OK)
                .payload(fileResponse).build();
        return ResponseEntity.ok(response);
    }
    @GetMapping()
    public ResponseEntity<?> getFileOrList(@RequestParam(required = false) String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            List<String> names = appFileService.listFileNames();
            FileUploadResponse<List<String>> response = FileUploadResponse.<List<String>>builder()
                    .message("success")
                    .httpStatus(HttpStatus.OK)
                    .payload(names)
                    .build();
            return ResponseEntity.ok(response);
        }
        Resource resource = appFileService.getFileByFileName(fileName.trim());
        MediaType mediaType;
        if (fileName.endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") ||
                fileName.endsWith(".gif")) {
            mediaType = MediaType.IMAGE_PNG;
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(mediaType).body(resource);
    }
}