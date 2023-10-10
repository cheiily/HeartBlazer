package pl.cheily;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
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
                Commands.message("Pin (thread-only)"),
                Commands.slash("ping", "Returns the current gateway ping as well as real response delay.")
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