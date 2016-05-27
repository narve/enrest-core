package no.dv8.dirs;

import no.dv8.enrest.Exchange;
import no.dv8.functions.XBiConsumer;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toList;

public class Dirs implements UnaryOperator<Exchange> {

    final String baseDir;
    final String prefix;

    public Dirs(String baseDir, String prefix) {
        this.baseDir = baseDir;
        this.prefix = prefix;
    }

    private Object dirs( String inPath) throws IOException {
        ol l = new ol();
        Path root = Paths.get( baseDir);
        String s = inPath.substring( inPath.indexOf( prefix) + prefix.length() );
        Path p = Paths.get( root.toString(), s );
        return list(root, p);

    }

    private List listItems(Path root, Path path ) throws IOException {
        return Files.list( path )
          .filter( p -> !p.getFileName().toString().startsWith("."))
          .sorted()
          .map( p -> new li().add( new a( p.toString() ).href( prefix + "/" + root.relativize(p).toString() ) ) )
          .collect( toList() );
    }

    public Element fileInfo(Path p ) {
        return new p()
          .add( new span( "filesize: " + p.toFile().getAbsoluteFile().length()))
          .add( new span(
            "mimetype: " + MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(p.toFile().getAbsoluteFile()))
          );
    }

    public Element list(Path root, Path path)  {
        try {
            return
              new div()
                .add( new h1( path.toString()))
                .add( Files.isDirectory( path ) ? new ol().add( listItems( root, path ) ) : fileInfo(path) );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Dirs{" +
          "baseDir='" + baseDir + '\'' +
          ", prefix='" + prefix + '\'' +
          '}';
    }

    @Override
    public Exchange apply(Exchange exchange) {
        try {
            String p = URLDecoder.decode( new URL( exchange.req.getRequestURL().toString()).getFile(), "utf-8");
            return exchange.withEntity( dirs(p));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
