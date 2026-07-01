package com.careerpilot.careerpilot.resume.service.impl;

import com.careerpilot.careerpilot.auth.entity.User;
import com.careerpilot.careerpilot.auth.repository.UserRepository;
import com.careerpilot.careerpilot.config.FileStorageProperties;
import com.careerpilot.careerpilot.resume.dto.ResumeUploadResponse;
import com.careerpilot.careerpilot.resume.entity.Resume;
import com.careerpilot.careerpilot.resume.entity.ResumeStatus;
import com.careerpilot.careerpilot.resume.repository.ResumeRepository;
import com.careerpilot.careerpilot.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final FileStorageProperties fileStorageProperties;

    @Override
    public ResumeUploadResponse upload(MultipartFile file, String userEmail) {
        validateFile(file);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
        Path filePath = uploadPath.resolve(storedFileName);

        try {
            Files.createDirectories(uploadPath);
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        Resume resume = Resume.builder()
                .user(user)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .storedPath(filePath.toString())
                .status(ResumeStatus.PROCESSING)
                .build();

        Resume saved = resumeRepository.save(resume);

        extractTextFromPdf(filePath, saved);

        return ResumeUploadResponse.builder()
                .id(saved.getId())
                .originalFileName(saved.getOriginalFileName())
                .status(saved.getStatus())
                .message("Resume uploaded and text extracted successfully")
                .uploadedAt(saved.getUploadedAt())
                .build();
    }

    @Override
    public List<ResumeUploadResponse> getMyResumes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return resumeRepository.findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(r -> ResumeUploadResponse.builder()
                        .id(r.getId())
                        .originalFileName(r.getOriginalFileName())
                        .status(r.getStatus())
                        .uploadedAt(r.getUploadedAt())
                        .build())
                .toList();
    }

    private String extractTextFromPdf(Path filePath, Resume resume) {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            String text = new PDFTextStripper().getText(document);
            resume.setExtractedText(text);
            resume.setStatus(ResumeStatus.PROCESSED);
            resume.setProcessedAt(LocalDateTime.now());
            resumeRepository.save(resume);
            return text;
        } catch (IOException e) {
            log.error("PDF text extraction failed for resume id={}", resume.getId(), e);
            resume.setStatus(ResumeStatus.FAILED);
            resumeRepository.save(resume);
            return null;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }
        long maxBytes = (long) fileStorageProperties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File size exceeds the " + fileStorageProperties.getMaxSizeMb() + "MB limit");
        }
    }
}
