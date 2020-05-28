package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.UserConfigParser;
import core.parsers.params.UserConfigParameters;
import core.parsers.params.UserConfigType;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.RemainingImagesMode;
import dao.entities.WhoKnowsMode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import java.util.List;

public class UserConfigCommand extends ConcurrentCommand<UserConfigParameters> {
    public UserConfigCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<UserConfigParameters> getParser() {
        return new UserConfigParser(getService());
    }

    @Override
    public String getDescription() {
        return "Configuration per user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("configuration", "config");
    }

    @Override
    public String getName() {
        return "User Configuration";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        UserConfigParameters parse = this.parser.parse(e);
        if (parse == null) {
            return;
        }
        UserConfigType config = parse.getConfig();
        String value = parse.getValue();
        boolean cleansing = value.equalsIgnoreCase("clear");
        switch (config) {
            case PRIVATE_UPDATE:
                boolean b = Boolean.parseBoolean(value);
                getService().setPrivateUpdate(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Successfully made private the update for user " + getUserString(e, e.getAuthor().getIdLong()));
                } else {
                    sendMessageQueue(e, "Successfully made non private the update for user " + getUserString(e, e.getAuthor().getIdLong()));
                }
                break;
            case NOTIFY_IMAGE:
                b = Boolean.parseBoolean(value);
                getService().setImageNotify(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Now you will get notified whenever an image you uploaded gets approved");
                } else {
                    sendMessageQueue(e, "Now you will not get notified whenever an image you uploaded gets approved");
                }
                break;
            case CHART_MODE:
                ChartMode chartMode;
                if (cleansing) {
                    chartMode = ChartMode.IMAGE;
                } else {
                    chartMode = ChartMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                getService().setChartEmbed(e.getAuthor().getIdLong(), chartMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now your charts are back to the default");
                } else {
                    sendMessageQueue(e, "Chart mode set to: **" + WordUtils.capitalizeFully(chartMode.toString()) + "**");
                }
                break;
            case WHOKNOWS_MODE:
                WhoKnowsMode whoKnowsMode;
                if (cleansing) {
                    whoKnowsMode = WhoKnowsMode.IMAGE;
                } else {
                    whoKnowsMode = WhoKnowsMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                getService().setWhoknowsMode(e.getAuthor().getIdLong(), whoKnowsMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now your who knows are back to the default");
                } else {
                    sendMessageQueue(e, "Who Knows mode set to: **" + WordUtils.capitalizeFully(whoKnowsMode.toString()) + "**");
                }
                break;
            case REMAINING_MODE:
                RemainingImagesMode remainingImagesMode;
                if (cleansing) {
                    remainingImagesMode = RemainingImagesMode.IMAGE;
                } else {
                    remainingImagesMode = RemainingImagesMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                getService().setRemainingImagesMode(e.getAuthor().getIdLong(), remainingImagesMode);
                if (cleansing) {
                    sendMessageQueue(e, "The mode of the remaining image commands to the default");
                } else {
                    sendMessageQueue(e, "The mode of the remaining image commands was set to: **" + WordUtils.capitalizeFully(remainingImagesMode.toString()) + "**");
                }
                break;
        }
    }
}