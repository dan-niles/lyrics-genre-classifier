package org.danniles.api.controller;

import org.danniles.api.service.LyricsService;
import org.danniles.driver.GenrePrediction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lyrics")
@CrossOrigin(origins = "*")
public class LyricsController {

    @Autowired
    private LyricsService lyricsService;

    @RequestMapping(value = "/train", method = RequestMethod.GET)
    ResponseEntity<Map<String, Object>> trainLyricsModel() {
        Map<String, Object> trainStatistics = lyricsService.classifyLyrics();

        return new ResponseEntity<>(trainStatistics, HttpStatus.OK);
    }

    @RequestMapping(value = "/predict", method = RequestMethod.POST)
    ResponseEntity<GenrePrediction> predictGenre(@RequestBody String unknownLyrics) {
        GenrePrediction genrePrediction = lyricsService.predictGenre(unknownLyrics);

        return new ResponseEntity<>(genrePrediction, HttpStatus.OK);
    }
}
