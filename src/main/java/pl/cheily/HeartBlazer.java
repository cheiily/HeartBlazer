package pl.cheily;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import pl.cheily.Actions.ActionCommands.DustLoop;
import pl.cheily.Actions.ActionCommands.Ping;
import pl.cheily.Actions.ActionHandler;

public class HeartBlazer {
    public final static Logger logger = LoggerFactory.getLogger("HeartBlazer-Log-main-v" + Config.version);
    public final static Marker mBotFeedback = MarkerFactory.getIMarkerFactory().getMarker("BOT-FEEDBACK");
    public final static ActionHandler actionHandler = new ActionHandler();


    public static void main(String[] args) {
        Config.load();
        startup();
    }

    public static void startup() {
        FirstEventListener client = new FirstEventListener();
        JDA jda = JDABuilder.createDefault(Config.token)
                .addEventListeners(client)
                .build();

        jda.updateCommands().addCommands(
                Commands.message("Pin | Unpin (thread-only)"),
                Commands.slash(Ping.PROP_NAME, "Returns the current gateway ping as well as real response delay."),
                Commands.slash(DustLoop.PROP_NAME, "Polls data from dustloop.")
                        .addSubcommands(
                                new SubcommandData(DustLoop.PROP_CMD_LIST, "Lists a character's moves by expected input.")
                                        .addOption(OptionType.STRING, DustLoop.PROP_ARG_CHAR, "Character to poll", true)
                                        .addOption(OptionType.STRING, DustLoop.PROP_ARG_WIKI, "Wiki to poll if not GBVSR by default", false),
                                new SubcommandData(DustLoop.PROP_CMD_DATA, "Returns a move's detailed data.")
                                        .addOption(OptionType.STRING, DustLoop.PROP_ARG_CHAR, "Character to poll", true)
                                        .addOption(OptionType.STRING, DustLoop.PROP_ARG_MOVE, "Move by input (strictly the same as on wiki)", true)
                                        .addOption(OptionType.STRING, DustLoop.PROP_ARG_WIKI, "Wiki to poll if not GBVSR by default", false)
                        )
        ).queue(
                cmds -> logger.debug(mBotFeedback, "Loaded interactions: " + cmds.toString())
        );

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            onStartupFailure(e);
        }

        onStartupSuccess(jda);
    }

    public static void onStartupSuccess(JDA jda) {
        logger.info(mBotFeedback, "----- NEW SESSION -----");
        logger.debug(mBotFeedback, "Gateway ping: " + jda.getGatewayPing() + "ms.");
        Config.selfId = jda.getSelfUser().getId();
    }

    public static void onStartupFailure(Exception ex) {
        logger.error(mBotFeedback, "Startup failure!", ex);
    }

}