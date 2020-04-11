package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.Year;

public class ChartYearParser extends ChartableParser<ChartYearParameters> {
    public ChartYearParser(ChuuService dao) {
        super(dao, TimeFrameEnum.WEEK);
    }


    @Override
    void setUpOptionals() {
        super.setUpOptionals();
        opts.add(new OptionalEntity("--nolimit", "makes the chart as big as possible"));
    }

    @Override
    public ChartYearParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {
        LastFMData discordName;

        if (words.length > 3) {
            sendError(getErrorMessage(5), e);
            return null;
        }

        int x = 5;
        int y = 5;

        ChartParserAux chartParserAux = new ChartParserAux(words);
        Point chartSize;
        try {
            chartSize = chartParserAux.getChartSize();
        } catch (InvalidChartValuesException ex) {
            sendError(getErrorMessage(8), e);
            return null;
        }

        if (chartSize != null) {
            boolean conflictFlag = e.getMessage().getContentRaw().contains("--nolimit");
            if (conflictFlag) {
                sendError(getErrorMessage(7), e);
                return null;
            }
            x = chartSize.x;
            y = chartSize.y;
        }
        Year year = chartParserAux.parseYear();
        words = chartParserAux.getMessage();
        if (Year.now().compareTo(year) < 0) {
            sendError(getErrorMessage(6), e);
            return null;
        }
        TimeFrameEnum timeFrameEnum = calculateTimeFrame(year);
        discordName = atTheEndOneUser(e, words);
        return new ChartYearParameters(e, discordName.getName(), discordName.getDiscordId(), timeFrameEnum, x, y, year);
    }

    private static TimeFrameEnum calculateTimeFrame(Year year) {
        TimeFrameEnum timeframe;
        LocalDateTime time = LocalDateTime.now();
        if (year.isBefore(Year.of(time.getYear()))) {
            timeframe = TimeFrameEnum.ALL;
        } else {
            int monthValue = time.getMonthValue();
            if (monthValue == 1 && time.getDayOfMonth() < 8) {
                timeframe = TimeFrameEnum.WEEK;
            } else if (monthValue < 2) {
                timeframe = TimeFrameEnum.MONTH;
            } else if (monthValue < 4) {
                timeframe = TimeFrameEnum.QUARTER;
            } else if (monthValue < 7)
                timeframe = TimeFrameEnum.SEMESTER;
            else {
                timeframe = TimeFrameEnum.YEAR;
            }
        }
        return timeframe;
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *Username* *YEAR* *sizeXsize*** \n" +
               "\tIf username is not specified defaults to authors account \n" +
               "\tIf YEAR not specified it default to current year\n" +
               "\tIf Size not specified it defaults to 5x5\n";
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(5, "You Introduced too many words");
        errorMessages.put(6, "YEAR must be the current year or lower");
        errorMessages.put(7, "Can't use a size for the chart if you specify the --nolimit flag!");
        errorMessages.put(8, "0 is not a valid value for a chart!");

    }
}
