package makanism.module.reactordocs.defs;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

public interface DocDef {
    String MISSING = "[missing]";

    default String formatMd(Element _el){
        // ` > ** > _ > []()
        // <code>, <b>, <i>, <a>
        Element el = _el.clone();

        el.select("code").forEach(e -> e.replaceWith(e.wholeText().split("\n").length > 1 ?  new TextNode("``` " + e.text() + "```") : new TextNode("`" + e.text() + "`")));
        el.select("b").forEach(e -> e.replaceWith(new TextNode("**" + e.html() + "**")));
        el.select("i").forEach(e -> e.replaceWith(new TextNode("_" + e.html() + "_")));
        el.select("u").forEach(e -> e.replaceWith(new TextNode("__" + e.html() + "__")));
        el.select("a").forEach(e -> e.replaceWith(new TextNode("[" + e.html() + "](" + e.attr("abs:href") + " \"" + e.attr("abs:href") + "\")")));
        el.select("p").forEach(e -> e.replaceWith(new TextNode("%%NL%%" + e.html())));
        return el.text().replace("%%NL%%", "\n");
    }
}
