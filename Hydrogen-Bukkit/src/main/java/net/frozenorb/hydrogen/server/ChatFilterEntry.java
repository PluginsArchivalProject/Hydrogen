package net.frozenorb.hydrogen.server;

import lombok.Data;

@Data
public class ChatFilterEntry {

    private final String id;
    private final String regex;

}
