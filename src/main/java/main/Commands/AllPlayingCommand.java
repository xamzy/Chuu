package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import DAO.Entities.UsersWrapper;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

public class AllPlayingCommand extends ConcurrentCommand {
	public AllPlayingCommand(DaoImplementation dao) {
		super(dao);
	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] message;
		try {
			message = parse(e);
		} catch (ParseException e1) {
			errorMessage(e, 1000, e1.getMessage());
			return;
		}
		boolean showFresh = Boolean.valueOf(message[0]);
		List<UsersWrapper> list = getDao().getAll(e.getGuild().getIdLong());
		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor()).setThumbnail(e.getGuild().getIconUrl())
				.setTitle("What is being played now in " + e.getGuild().getName());
		StringBuilder a = new StringBuilder();


		Map<UsersWrapper, Optional<NowPlayingArtist>> npList = list.parallelStream().
				collect(Collectors.toConcurrentMap(u -> u, uw ->

				{
					try {
						return Optional.of(lastFM.getNowPlayingInfo(uw.getLastFMName()));
					} catch (Exception ex) {
						return Optional.empty();
					}
				}));


		npList.forEach((usersWrapper, optionalNowPlayingArtist) -> {
			if (!optionalNowPlayingArtist.isPresent())
						return;
			NowPlayingArtist nowPlayingArtist = optionalNowPlayingArtist.get();
					if (showFresh) {

						if (!nowPlayingArtist.isNowPlaying()) {
							return;
						}
					}

			Member member = e.getGuild().getMemberById(usersWrapper.getDiscordID());

			String username = member == null ? usersWrapper.getLastFMName() : member.getEffectiveName();

					a.append("+ ").append("[")
							.append(username).append("](").append("https://www.last.fm/user/").append(usersWrapper.getLastFMName())
							.append("): ")
							.append("**").append(nowPlayingArtist.getSongName())
							.append("** - ").append(nowPlayingArtist.getAlbumName()).append(" | ")
							.append(nowPlayingArtist.getArtistName()).append("\n");
				}
		);
		if (a.length() == 0) {
			sendMessage(e, "None is listening to music at the moment UwU");
			return;
		}

		embedBuilder.setDescription(a);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!playing");
	}

	@Override
	public String getDescription() {
		return ("Returns lists of all people playing music rn");
	}

	@Override
	public String getName() {
		return "Playing";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!playing  \n" +
				"\t--recent for last track\n\n ");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] subMessage = getSubMessage(e.getMessage());
		boolean showFresh = Arrays.stream(subMessage).noneMatch(s -> s.equals("--recent"));

		return new String[]{Boolean.toString(showFresh)};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";
		String message;

		switch (code) {

			case 0:
				message = "There was a problem with Last FM Api" + cause;
				break;
			case 1:
				message = "User hasnt played any song recently!" + cause;
				break;
			default:
				message = "An unknown error happned";


		}
		sendMessage(e, base + message);
	}
}
