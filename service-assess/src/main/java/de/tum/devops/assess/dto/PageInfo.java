package de.tum.devops.assess.dto;

/**
 * Pagination information
 */
public class PageInfo {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public PageInfo() {
    }

    public PageInfo(int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // Getters and setters
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}