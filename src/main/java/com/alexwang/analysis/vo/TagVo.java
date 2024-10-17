package com.alexwang.analysis.vo;

import com.alexwang.analysis.po.Analysis;
import com.alexwang.analysis.po.Tag;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class TagVo {

    private Long id;
    private String name;

    private List<Analysis> analyses = new ArrayList<>();

    public TagVo() {
    }

    public Tag convertToPo(TagVo tagVo, Tag tag){
        BeanUtils.copyProperties(tagVo, tag);
        return tag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Analysis> getBlogs() {
        return analyses;
    }

    public void setBlogs(List<Analysis> analyses) {
        this.analyses = analyses;
    }

    @Override
    public String toString() {
        return "";
    }
}
