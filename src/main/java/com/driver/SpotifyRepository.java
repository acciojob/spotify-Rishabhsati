package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile); // creating new user
        users.add(user); // adding user in the users list
        userPlaylistMap.put(user,new ArrayList<>()); // created empty value hashmap for user playlist
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name); // creating artist
        artists.add(artist); // adding artist in the artist list
        artistAlbumMap.put(artist,new ArrayList<>()); // creating empty value hashmap for artist albums
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist =null;
        for(Artist artist1 : artists){
            if(artist1.getName().equals(artistName)){ // check for does artist exist or not
                artist.setName(artist1.getName());
                break;
            }
        }
        if(artist==null) artist = createArtist(artistName); // if artist is not existing then throw the exception
        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.get(artist).add(album); // adding the artist and the artist album in the artistalbum hashmap
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album=null;
        for(Album album1:albums){
            if(album1.getTitle().equals(albumName)){ // checking does album exist or not
                album.setTitle(album1.getTitle());
            }
        }
        if(album==null) throw new Exception("album does not exist "); // throw exception if album does not exist
        Song song = new Song(title,length); //creating song
        song.setLikes(0); // set song likes 0
        albums.add(album); // added album in the arraylist
        songLikeMap.put(song,new ArrayList<>()); // creating song like map and add the song in the hashmap
        albumSongMap.get(album).add(song); // added album to the album song map
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = null;

        for(User user1 : userPlaylistMap.keySet()){
            if(user1.getMobile().equals(mobile)){
                user = user1;
                break;
            }
        }

        if(user==null) throw new Exception("User does not exist");
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        playlistSongMap.put(playlist,new ArrayList<>());
        playlistListenerMap.put(playlist,new ArrayList<>());

        for(Song song : songs){
            if(song.getLength()==length) playlistSongMap.get(playlist).add(song);
        }

        playlistListenerMap.get(playlist).add(user);
        creatorPlaylistMap.put(user,playlist);
        userPlaylistMap.get(user).add(playlist);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = null;

        for(User user1 : userPlaylistMap.keySet()){  //  finding the user in the user playlist hashmap
            if(user1.getMobile().equals(mobile)){
                user = user1;
                break;
            }
        }

        if(user != null) throw new Exception("User does not exist"); // throw error if user not exist
        Playlist playlist = new Playlist(title); // create playlist
        playlists.add(playlist); //add playlist

        for(Song song : songs){ //finding song
            if(song.getTitle().equals(title)) playlistSongMap.get(playlist).add(song);
        }

        playlistListenerMap.get(playlist).add(user); // add playlist and user in the listner hashmap
        creatorPlaylistMap.put(user,playlist); // add user and playlist in the creator hashmap
        userPlaylistMap.get(user).add(playlist); // add user and playlist int the playlist hashmap

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = null;
        for(User user1:users){ //  finding the user in the users
            if(user1.getMobile().equals(mobile)){
                user = user1;
                break;
            }
        }
        if(user == null) throw new Exception("User does not exist");

        Playlist playlist = null;
        for(Playlist playlist1 : playlists){ // finding does playlist exist
            if(playlist1.equals(playlistTitle)){
                playlist = playlist1;
                break;
            }
        }
        if(playlist == null) throw new Exception("Playlist does not exist");

        playlistListenerMap.get(playlist).add(user); // add playlist to the listners hashmap
        if(!userPlaylistMap.get(user).contains(playlist)) userPlaylistMap.get(user).add(playlist); // add playlist to the playlist hashmap

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = null;
        for(User user1:users){ //  finding the user in the users
            if(user1.getMobile().equals(mobile)){
                user = user1;
                break;
            }
        }
        if(user == null) throw new Exception("User does not exist");

        Song song = null;
        for(Song song1:songs){ // finding song in the songs
            if(song1.getTitle().equals(mobile)){
                song = song1;
                break;
            }
        }
        if(song == null) throw new Exception("Song does not exist");

        song.setLikes(song.getLikes()+1); // increasing the song like
        songLikeMap.get(song).add(user); // creating a like hashmap for the song and user

        for(Album album:albumSongMap.keySet()){ //finding the song in the albums
            if(albumSongMap.get(album).contains(song)){ // if yes then check the song in the artist hashmap
                for(Artist artist:artistAlbumMap.keySet()){
                    if(artistAlbumMap.get(artist).contains(album)){ // if yes then increase the song like
                        artist.setLikes(artist.getLikes()+1);
                        break;
                    }
                }
                break;
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        int countLikes=0;
        String popularArtist="";
        for(Artist artist:artists){ // finding the most popular artist in the artist table
            if(artist.getLikes() > countLikes){
                popularArtist=artist.getName();
                countLikes=artist.getLikes();
            }
        }
        return popularArtist;
    }

    public String mostPopularSong() {
        int countLikes=0;
        String popularSong="";
        for(Song song:songs){ // finding most popular song in the songs table
            if(song.getLikes() > countLikes){
                popularSong=song.getTitle();
                countLikes=song.getLikes();
            }
        }
        return popularSong;
    }
}
