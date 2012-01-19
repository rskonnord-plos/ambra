/* $HeadURL:: $
 * $Id: $
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
 * Return various useful alerts information from Mulgara. To execute
 * this using maven, please use:
 *
 *        mvn -o -f topazproject/tools/alerts/pom.xml -DAlertsInfo
 *                 -Dargs="-uri <mulgara uri>"
 *
 * @author Amit Kapoor
 */
public class AlertsInfo {
  private static final String ALERTS = "<local:///topazproject#filter:model=alerts>";
  private static final String PREFS = "<local:///topazproject#filter:model=preferences>";

  private static final String QUERY =
    // The user
    "select $user " +
    // email
    "    subquery(select $email from " +
    "             ${PREFS} where $pref <topaz:preference> $prefn and " +
    "             $prefn <topaz:prefName> 'alertsEmailAddress' and " +
    "             $prefn <topaz:prefValue> $email) " +
    // Journal alerts subscription
    "    subquery(select $alertsJournals from " +
    "             ${PREFS} where $pref <topaz:preference> $prefn and " +
    "             $prefn <topaz:prefName> 'alertsJournals' and " +
    "             $prefn <topaz:prefValue> $alertsJournals) " +
    // Categories of interest
    "    subquery(select $alertsCategories from " +
    "             ${PREFS} where $pref <topaz:preference> $prefn and " +
    "             $prefn <topaz:prefName> 'alertsCategories' and " +
    "             $prefn <topaz:prefValue> $alertsCategories) " +
    " from ${PREFS} " +
    "   where $user <topaz:hasPreferences> $pref " +
    "   and $pref <dc_terms:mediator> 'alerts' " +
    " order by $user;";

  private ItqlHelper itql;

  private static final Options options;

  // Set up the command line options
  static {
    options = new Options();
    OptionGroup urlOption = new OptionGroup();
    urlOption.addOption(OptionBuilder.withArgName("mulgara uri").hasArg().
        withValueSeparator(' ').withDescription("URI to access Mulgara").create("uri"));
    urlOption.addOption(OptionBuilder.withArgName("mulgara base uri").hasArg().
        withValueSeparator(' ').withDescription("Base URL to access Mulgara").
        create("baseURL"));
    options.addOptionGroup(urlOption);
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
  public AlertsInfo(URI mulgaraUri)
    throws MalformedURLException, ServiceException, RemoteException {
    itql = new ItqlHelper(mulgaraUri);
  }

  /**
   * Queries the ITQL database and returns all the values as a string array
   *
   * @return returns an array of strings
   *
   * @throws ItqlInterpreterException if an exception was encountered while
   *         processing the queries
   * @throws AnswerException if an exception occurred parsing the query response
   * @throws RemoteException if an exception occurred talking to the service
   */
  public String[] getValues()
    throws ItqlInterpreterException, AnswerException, RemoteException {
    AnswerSet ans;
    String query = ItqlHelper.bindValues(QUERY, "PREFS", PREFS, "ALERTS", ALERTS);

    synchronized (this) {
      ans = new AnswerSet(itql.doQuery(query, null));
    }
    ans.next();
    AnswerSet.QueryAnswerSet rows = ans.getQueryResults();

    List infos = new ArrayList();
    while (rows.next()) {
      StringBuffer buf = new StringBuffer();
      buf.append(rows.getString("user"));
      buf.append(',');
      // emails
      AnswerSet.QueryAnswerSet subAns = (AnswerSet.QueryAnswerSet)
        rows.getSubQueryResults(1);
      while(subAns.next()) {
        buf.append("email:");
        buf.append(subAns.getString("email"));
        buf.append(',');
      }
      // Journal alerts
      subAns = (AnswerSet.QueryAnswerSet) rows.getSubQueryResults(2);
      while(subAns.next()) {
        buf.append("journal_alerts:");
        buf.append(subAns.getString("alertsJournals"));
        buf.append(',');
      }
      // Categories
      subAns = (AnswerSet.QueryAnswerSet) rows.getSubQueryResults(3);
      while(subAns.next()) {
        buf.append("journal_categories:");
        buf.append(subAns.getString("alertsCategories"));
        buf.append(',');
      }

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
    formatter.printHelp("AlertsInfo", options);
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
        uri = line.getOptionValue("baseURL") + ":9091/mulgara-service/services/ItqlBeanService";
      } else {
        help();
      }
      AlertsInfo alerts = new AlertsInfo(new URI(uri));
      String[] values = alerts.getValues();
      for (int idx = 0; idx < values.length; idx++) {
        System.out.println(values[idx]);
      }
    } catch( ParseException exp ) {
        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        help();
    }
  }
}
