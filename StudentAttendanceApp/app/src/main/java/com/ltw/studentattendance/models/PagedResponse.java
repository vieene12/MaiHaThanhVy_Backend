package com.ltw.studentattendance.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PagedResponse<T> {
    @SerializedName("items")
    private List<T> items;

    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("pageSize")
    private int pageSize;

    public PagedResponse() {}

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
