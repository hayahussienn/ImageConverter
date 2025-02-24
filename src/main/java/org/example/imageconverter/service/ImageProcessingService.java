package org.example.imageconverter.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageProcessingService {

    private final Cloudinary cloudinary;
    private final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

    // Read Cloudinary credentials from application.properties
    public ImageProcessingService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadAndConvertToBlackAndWhite(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided or file is empty");
        }

        try {
            // Get original filename and extension
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String uniqueFilename = "bw_" + UUID.randomUUID().toString() + extension;

            logger.info("Processing file: {} -> {}", originalFilename, uniqueFilename);

            // Upload with direct B&W transformation
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", uniqueFilename,
                    "folder", "converted_images",
                    "transformation", new Transformation().effect("grayscale")
            ));

            String convertedImageUrl = (String) uploadResult.get("secure_url");

            if (convertedImageUrl == null) {
                throw new IOException("Failed to get converted image URL from Cloudinary");
            }

            logger.info("Successfully converted image. URL: {}", convertedImageUrl);
            return convertedImageUrl;

        } catch (IOException e) {
            logger.error("Failed to process image", e);
            throw new IOException("Failed to process image: " + e.getMessage());
        }
    }
}
