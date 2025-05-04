package com.astra.load_backend.controller;

import com.astra.load_backend.domain.model.FileUse;
import com.astra.load_backend.domain.port.in.FileUseIn;
import com.astra.load_backend.domain.port.out.FileUseOutPort;
import com.astra.load_backend.dto.EmailRequest;
import com.astra.load_backend.dto.FileProcessingResponse;
import com.astra.load_backend.dto.SegmentResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mail.javamail.JavaMailSender;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/archives")
public class LoadFileController {
    private final FileUseIn fileUseIn;
    private final FileUseOutPort fileUseOutPort;

    @Autowired
    private JavaMailSender mailSender;

    public LoadFileController(FileUseIn fileUseIn, FileUseOutPort fileUseOutPort) {
        this.fileUseIn = fileUseIn;
        this.fileUseOutPort = fileUseOutPort;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileProcessingResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("partSizes") List<Integer> partSizes
    ) throws IOException {

        List<FileUse> segments = fileUseIn.splitFile(file, partSizes);

        List<SegmentResponse> segmentResponses = segments.stream().map(segment ->
                new SegmentResponse(
                        segment.getName(),
                        "http://localhost:8080/files/" + segment.getName(),
                        segment.getContent().length / 1024
                )
        ).collect(Collectors.toList());

        FileProcessingResponse response = new FileProcessingResponse(
                file.getOriginalFilename(),
                segmentResponses.size(),
                segmentResponses
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/segments")
    public ResponseEntity<List<String>> getSegments(@RequestParam String fileName) {
        List<FileUse> segments = fileUseOutPort.getSegments(fileName);
        List<String> segmentNames = segments.stream()
                .map(FileUse::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(segmentNames);
    }

    @PostMapping("/send-email")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request) throws MessagingException {
        String email = request.getEmail();
        String fileName = request.getOriginalFileName();
        List<String> segmentNames = request.getSegments().stream()
                .map(SegmentResponse::getName)
                .toList();

        List<FileUse> segments = fileUseOutPort.getSegments(fileName).stream()
                .filter(s -> segmentNames.contains(s.getName()))
                .toList();

        for (FileUse segment : segments) {
            if ((segment.getContent().length / 1024) > 10 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Archivo demasiado grande para enviar por correo");
                return ResponseEntity.badRequest().body(error);            }
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Segmentos del archivo " + fileName);
        helper.setText("Adjuntamos los segmentos solicitados.");

        for (FileUse segment : segments) {
            helper.addAttachment(segment.getName(), new ByteArrayResource(segment.getContent()));
        }

        mailSender.send(message);
        return ResponseEntity.ok(Map.of("message", "Correo enviado correctamente."));
    }

    @PostMapping("/reconstruct-from-parts")
    public ResponseEntity<ByteArrayResource> reconstructFromParts(@RequestParam("parts") List<MultipartFile> parts) throws IOException {
        List<FileUse> segments = new ArrayList<>();

        for (MultipartFile part : parts) {
            byte[] content = part.getBytes();
            String filename = part.getOriginalFilename();
            int index = Integer.parseInt(filename.substring(filename.lastIndexOf('.') + 1)); // ".0", ".1", etc.

            FileUse fileUse = new FileUse(filename, content, index);
            segments.add(fileUse);
        }

        segments.sort(Comparator.comparingInt(FileUse::getIndex));
        byte[] reconstructed = fileUseIn.reconstructFile(segments);
        String originalName = segments.get(0).getName().split("\\.")[0];

        ByteArrayResource resource = new ByteArrayResource(reconstructed);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalName + "\"")
                .body(resource);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadSegment(@PathVariable String fileName) {
        byte[] data = fileUseOutPort.getSegmentBytes(fileName);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }



    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }


}