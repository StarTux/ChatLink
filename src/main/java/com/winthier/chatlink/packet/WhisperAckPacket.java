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

public class WhisperAckPacket implements Packet {
        public final String recipient;
        public final String sender;
        public final String message;

        public WhisperAckPacket(String recipient, String sender, String message) {
                this.recipient = recipient;
                this.sender = sender;
                this.message = message;
        }

    public Map<String, String> serialize() {
        Map<String, String> result = new HashMap<>();
        result.put("type", "WhisperAck");
        result.put("sender", sender);
        result.put("recipient", recipient);
        result.put("message", message);
        return result;
    }

    public static WhisperAckPacket deserialize(Map<String, String> map) {
        return new WhisperAckPacket(
            (String)map.get("sender"),
            (String)map.get("recipient"),
            (String)map.get("message")
            );
    }
}
