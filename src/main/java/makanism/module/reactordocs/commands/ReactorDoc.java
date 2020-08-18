package makanism.module.reactordocs.commands;

import discord4j.core.object.entity.channel.MessageChannel;
import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import makanism.module.reactordocs.ReactorDocs;
import makanism.module.reactordocs.defs.MethodDef;
import org.jsoup.Jsoup;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@CommandInfo(
    accessLevel = AccessLevel.GENERAL,
    desc = "Reactor docs! WIP."
)
public class ReactorDoc extends GuildModuleCommand<ReactorDocs> {

    @Override
    public Mono<?> execute(CommandCtx ctx, ReactorDocs module) {
        if (ctx.getArgs().isEmpty()) return ctx.sendReply("No method name specified!");

        return module.getFile()
            .flatMap(s -> ctx.getChannel().flatMap(MessageChannel::type).thenReturn(s))
            .map(Jsoup::parse)
            .doOnNext(d -> d.setBaseUri("https://projectreactor.io/docs/core/release/api/reactor/core/publisher"))
            .flatMapIterable(doc -> doc.body()
                .selectFirst("div.contentContainer > div.details")
                .selectFirst("ul.blockList:eq(0) > li.blockList")
                .selectFirst("ul.blockList:eq(1) > li.blockList")
                .select("ul.blockList, ul.blockListLast")
            )
            .map(el -> el.selectFirst("li.blockList"))
            .filter(el -> el.selectFirst("h4").text().matches(ctx.getArg(1)))   // err
            .next()
            .map(MethodDef::from)
            .flatMap(def -> ctx.sendReply(embed -> {
                embed.setTitle(def.name())
                    .setAuthor("Flux", null, null)
                    .setDescription("`" + def.signature() + "`\n\n" + def.description())
                    .setFooter(ctx.getAuthorIdText(), null);
                def.fields().forEach(t -> embed.addField(t.getT1().trim() + "_ _", "> " + t.getT2().trim() + "_ _", false));
                String links = def.images().stream()
                    .map(s -> String.format("[%s](%s)\n", s.substring(s.lastIndexOf('/')+1), s))
                    .collect(Collectors.joining());
                if (!links.isBlank())
                    embed.addField("Images: ", links, false);
            })
                .then(Flux.fromIterable(def.images()).next())
            )
            .flatMap(svg -> module.getImage(ctx, svg))
            .flatMap(ctx::sendReply);
    }
}
