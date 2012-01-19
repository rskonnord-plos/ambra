/* $HeadURL:: $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import java.net.MalformedURLException;
import java.net.URI;

import java.rmi.RemoteException;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;

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

/**
 * Return various useful information about users from Mulgara.
 *
 *        mvn -o -f topazproject/tools/alerts/pom.xml -DUsers
 *                 -Dargs="-uri <mulgara uri> -email <user email>"
 *        mvn -o -f topazproject/tools/alerts/pom.xml -DUsers
 *                 -Dargs="-uri <mulgara uri> -topazId <topaz account id>"
 *
 * @author Amit Kapoor
 */
public class Users {
  private static final String FOAF_URI = "http://xmlns.com/foaf/0.1/";
  private static final String PROFILES = "<local:///topazproject#filter:model=profiles>";

  // The user account
  private static final String ACCOUNT =
    "select $user $account " +
    " from ${PROFILES} " +
    "   where $user <foaf:mbox> ${email} " +
    "   and $user <foaf:holdsAccount> $account;";

  private ItqlHelper itql;
  private static final Options options;

  // Set up the command line options
  static {
    options = new Options();
    options.addOption(OptionBuilder.withArgName("mulgara uri").hasArg().
        isRequired(true).withValueSeparator(' ').
        withDescription("URI to access Mulgara").  create("uri"));

    OptionGroup xform = new OptionGroup();
    xform.addOption(OptionBuilder.withArgName("Email address").hasArg().
        withValueSeparator(' ').
        withDescription("Email address used to lookup account").create("email"));
    /**
      xform.addOption(OptionBuilder.withArgName("Topaz URI").hasArg().
      withValueSeparator(' ').
      withDescription("Topaz User uri, exapmple, info:doi/10.1371/account/1").
      create("topazId"));
     */
    options.addOptionGroup(xform);
  }

  /**
   * Creates a new AlertsInfo object.
   *
   * @param mulgaraUri the mulgara service uri
   *
   * @throws MalformedURLException if service's uri is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public Users(URI mulgaraUri)
    throws MalformedURLException, ServiceException, RemoteException {
    itql = new ItqlHelper(mulgaraUri);
  }

  /**
   * Queries the ITQL database and returns the account id
   * 
   * @return returns the account as string
   * 
   * @throws ItqlInterpreterException if an exception was encountered while
   *         processing the queries
   * @throws AnswerException if an exception occurred parsing the query response
   * @throws RemoteException if an exception occurred talking to the service
   */
  public String[] getValues(String email)
    throws ItqlInterpreterException, AnswerException, RemoteException {
    AnswerSet ans;
    String query = ItqlHelper.bindValues(ACCOUNT, "PROFILES", PROFILES, "email", email);
    Map aliases = ItqlHelper.getDefaultAliases();
    aliases.put("foaf", FOAF_URI);

    synchronized (this) {
      ans = new AnswerSet(itql.doQuery(query, aliases));
    }
    ans.next();
    AnswerSet.QueryAnswerSet rows = ans.getQueryResults();

    List infos = new ArrayList();
    while (rows.next()) {
      StringBuffer buf = new StringBuffer();
      buf.append(rows.getString("user"));
      buf.append(',');
      buf.append(rows.getString("account"));
      infos.add(buf.toString());
    }
    itql.close();

    return (String[]) infos.toArray(new String[infos.size()]);
  }

  // Convert from string to array of strings
  private static final String[] parseArgs(String cmdLine) {
    return new StrTokenizer(cmdLine, StrMatcher.trimMatcher(),
        StrMatcher.quoteMatcher()).getTokenArray();
  }

  // Print help message
  private static final void help() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Users", options);
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
      Users alerts = new Users(new URI(line.getOptionValue("uri")));

      if (line.hasOption("email")) {
        String[] values = alerts.getValues("<mailto:" + line.getOptionValue("email")+ ">");
        for (int idx = 0; idx < values.length; idx++) {
          System.out.println(values[idx]);
        }
      }
    } catch( ParseException exp ) {
        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        help();
    }
  }
}
