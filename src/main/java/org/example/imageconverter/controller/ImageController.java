package org.example.imageconverter.controller;

import org.example.imageconverter.service.ImageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/convert")
public class ImageController {
    private final ImageProcessingService imageProcessingService;
    private final Logger logger = LoggerFactory.getLogger(ImageController.class);

    public ImageController(ImageProcessingService imageProcessingService) {
        this.imageProcessingService = imageProcessingService;
    }

    @PostMapping("/blackwhite")
    public ResponseEntity<Map<String, Object>> convertToBlackWhite(@RequestParam("file") MultipartFile file) {
        logger.info("Received conversion request for file: {}", file.getOriginalFilename());
        logger.info("File size: {} bytes", file.getSize());
        if (file == null || file.isEmpty()) {
            logger.error("No file provided or file is empty");
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "No file provided or file is empty"
                    ));
        }

        try {
            logger.info("Processing file: {}", file.getOriginalFilename());
            String convertedImageUrl = imageProcessingService.uploadAndConvertToBlackAndWhite(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("convertedImageUrl", convertedImageUrl);
            response.put("originalFilename", file.getOriginalFilename());

            logger.info("Successfully converted image to B&W: {}", convertedImageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Failed to convert image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Image conversion failed: " + e.getMessage()
                    ));
        }
    }
}