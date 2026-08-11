package com.hh_goa.goa_hh.Service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FrameStore {

    private final Map<String, String> frames =
            new ConcurrentHashMap<>();

    public void save(
            String id,
            String imageUrl
    ) {
        frames.put(id, imageUrl);
    }

    public String getImageUrl(String id) {
        return frames.get(id);
    }
}