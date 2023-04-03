package com.github.lamtong.msp;

import io.github.lamtong.msp.autoconfigure.ScanMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@ScanMapper
@SpringBootApplication
public class MspApplication {

    public static void main(String[] args) {
        SpringApplication.run(MspApplication.class, args);
    }

}
