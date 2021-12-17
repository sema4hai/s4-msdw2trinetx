package com.delivery.pharma.sema4.loinc2hpo.s4_pd_dmsdw;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.delivery.pharma.sema4.loinc2hpo.s4_pd_dmsdw.cmd.DmsdwCMD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class AppRunner implements CommandLineRunner {

    @Autowired @Qualifier("dmsdw2trinetx")
    DmsdwCMD dmsdw2trinetx;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Start Sema4DMSDW2TriNetX-Lite CliRunner...");

        long startTime = System.currentTimeMillis();

        JCommander jc = JCommander.newBuilder()
                .addObject(this)
                .acceptUnknownOptions(true) //this is for the sake of passing Spring variables
                .addCommand("dmsdw2trinetx", dmsdw2trinetx)
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

        DmsdwCMD cmd = null;
        System.out.println("Starting com.sema4.s4-dmsdw-2-trinetx.command " + command);

        switch (command) {
            case "dmsdw2trinetx":
                cmd = dmsdw2trinetx;
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
