package kr.co.cofile.sbbfile.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.cofile.sbbfile.dto.ErrorResponse;
import kr.co.cofile.sbbfile.exception.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/file")
public class FileUploadController {
    @Value("${file.upload.directory}")
    private String uploadDirectory;

    // 파일 업로드 양식
    @GetMapping("/fileform")
    public String fileForm() {
        return "file-form";
    }

    // 단일 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 파일명 충돌 방지를 위한 고유 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            // 파일 저장
            Path filePath = Paths.get(uploadDirectory, uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("File uploaded successfully: " + uniqueFilename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

    // 다중 파일 업로드
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<String>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDirectory, uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                uploadedFiles.add(uniqueFilename);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(List.of("Failed to upload files: " + e.getMessage()));
            }
        }

        return ResponseEntity.ok(uploadedFiles);
    }

    // 파일 다운로드
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
//                throw new RuntimeException("Could not read the file!");
                throw new FileNotFoundException("File not found: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }


    // 예외 처리 메서드
    @ExceptionHandler(FileNotFoundException.class)
    public String handleFileNotFoundException(
            FileNotFoundException e,
            Model model,
            HttpServletRequest request) {

        log.error("파일 다운로드 오류: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                "FILE_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("error", errorResponse);

        return "error/error";  // error.html 템플릿을 사용
    }

}