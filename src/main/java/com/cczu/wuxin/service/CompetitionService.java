package com.cczu.wuxin.service;

import com.cczu.wuxin.entity.Competition;
import com.cczu.wuxin.mapper.CompetitionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompetitionService {

    private final CompetitionMapper competitionMapper;

    public CompetitionService(CompetitionMapper competitionMapper) {
        this.competitionMapper = competitionMapper;
    }

    public Map<String, Object> getList(String keyword, LocalDate startDate, LocalDate endDate,
                                       String college, String category, String level, int page, int size) {
        int offset = (page - 1) * size;
        List<Competition> list = competitionMapper.selectList(keyword, startDate, endDate, college, category, level, offset, size);
        int total = competitionMapper.countList(keyword, startDate, endDate, college, category, level);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public Competition getById(Long id) {
        return competitionMapper.selectById(id);
    }

    public List<String> getColleges() {
        return competitionMapper.selectColleges();
    }

    public List<String> getCategories() {
        return competitionMapper.selectCategories();
    }

    public List<String> getLevels() {
        return competitionMapper.selectLevels();
    }
}
