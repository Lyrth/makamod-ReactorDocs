package makanism.module.reactordocs.commands;

import discord4j.core.object.entity.channel.MessageChannel;
import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import makanism.module.reactordocs.ReactorDocs;
import org.jsoup.Jsoup;
import reactor.core.publisher.Mono;

@CommandInfo(
    accessLevel = AccessLevel.GENERAL,
    desc = "Reactor docs! WIP."
)
public class ReactorDoc extends GuildModuleCommand<ReactorDocs> {

    @Override
    public Mono<?> execute(CommandCtx ctx, ReactorDocs module) {
        return module.getFile()
            .flatMap(s -> ctx.getChannel().flatMap(MessageChannel::type).thenReturn(s))
            .map(Jsoup::parse)
            .flatMapIterable(doc -> doc.body()
                .selectFirst("div.contentContainer > div.details")
                .selectFirst("ul.blockList:eq(0) > li.blockList")
                .selectFirst("ul.blockList:eq(1) > li.blockList")
                .select("ul.blockList, ul.blockListLast")
            )
            .map(el -> el.selectFirst("li.blockList > h4").text())
            .map(s -> s + ", ")
            .collect(StringBuffer::new, StringBuffer::append)
            .map(sb -> sb.length() > 899 ? sb.substring(0,898) : sb.toString())
            .flatMap(reply -> ctx.sendReply(embed -> embed.setDescription(reply)));
    }
}
