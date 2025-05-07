package org.danniles.controller;

import org.danniles.service.Word2VecService;
import org.danniles.word2vec.Similarity;
import org.danniles.word2vec.Synonym;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/word-to-vec")
public class Word2VecController {

    private final Word2VecService word2VecService;

    // Constructor injection
    public Word2VecController(Word2VecService word2VecService) {
        this.word2VecService = word2VecService;
    }

    @RequestMapping(value = "/train", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> trainWord2VecModel() {
        Map<String, Object> trainStatistics = word2VecService.train();
        return new ResponseEntity<>(trainStatistics, HttpStatus.OK);
    }

    @RequestMapping(value = "/synonyms", method = RequestMethod.POST)
    public ResponseEntity<List<Synonym>> findSynonyms(@RequestBody String lyrics) {
        List<Synonym> synonyms = word2VecService.findSynonyms(lyrics);
        return new ResponseEntity<>(synonyms, HttpStatus.OK);
    }

    @RequestMapping(value = "/similarity", method = RequestMethod.POST)
    public ResponseEntity<List<Similarity>> calculateSimilarity(@RequestBody String lyrics) {
        List<Similarity> similarities = word2VecService.calculateSimilarity(lyrics);
        return new ResponseEntity<>(similarities, HttpStatus.OK);
    }
}
