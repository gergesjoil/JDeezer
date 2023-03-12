
# JDeezer

Is a Java library that allows you to interact with the Deezer API. You can search for tracks, albums, and artists, as well as access information about playlists and users.

JDeezer is a remake of the PyDeezer library for Python, with a similar API and functionality.


## Usage
To use JDeezer in your Java code, you will need to create a Deezer object and use its methods to interact with the Deezer API.
```
import com.tools.Deezer;

public class Main {
    public static void main(String[] args) throws Exception {
        String arl = "Your Arl from cookies!";
        Deezer deezer = new Deezer(arl); //login
        String track_id = "1109737";
        String download_dir = "C:\\Users\\User\\Music";
        deezer.get_track(track_id); //get track Id to download it
        deezer.download_track(download_dir, "MP3_128", null);
        //Search
        JSONObject popularPlaylists = deezer.get_popular_playlists();
        System.out.println(popularPlaylists);
        JSONObject getSuggestedInfo = deezer.SearchByQuery("MONTERO - Call Me By Your Name"); //replace it with the name of the artist or song you want to search for.
        System.out.println(getSuggestedInfo);
```


## Installation
To use JDeezer in your Java project, you will need to add the JDeezer jar file to your project's classpath. You can do this by downloading the latest release from the JDeezer releases page and adding it to your project's dependencies.
