/* $HeadURL:: $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import java.io.StringReader;
import java.io.StringWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleClientFactory;

import javax.xml.rpc.ServiceException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

/**
 * Return information on articles in Topaz through various filter. This
 * information can then be transformed either using built-in stylesheets or
 * through externally passed stylesheets.
 *
 * To store native XML:
 *
 *        mvn -o -f topazproject/tools/rss/pom.xml -DRSSInfo \
 *                 -Dargs="-uri <Topaz article uri>"
 *
 * To transform to RSS 2.0
 *
 *        mvn -o -f topazproject/tools/rss/pom.xml -DRSSInfo \
 *                 -Dargs="-uri <Topaz article uri> -rss"
 *
 * To transform using external stylesheet
 *
 *        mvn -o -f topazproject/tools/rss/pom.xml -DRSSInfo \
 *                 -Dargs="-uri <Topaz article uri> -xslt <xlst file name>"
 *
 * To generate a pseudo crawl page
 *
 *        mvn -o -f topazproject/tools/rss/pom.xml -DRSSInfo \
 *                 -Dargs="-uri <Topaz article uri> -crawl"
 *
 *
 * @author Amit Kapoor
 */
public class RSSInfo {
  private Article service;

  private static final Options options;

  // Set up the command line options
  static {
    options = new Options();

    OptionGroup urlOption = new OptionGroup();
    urlOption.addOption(OptionBuilder.withArgName("Topaz article uri").hasArg().
        isRequired(true).withValueSeparator(' ').
        withDescription("URI to access article service").create("uri"));
    urlOption.addOption(OptionBuilder.withArgName("Topaz base uri").hasArg().
        withValueSeparator(' ').withDescription("Base URL to access Topaz").
        create("baseURL"));
    options.addOptionGroup(urlOption);

    OptionGroup xform = new OptionGroup();
    xform.addOption(new Option("rss","Transform using inbuilt RSS stylesheet"));
    xform.addOption(new Option("crawl","Create HTML output page for crawler"));
    xform.addOption(OptionBuilder.withArgName("XSLT stylesheet file").hasArg().
        withValueSeparator(' ').withDescription("Transform the received XML with XSLT").
        create("xslt"));
    options.addOptionGroup(xform);

    // RSS Parameters
    options.addOption(OptionBuilder.withArgName("quoted string").hasArg().
        withDescription("RSS Title").create("rssTitle"));
    options.addOption(OptionBuilder.withArgName("URL").hasArg().
        withValueSeparator(' ').withDescription("RSS Link").create("rssLink"));
    options.addOption(OptionBuilder.withArgName("URL").hasArg().
        withValueSeparator(' ').withDescription("RSS Image Link").create("rssImage"));
    options.addOption(OptionBuilder.withArgName("quoted string").hasArg().
        withDescription("RSS Description").create("rssDescription"));
    options.addOption(OptionBuilder.withArgName("Prefix for link").hasArg().
        withValueSeparator(' ').withDescription("Prefix added to article URI").
        create("linkPrefix"));

    options.addOption(OptionBuilder.withArgName("File name").hasArg().
        withValueSeparator(' ').withDescription("Write output to file").
        create("out"));
    options.addOption(OptionBuilder.withArgName("yyyy-MM-dd'T'HH:mm:ss").hasArg().
        withValueSeparator(' ').withDescription("Start date for articles").
        create("startDate"));
    options.addOption(OptionBuilder.withArgName("yyyy-MM-dd'T'HH:mm:ss").hasArg().
        withValueSeparator(' ').withDescription("End date for articles").
        create("endDate"));
    options.addOption(OptionBuilder.withArgName("category1 category2..").hasArg().
        withDescription("List of categories").create("categories"));
    options.addOption(OptionBuilder.withArgName("author1 author2..").hasArg().
        withDescription("List of authors").create("authors"));
  }

  /**
   * Creates a new RSSInfo object.
   *
   * @param articleUri the Topaz article service uri
   *
   * @throws MalformedURLException if service's uri is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public RSSInfo(String articleUri)
    throws MalformedURLException, ServiceException, RemoteException {
    service = ArticleClientFactory.create(articleUri);
  }

  /**
   * Queries the Topaz article service and returns the requisite information as
   * a string (XML).
   *
   * @return returns an information as a string
   *
   * @throws RemoteException if an exception occurred talking to the service
   */
  public String getFeed(String startDate, String endDate, String[] categories,
      String[] authors, int[] states, boolean ascending)
    throws RemoteException {
    return service.getArticles(startDate, endDate, categories, authors, states, ascending);
  }

  // Convert from string to array of strings
  private static final String[] parseArgs(String cmdLine) {
    return new StrTokenizer(cmdLine, StrMatcher.trimMatcher(),
        StrMatcher.quoteMatcher()).getTokenArray();
  }

  // Convert from string to array of strings
  private static final String[] parseList(String line) {
    String[] args = new StrTokenizer(line, StrMatcher.commaMatcher(),
        StrMatcher.quoteMatcher()).getTokenArray();
    for (int idx = 0; idx < args.length; idx++) {
      args[idx] = args[idx].trim();
    }

    return args;
  }

  // Print help message
  private static final void help() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("RSSInfo", options);
    System.exit(0);
  }

  /**
   * main
   *
   * @param args the list of arguments
   */
  public static void main(String[] args) throws Exception {
    // for broken exec:java : parse the command line ourselves
    if (args.length == 1 && args[0] == null) {
      help();
    }
    if (args.length == 1 && args[0].indexOf(' ') > 0)
      args = parseArgs(args[0]);

    CommandLineParser parser = new GnuParser();
    try {
        // parse the command line arguments
      CommandLine line = parser.parse(options, args);
      String uri = null;
      if (line.hasOption("uri")) {
        uri = line.getOptionValue("uri");
      } else if (line.hasOption("baseURL")) {
        uri = line.getOptionValue("baseURL") + ":8008/ws-articles-webapp/services/ArticleServicePort";
      } else {
        help();
      }
      RSSInfo rss = new RSSInfo(uri);

      // Grab various parameters
      String startDate = line.getOptionValue("startDate");
      String endDate = line.getOptionValue("endDate");
      String[] categories = null;
      if (line.hasOption("categories")) {
        categories = parseList(line.getOptionValue("categories"));
      }
      String[] authors = null;
      if (line.hasOption("authors")) {
        authors = parseList(line.getOptionValue("authors"));
      }
      int[] states = null;
      boolean ascending = false;

      String feed = rss.getFeed(startDate, endDate, categories, authors, states, ascending);
      // Style sheet specified?
      String xslt = null;
      if (line.hasOption("rss")) {
        xslt = rss.getClass().getResource("XMLToRSS.xslt").toString();
      } else if (line.hasOption("crawl")) {
        xslt = rss.getClass().getResource("XMLToHTML.xslt").toString();
      } else if (line.hasOption("xslt")) {
        xslt = line.getOptionValue("xslt");
      }

      // Carry out transformation if necessary
      if (xslt != null) {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(xslt));
        // Set the XSLT parameters
        if (line.hasOption("rssTitle")) {
          transformer.setParameter("rssTitle",line.getOptionValue("rssTitle"));
        }
        if (line.hasOption("rssLink")) {
          transformer.setParameter("rssLink",line.getOptionValue("rssLink"));
        }
        if (line.hasOption("rssImage")) {
          transformer.setParameter("rssImage",line.getOptionValue("rssImage"));
        }
        if (line.hasOption("rssDescription")) {
          transformer.setParameter("rssDescription",line.getOptionValue("rssDescription"));
        }
        if (line.hasOption("linkPrefix")) {
          transformer.setParameter("linkPrefix",line.getOptionValue("linkPrefix"));
        }

        StringWriter out = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(feed)), new StreamResult(out));
        feed = out.toString();
      }

      OutputStream out = System.out;
      if (line.hasOption("out")) {
        out = new FileOutputStream(line.getOptionValue("out"));
      }
      out.write(feed.getBytes("UTF-8"));
    } catch( ParseException exp ) {
        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        help();
    }
  }
}
