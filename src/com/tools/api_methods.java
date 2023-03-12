package com.tools;

public class api_methods {

        //User methods
        String GET_USER_DATA = "deezer.getUserData";

        // Song info
        String SONG_GET_DATA = "song.getData";
        String PAGE_SEARCH = "deezer.pageSearch";
        String PAGE_TRACK = "deezer.pageTrack";
        String SONG_GET_LIST_DATA = "song.getListData";
        String SONG_LYRICS = "song.getLyrics";

        // Album info
        String ALBUM_GET_DATA = "album.getData";
        String ALBUM_TRACKS = "song.getListByAlbum";

        // Artist info
        String PAGE_ARTIST = "deezer.pageArtist";
        String ARTIST_DISCOGRAPHY = "album.getDiscography";
        String ARTIST_TOP_TRACKS = "artist.getTopTrack";

        //# Playlist info
        String PAGE_PLAYLIST = "deezer.pagePlaylist";
        String PLAYLIST_TRACKS = "playlist.getSongs";

        // Search Methods
        String GET_SUGGESTED_QUERIES = "search_getSuggestedQueries";
        String SEARCH_TRACK = "search/track";
        String SEARCH_PLAYLIST = "search/playlist";
        String SEARCH_ALBUM = "search/album";
        String SEARCH_ARTIST = "search/artist";


}
