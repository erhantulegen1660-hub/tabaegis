package dev.aegis.tab;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public final class AegisTab extends JavaPlugin implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private List<String> header = List.of();
    private List<String> footer = List.of();
    private BukkitTask task;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        start();
    }

    @Override
    public void onDisable() {
        if (task != null) task.cancel();
    }

    private void start() {
        if (task != null) task.cancel();
        reloadConfig();
        header = getConfig().getStringList("header");
        footer = getConfig().getStringList("footer");
        long period = Math.max(5L, getConfig().getLong("update-ticks", 20L));
        task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) send(p);
        }, 1L, period);
    }

    private void send(Player p) {
        TagResolver r = TagResolver.resolver(
                Placeholder.unparsed("online", Integer.toString(Bukkit.getOnlinePlayers().size())),
                Placeholder.unparsed("max", Integer.toString(Bukkit.getMaxPlayers())),
                Placeholder.unparsed("ping", Integer.toString(p.getPing())),
                Placeholder.unparsed("version", Bukkit.getMinecraftVersion()));
        p.sendPlayerListHeaderAndFooter(build(header, r), build(footer, r));
    }

    private Component build(List<String> lines, TagResolver r) {
        List<Component> parts = new ArrayList<>(lines.size());
        for (String line : lines) parts.add(MM.deserialize(line, r));
        return Component.join(JoinConfiguration.newlines(), parts);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        send(e.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            start();
            sender.sendMessage(Component.text("AegisTab перезагружен."));
        } else {
            sender.sendMessage(Component.text("/aegistab reload"));
        }
        return true;
    }
}
