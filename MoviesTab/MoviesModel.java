package MoviesTab;

import java.util.ArrayList;

public class MoviesModel {

    // movie elements
    private String movieTitle;
    private String movieRating;
    private String movieGenre;
    private String movieRuntime;
    private String movieTimesStr;

    /**
     * Constructor for a MoviesModel object.
     * @param movieTitle title of this movie
     * @param movieRating rating of this movie
     * @param movieRuntime runtime of this movie
     * @param movieTimes show times of this movie
     */
    public MoviesModel(String movieTitle, String movieRating, String movieRuntime, String movieGenre, String movieTimes) {
        this.movieTitle = movieTitle;
        this.movieRating = movieRating;
        this.movieGenre = movieGenre;
        this.movieRuntime = movieRuntime;
        this.movieTimesStr = movieTimes;
    }

    /**
     * Get method for this movie's title.
     * @return movieTitle
     */
    public String getMovieTitle() {
        return movieTitle;
    }

    /**
     * Get method for this movie's rating.
     * @return movieRating
     */
    public String getMovieRating() {
        return movieRating;
    }

    /**
     * Get method for this movie's runtime
     * @return
     */
    public String getMovieRuntime() {
        return movieRuntime;
    }

    /**
     * Get method for this movie's genre
     * @return
     */
    public String getMovieGenre() {
        return movieGenre;
    }

    /**
     * Get method for this movie's show times
     * @return moviesTimesStr
     */
    public String getMovieTimesStr() {
        movieTimesStr = movieTimesStr.replace("[", "");
        movieTimesStr = movieTimesStr.replace("]", "");
        movieTimesStr = movieTimesStr.replace("\"", "");
        movieTimesStr = movieTimesStr.replace(",", " / ");

        return movieTimesStr;
    }


}
