package core.commands.crowns;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.TrackPlays;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static core.parsers.NumberParser.generateThresholdParser;

public class
TrackCrownsCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public TrackCrownsCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        return generateThresholdParser(new OnlyUsernameParser(db));
    }

    @Override
    public String getDescription() {
        return ("List of tracks you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("albumtracks", "crownstrack", "crownstr", "ctr");
    }

    @Override
    public String getName() {
        return "Your track crowns";
    }

    @Override
    protected void onCommand(Context e, @NotNull NumberParameters<ChuuDataParams> params) {

        ChuuDataParams innerParams = params.getInnerParams();
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, innerParams.getLastFMData().getDiscordId());
        String name = userInfo.getUsername();
        String url = userInfo.getUrlImage();
        Long threshold = params.getExtraParam();
        long idLong = innerParams.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) db.getGuildCrownThreshold(idLong);
        }
        UniqueWrapper<TrackPlays> uniqueDataUniqueWrapper = db
                .getUserTrackCrowns(innerParams.getLastFMData().getName(), e.getGuild().getIdLong(), Math.toIntExact(threshold));
        List<TrackPlays> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();

        int rows = resultWrapper.size();
        if (rows == 0) {
            sendMessageQueue(e, name + " doesn't have any track crown :'(");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            TrackPlays g = resultWrapper.get(i);
            a.append(i + 1).append(g.toString());
        }
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setDescription(a)
                .setTitle(String.format("%s's track crowns", name), CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()))
                .setFooter(String.format("%s has %d track crowns!!%n", CommandUtil.unescapedUser(name, innerParams.getLastFMData().getDiscordId(), e), resultWrapper.size()), null)
                .setThumbnail(url);

        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(resultWrapper, message1, embedBuilder));
    }

}



