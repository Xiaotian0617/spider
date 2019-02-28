package com.al.dbspider.plugin;

/**
 * Create on 2017/10/24.
 *
 * @author Asin Liu
 */
public class Page {

    public Page() {
    }

    public Page(int reqPage) {
        this.currentPage = reqPage;
    }

    public Page(int reqPage, int pageSize) {
        this.currentPage = reqPage;
        this.pageSize = pageSize;
    }

    // Current page of the request.
    private int currentPage;

    // Page size of the request.
    private int pageSize = 20;

    // Calculated the index of row count.
    private int index;

    // Total row count of the query result.
    private long rowCount;

    // Calculated total page of the query result.
    private long pageCount;


    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage < 1 || currentPage > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("currentPage must be greater than 1 and less than " + Integer.MAX_VALUE);
        }
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1 || pageSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("pageSize must be greater than 1 and less than " + Integer.MAX_VALUE);
        }
        this.pageSize = pageSize;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
        calculate();
    }

    public long getPageCount() {
        return pageCount;
    }


    private void calculate() {
        index = (currentPage - 1) * pageSize;
        pageCount = (rowCount + pageSize - 1) / pageSize;
    }
}
