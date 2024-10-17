package com.alexwang.analysis.service;

import com.alexwang.analysis.po.Analysis;
import com.alexwang.analysis.vo.BlogQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BlogService {

    Analysis getBlog(Long id);

    Analysis getAndConvert(Long id);

    Page<Analysis> listBlog(Pageable pageable, BlogQuery blog);

    Page<Analysis> listBlog(Pageable pageable);

    Page<Analysis> listBlog(Long tagId, Pageable pageable);

    Page<Analysis> listBlog(String query, Pageable pageable);

    List<Analysis> listRecommendBlogTop(Integer size);

    Map<String, List<Analysis>> archiveBlog();

    Long countBlog();

    Analysis saveBlog(Analysis analysis);

    Analysis updateBlog(Long id, Analysis analysis);

    void deleteBlog(Long id);
}
