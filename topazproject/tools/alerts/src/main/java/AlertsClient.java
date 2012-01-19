/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import org.topazproject.ws.alerts.AlertsClientFactory;
import org.topazproject.ws.alerts.Alerts;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

/**
 * Force alerts to be sent.
 *
 * Usage:
 * <pre>
 *   mvn -o -PAlertsClient -Dargs="--help"
 * </pre>
 *
 * @author Eric Brown
 */
public class AlertsClient {
  private static final String ALERTS = "<local:///topazproject#filter:model=alerts>";
  private static final String PREFS = "<local:///topazproject#filter:model=preferences>";
  private static final String DEFAULT_HOST = "localhost";
  private static final String DEFAULT_PORT = "8008";
  private static final String DEFAULT_LOC = "/ws-alerts-webapp/services/AlertsServicePort";

  private static final Options options;
  private static CommandLine cmd;
  private static String userid;
  private static String email;
  private static String date;
  private static int count;
  private static String uri;
  
  static {
    options = new Options();
    options.addOption("e", "email", true, "the email address to trigger an alert on");
    options.addOption("d", "date",  true, "the date to use for the desired function");
    options.addOption("n", "count", true, "the number of alerts to send");
    options.addOption("h", "host",  true, "host to find alerts on");
    options.addOption("p", "port",  true, "port to find alerts service on");
    options.addOption("u", "userid", true, "the userid to start or clear");
    options.addOption("U", "alerturi", true, "actual URI for alerts service");
    options.addOption("S", "start", false, "start a user (optionally to the supplied date)");
    options.addOption("C", "clear", false, "stop sending alerts to the specified user");
    options.addOption("s", "send",  false, "send alert(s) (1 if email address is specified)");
    options.addOption("?", "help",  false, "usage information");
  }

  private static void parse(String[] args) {
    try {
      CommandLineParser parser = new PosixParser();
      cmd    = parser.parse(options, args);
      count  = Integer.valueOf(cmd.getOptionValue("n", "0"));
      userid = cmd.getOptionValue("u");
      email  = cmd.getOptionValue("e");
      date   = cmd.getOptionValue("d");
      uri    = cmd.getOptionValue("U");
      if (uri == null) {
        String host = cmd.getOptionValue("h", DEFAULT_HOST);
        String port = cmd.getOptionValue("p", DEFAULT_PORT);
        uri = "http://" + host + ":" + port + DEFAULT_LOC;
      }
    } catch (ParseException pe) {
      System.err.println("Invalid arguments: " + pe.getMessage());
      usage();
    }

    if (cmd.hasOption("?"))
      usage();
  }

  private static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("alertscli", options);
    System.exit(1);
  }
  
  public static void main(String[] args) throws Exception {
    parse(args);

    Alerts service = AlertsClientFactory.create(uri);
    if (cmd.hasOption("s")) {
      if (email != null && date != null)
        service.sendAlert(date, email);
      else if (count > 0)
        service.sendAlerts(date, count);
      else
        service.sendAllAlerts();
    }
    else if (cmd.hasOption("C"))
      service.clearUser(userid);
    else if (cmd.hasOption("S"))
      service.startUser(userid, date);
    else
      usage();
  }
}
