package com.alexwang.analysis.dao;

import com.alexwang.analysis.po.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long>, JpaSpecificationExecutor<Analysis> {

    @Query("select b from Analysis b where b.recommend = true")
    List<Analysis> findTop(Pageable pageable);

    @Query("select b from Analysis b where b.title like ?1 or b.content like ?1")
    Page<Analysis> findByQuery(String query, Pageable pageable);

    @Transactional
    @Modifying
    @Query("update Analysis b set b.views = b.views+1 where b.id = ?1")
    int updateViews(Long id);

    @Query("select function('date_format', b.updateTime, '%Y') as year from Analysis b group by function('date_format', b.updateTime, '%Y') order by year desc")
    List<String> findGroupYear();

    @Query("select b from Analysis b where function('date_format',b.updateTime,'%Y') = ?1")
    List<Analysis> findByYear(String year);

}
