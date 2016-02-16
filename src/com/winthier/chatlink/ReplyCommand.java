/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012-2014 StarTux
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

package com.winthier.chatlink;

import com.winthier.winlink.WinLink;
import com.winthier.winlink.WinLinkPlugin;
import java.util.Collections;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ReplyCommand implements CommandExecutor {
    private ChatLinkPlugin plugin;
    public String senderFormat;
    public String recipientFormat;

    public ReplyCommand(ChatLinkPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("xreply").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        if (args.length < 1) return false;
        String replier = plugin.repliers.get(player.getName());
        if (replier == null) {
            sender.sendMessage(ChatColor.RED + "Nobody messaged you");
            return true;
        }
        StringBuilder sb = new StringBuilder("xmsg ").append(replier);
        for (int i = 0; i < args.length; ++i) {
            sb.append(" ").append(args[i]);
        }
        final String cmd = sb.toString();
        new BukkitRunnable() {
            @Override public void run() {
                player.performCommand(cmd);
            }
        }.runTask(plugin);
        return true;
    }
}
