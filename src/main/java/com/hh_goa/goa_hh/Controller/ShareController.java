package com.hh_goa.goa_hh.Controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/share")
@CrossOrigin(origins = "*")
public class ShareController {

    private final Path uploadDirectory =
            Paths.get("uploads");

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFrame(
            @PathVariable String id
    ) {

        try {

            Path filePath =
                    uploadDirectory.resolve(id + ".png");

            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists()
                    || !resource.isReadable()) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            return ResponseEntity
                    .ok()
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);

        } catch (MalformedURLException e) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }
}