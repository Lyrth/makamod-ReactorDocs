package makanism.module.reactordocs.commands;

import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import makanism.module.reactordocs.ReactorDocs;
import reactor.core.publisher.Mono;

@CommandInfo(
    accessLevel = AccessLevel.GENERAL,
    desc = "Reactor docs! WIP."
)
public class Embed extends GuildModuleCommand<ReactorDocs> {

    @Override
    public Mono<?> execute(CommandCtx ctx, ReactorDocs module) {
        return ctx.sendReply(e -> e.setDescription(ctx.getArgs().getRest(1)));
    }
}