package dao;

import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface TrackDao {
    List<ScrobbledTrack> getUserTopTracksNoSpotifyId(Connection connection, String lastfmid, int limit);

    void fillIds(Connection connection, List<ScrobbledTrack> list);

    void deleteAllUserTracks(Connection con, String lastfmId);

    void insertTracks(Connection connection, List<ScrobbledTrack> nonExistingId);

    void insertTrack(Connection connection, ScrobbledTrack x);

    long getTrackIdByName(Connection connection, String track, long artistId) throws InstanceNotFoundException;

    ScrobbledTrack getTrackByName(Connection connection, String album, long artistId) throws InstanceNotFoundException;

    void addSrobbledTracks(Connection con, List<ScrobbledTrack> scrobbledAlbums);

    List<AlbumUserPlays> getUserTopArtistTracks(Connection connection, long discord_id, long artistId, int limit);

    List<AlbumUserPlays> getServerTopArtistTracks(Connection connection, long guildId, long artistId, int limit);


    List<AlbumUserPlays> getGlobalTopArtistTracks(Connection connection, long artistId, int limit);

    Map<Genre, Integer> genreCountsByTracks(Connection connection, List<AlbumInfo> albumInfos);

    String getTrackUrlByName(Connection connection, String name, long artistId) throws InstanceNotFoundException;

    void fillIdsMbids(Connection connection, List<ScrobbledTrack> list);

    FullAlbumEntity getAlbumTrackList(Connection connection, long trackId, String lastfmId);

    List<ScrobbledTrack> getUserTopTracks(Connection connection, String lastfmid);

    List<ScrobbledTrack> getTopSpotifyTracksIds(Connection connection, String lastfmId, int limit);


}
