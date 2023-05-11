package models;

public class Search {
    private final int count;
    private final int page;
    private final Integer year;
    private final String title;
    private final String starName;
    private final String director;
    private final Integer genreId;
    private final String sortBy;
    private final String advanced;

    public Search(int count, int page, Integer year, String title, String starName, String director, Integer genreId, String sortBy, String advanced) {
        this.count = count;
        this.page = page;
        this.year = year;
        this.title = title;
        this.starName = starName;
        this.director = director;
        this.genreId = genreId;
        this.sortBy = sortBy;
        this.advanced = advanced;
    }

    public Integer getYear() {
        return year;
    }

    public int getCount() {
        return count;
    }

    public int getPage() {
        return page;
    }

    public String getTitle() {
        return title;
    }

    public String getStarName() {
        return starName;
    }

    public String getDirector() {
        return director;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getAdvanced() {
        return advanced;
    }
}
