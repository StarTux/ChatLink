/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012 StarTux
 *
 * This file is part of ChatLink.
 *
 * ChatLink is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * ChatLink is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with ChatLink.  If not, see
 * <http://www.gnu.org/licenses/>.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.winthier.chatlink.packet;

import java.util.Map;
import java.util.HashMap;

public class WhisperPacket implements Packet {
    public final String sender;
    public final String recipient;
    public final String message;

    public WhisperPacket(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    public Map<String, String> serialize() {
        Map<String, String> result = new HashMap<>();
        result.put("type", "Whisper");
        result.put("sender", sender);
        result.put("recipient", recipient);
        result.put("message", message);
        return result;
    }

    public static WhisperPacket deserialize(Map<String, String> map) {
        return new WhisperPacket(
            (String)map.get("sender"),
            (String)map.get("recipient"),
            (String)map.get("message")
            );
    }
}
