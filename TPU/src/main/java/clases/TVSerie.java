package clases;

import java.io.Serializable;

/**
 * Series_Title: título de la serie
 * Runtime_of_Series: años inicial y final (si existe) de emisión
 * Certificate: calificación
 * Runtime_of_Episodes: duración de los episodios
 * Genre: género (una cadena separada por pipes conteniendo varios géneros a los que pertenece la serie)
 * IMDB_Rating: puntuación de los usuarios en el sitio IMDB
 * Overview: argumento
 * Star1: nombre de uno de los protagonistas
 * Star2: nombre de uno de los protagonistas
 * Star3: nombre de uno de los protagonistas
 * Star4: nombre de uno de los protagonistas
 * No_of_Votes: cantidad de votos recibidos
 *
 * Game of Thrones,(2011–2019),A,57 min,Action|Adventure|Drama,9.3,Nine noble families fight for control over the lands of Westeros while an ancient enemy returns after being dormant for millennia.,Emilia Clarke,Peter Dinklage,Kit Harington,Lena Headey,1773458
 */

public class TVSerie implements Serializable {
    private String Series_Title;
    private String Runtime_of_Series;
    private String Certificate;
    private String Runtime_of_Episodes;
    private String Genre;
    private float IMDB_Rating;
    private String Overview;
    private String Star1;
    private String Star2;
    private String Star3;
    private String Star4;
    private int No_of_Votes;

    public TVSerie(String series_Title, String runtime_of_Series, String certificate, String runtime_of_Episodes, String genre, float IMDB_Rating, String overview, String star1, String star2, String star3, String star4, int no_of_Votes) {
        Series_Title = series_Title;
        Runtime_of_Series = runtime_of_Series;
        Certificate = certificate;
        Runtime_of_Episodes = runtime_of_Episodes;
        Genre = genre;
        this.IMDB_Rating = IMDB_Rating;
        Overview = overview;
        Star1 = star1;
        Star2 = star2;
        Star3 = star3;
        Star4 = star4;
        No_of_Votes = no_of_Votes;
    }

    public String getSeries_Title() {
        return Series_Title;
    }

    public void setSeries_Title(String series_Title) {
        Series_Title = series_Title;
    }

    public String getRuntime_of_Series() {
        return Runtime_of_Series;
    }

    public void setRuntime_of_Series(String runtime_of_Series) {
        Runtime_of_Series = runtime_of_Series;
    }

    public String getCertificate() {
        return Certificate;
    }

    public void setCertificate(String certificate) {
        Certificate = certificate;
    }

    public String getRuntime_of_Episodes() {
        return Runtime_of_Episodes;
    }

    public void setRuntime_of_Episodes(String runtime_of_Episodes) {
        Runtime_of_Episodes = runtime_of_Episodes;
    }

    public String getGenre() {
        return Genre;
    }

    public void setGenre(String genre) {
        Genre = genre;
    }

    public float getIMDB_Rating() {
        return IMDB_Rating;
    }

    public void setIMDB_Rating(float IMDB_Rating) {
        this.IMDB_Rating = IMDB_Rating;
    }

    public String getOverview() {
        return Overview;
    }

    public void setOverview(String overview) {
        Overview = overview;
    }

    public String getStar1() {
        return Star1;
    }

    public void setStar1(String star1) {
        Star1 = star1;
    }

    public String getStar2() {
        return Star2;
    }

    public void setStar2(String star2) {
        Star2 = star2;
    }

    public String getStar3() {
        return Star3;
    }

    public void setStar3(String star3) {
        Star3 = star3;
    }

    public String getStar4() {
        return Star4;
    }

    public void setStar4(String star4) {
        Star4 = star4;
    }

    public int getNo_of_Votes() {
        return No_of_Votes;
    }

    public void setNo_of_Votes(int no_of_Votes) {
        No_of_Votes = no_of_Votes;
    }
}
