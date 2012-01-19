/* $HeadURL:: $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import java.util.List;
import java.util.ArrayList;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

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
import org.topazproject.ws.article.IngestException;
import org.topazproject.ws.article.DuplicateArticleIdException;
import org.topazproject.ws.article.NoSuchArticleIdException;

/**
 * This tool can be used to perform various articles related operations in
 * Topaz.
 *
 * To ingest an article:
 *        mvn -o -f topazproject/tools/rss/pom.xml -DArticles
 *            -Dargs="-uri <Topaz article uri> -ingest <zip file>"
 *
 * To delete an article:
 *        mvn -o -f topazproject/tools/rss/pom.xml -DArticles
 *            -Dargs="-uri <Topaz article uri> -delete <article URI>"
 *
 * @author Amit Kapoor
 */
public class Articles {
  // The different operations we can perform on Articles
  private static final String INGEST = "ingest";
  private static final String DELETE = "delete";

  private Article service;

  private static final Options options;

  // Set up the command line options
  static {
    options = new Options();

    options.addOption(OptionBuilder.withArgName("Topaz Article URI").hasArg().
        isRequired(true).withValueSeparator(' ').
        withDescription("URI to access article service").create("uri"));

    OptionGroup params = new OptionGroup();
    params.addOption(OptionBuilder.withArgName("Zipped article file").hasArg().
        withValueSeparator(' ').withDescription("Ingest the zip file containing article").
        create(INGEST));
    params.addOption(OptionBuilder.withArgName("Article URI").hasArg().
        withValueSeparator(' ').withDescription("Delete the article associated with the URI").
        create(DELETE));
    options.addOptionGroup(params);
  }

  /**
   * Creates a new Articles object.
   *
   * @param articleUri the Topaz article service uri
   *
   * @throws MalformedURLException if service's uri is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public Articles(String articleUri)
    throws MalformedURLException, ServiceException, RemoteException {
    service = ArticleClientFactory.create(articleUri);
  }

  /**
   * Carries out the actual ingestion of the article.
   *
   * @return returns the URI of the ingested article
   *
   * @throws MalformedURLException if the URL created from Zip file name is
   *                               wrong
   * @throws DuplicateArticleIdException if the article has already been
   *                                     ingested
   * @throws IngestException problems related to ingesting the article
   * @throws RemoteException if an exception occurred talking to the service
   */
  public String ingestArticle(String zipFileName) 
    throws MalformedURLException, DuplicateArticleIdException, 
                    IngestException, RemoteException {
             return service.ingest(new DataHandler(new File(zipFileName).toURL()));
  }

  /**
   * Delete the article specified by the uri
   *
   * @param uri the URI of the article to delete
   *
   * @throws NoSuchArticleIdException no article associated with the passed URI
   * @throws RemoteException if an exception occured talking to the service
   */
   public void deleteArticle(String uri)
     throws RemoteException, NoSuchArticleIdException {
     service.delete(uri);
   }

  // Convert from string to array of strings
  private static final String[] parseArgs(String cmdLine) {
    return new StrTokenizer(cmdLine, StrMatcher.trimMatcher(),
        StrMatcher.quoteMatcher()).getTokenArray();
  }

  // Print help message
  private static final void help() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Articles", options);
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
      Articles articles = new Articles(line.getOptionValue("uri"));

      // The command to execute
      if (line.hasOption(INGEST)) {
        System.out.println("URI: " + articles.ingestArticle(line.getOptionValue(INGEST)));
      } else if (line.hasOption(DELETE)) {
        articles.deleteArticle(line.getOptionValue(DELETE));
      }
    } catch( ParseException exp ) {
        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        help();
    }
  }
}
