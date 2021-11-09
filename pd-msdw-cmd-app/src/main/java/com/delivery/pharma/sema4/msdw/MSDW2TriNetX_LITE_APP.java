package com.delivery.pharma.sema4.msdw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run this application to transform MSDW into a model similar but not identical to TriNetX.
 *
 */
@SpringBootApplication
public class MSDW2TriNetX_LITE_APP {
    public static void main( String[] args ) {
        SpringApplication.run(MSDW2TriNetX_LITE_APP.class, args);
    }
}
