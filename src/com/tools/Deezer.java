package com.tools;

import com.google.common.primitives.Bytes;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class Deezer {
    String arl;
    String token;
    JSONObject user;
    JSONObject track;
    track_formats track_formats = new track_formats();
    api_methods method = new api_methods();
    String postUrl;
    BasicCookieStore cookieStore;
    HttpClient httpClient;
    private boolean should_include_featuring;
    public Deezer(String arl) throws Exception {
        this.arl = arl;
        postUrl = "https://www.deezer.com/ajax/gw-light.php";// put in your url
        this.get_user_data();
    }

    public JSONObject api_data() throws Exception {

        cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("arl", arl);
        cookie.setDomain("deezer.com");
        cookie.setAttribute(cookie.DOMAIN_ATTR, "true");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
        httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        String json = "{" +
                "\"api_version\":\"" + "1.0" + "\", " +
                "\"api_token\":\"" + "null" + "\", " +
                "\"input\":\"" + "3" + "\", " +
                "\"method\":\"" + method.GET_USER_DATA + "\"" +
                "}";
        HttpPost post = new HttpPost(postUrl);
        post.setEntity(new StringEntity(json));
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
        post.setHeader("Content-Language", "en-US");
        post.setHeader("Cache-Control", "max-age=0");
        post.setHeader("Accept", "*/*");
        post.setHeader("Accept-Charset", "utf-8,ISO-8859-1;q=0.7,*;q=0.3");
        post.setHeader("Accept-Language", "en-US,en;q=0.9,en-US;q=0.8,en;q=0.7");
        post.setHeader("Connection", "keep-alive");
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(post);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);
        JSONObject myObject = new JSONObject(result).getJSONObject("results");
        this.token = myObject.getString("checkForm");
        return myObject;
    }
    public JSONObject api_call(String method, JSONObject params) throws Exception {
        if (!method.equals(this.method.GET_USER_DATA)){
            JSONObject api_params = new JSONObject();
            api_params.put("api_version", "1.0").put("api_token", this.token).put("input", "3").put("method", method);
            JSONObject call_params = new JSONObject(params, JSONObject.getNames(params));
            for(String key : JSONObject.getNames(api_params))
            {
                call_params.put(key, api_params.get(key));
            }
            HttpPost post = new HttpPost(postUrl);
            post.setEntity(new StringEntity(call_params.toString()));
            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject myObject = new JSONObject(result).getJSONObject("results");
            return myObject;
        }
        return null;
    }
    public JSONObject legacy_api_call(String method) throws Exception {
        String LEGACY_API_URL = "https://api.deezer.com";
        HttpGet get = new HttpGet(LEGACY_API_URL + "/" + method);
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(get);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);
        JSONObject myObject = new JSONObject(result);
        return myObject;
    }
    public BufferedInputStream get_image(String url) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            BufferedInputStream bis = new BufferedInputStream(entity.getContent());
            return bis;
        }
        return null;
    }
    public void get_user_data() throws Exception {

        JSONObject data = api_data();
        JSONObject user = data.getJSONObject("USER");
        long userid = user.getLong("USER_ID");
        if (userid==0){
            throw new java.lang.Error("Arl is invalid.");
        }
        if (user.getString("USER_PICTURE").isEmpty()){
            this.user = new JSONObject();
            this.user.put("id", userid).put("name", user.getString("BLOG_NAME")).put("arl", this.arl)
                    .put("image", "https://e-cdns-images.dzcdn.net/images/user/250x250-000000-80-0-0.jpg");
        }else {
            this.user = new JSONObject();
            this.user.put("id", userid).put("name", user.getString("BLOG_NAME")).put("arl", this.arl)
                    .put("image", String.format("https://e-cdns-images.dzcdn.net/images/user/%s/250x250-000000-80-0-0.jpg", user.getString("USER_PICTURE")));
        }
    }

    public void get_track(String track_id) throws Exception {
        String method = this.method.SONG_GET_DATA;
        if (Integer.parseInt(track_id) >= 0)
            method = this.method.PAGE_TRACK;
        JSONObject params = new JSONObject();
        params.put("SNG_ID", track_id);
        JSONObject track_data = api_call(method, params);
        this.track = track_data;
    }
    public JSONObject SearchByQuery(String query) throws Exception {
        String method = this.method.PAGE_SEARCH;
        JSONObject params = new JSONObject();
        params.put("query", query).put("artist_suggest","true").put("suggest","true").put("top_tracks","true").put("nb",40).put("start",0);
        JSONObject track_data = api_call(method, params);
        return track_data;
    }
    public JSONObject get_track_tags() throws Exception {
        JSONObject track = this.track.getJSONObject("DATA");
        if (track == null){
            track = this.track;
        }
        JSONObject album_data = get_album(track.getString("ALB_ID"));
        StringBuilder artists = new StringBuilder();
        if (track.getJSONObject("SNG_CONTRIBUTORS").has("main_artist")){
            JSONArray main_artists = track.getJSONObject("SNG_CONTRIBUTORS").getJSONArray("main_artist");

            artists.append(main_artists.getString(0));
            for (int i = 1 ; i < main_artists.length(); i++) {
                artists.append(", ").append(main_artists.getString(i));
            }
        }else {
            artists.append(track.getString("ART_NAME"));
        }
        StringBuilder title = new StringBuilder().append(track.getString("SNG_TITLE"));
        if (track.has("VERSION") && !track.getString("VERSION").isEmpty()){
            title.append(" ").append(track.getString("VERSION"));
        }
        boolean should_include_featuring = true;
        String[] feat_keywords = {"feat", "featuring", "ft."};
        for (String keyword : feat_keywords) {
            should_include_featuring = !title.toString().toLowerCase().contains(keyword);
        }
        if (should_include_featuring && track.getJSONObject("SNG_CONTRIBUTORS").has("featuring")){
           JSONArray featuring_artists_data = track.getJSONObject("SNG_CONTRIBUTORS").getJSONArray("featuring");
           StringBuilder featuring_artists = new StringBuilder();
           featuring_artists.append(featuring_artists_data.getString(0));
            for (int i = 1 ; i < featuring_artists_data.length(); i++) {
                featuring_artists.append(", ").append(featuring_artists_data.getString(i));
            }
            title.append("(feat. ").append(featuring_artists).append(")");
        }
        int total_tracks = album_data.getInt("nb_tracks");
        String track_number = track.getInt("TRACK_NUMBER")+"/"+total_tracks;
        String coverid = album_data.getString("cover_id");
        JSONObject cover = get_poster(coverid, 500, "jpg", "cover");
        JSONObject tags = new JSONObject();
        tags.put("title", title.toString()).put("artist", artists.toString()).put("genre", "null").put("album", track.getString("ALB_TITLE"))
                .put("albumartist", track.getString("ART_NAME")).put("label", album_data.getString("label"))
                .put("date", track.getString("PHYSICAL_RELEASE_DATE")).put("discnumber", track.getString("DISK_NUMBER")).put("tracknumber", track_number)
                .put("isrc", track.getString("ISRC")).put("copyright", track.getString("COPYRIGHT")).put("_albumart", cover);
        JSONArray geners = album_data.getJSONObject("genres").getJSONArray("data");
        if (geners.length()>0){
            tags.put("genre", geners.getJSONObject(0).getString("name"));
        }
        if (track.getJSONObject("SNG_CONTRIBUTORS").has("author")){
            JSONArray _author = track.getJSONObject("SNG_CONTRIBUTORS").getJSONArray("author");
            StringBuilder authors = new StringBuilder();
            authors.append(_author.getString(0));
            for (int i = 1 ; i < _author.length(); i++) {
                authors.append(", ").append(_author.getString(i));
            }
            tags.put("author", authors);
        }
        return tags;
    }

    public ArrayList<String> get_track_download_url(String quality) throws Exception {
        if (quality == null) quality = track_formats.MP3_128;
        JSONObject track = this.track.getJSONObject("DATA");
        if (track == null) track = this.track;
        ArrayList<String> url = new ArrayList<>();
        try {


            if (!track.has("MD5_ORIGIN")){
                throw new java.lang.Error("MD5 is needed to decrypt the download link.");
            }
            String md5_origin = track.getString("MD5_ORIGIN");
            String track_id = track.getString("SNG_ID");
            String media_version = track.getString("MEDIA_VERSION");
            int quality_code = track_formats.TRACK_FORMAT_MAP().getJSONObject(quality).getInt("code");
            url.add(0, encrypt_url(md5_origin, track_id, media_version, quality_code));
            url.add(1, quality);
        } catch (IllegalArgumentException e) {
            throw new java.lang.IllegalArgumentException("You have passed an invalid argument. This method needs the \"DATA\" value in the dictionary returned by the get_track() method.");
        }
        HttpGet get = new HttpGet(url.get(0));
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(get);
        String h = response.getHeader("Content-length").getValue();
        int header_code = Integer.parseInt(h);
        if (response.getCode() == 200 && header_code>0){
            response.close();
            return url;
        }else {
            String[] fallback_qualities = track_formats.FALLBACK_QUALITIES;
            for (String key : fallback_qualities){
                String md5_origin = track.getString("MD5_ORIGIN");
                String track_id = track.getString("SNG_ID");
                String media_version = track.getString("MEDIA_VERSION");
                url.set(0,encrypt_url(md5_origin, track_id, media_version, track_formats.TRACK_FORMAT_MAP().getJSONObject(key).getInt("code")));
                get = new HttpGet(url.get(0));
                response = (CloseableHttpResponse) httpClient.execute(get);
                h = response.getHeader("Content-length").getValue();
                header_code = Integer.parseInt(h);
                if (response.getCode() == 200 && header_code>0){
                    response.close();
                    return url;
                }
            }
        }
        return null;
    }

    public String encrypt_url(String md5_origin, String track_id, String media_version, int quality_code) throws NoSuchAlgorithmException {
        String magic_char = "¤";
        StringBuilder step1 = new StringBuilder();
        step1.append(md5_origin).append(magic_char).append(quality_code).append(magic_char).append(track_id).append(magic_char).append(media_version);
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (char ch: step1.toString().toCharArray()) {
            int ascii = ch;
            md.update((byte) ascii);
        }
        byte[] digest = md.digest();
        String myHash = util.byteArrayToHex(digest).toLowerCase();
        String step2 = myHash + magic_char + step1 + magic_char;
        step2 = StringUtils.rightPad(step2, 80);
        String key = "jo6aey6haid2Teih";
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            ArrayList<Byte> byte_array = new ArrayList<>();
            for (char ch: step2.toCharArray()) {
                int ascii = ch;
                byte_array.add((byte) ascii);
            }
            byte[] bytes = Bytes.toArray(byte_array);
            byte[] encrypted = cipher.update(bytes);
            String myHex = DatatypeConverter.printHexBinary(encrypted).toLowerCase();
            char cdn = md5_origin.charAt(0);
            return "https://e-cdns-proxy-" + cdn + ".dzcdn.net/mobile/1/" + myHex;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public Object download_track(String download_dir, String quality, String filename) throws Exception {
        JSONObject track = this.track.getJSONObject("DATA");
        if (track == null){
            track = this.track;
        }
        util util = new util();
        JSONObject tags = get_track_tags();
        ArrayList<String> url_withquality = get_track_download_url(quality);
        byte[] blowfish_key = util.get_blowfish_key(track.getString("SNG_ID"));
        JSONObject quality_info = track_formats.TRACK_FORMAT_MAP().getJSONObject(url_withquality.get(1));
        String title = tags.getString("title");
        String ext = quality_info.getString("ext");
        if (filename == null){
            filename = title ;}
        if (!filename.endsWith(ext)){
            filename += ext;}
        filename = util.clean_filename(filename);
        String download_path = Paths.get(download_dir, filename).toString();
        HttpGet get = new HttpGet(url_withquality.get(0));
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        InputStream inputStream = null;
        FileOutputStream outputFile = null;
        try {
         response = (CloseableHttpResponse) httpClient.execute(get);
         entity = response.getEntity();
         inputStream = entity.getContent();
        byte[] data_bytes = inputStreamToByteArray(inputStream);
        outputFile = new FileOutputStream(download_path);
        int totalBytes = data_bytes.length;
        int downloadedBytes = 0;
        int chunksize = 2048;
        for (int i = 0; i <= data_bytes.length; i+=2048) {
            byte[] chunk = Arrays.copyOfRange(data_bytes, i, i + chunksize);
            if (i + chunksize > data_bytes.length) chunk = Arrays.copyOfRange(data_bytes, i, data_bytes.length);
            if (i % 3 > 0) {
                outputFile.write(chunk);
            } else if (chunk.length < chunksize) {
                outputFile.write(chunk);
                outputFile.close();

                break;
            } else {
                SecretKeySpec keySpec = new SecretKeySpec(blowfish_key, "Blowfish");
                Cipher cipher = Cipher.getInstance("Blowfish/CBC/NoPadding");
                ArrayList<Integer> range = new ArrayList<>();
                for (int l = 0; l < 8; l++)
                    range.add(l);
                byte[] loop = Bytes.toArray(range);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(loop));
                byte[] dec_data = cipher.update(chunk);
                outputFile.write(dec_data);

            }


            downloadedBytes += chunk.length;
            float progress = (float) downloadedBytes / totalBytes;
            String progressBar = String.format("[%-" + 30 + "s] %d%%",
                    "#".repeat((int) (progress * 50)), (int) (progress * 100));
            System.out.print("\r" +tags.getString("title")+ progressBar);
        }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
            if (entity != null) {
                entity.getContent().close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputFile != null) {
                outputFile.close();
            }
        }
        if (ext.equalsIgnoreCase(".flac")) {
            _write_flac_tags(download_path, tags);
        }else {
            _write_mp3_tags(download_path, tags);
        }
        return null;
    }

    public boolean _write_mp3_tags(String path, JSONObject tags) throws Exception {
        if (tags == null) tags = get_track_tags();
        Mp3File mp3file = new Mp3File(path);
        mp3file.removeId3v2Tag();
        ID3v2 id3v2Tag;
        id3v2Tag = new ID3v24Tag();
        mp3file.setId3v2Tag(id3v2Tag);
        JSONObject cover = tags.getJSONObject("_albumart");
        id3v2Tag.setTrack(tags.getString("tracknumber"));
        id3v2Tag.setArtist(tags.getString("artist"));
        id3v2Tag.setTitle(tags.getString("title"));
        id3v2Tag.setAlbum(tags.getString("album"));
        id3v2Tag.setDate(tags.getString("date"));
        id3v2Tag.setGenreDescription(tags.getString("genre"));
        id3v2Tag.setAlbumArtist(tags.getString("albumartist"));
        id3v2Tag.setCopyright(tags.getString("copyright"));
        if (cover.has("image")){
            byte[] image_bytes = inputStreamToByteArray((InputStream) cover.get("image"));
            id3v2Tag.setAlbumImage(image_bytes, cover.getString("mime_type"));
        }
        mp3file.save(path+".new");
        if (new File(path).delete()){
            new File(path+".new").renameTo(new File(path));
            System.out.println(" Done!!");
        }else {
            throw new java.lang.Error(String.format("Path: %s (The process cannot access the file because it is being used by another process)", path));
        }
        return true;
    }
    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
    public boolean _write_flac_tags(String path, JSONObject tags) throws Exception {
        if (tags == null) tags = get_track_tags();
        AudioFile audioFile = AudioFileIO.read(new File(path));
        FlacTag tag = (FlacTag)audioFile.getTag();
        JSONObject cover = tags.getJSONObject("_albumart");
        tag.setField(FieldKey.SINGLE_DISC_TRACK_NO,tags.getString("tracknumber"));
        tag.setField(FieldKey.ARTIST,tags.getString("artist"));
        tag.setField(FieldKey.TITLE,tags.getString("title"));
        tag.setField(FieldKey.ALBUM,tags.getString("album"));
        tag.setField(FieldKey.GENRE,tags.getString("genre"));
        tag.setField(FieldKey.ALBUM_ARTIST,tags.getString("albumartist"));
        tag.setField(FieldKey.COPYRIGHT,tags.getString("copyright"));
        tag.setField(FieldKey.ISRC,tags.getString("isrc"));
        if (cover.has("image")){
            InputStream inputimgStream = (InputStream) cover.get("image");
            byte[] image_bytes = inputStreamToByteArray(inputimgStream);
            tag.setField(tag.createArtworkField(image_bytes,
                    PictureTypes.DEFAULT_ID,
                    cover.getString("mime_type"),
                    "test",
                    cover.getInt("width"),
                    cover.getInt("height"),
                    24,
                    0));
        }
        AudioFileIO.write(audioFile);
        System.out.println(" Done!!");
        return true;
    }

    public JSONObject get_album(String album_id) throws Exception {
        JSONObject data = legacy_api_call(String.format("album/%s", album_id));
        String cover_id = data.getString("cover_small").split("cover/")[1].split("/")[0];
        data.put("cover_id", cover_id);
        return data;
    }

    public JSONObject get_album_tracks(String album_id) throws Exception {
        JSONObject params = new JSONObject();
        params.put("ALB_ID", album_id).put("NB", -1);
        JSONObject data = api_call(method.ALBUM_TRACKS, params);
        return data;
    }

    public JSONObject get_artist(String artist_id) throws Exception {
        JSONObject params = new JSONObject();
        params.put("ART_ID", artist_id).put("LANG", "en");
        JSONObject data = api_call(method.PAGE_ARTIST, params);
        return data;
    }

    public void get_artist_poster(JSONObject artist, int size, String ext) throws IOException, JSONException {
        if (!artist.has("ART_PICTURE") && artist.has("DATA")){
            artist = artist.getJSONObject("DATA");
        }
        get_poster(artist.getString("ART_PICTURE"), size, ext, "artist");
    }

    public JSONArray get_artist_discography(String artist_id) throws Exception {
        JSONObject params = new JSONObject();
        params.put("ART_ID", artist_id).put("NB", 500).put("NB_SONGS", -1).put("START", 0);
        JSONObject data = api_call(method.ARTIST_DISCOGRAPHY, params);
        return data.getJSONArray("data");
    }
    public JSONObject page (String method, JSONObject params) throws Exception {
        if (!method.equals(this.method.GET_USER_DATA)){
            JSONObject api_params = new JSONObject();
            api_params.put("api_version", "1.0").put("api_token", this.token).put("input", "3").put("method", method);
            JSONObject call_params = new JSONObject(params, JSONObject.getNames(params));
            for(String key : JSONObject.getNames(api_params))
            {
                call_params.put(key, api_params.get(key));
            }
            HttpGet post = new HttpGet(postUrl);
            post.setEntity(new StringEntity(call_params.toString()));
            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject myObject = new JSONObject(result).getJSONObject("results");
            return myObject;
        }
        return null;
    }
    public JSONObject get_popular_playlists() throws Exception {
        JSONObject params = new JSONObject();
        params.put("cid", 487225562);
        JSONObject data = page("page.get", params);
        return data;
    }

    public JSONArray get_artist_top_tracks(String artist_id) throws Exception {
        JSONObject params = new JSONObject();
        params.put("ART_ID", artist_id).put("NB", 100);
        JSONObject data = api_call(method.ARTIST_TOP_TRACKS, params);
        return data.getJSONArray("data");
    }

    public JSONObject get_playlist(String playlist_id) throws Exception {
        JSONObject params = new JSONObject();
        params.put("playlist_id", playlist_id).put("LANG", "en");
        JSONObject data = api_call(method.PAGE_PLAYLIST, params);
        return data;
    }

    public JSONArray get_playlist_tracks(String playlist_id) throws Exception {
        JSONObject params = new JSONObject();
        params.put("PLAYLIST_ID", playlist_id).put("NB", -1);
        JSONObject data = api_call(method.PLAYLIST_TRACKS, params);
        return data.getJSONArray("data");
    }

    public String get_suggested_queries(String query) throws Exception {
        JSONObject params = new JSONObject();
        params.put("QUERY", query);
        JSONObject data = api_call(method.GET_SUGGESTED_QUERIES, params);

        JSONArray results = data.getJSONArray("SUGGESTION");
        for (int i = 0 ; i < results.length(); i++){
            results.getJSONObject(i).remove("HIGHLIGHT");
        }
        String decodedJsonArrayString  = StringEscapeUtils.unescapeJson(results.toString());
        return decodedJsonArrayString;
    }

    public JSONArray search_tracks(String query, int limit, int index) throws Exception {

        return legacy_search(method.SEARCH_TRACK, query, limit, index);
    }

    public JSONArray search_albums(String query, int limit, int index) throws Exception {

        return legacy_search(method.SEARCH_ALBUM, query, limit, index);
    }

    public JSONArray search_artists(String query, int limit, int index) throws Exception {

        return legacy_search(method.SEARCH_ARTIST, query, limit, index);
    }
    public JSONArray search_playlists(String query, int limit, int index) throws Exception {

        return legacy_search(method.SEARCH_PLAYLIST, query, limit, index);
    }

    public JSONArray legacy_search(String method, String query, int limit, int index) throws Exception {
        JSONObject params = new JSONObject();
        params.put("q", query).put("limit", limit).put("index", index);
        JSONArray data = legacy_search_call(method, params).getJSONArray("data");
        return data;
    }

    public JSONObject legacy_search_call(String method, JSONObject params) throws Exception {
        String LEGACY_API_URL = "https://api.deezer.com";
        URIBuilder builder = new URIBuilder(LEGACY_API_URL + "/" + method);
        Iterator<?> iterator = params.keys();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Object value = params.get(key.toString());
            builder.setParameter(key.toString(), value.toString());
        }
        HttpGet get = new HttpGet(builder.build());
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(get);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);
        JSONObject myObject = new JSONObject(result);
        return myObject;
    }

    public JSONObject get_poster(String poster_id, int size, String ext, String type_image) throws IOException, JSONException {
        // type_image: is type of "artist" or "cover" image it will be changed in url.
         ext = ext.toLowerCase();
         if (!ext.equals("jpg") && !ext.equals("png")){
             throw new java.lang.Error("Image extension should only be jpg or png!");
         }
        JSONObject image_info = new JSONObject();
         image_info.put("image", get_image(String.format("https://e-cdns-images.dzcdn.net/images/%s/", type_image) + poster_id + "/" + size + "x" + size + "." + ext)).put("width", size).put("height", size).put("ext", ext);
         if (ext.equals("jpg")){
             image_info.put("mime_type", "image/jpeg");
         }else image_info.put("mime_type", "image/png");
         return image_info;
         }

    }