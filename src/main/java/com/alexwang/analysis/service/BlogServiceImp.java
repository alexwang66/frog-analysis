package com.alexwang.analysis.service;

import com.alexwang.analysis.NotFoundException;
import com.alexwang.analysis.dao.AnalysisRepository;
import com.alexwang.analysis.po.Analysis;
import com.alexwang.analysis.po.Type;
import com.alexwang.analysis.util.MarkdownUtils;
import com.alexwang.analysis.vo.BlogQuery;
import com.alexwang.analysis.util.MyBeanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.*;


@Service
public class BlogServiceImp implements BlogService {


    @Autowired
    private AnalysisRepository blogRepository;


    @Override
    public Analysis getBlog(Long id) {
        return blogRepository.getOne(id);
    }

    @Transactional
    @Override
    public Analysis getAndConvert(Long id) {
        Analysis analysis = blogRepository.getOne(id);
        if (analysis == null) {
            throw new NotFoundException("该博客不存在");
        }
        Analysis b = new Analysis();
        BeanUtils.copyProperties(analysis,b);
        String content = b.getContent();
        b.setContent(MarkdownUtils.markdownToHtmlExtensions(content));

        blogRepository.updateViews(id);
        return b;
    }

    @Override
    public Page<Analysis> listBlog(Pageable pageable, BlogQuery blog) {
        return blogRepository.findAll(new Specification<Analysis>() {
            @Override
            public Predicate toPredicate(Root<Analysis> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (!"".equals(blog.getTitle()) && blog.getTitle() != null) {
                    predicates.add(cb.like(root.<String>get("title"), "%"+blog.getTitle()+"%"));
                }
                if (blog.getTypeId() != null) {
                    predicates.add(cb.equal(root.<Type>get("type").get("id"), blog.getTypeId()));
                }
                if (blog.isRecommend()) {
                    predicates.add(cb.equal(root.<Boolean>get("recommend"), blog.isRecommend()));
                }
                cq.where(predicates.toArray(new Predicate[predicates.size()]));
                return null;
            }
        },pageable);
    }

    @Override
    public Page<Analysis> listBlog(Pageable pageable) {
        return blogRepository.findAll(pageable);
    }

    @Override
    public Page<Analysis> listBlog(Long tagId, Pageable pageable) {
        return blogRepository.findAll(new Specification<Analysis>() {
            @Override
            public Predicate toPredicate(Root<Analysis> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Join join = root.join("tags");
                return cb.equal(join.get("id"),tagId);
            }
        },pageable);
    }

    @Override
    public Page<Analysis> listBlog(String query, Pageable pageable) {
        return blogRepository.findByQuery(query,pageable);
    }

    @Override
    public List<Analysis> listRecommendBlogTop(Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC,"updateTime");
        Pageable pageable = new PageRequest(0, size, sort);
        return blogRepository.findTop(pageable);
    }

    @Override
    public Map<String, List<Analysis>> archiveBlog() {
        List<String> years = blogRepository.findGroupYear();
        Map<String, List<Analysis>> map = new HashMap<>();
        for(String year : years){
            map.put(year,blogRepository.findByYear(year));
        }
        return map;
    }

    @Override
    public Long countBlog() {
        return blogRepository.count();
    }

    @Transactional
    @Override
    public Analysis saveBlog(Analysis analysis) {
        if (analysis.getId() == null) {
            //新增blog
            analysis.setCreateTime(new Date());
            analysis.setUpdateTime(new Date());
            analysis.setViews(0);
        } else {
            //修改blog
            analysis.setUpdateTime(new Date());
        }
        return blogRepository.save(analysis);
    }

    @Transactional
    @Override
    public Analysis updateBlog(Long id, Analysis analysis) {
        Analysis b = blogRepository.getOne(id);
        if (b == null) {
            throw new NotFoundException("该博客不存在");
        }
        BeanUtils.copyProperties(analysis,b, MyBeanUtils.getNullPropertyNames(analysis));
        b.setUpdateTime(new Date());
        return blogRepository.save(b);
    }

    @Transactional
    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }

}
