package org.example.tears.Config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PasswordGenerator {

    public String generate() {
        return UUID.randomUUID().toString()
                .substring(0, 10) + "@A1";
    }
}
