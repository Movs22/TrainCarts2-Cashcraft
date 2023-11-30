package com.movies22.cashcraft.tc.controller;

import com.bergerkiller.bukkit.common.TickTracker;
import com.bergerkiller.bukkit.common.chunk.ForcedChunk;
import com.bergerkiller.bukkit.common.utils.ChunkUtil;
import com.bergerkiller.bukkit.common.utils.MathUtil;
import com.bergerkiller.bukkit.common.wrappers.LongHashMap;
import com.bergerkiller.mountiplex.reflection.SafeMethod;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.World;
import org.bukkit.block.Block;

public class ForwardChunkArea {
   private final TickTracker beginTickTracker = new TickTracker();
   private World world = null;
   private final LongHashMap<ForwardChunkArea.Entry> entries = new LongHashMap();
   private final List<ForwardChunkArea.Entry> entriesList = new ArrayList();
   private ForwardChunkArea.Entry lastEntry = null;
   private boolean state = false;
   private static final ForwardChunkArea.ForceLoadedFunc FORCE_LOADED_FUNC;

   public ForwardChunkArea() {
      this.beginTickTracker.setRunnable(() -> {
         boolean expectedState = this.state;
         if (this.lastEntry != null && this.lastEntry.state != expectedState) {
            this.lastEntry = null;
         }

         Iterator iter = this.entriesList.iterator();

         while(iter.hasNext()) {
            ForwardChunkArea.Entry e = (ForwardChunkArea.Entry)iter.next();
            if (e.state != expectedState) {
               iter.remove();
               this.entries.remove(e.key);
               e.chunk.close();
            }
         }

         this.state = !expectedState;
      });
   }

   public void begin() {
      this.beginTickTracker.update();
   }

   public void reset() {
      if (!this.entriesList.isEmpty()) {
         Iterator var1 = this.entriesList.iterator();

         while(var1.hasNext()) {
            ForwardChunkArea.Entry e = (ForwardChunkArea.Entry)var1.next();
            e.chunk.close();
         }

         this.entries.clear();
         this.entriesList.clear();
         this.lastEntry = null;
      }

   }

   public void addBlock(Block block) {
      this.add(block.getWorld(), block.getX() >> 4, block.getZ() >> 4);
   }

   public void add(World world, int cx, int cz) {
      if (this.world != world) {
         this.reset();
         this.world = world;
      }

      long key = MathUtil.longHashToLong(cx, cz);
      ForwardChunkArea.Entry e = this.lastEntry;
      if (e == null || e.key != key) {
         e = (ForwardChunkArea.Entry)this.entries.computeIfAbsent(key, (k) -> {
            ForwardChunkArea.Entry newEntry = new ForwardChunkArea.Entry(FORCE_LOADED_FUNC.forceLoaded(world, cx, cz), k, false);
            this.entriesList.add(newEntry);
            return newEntry;
         });
         this.lastEntry = e;
      }

      e.state = this.state;
   }

   static {
      if (SafeMethod.contains(ForcedChunk.class, "load", new Class[]{World.class, Integer.TYPE, Integer.TYPE, Integer.TYPE})) {
         FORCE_LOADED_FUNC = (w, cx, cz) -> {
            return ForcedChunk.load(w, cx, cz, 0);
         };
      } else {
         FORCE_LOADED_FUNC = ChunkUtil::forceChunkLoaded;
      }

   }

   private static final class Entry {
      public final ForcedChunk chunk;
      public final long key;
      public boolean state;

      public Entry(ForcedChunk chunk, long key, boolean state) {
         this.chunk = chunk;
         this.key = key;
         this.state = state;
      }
   }

   @FunctionalInterface
   private interface ForceLoadedFunc {
      ForcedChunk forceLoaded(World var1, int var2, int var3);
   }
}
