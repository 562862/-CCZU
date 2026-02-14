package com.cczu.wuxin.controller;

import com.cczu.wuxin.entity.Competition;
import com.cczu.wuxin.service.CompetitionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CompetitionController {

    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/competitions")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(competitionService.getList(keyword, startDate, endDate, college, category, level, page, size));
    }

    @GetMapping("/competitions/{id}")
    public ResponseEntity<Competition> detail(@PathVariable Long id) {
        Competition comp = competitionService.getById(id);
        if (comp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comp);
    }

    @GetMapping("/colleges")
    public ResponseEntity<List<String>> colleges() {
        return ResponseEntity.ok(competitionService.getColleges());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(competitionService.getCategories());
    }

    @GetMapping("/levels")
    public ResponseEntity<List<String>> levels() {
        return ResponseEntity.ok(competitionService.getLevels());
    }
}
