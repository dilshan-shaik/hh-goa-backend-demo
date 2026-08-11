package com.hh_goa.goa_hh.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hh_goa.goa_hh.Service.FrameStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/frames")
@CrossOrigin(origins = "http://localhost:3000")
public class FrameController {

    private final Cloudinary cloudinary;
    private final FrameStore frameStore;
    private final Path uploadDirectory = Paths.get("uploads");

    public FrameController(
            Cloudinary cloudinary,
            FrameStore frameStore
    ) {
        this.cloudinary = cloudinary;
        this.frameStore = frameStore;
        try {
            Files.createDirectories(uploadDirectory);
        } catch (Exception ignored) {
        }
    }

    @PostMapping
    public ResponseEntity<?> uploadFrame(
            @RequestParam("image") MultipartFile image
    ) {
        long startTime = System.currentTimeMillis();

        try {
            if (image.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("Image is empty");
            }

            String frameId = UUID.randomUUID().toString().substring(0, 8);

            // Fast In-Memory Image Compression & Optimization
            byte[] imageBytes = compressImage(image);

            // Save locally for instant local share serving
            Path localFilePath = uploadDirectory.resolve(frameId + ".png");
            Files.write(localFilePath, imageBytes);

            // Fast Cloudinary Upload with auto-formatting and auto-quality
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                            "public_id", "hh-goa/" + frameId,
                            "resource_type", "image",
                            "overwrite", false,
                            "quality", "auto:good",
                            "fetch_format", "auto"
                    )
            );

            String imageUrl = uploadResult.get("secure_url").toString();

            // Store ID -> Cloudinary URL in fast memory cache
            frameStore.save(frameId, imageUrl);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Frame processed and uploaded in " + duration + " ms. Frame ID: " + frameId);

            return ResponseEntity.ok(
                    new FrameResponse(
                            frameId,
                            imageUrl
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body("Failed to upload image: " + e.getMessage());
        }
    }

    private byte[] compressImage(MultipartFile file) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                return file.getBytes();
            }

            int maxWidth = 1200;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width > maxWidth) {
                height = (int) (((double) maxWidth / width) * height);
                width = maxWidth;
            } else {
                // If small enough, write back directly for maximum speed
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(originalImage, "png", baos);
                return baos.toByteArray();
            }

            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            try {
                return file.getBytes();
            } catch (Exception ex) {
                return new byte[0];
            }
        }
    }

    public record FrameResponse(
            String id,
            String imageUrl
    ) {
    }
}