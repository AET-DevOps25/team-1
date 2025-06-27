package de.tum.devops.application.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PagedResponseDto<T> {

    private List<T> content;
    private PageInfoDto pageInfo;

    public PagedResponseDto(Page<T> page) {
        this.content = page.getContent();
        Sort.Order order = page.getSort().stream().findFirst().orElse(null);
        String sortBy = order != null ? order.getProperty() : null;
        String sortOrder = order != null ? order.getDirection().name() : null;
        this.pageInfo = new PageInfoDto(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                sortBy,
                sortOrder
        );
    }

    // Getters and Setters

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageInfoDto getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfoDto pageInfo) {
        this.pageInfo = pageInfo;
    }
}
