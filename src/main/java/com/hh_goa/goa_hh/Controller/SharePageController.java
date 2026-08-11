package com.hh_goa.goa_hh.Controller;

import com.hh_goa.goa_hh.Service.FrameStore;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/share")
@CrossOrigin(origins = "*")
public class SharePageController {

    private final FrameStore frameStore;
    private final Path uploadDirectory = Paths.get("uploads");

    public SharePageController(FrameStore frameStore) {
        this.frameStore = frameStore;
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> sharePage(
            @PathVariable String id
    ) {
        // Fast In-Memory Lookup first
        String imageUrl = frameStore.getImageUrl(id);

        if (imageUrl == null || imageUrl.isEmpty()) {
            Path imagePath = uploadDirectory.resolve(id + ".png");
            if (Files.exists(imagePath)) {
                imageUrl = "http://localhost:8080/uploads/" + id + ".png";
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        String shareUrl = "http://localhost:8080/share/" + id;

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>HH Goa 2026 — FrameInGoa</title>
                    <meta name="description" content="Check out my HH Goa 2026 frame!">
                    
                    <!-- OPEN GRAPH -->
                    <meta property="og:type" content="website">
                    <meta property="og:title" content="HH Goa 2026 — FrameInGoa">
                    <meta property="og:description" content="I created my HH Goa 2026 frame!">
                    <meta property="og:image" content="%s">
                    <meta property="og:url" content="%s">

                    <!-- X / TWITTER -->
                    <meta name="twitter:card" content="summary_large_image">
                    <meta name="twitter:title" content="HH Goa 2026 — FrameInGoa">
                    <meta name="twitter:description" content="Check out my HH Goa 2026 frame!">
                    <meta name="twitter:image" content="%s">

                    <style>
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 30px;
                            background: #08090d;
                            color: white;
                            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
                            text-align: center;
                        }
                        .container { width: 100%%; max-width: 500px; }
                        img {
                            width: 100%%;
                            max-width: 400px;
                            display: block;
                            margin: 0 auto;
                            border-radius: 20px;
                            box-shadow: 0 30px 80px rgba(0, 0, 0, 0.6);
                            transition: transform 0.3s ease;
                        }
                        img:hover { transform: scale(1.02); }
                        h1 { margin-top: 25px; font-size: 28px; letter-spacing: 0.5px; }
                        p { color: #ff9a69; font-size: 14px; font-weight: 700; letter-spacing: 1px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <img src="%s" alt="HH Goa 2026 Frame" loading="eager">
                        <h1>HH Goa 2026</h1>
                        <p>#FrameInGoa</p>
                    </div>
                </body>
                </html>
                """.formatted(imageUrl, shareUrl, imageUrl, imageUrl);

        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}