package de.groodian.minecraftparty.gamestates;

import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;

public class WoolBlockState extends MiniGame {

	private final int TIME_BETWEEN_ROUNDS;
	private final int REMOVE_START_TIME;
	private final int TIME_GETS_FASTER_EVERY_ROUND;
	private final int MIN_REMOVE_TIME;

	private Location start;
	private Location field;
	private Location spectator;

	private byte data;

	private int mode;
	private int currentTime;
	private int removeTime;
	private int levelCounter;
	private int placeCount;
	private int lastPlayerDiePlaceCount;

	private boolean delayIsRunnung;
	private boolean generate;

	private int x;
	private int y;
	private int z;
	private World world;
	private net.minecraft.server.v1_8_R3.World nmsWorld;

	private byte[] woolData;
	private Map<Integer, String> blockName;

	private SplittableRandom splittableRandom;

	private BukkitTask gameTask;

	public WoolBlockState(String name, Main plugin) {
		super(name, plugin);
		super.timeScoreboardGame = false;
		super.setRecords = false;
		this.TIME_BETWEEN_ROUNDS = MainConfig.getInt("WoolBlock.time-between-rounds");
		this.REMOVE_START_TIME = MainConfig.getInt("WoolBlock.remove-start-time");
		this.TIME_GETS_FASTER_EVERY_ROUND = MainConfig.getInt("WoolBlock.time-gets-faster-every-round");
		this.MIN_REMOVE_TIME = MainConfig.getInt("WoolBlock.min-remove-time");
		this.start = plugin.getLocationManager().WOOLBLOCK_START;
		this.field = plugin.getLocationManager().WOOLBLOCK_FIELD;
		this.spectator = plugin.getLocationManager().WOOLBLOCK_SPECTATOR;
		this.data = 0;
		this.mode = 0;
		this.currentTime = 0;
		this.removeTime = REMOVE_START_TIME;
		this.levelCounter = 0;
		this.placeCount = -1;
		this.delayIsRunnung = false;
		this.generate = true;
		this.x = (int) field.getX();
		this.y = (int) field.getY();
		this.z = (int) field.getZ();
		this.world = field.getWorld();
		this.nmsWorld = ((CraftWorld) world).getHandle();
		this.woolData = new byte[7];
		this.woolData[0] = 1;
		this.woolData[1] = 3;
		this.woolData[2] = 4;
		this.woolData[3] = 5;
		this.woolData[4] = 6;
		this.woolData[5] = 11;
		this.woolData[6] = 14;
		this.blockName = new HashMap<>();
		this.blockName.put(1, Messages.get("WoolBlock.orange"));
		this.blockName.put(3, Messages.get("WoolBlock.light-blue"));
		this.blockName.put(4, Messages.get("WoolBlock.yellow"));
		this.blockName.put(5, Messages.get("WoolBlock.green"));
		this.blockName.put(6, Messages.get("WoolBlock.pink"));
		this.blockName.put(11, Messages.get("WoolBlock.blue"));
		this.blockName.put(14, Messages.get("WoolBlock.red"));
		this.splittableRandom = new SplittableRandom();
		this.gameTask = null;
	}

	@Override
	protected void prepare() {
		Location temp = field.clone();
		int x = (int) temp.getX();
		int z = (int) temp.getZ();

		for (int i = 0; i < 30; i++) {
			temp.setX(i + x);
			for (int j = 0; j < 30; j++) {
				temp.setZ(j + z);
				temp.getBlock().setType(Material.WOOL);
			}
		}

	}

	@Override
	protected void beforeCountdownStart() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.teleport(start);
			plugin.getTeleportFix().doFor(player);
		}
	}

	@Override
	protected void startMiniGame() {

		gameTask = new BukkitRunnable() {

			@Override
			public void run() {

				levelCounter++;
				float exp = (1.0f / currentTime) * levelCounter;

				if (exp > 1f) {
					exp = 1f;
				}

				if (!delayIsRunnung) {
					delayIsRunnung = true;
					delay();
					levelCounter = 0;
				}

				for (Player player : plugin.getPlayers()) {
					player.setExp(exp);
					if (!diePlayers.contains(player)) {
						ranking.put(player, placeCount);
						if (player.getLocation().getY() <= field.getY()) {
							lastPlayerDiePlaceCount = placeCount;
							addDiePlayer(player);
							plugin.getRecord().setRecord(player, "woolblock", placeCount, true);
							player.teleport(spectator);
							player.setAllowFlight(true);
							player.setFlying(true);
							for (Player all : Bukkit.getOnlinePlayers()) {
								all.hidePlayer(player);
							}
						}
					}
				}

				if (((plugin.getPlayers().size() == diePlayers.size() + 1) && placeCount != lastPlayerDiePlaceCount) || plugin.getPlayers().size() == 1 || diePlayers.size() == plugin.getPlayers().size()) {

					for (Player player : plugin.getPlayers()) {
						if (!diePlayers.contains(player)) {
							plugin.getRecord().setRecord(player, "woolblock", placeCount, true);
						}
					}

					for (Player player : Bukkit.getOnlinePlayers()) {
						for (Player player1 : plugin.getPlayers()) {
							player.showPlayer(player1);
						}
					}

					for (Player player : plugin.getPlayers()) {
						player.setExp(0.0f);
						player.setAllowFlight(false);
						player.setFlying(false);
					}

					gameTask.cancel();
					plugin.getGameStateManager().setRandomGameState();

				}

			}

		}.runTaskTimer(plugin, 0, 1);

	}

	private void delay() {

		new BukkitRunnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if (started) {

					if (generate) {

						data = woolData[splittableRandom.nextInt(woolData.length)];
						generateWool(data);

						currentTime = removeTime;

						for (Player all : plugin.getPlayers()) {
							for (int i = 0; i < 9; i++) {
								all.getInventory().setItem(i, new ItemBuilder(Material.WOOL, (short) data).setName(blockName.get((int) data)).build());
							}
						}

						generate = false;
					} else {

						for (int i = 0; i < 30; i++) {
							for (int j = 0; j < 30; j++) {
								Block block = world.getBlockAt(x + i, y, z + j);
								if (block.getData() != data) {
									block.setType(Material.WATER);
								}
							}
						}

						removeTime -= TIME_GETS_FASTER_EVERY_ROUND;
						if (removeTime < MIN_REMOVE_TIME) {
							removeTime = MIN_REMOVE_TIME;
						}
						currentTime = TIME_BETWEEN_ROUNDS;

						generate = true;
					}

					delayIsRunnung = false;

				}
			}
		}.runTaskLater(plugin, currentTime);

	}

	@SuppressWarnings("deprecation")
	private void generateWool(int data) {
		int count = 0;

		placeCount++;

		do {

			count = 1;

			if (mode % 4 == 0) {
				randomWool();
			} else if (mode % 4 == 1) {
				lineWool();
			} else if (mode % 4 == 2) {
				threexthreeWool();
			} else {
				twoxtwoWool();
			}

			for (int i = 0; i < 30; i++) {
				for (int j = 0; j < 30; j++) {
					if (world.getBlockAt(x + i, y, z + j).getData() == data) {
						count++;
					}
				}
			}

		} while (count < 91);

		mode++;

	}

	// https://www.spigotmc.org/threads/methods-for-changing-massive-amount-of-blocks-up-to-14m-blocks-s.395868/

	private void setBlockInNativeWorld(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics) {
		BlockPosition bp = new BlockPosition(x, y, z);
		IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));
		nmsWorld.setTypeAndData(bp, ibd, applyPhysics ? 3 : 2);
	}

	private void randomWool() {
		byte data = woolData[splittableRandom.nextInt(woolData.length)];

		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < 30; j++) {
				setBlockInNativeWorld(world, x + i, y, z + j, 35, data, false);
				data = woolData[splittableRandom.nextInt(woolData.length)];
			}
		}

	}

	private void lineWool() {
		int count = 1;
		byte data = woolData[splittableRandom.nextInt(woolData.length)];

		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < 30; j++) {
				if (count == 1) {
					data = woolData[splittableRandom.nextInt(woolData.length)];
				}
				if (count < 30) {
					setBlockInNativeWorld(world, x + i, y, z + j, 35, data, false);
				} else {
					setBlockInNativeWorld(world, x + i, y, z + j, 35, data, false);
					count = 0;
				}
				count++;
			}
		}

	}

	private void threexthreeWool() {
		byte data = woolData[splittableRandom.nextInt(woolData.length)];

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				data = woolData[splittableRandom.nextInt(woolData.length)];
				for (int o = 0; o < 3; o++) {
					for (int m = 0; m < 3; m++) {
						setBlockInNativeWorld(world, o + j * 3 + x, y, m + i * 3 + z, 35, data, false);
					}
				}
			}
		}

	}

	private void twoxtwoWool() {
		byte data = woolData[splittableRandom.nextInt(woolData.length)];

		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				data = woolData[splittableRandom.nextInt(woolData.length)];
				for (int o = 0; o < 2; o++) {
					for (int m = 0; m < 2; m++) {
						setBlockInNativeWorld(world, o + j * 2 + x, y, m + i * 2 + z, 35, data, false);
					}
				}
			}
		}

	}

}
