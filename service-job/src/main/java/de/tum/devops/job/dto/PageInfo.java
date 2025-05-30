package de.tum.devops.job.dto;

/**
 * Pagination information for paginated responses
 */
public class PageInfo {

    private int page;
    private int size;
    private long total;
    private int totalPages;

    // Constructors
    public PageInfo() {
    }

    public PageInfo(int page, int size, long total, int totalPages) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}