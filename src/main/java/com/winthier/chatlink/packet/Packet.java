package com.winthier.chatlink.packet;

import java.util.Map;


public interface Packet {
    Map<String, String> serialize();
}
