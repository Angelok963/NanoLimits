package angelok.NanoLimits.com;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Main extends JavaPlugin implements Listener {

	public final static Logger log = Logger.getLogger("Minecraft");

	private File data = new File(getDataFolder() + File.separator + "Players" + File.separator + "data.yml");

	private YamlConfiguration datap;

	private void save() {

		try {
			datap.save(data);
		} catch (IOException r) {
			r.printStackTrace();
		}

	}

	@Override
	public void onEnable() {

		log.info("§7[§cNanoLimits§7] §aПлагин §cNanoLimits§a загружается... Автор: §eangelok§a.");

		YamlConfiguration datap = YamlConfiguration.loadConfiguration(data);

		this.datap = datap;

		File file = new File(getDataFolder() + File.separator + "config.yml");
		if (!file.exists()) {
			getLogger().warning("Конфигурационный файл не найден. Создаю конфиг по умолчанию.");

			FileConfiguration cfg = getConfig();
			
			ArrayList<String> s = new ArrayList<>();
			s.add("BLOCK_NAME");

			cfg.addDefault("Messages.PlayerLimit", "&f[&cЛимит&f] &7Вы можете поставить только &c{limit} {name}&7!");
			cfg.addDefault("Messages.ChunkLimit",
					"&f[&cЛимит&f] &7Вы можете поставить только &c{limit} {name}&7 в чанке!");
			cfg.addDefault("Messages.RegionLimit",
					"&f[&cЛимит&f] &7Вы можете поставить только &c{limit} {name}&7 в привате!");
			cfg.addDefault("Messages.NoBuilderWand", "&f[&cЛимит&f] &7Вы &cне можете &7размещать этот блок строительным жезлом!");
			cfg.addDefault("Messages.not-permission-cmd", "&f[&cЛимит&f] &7У вас &cнедостаточно &7прав!");
			cfg.addDefault("PlayerLimit.enable", false);
			cfg.addDefault("ChunkLimit.enable", false);
			cfg.addDefault("RegionLimit.enable", false);
			cfg.addDefault("Utils.FixBuilderWand.enable", true);
			cfg.addDefault("Utils.FixBuilderWand.BlacklistBlocks", s);
			cfg.addDefault("PlayerLimit.blocks.BLOCK_NAME.default", 0);
			cfg.addDefault("ChunkLimit.blocks.BLOCK_NAME.default", 0);
			cfg.addDefault("RegionLimit.blocks.BLOCK_NAME.default", 0);

			
			
			cfg.options().copyDefaults(true);
		}
		saveConfig();
		reloadConfig();

		if (!data.exists())
			try {
				datap.save(data);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		if (data.exists()) {

			save();

		}

		getServer().getPluginManager().registerEvents(this, this);

	}

	protected static WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {

		String p = e.getPlayer().getName();

		if (datap.contains(p))
			return;

		ArrayList<String> s = new ArrayList<>();

		s.add("world:0:0:0");

		datap.set(p + ".BLOCK", s);

		save();

	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void breakBlock(BlockBreakEvent e) {
		
		Set<String> f = getConfig().getConfigurationSection("PlayerLimit.blocks").getKeys(false);
		
		if(f.contains(e.getBlock().getType().name()) || f.contains(e.getBlock().getType().name() + ":" + String.valueOf(e.getBlock().getData())))
			updateLimitsData();
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void fixWand(PlayerInteractEvent e){
		
		if(!getConfig().getBoolean("Utils.FixBuilderWand.enable")) return;
		ItemStack i = e.getItem();
		
		if(i == null)return;
		
		if(!i.getType().name().equals("EXTRAUTILS2_ITEMBUILDERSWAND")) return;
		
		Block b = e.getClickedBlock();
		
		
		List<String> l = getConfig().getStringList("Utils.FixBuilderWand.BlacklistBlocks");
		
		if(l.contains(b.getType().name()) || l.contains(b.getType().name() + ":" + String.valueOf(b.getData()))){
			e.setCancelled(true);
			
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoBuilderWand")));
		}
	}
	
	
	

	@EventHandler
	public void setBlock(BlockPlaceEvent e) {

    
		
		if (e.isCancelled())
			return;

		if (!getConfig().getBoolean("PlayerLimit.enable"))
			return;

		Player p = e.getPlayer();

		ItemStack i = p.getInventory().getItemInMainHand();

		boolean b = false;

		boolean hasmeta = false;

		for (String g : getConfig().getConfigurationSection("PlayerLimit.blocks").getKeys(false)) {

			if (g.contains(":")) {

				String[] f = g.split(":");

				if (i.getType().name().equals(f[0]))
					if (i.getDurability() == Integer.valueOf(f[1])) {

						b = true;
						hasmeta = true;
						break;

					}

			} else {
				if (i.getType().name().equals(g)) {

					b = true;
					break;

				}
			}

		}
		if (!b)
			return;

		String is = i.getType().name();

		if (hasmeta)
			is = is + ":" + String.valueOf(i.getDurability());

		List<String> s = (datap.contains(p.getName() + "." + is)) ? datap.getStringList(p.getName() + "." + is)
				: new ArrayList<>();

		Location l = e.getBlockPlaced().getLocation();

		s.add(l.getWorld().getName() + ":" + String.valueOf(l.getBlockX()) + ":" + String.valueOf(l.getBlockY()) + ":"
				+ String.valueOf(l.getBlockZ()));

		datap.set(p.getName() + "." + is, s);

		save();

	}

	@EventHandler
	public void limitPlayer(BlockPlaceEvent e) {

		if (!getConfig().getBoolean("PlayerLimit.enable"))
			return;

		Player p = e.getPlayer();

		if (p.hasPermission("NanoLimits.bypass.PlayerLimit"))
			return;

		ItemStack i = p.getInventory().getItemInMainHand();

		boolean b = false;
		byte meta = 0;
		String name = "";
		boolean hasmeta = false;

		for (String g : getConfig().getConfigurationSection("PlayerLimit.blocks").getKeys(false)) {

			if (g.contains(":")) {

				String[] f = g.split(":");

				if (i.getType().name().equals(f[0]))
					if (i.getDurability() == Integer.valueOf(f[1])) {

						b = true;
						hasmeta = true;
						meta = Byte.valueOf(f[1]);
						name = f[0];
						break;

					}

			} else {
				if (i.getType().name().equals(g)) {
					name = g;
					b = true;
					break;

				}
			}

		}

		if (!b)
			return;

		updateLimitsData();

		String v = i.getType().name();
		if (hasmeta)
			v = v + ":" + String.valueOf(i.getDurability());

		int limit = datap.getStringList(p.getName() + "." + v).size()+1;

		if (limitBlock(hasmeta, name, meta, p, limit, "PlayerLimit"))
			e.setCancelled(true);

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void limitRegion(BlockPlaceEvent e) {

		if (!getConfig().getBoolean("RegionLimit.enable"))
			return;

		if (e.isCancelled())
			return;

		Player p = e.getPlayer();

		if (p.hasPermission("NanoLimits.bypass.RegionLimit"))
			return;

		ItemStack i = p.getInventory().getItemInMainHand();

		boolean b = false;
		String name = "";
		byte meta = 0;
		boolean hasmeta = false;

		for (String g : getConfig().getConfigurationSection("RegionLimit.blocks").getKeys(false)) {

			if (g.contains(":")) {

				String[] f = g.split(":");

				if (i.getType().name().equals(f[0]))
					if (i.getDurability() == Integer.valueOf(f[1])) {

						b = true;
						hasmeta = true;
						meta = Byte.valueOf(f[1]);
						name = f[0];
						break;

					}

			} else {
				if (i.getType().name().equals(g)) {

					b = true;
					name = g;
					break;

				}
			}

		}

		if (!b)
			return;

		RegionManager v = wg.getRegionManager(e.getBlockPlaced().getWorld());

		Set<ProtectedRegion> r = v.getApplicableRegions(e.getBlockPlaced().getLocation()).getRegions();

		if (r.size() == 0)
			return;

		int limit = 0;

		for (ProtectedRegion g : r) {

			BlockVector one = g.getMinimumPoint();
			BlockVector two = g.getMaximumPoint();

			for (int y = (int) one.getY(); y <= (int) two.getY(); y++) {

				for (int x = (int) one.getX(); x <= (int) two.getX(); x++) {

					for (int z = (int) one.getZ(); z <= (int) two.getZ(); z++) {

						Block a = Bukkit.getWorld(p.getWorld().getName()).getBlockAt(x, y, z);

						if (a.getType().name().equals(name))
							if (a.getData() == meta) {

								limit++;

							}

					}

				}

			}

		}

		if (limitBlock(hasmeta, name, meta, p, limit, "RegionLimit"))
			e.setCancelled(true);

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void limitChunk(BlockPlaceEvent e) {

		if (!getConfig().getBoolean("ChunkLimit.enable"))
			return;

		Player p = e.getPlayer();

		if (p.hasPermission("NanoLimits.bypass.ChunkLimit"))
			return;

		ItemStack i = p.getInventory().getItemInMainHand();

		boolean b = false;
		byte meta = 0;
		String name = "";
		boolean hasmeta = false;

		for (String g : getConfig().getConfigurationSection("ChunkLimit.blocks").getKeys(false)) {

			if (g.contains(":")) {

				String[] f = g.split(":");

				if (i.getType().name().equals(f[0]))
					if (i.getDurability() == Integer.valueOf(f[1])) {

						b = true;
						meta = Byte.valueOf(f[1]);
						name = f[0];
						hasmeta = true;
						break;

					}

			} else {
				if (i.getType().name().equals(g)) {

					b = true;
					name = g;
					break;

				}
			}

		}

		if (!b)
			return;

		Chunk l = e.getBlockPlaced().getLocation().getChunk();

		int limit = 0;

		for (int y = 0; y < Bukkit.getWorld(l.getWorld().getName()).getMaxHeight(); y++) {

			for (int x = 0; x < 15; x++) {

				for (int z = 0; z < 15; z++) {

					Block j = l.getBlock(x, y, z);

					if (j.getType().name().equals(name))
						if (j.getData() == meta)
							limit++;

				}

			}

		}

		if (limitBlock(hasmeta, name, meta, p, limit, "ChunkLimit"))
			e.setCancelled(true);

	}

	@SuppressWarnings("deprecation")
	public void updateLimitsData() {

		for (String p : datap.getConfigurationSection("").getKeys(false)) {

			for (String b : datap.getConfigurationSection(p).getKeys(false)) {

				List<String> r = datap.getStringList(p + "." + b);

				for (String list : datap.getStringList(p + "." + b)) {

					String[] g = list.split(":");

					Block l = (new Location(Bukkit.getWorld(g[0]), Integer.valueOf(g[1]), Integer.valueOf(g[2]),
							Integer.valueOf(g[3]))).getBlock();

					byte data = 0;
					String name = "";

					if (b.contains(":")) {

						String[] v = b.split(":");

						name = v[0];
						data = Byte.valueOf(v[1]);

					} else {

						name = b;

					}

					if (!l.getType().name().equals(name) || l.getData() != data) {

						r.remove(r.indexOf(list));

					}

					datap.set(p + "." + b, r);

				}

			}

		}

		save();

	}

	private boolean limitBlock(boolean hasmeta, String name, byte meta, Player p, int limit, String limitType) {

		boolean b = false;

		ArrayList<Integer> x = new ArrayList<>();

		String z = name;
		if (hasmeta)
			z = z + ":" + String.valueOf(meta);

		for (String g : getConfig().getConfigurationSection(limitType + ".blocks." + z).getKeys(false)) {

			if (p.hasPermission("NanoLimits." + limitType + "." + g))
				x.add(getConfig().getInt(limitType + ".blocks." + z + "." + g));

		}

		if (x.size() == 0) {
			p.sendMessage(
					"§f[§cЛимит | §4ERROR§f] §7Права игрока §cне настроены§7. Если вы игрок - сообщите об ошибке администрации");
			return true;
		}

		int s = Collections.max(x);

		if (s <= 0) {
			p.sendMessage(
					"§f[§cЛимит | §4ERROR§f] §7В конфигурации задан лимит §c0 §7блоков. Если вы игрок - сообщите об ошибке администрации");
			return true;
		}

		if (limit > s) {

			p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages." + limitType)
					.replace("{limit}", String.valueOf(s)).replace("{name}", z)));
			return true;
		}

		return b;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

		String no = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.not-permission-cmd"));

		if (args.length == 0) {

			sender.sendMessage(
					"§f[§cЛимит§f] §7Команда введена §cне полностью§7. Используйте §c/limits help §7для просмотра доступных команд.");
			return false;
		}

		switch (args[0]) {
		case "reload":

			if (sender.hasPermission("NanoLimits.command.reload")) {
				reloadConfig();
				sender.sendMessage("§f[§cЛимит§f] §7Конфигурация §2успешно §7обновлена.");
				return true;
			} else {
				sender.sendMessage(no);
				return false;
			}

		case "help":
			if (sender.hasPermission("NanoLimits.command.help")) {

				sender.sendMessage("§e============== (§l§cNanoLimits§r§e) ==============");
				sender.sendMessage("");

				if (sender.hasPermission("NanoLimits.command.help"))
					sender.sendMessage("§c> /limits help §7- список доступных вам команд");

				if (sender.hasPermission("NanoLimits.command.list.other")) {
					sender.sendMessage("§c> /limits list [Игрок] §7- посмотреть лимиты игрока");
				} else {
					if (sender.hasPermission("NanoLimits.command.list") && sender instanceof Player)
						sender.sendMessage("§c> /limits list §7- посмотреть свои лимиты");
				}

				if (sender.hasPermission("NanoLimits.command.reload"))
					sender.sendMessage("§c> /limits reload §7- перезагрузка конфигурации");

				sender.sendMessage("");
				sender.sendMessage("§e================================================");

				return true;
			} else {
				sender.sendMessage(no);
				return false;
			}

		case "list":

			if (sender.hasPermission("NanoLimits.command.list")) {

				Player p = null;

				if (args.length == 1) {

					if (sender instanceof Player)
						p = (Player) sender;
					else {
						sender.sendMessage("§f[§cЛимит§f] §7Пожалуйста, укажите §cник игрока §7для использования.");
						return false;
					}
				} else {
					if (!sender.hasPermission("NanoLimits.command.list.other")) {
						sender.sendMessage(no);
						return false;
					}

					if (Bukkit.getPlayer(args[1]) != null)

						p = Bukkit.getPlayer(args[1]);
					else {
						sender.sendMessage("§f[§cЛимит§f] §7Игрок §c\"" + args[1] + "\" §7не найден.");
						return false;
					}
				}

				updateLimitsData();
				ArrayList<Integer> x = new ArrayList<>();

				int i = 0;

				for (String n : getConfig().getConfigurationSection("PlayerLimit.blocks").getKeys(false)) {

					x.clear();
					i++;
					for (String g : getConfig().getConfigurationSection("PlayerLimit.blocks." + n).getKeys(false)) {

						if (p.hasPermission("NanoLimits.PlayerLimit." + g))
							x.add(getConfig().getInt("PlayerLimit.blocks." + n + "." + g));

					}

					String s = String.valueOf(Collections.max(x));
					if (p.hasPermission("NanoLimits.bypass.PlayerLimit"))
						s = "∞";

					String m = "0";
					if (datap.getConfigurationSection(p.getName()).contains(n))
						m = String.valueOf(datap.getStringList(p.getName() + "." + n).size());

					sender.sendMessage("§7" + String.valueOf(i) + ". Блок §c" + n + "§7 (Установлено§c " + m
							+ " §7из §c" + s + "§7)");

					if (sender.hasPermission("NanoLimits.command.list.seenloc")
							&& datap.getStringList(p.getName() + "." + n).size() != 0) {

						for (String g : datap.getStringList(p.getName() + "." + n)) {

							String[] a = g.split(":");

							sender.sendMessage("§c> §7Мир: §c" + a[0] + "§7. Координаты: §cX:" + a[1] + " Y:" + a[2]
									+ " Z:" + a[3]);
						}

					}

				}

				return true;
			} else {
				sender.sendMessage(no);
				return false;
			}

		default:
			sender.sendMessage("§f[§cЛимит§f] §7Подкоманда §c\"" + args[0]
					+ "\" не найдена§7. Используйте §c/limits help §7для просмотра доступных команд.");
			return false;
		}

	}

}
