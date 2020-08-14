package makanism.module.reactordocs.defs;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static reactor.function.TupleUtils.function;

public class MethodDef implements DocDef {
    private final Element methodName;
    private final Element methodSig;
    private final Element methodDesc;

    private final Elements images;
    private final List<Tuple2<Element, Elements>> fields;

    private MethodDef(Element methodName, Element methodSig, Element methodDesc, Elements images, List<Tuple2<Element, Elements>> fields) {
        this.methodName = methodName;
        this.methodSig = methodSig;
        this.methodDesc = methodDesc;
        this.images = images;
        this.fields = fields;
    }

    public static MethodDef from(Element block){
        Element methodName  = block.selectFirst("h4");
        Element methodSig   = block.selectFirst("pre");
        Element methodDesc  = block.selectFirst("div.block").clone();
        methodDesc.select("img").remove();

        Elements images = block.selectFirst("div.block").select("img");

        Elements dictTree = block.selectFirst("dl").children();

        List<Tuple2<Element, Elements>> fields = new ArrayList<>();
        for (Element item : dictTree) {
            if (item.tagName().equals("dt")) {   // term - field title
                fields.add(Tuples.of(item, new Elements()));
            } else if (item.tagName().equals("dd") && !fields.isEmpty()) {
                fields.set(fields.size() -1,
                    fields.get(fields.size()-1)
                        .mapT2(it -> {it.add(item); return it;}));

            }
        }

        return new MethodDef(methodName, methodSig, methodDesc, images, fields);
    }

    public String name(){
        return methodName.text();       // no need to format
    }

    public String signature(){
        return methodSig.text();
    }

    public String description(){
        return formatMd(methodDesc);
    }

    //                  title, content
    public List<Tuple2<String, String>> fields(){
        return fields.stream()
            .map(function((label, defs) -> {
                String title = label.text();    // no markdown rip
                String desc = defs.stream()
                    .map(this::formatMd)
                    .map("\n"::concat)
                    .collect(Collectors.joining());
                return Tuples.of(title, desc);
            }))
            .collect(Collectors.toUnmodifiableList());
    }

    public List<String> images(){
        return images.eachAttr("abs:src");
    }
}
