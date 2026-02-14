package com.cczu.wuxin.mapper;

import com.cczu.wuxin.entity.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CompetitionMapper {

    int insert(Competition competition);

    Competition selectById(@Param("id") Long id);

    List<Competition> selectList(@Param("keyword") String keyword,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("college") String college,
                                 @Param("category") String category,
                                 @Param("level") String level,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    int countList(@Param("keyword") String keyword,
                  @Param("startDate") LocalDate startDate,
                  @Param("endDate") LocalDate endDate,
                  @Param("college") String college,
                  @Param("category") String category,
                  @Param("level") String level);

    boolean existsByUrl(@Param("url") String url);

    List<String> selectColleges();

    List<String> selectCategories();

    List<String> selectLevels();
}
