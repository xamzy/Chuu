package DAO;

import DAO.Entities.*;
import org.apache.commons.collections4.map.MultiValueMap;

import javax.management.InstanceNotFoundException;
import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class DaoImplementation {
	private final DataSource dataSource;
	private final SQLQueriesDao queriesDao;
	private final UpdaterDao updaterDao;
	private final UserGuildDao userGuildDao;

	public DaoImplementation(DataSource dataSource) {

		this.dataSource = dataSource;
		this.queriesDao = new SQLQueriesDaoImpl();
		this.userGuildDao = new UserGuildDaoImpl();
		this.updaterDao = new UpdaterDaoImpl();
	}

	public DaoImplementation() {

		this.dataSource = new SimpleDataSource(true);
		this.queriesDao = new SQLQueriesDaoImpl();
		this.userGuildDao = new UserGuildDaoImpl();
		this.updaterDao = new UpdaterDaoImpl();
	}

	public void updateUserTimeStamp(String lastFmName, Integer timestamp, Integer timestampControl) {
		try (Connection connection = dataSource.getConnection()) {
			updaterDao.setUpdatedTime(connection, lastFmName, timestamp, timestampControl);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public void insertArtistDataList(List<ArtistData> list, String id) {
		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setAutoCommit(false);

				/* Do work. */
				list.forEach(artistData -> {
					artistData.setDiscordID(id);
					updaterDao.addUrl(connection, artistData);
					updaterDao.addArtist(connection, artistData);
				});
				updaterDao.setUpdatedTime(connection, id, null, null);

				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void incrementalUpdate(TimestampWrapper<List<ArtistData>> list, String id) {
		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setAutoCommit(false);

				/* Do work. */

				list.getWrapped().forEach(artistData -> {
					artistData.setDiscordID(id);
					updaterDao.upsertArtist(connection, artistData);
					updaterDao.upsertUrlBitMask(connection, new ArtistInfo(artistData.getUrl(), artistData
							.getArtist()), artistData.isUpdateBit());
				});
				updaterDao.setUpdatedTime(connection, id, list.getTimestamp(), list.getTimestamp());

				connection.commit();


			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
//			} catch (InstanceNotFoundException e) {
//				throw new RuntimeException(e);
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void addGuildUser(long userID, long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			userGuildDao.addGuild(connection, userID, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public void insertArtistDataList(LastFMData data) {
		try (Connection connection = dataSource.getConnection()) {

			try {
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				userGuildDao.insertUserData(connection, data);
				userGuildDao.addGuild(connection, data.getDiscordId(), data.getGuildID());
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public void updateLastFmData(long discordID, String lastFMID) {

		try (Connection connection = dataSource.getConnection()) {

			try {

				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				LastFMData lastFmData = userGuildDao.findLastFmData(connection, discordID);

				lastFmData.setName(lastFMID);

				userGuildDao.updateLastFmData(connection, lastFmData);
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			} catch (InstanceNotFoundException e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public LastFMData findLastFMData(long discordID) throws InstanceNotFoundException {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.findLastFmData(connection, discordID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public ResultWrapper getSimilarities(List<String> lastFmNames) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.similar(connection, lastFmNames);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public WrapperReturnNowPlaying whoKnows(String artist, long guildId) {
		return whoKnows(artist, guildId, 10);
	}

	public WrapperReturnNowPlaying whoKnows(String artist, long guildId, int limit) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.knows(connection, artist, guildId, limit);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UpdaterUserWrapper getLessUpdated() {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return updaterDao.getLessUpdated(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<UsersWrapper> getAll(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return userGuildDao.getAll(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UniqueWrapper<UniqueData> getUniqueArtist(Long guildID, String lastFmId) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.getUniqueArtist(connection, guildID, lastFmId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Long> getGuildList(long userId) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return userGuildDao.guildList(connection, userId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<UrlCapsule> getGuildTop(long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.getGuildTop(connection, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UniqueWrapper<UniqueData> getCrowns(String lastFmID, long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.getCrowns(connection, lastFmID, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getNullUrls() {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return updaterDao.selectNullUrls(connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getSpotifyNulledUrls() {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return updaterDao.selectNullUrls(connection, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public String getArtistUrl(String url) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return updaterDao.getArtistUrl(connection, url);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public void upsertUrl(ArtistInfo artistInfo) {
		try (Connection connection = dataSource.getConnection()) {
			updaterDao.upsertUrl(connection, artistInfo);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public void upsertSpotify(ArtistInfo artistInfo) {
		try (Connection connection = dataSource.getConnection()) {
			updaterDao.upsertSpotify(connection, artistInfo);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public void addLogo(long guildId, BufferedImage in) {
		try (Connection connection = dataSource.getConnection()) {
			userGuildDao.addLogo(connection, guildId, in);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	@Deprecated
	public void removeLogo(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			userGuildDao.removeLogo(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public InputStream findLogo(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.findLogo(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public long getDiscordIdFromLastfm(String lasFmName, long guildId) throws InstanceNotFoundException {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return userGuildDao.getDiscordIdFromLastFm(connection, lasFmName, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public int getArtistPlays(String artist, String whom) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.userPlays(connection, artist, whom);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public List<LbEntry> getGuildCrownLb(long guildId) {

		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.crownsLeaderboard(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public void removeUserFromOneGuildConsequent(long discordID, long guildID) {
		removeFromGuild(discordID, guildID);
		MultiValueMap<Long, Long> map = getMapGuildUsers();
		if (!map.containsValue(discordID)) {
			removeUserCompletely(discordID);
		}
	}

	public void removeUserCompletely(Long discordID) {

		try (Connection connection = dataSource.getConnection()) {
			try {
				/* Prepare connection. */

				connection.setAutoCommit(false);
				/* Do work. */
				userGuildDao.removeUser(connection, discordID);
				/* Commit. */
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void removeFromGuild(Long discordID, long guildID) {

		try (Connection connection = dataSource.getConnection()) {
			try {
				/* Prepare connection. */

				connection.setAutoCommit(false);
				/* Do work. */
				userGuildDao.removeUserGuild(connection, discordID, guildID);
				/* Commit. */
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public MultiValueMap<Long, Long> getMapGuildUsers() {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return userGuildDao.getWholeUser_Guild(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public UpdaterStatus getUpdaterStatus(String artist_id) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return updaterDao.getUpdaterStatus(connection, artist_id);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}


	public void createCorrection(String artist, String correction) {
		try (Connection connection = dataSource.getConnection()) {
			if (!artist.equalsIgnoreCase(correction)) {
				updaterDao.insertCorrection(connection, artist, correction);
			}
			updaterDao.updateStatusBit(connection, artist);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public void insertCorrection(String artist, String correction) {
		if (artist.equalsIgnoreCase(correction))
			return;

		try (Connection connection = dataSource.getConnection()) {
			updaterDao.insertCorrection(connection, artist, correction);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public String findCorrection(String artist_id) {
		try (Connection connection = dataSource.getConnection()) {
			return updaterDao.findCorrection(connection, artist_id);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public void updateMetric(int metricId, long value) {
		try (Connection connection = dataSource.getConnection()) {
			updaterDao.updateMetric(connection, metricId, value);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public List<LbEntry> getUniqueLeaderboard(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.uniqueLeaderboard(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public int getUserArtistCount(String lastfmId) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			return queriesDao.userArtistCount(connection, lastfmId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}