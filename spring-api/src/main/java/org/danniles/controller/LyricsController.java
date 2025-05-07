package org.danniles.controller;

import org.danniles.GenrePrediction;
import org.danniles.service.LyricsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lyrics")
public class LyricsController {

    private final LyricsService lyricsService;

    // Constructor injection
    public LyricsController(LyricsService lyricsService) {
        this.lyricsService = lyricsService;
    }

    @GetMapping("/train")
    public ResponseEntity<Map<String, Object>> trainLyricsModel() {
        Map<String, Object> trainStatistics = lyricsService.classifyLyrics();
        return new ResponseEntity<>(trainStatistics, HttpStatus.OK);
    }

    @PostMapping("/predict")
    public ResponseEntity<GenrePrediction> predictGenre(@RequestBody String unknownLyrics) {
        GenrePrediction genrePrediction = lyricsService.predictGenre(unknownLyrics);
        return new ResponseEntity<>(genrePrediction, HttpStatus.OK);
    }
}
