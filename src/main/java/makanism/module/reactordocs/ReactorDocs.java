package makanism.module.reactordocs;

import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.annotation.GuildModuleInfo;
import makanism.module.reactordocs.commands.ReactorDoc;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@GuildModuleInfo(
    commands = {
        ReactorDoc.class
    }
)
public class ReactorDocs extends GuildModule<DocsConfig> {
    private static final Logger log = LoggerFactory.getLogger(ReactorDocs.class);

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
}
