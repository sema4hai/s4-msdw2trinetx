package com.delivery.pharma.sema4.msdw;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.delivery.pharma.sema4.msdw.command.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CliRunner implements CommandLineRunner {

    @Autowired @Qualifier("msdw2trinetx")
    Command msdw2trinetx;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Start Sema4MSDW2TriNetX-Lite CliRunner...");

        long startTime = System.currentTimeMillis();

        JCommander jc = JCommander.newBuilder()
                .addObject(this)
                .acceptUnknownOptions(true) //this is for the sake of passing Spring variables
                .addCommand("msdw2trinetx", msdw2trinetx)
                .build();

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            for (String arg : args) {
                if (arg.contains("h")) {
                    jc.usage();
                    System.exit(0);
                }
            }
            e.printStackTrace();
            jc.usage();
            System.exit(0);
        }

        String command = jc.getParsedCommand();

        if (command == null) {
            jc.usage();
            System.exit(0);
        }

        Command cmd = null;
        System.out.println("Starting com.sema4.s4-msdw-2-trinetx.command " + command);

        switch (command) {
            case "msdw2trinetx":
                cmd = msdw2trinetx;
                break;
            default:
                System.err.println(String.format("[ERROR] com.sema4.s4loinc2hpo.command \"%s\" not recognized",command));
                jc.usage();
                System.exit(1);
        }

        cmd.run();

        long stopTime = System.currentTimeMillis();
        System.out.println("S4Loinc2HpoCli: Elapsed time was " + (stopTime - startTime)*(1.0)/1000 + " seconds.");

    }
}
