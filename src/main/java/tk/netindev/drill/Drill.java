package tk.netindev.drill;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.drill.miner.Miner;
import tk.netindev.drill.util.Misc;

/**
 *
 * @author netindev
 *
 */
public class Drill {

   private static final Logger logger = LoggerFactory
         .getLogger(Drill.class.getName());
   public static final double PACKAGE_VERSION = 0.02D;

   public static void main(String[] args) {
      if (args.length == 0) {
         logger.error(
               "Invalid arguments, please add to the arguments \"--help\".");
         return;
      }
      try {
         System.out.println("Drill cryptonight miner, written by netindev, V: "
               + PACKAGE_VERSION);
         final String system = System.getProperty("os.name").toLowerCase();
         if (!(system.indexOf("win") >= 0 || system.indexOf("nix") >= 0
               || system.indexOf("nux") >= 0 || system.indexOf("aix") >= 0)) {
            logger.error("Unfortunately, " + system
                  + " isn't supported at this time.");
            return;
         }
         parseArgs(args);
      } catch (final Throwable e) {
         logger.error(e.getMessage());
      }
   }

   private static void parseArgs(String[] args) {
      final Options options = new Options();
      options.addOption(Option.builder("host").hasArg().required().build());
      options.addOption(Option.builder("user").hasArg().required().build());
      options.addOption(Option.builder("port").hasArg().required().build());

      options.addOption(Option.builder("pass").hasArg().build());
      options.addOption(Option.builder("thread").hasArg().build());
      options.addOption(Option.builder("variant").hasArg().build());
      options.addOption(Option.builder("help").hasArg().build());
      try {
         final CommandLine parse = new DefaultParser().parse(options, args);
         if (parse.hasOption("help")) {
            logger.info("Arguments with * are optional.");
            logger.info(
                  "java -jar drill.jar -host \"localhost\" -user \"netindev.8700k\" -port \"1000\" -pass* \"12345\" -thread* \"7\" -variant* 2");
            return;
         }
         final String host = parse.getOptionValue("host"),
               user = parse.getOptionValue("user"),
               port = parse.getOptionValue("port");
         final String pass = parse.hasOption("pass")
               ? parse.getOptionValue("pass")
               : "",
               thread = parse.hasOption("thread")
                     ? parse.getOptionValue("thread")
                     : String.valueOf(
                           Runtime.getRuntime().availableProcessors() - 1),
               variant = parse.hasOption("variant")
                     ? parse.getOptionValue("variant")
                     : "-1";
         if (!Misc.isInteger(port)) {
            logger.error("The port isn't an integer");
            return;
         } else if (!Misc.isInteger(thread)) {
            logger.error("The thread isn't an integer");
            return;
         }
         new Miner(host, user, pass, Integer.parseInt(port),
               Integer.parseInt(thread), Integer.parseInt(variant)).start();
      } catch (final ParseException e) {
         logger.error(e.getMessage());
         logger.error("Correct use: java -jar scuti-lite.jar --help");
      }
   }

}
