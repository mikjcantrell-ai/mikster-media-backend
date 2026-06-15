package com.mikstermedia.dto;

import java.util.List;

public class SpotifySearchPage {
    private List<SpotifySearchResult> items;
    private int total;
    private int offset;
    private int limit;

    public SpotifySearchPage() {}

    public SpotifySearchPage(List<SpotifySearchResult> items, int total, int offset, int limit) {
        this.items  = items;
        this.total  = total;
        this.offset = offset;
        this.limit  = limit;
    }

    public List<SpotifySearchResult> getItems()  { return items; }
    public void setItems(List<SpotifySearchResult> items) { this.items = items; }
    public int getTotal()   { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getOffset()  { return offset; }
    public void setOffset(int offset) { this.offset = offset; }
    public int getLimit()   { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
