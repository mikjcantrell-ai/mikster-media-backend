package com.mikstermedia.dto;

import java.util.List;

public class YouTubeSearchPage {
    private List<YouTubeSearchResult> items;
    private int total;
    private String nextPageToken;
    private boolean hasMore;

    public YouTubeSearchPage() {}
    public YouTubeSearchPage(List<YouTubeSearchResult> items, int total, String nextPageToken, boolean hasMore) {
        this.items = items;
        this.total = total;
        this.nextPageToken = nextPageToken;
        this.hasMore = hasMore;
    }

    public List<YouTubeSearchResult> getItems() { return items; }
    public void setItems(List<YouTubeSearchResult> items) { this.items = items; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public String getNextPageToken() { return nextPageToken; }
    public void setNextPageToken(String nextPageToken) { this.nextPageToken = nextPageToken; }
    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
}
