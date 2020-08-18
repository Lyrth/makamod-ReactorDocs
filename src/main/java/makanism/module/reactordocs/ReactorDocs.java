package makanism.module.reactordocs;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.annotation.GuildModuleInfo;
import lyrth.makanism.api.object.CommandCtx;
import makanism.module.reactordocs.commands.Embed;
import makanism.module.reactordocs.commands.ReactorDoc;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.function.Tuple2;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import static reactor.function.TupleUtils.function;

@GuildModuleInfo(
    commands = {
        ReactorDoc.class,
        Embed.class,
    }
)
public class ReactorDocs extends GuildModule<DocsConfig> {
    private static final Logger log = LoggerFactory.getLogger(ReactorDocs.class);

    private final PNGTranscoder transcoder = new PNGTranscoder();

    public static void main(String[] args) throws IOException, TranscoderException {
        log.info("Hi.");
        Jsoup.parse("<span></span>");

        // Create a JPEG transcoder
        PNGTranscoder t = new PNGTranscoder();

        // Set the transcoding hints.
        t.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);

        // Create the transcoder input.
        String svgURI = new File("repeatWhenForFlux.svg").toURI().toString();
        TranscoderInput input = new TranscoderInput(svgURI);

        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream("out1.png");
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
        System.exit(0);

    }

    private final HttpClient httpClient = HttpClient.create().secure().baseUrl("https://projectreactor.io/docs");

    public ReactorDocs(){
        transcoder.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);
        //System.setProperty("javax.xml.parsers.SAXParserFactory", SAXParserFactoryImpl.class.getName());
    }

    @Override
    protected Mono<?> initModule() {
        return Mono.empty();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Mono<String> getFile() {
        String fileName = "saved.html";

        Mono<String> fetch = getHttpClient()
            .get()
            .uri("/core/release/api/reactor/core/publisher/Flux.html")
            .responseSingle((resp, buf) ->
                buf.asString()
                    //.map(Jsoup::parse)
                    //.doOnNext(doc -> doc.setBaseUri(resp.resourceUrl()))
            ).flatMap(s -> Mono.fromCallable(() -> Files.writeString(Paths.get(fileName), s)).thenReturn(s));

        return Mono.fromCallable(() -> Files.readString(Paths.get(fileName)))
            .onErrorReturn("")
            .filter(s -> !s.isBlank())
            .switchIfEmpty(fetch);
    }

    // I know, hanging IDs. Imma change that later.
    // 745140882029805568/745140882029805571
    public Mono<String> getImage(CommandCtx ctx, String svgUrl){
        Function<Mono<Tuple2<InputStream, PipedOutputStream>>, Mono<PipedOutputStream>> processor = self ->
            self.map(t -> t
                    .mapT1(TranscoderInput::new)
                    .mapT2(TranscoderOutput::new)
                )
                .flatMap(function((i,o) -> Mono.fromCallable(() -> {transcoder.transcode(i,o); return 0;})))
                .then(self)
                .map(Tuple2::getT2);

        return getHttpClient().get()
            .uri(svgUrl)
            .responseContent()
            .aggregate()
            .asInputStream()
            .zipWith(Mono.fromCallable(PipedOutputStream::new))
            .transform(processor)
            .flatMap(o -> Mono.fromCallable(() -> new PipedInputStream(o)))
            .flatMap(i -> ctx.getClient().getChannelById(Snowflake.of(745140882029805571L)).ofType(MessageChannel.class)
                .flatMap(c -> c.createMessage(spec -> spec.addFile("image", i))))
            .flatMapIterable(Message::getAttachments)
            .next()
            .map(Attachment::getUrl);
    }
}
