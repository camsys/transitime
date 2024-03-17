package org.transitclock.service;

import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.BlockInterface;
import org.transitclock.gtfs.DbConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Combine Static and Dynamic instances into interface.
 */
public class BackingStore {

  private DbConfig config;
  public BackingStore(DbConfig config) {
    this.config = config;
  }
  public List<BlockInterface> getBlocksForRoute(String id) {
    List<BlockInterface> blocks = new ArrayList<>();
    List<Block> blocksForRoute = config.getBlocksForRoute(id);
    for (Block block : blocksForRoute) {
      blocks.add(block);
    }
    return blocks;
  }

  public List<BlockInterface> getBlocksForRoute(String serviceId, String routeId) {
    List<BlockInterface> blocks = new ArrayList<>();
    List<Block> blocksForRoute = config.getBlocksForRoute(serviceId, routeId);
    for (Block block : blocksForRoute) {
      blocks.add(block);
    }
    return blocks;
  }
  public List<BlockInterface> getBlocks() {
    List<BlockInterface> blocks = new ArrayList<>();
    for (Block block : config.getBlocks()) {
      blocks.add(block);
    }
    return blocks;
  }

  public Collection<BlockInterface> getBlocks(String serviceId) {
    List<BlockInterface> blocks = new ArrayList<>();
    Collection<Block> configBlocks = config.getBlocks(serviceId);
    for (Block configBlock : configBlocks) {
      blocks.add(configBlock);
    }
    return blocks;
  }

  public Collection<BlockInterface> getBlocksForAllServiceIds(String blockId) {
    List<BlockInterface> blocks = new ArrayList<>();
    for (Block block : config.getBlocksForAllServiceIds(blockId)) {
      blocks.add(block);
    }
    return blocks;
  }
}
