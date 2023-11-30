package com.movies22.cashcraft.tc.controller;

import com.bergerkiller.bukkit.common.chunk.ForcedChunk;
import com.bergerkiller.bukkit.common.utils.ChunkUtil;
import com.bergerkiller.bukkit.common.utils.MathUtil;
import com.bergerkiller.bukkit.common.wrappers.LongHashMap;
import com.bergerkiller.bukkit.common.wrappers.LongHashSet;
import com.bergerkiller.bukkit.common.wrappers.LongHashSet.LongIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.bukkit.World;

public class ChunkArea {
   public static final Runnable DUMMY_RUNNABLE = new Runnable() {
      public void run() {
      }
   };
   public static final int CHUNK_RANGE = 2;
   public static final int CHUNK_EDGE = 5;
   public static final int CHUNK_AREA = 25;
   private World current_world = null;
   private final ForwardChunkArea forward_chunk_area = new ForwardChunkArea();
   private final LongHashSet added_chunk_centers = new LongHashSet();
   private LongHashMap<ChunkArea.OwnedChunk> chunks = new LongHashMap();
   private final List<ChunkArea.OwnedChunk> all_chunks = new ArrayList();
   private final List<ChunkArea.OwnedChunk> removed_chunks = new ArrayList();
   private final List<ChunkArea.OwnedChunk> added_chunks = new ArrayList();

   public void reset() {
      this.added_chunk_centers.clear();
      this.chunks.clear();
      Iterator var1 = this.all_chunks.iterator();

      while(var1.hasNext()) {
         ChunkArea.OwnedChunk chunk = (ChunkArea.OwnedChunk)var1.next();
         chunk.forcedChunk.close();
      }

      this.all_chunks.clear();
      this.removed_chunks.clear();
      this.added_chunks.clear();
      this.forward_chunk_area.reset();
   }

   public void refresh(World world, LongHashSet coordinates) {
      this.removed_chunks.clear();
      this.added_chunks.clear();
      Iterator var3;
      ChunkArea.OwnedChunk owned;
      if (this.current_world != world) {
         this.current_world = world;
         this.removed_chunks.addAll(this.chunks.getValues());
         var3 = this.removed_chunks.iterator();

         while(var3.hasNext()) {
            owned = (ChunkArea.OwnedChunk)var3.next();
            owned.forcedChunk.close();
         }

         this.added_chunk_centers.clear();
         this.chunks = new LongHashMap();
         this.all_chunks.clear();
         this.forward_chunk_area.reset();
      }

      var3 = this.all_chunks.iterator();

      while(var3.hasNext()) {
         owned = (ChunkArea.OwnedChunk)var3.next();
         owned.distance_previous = owned.distance;
      }

      LongIterator iter = coordinates.longIterator();

      while(true) {
         int mx;
         int mz;
         int cx;
         long coord;
         do {
            if (!iter.hasNext()) {
               LongIterator added_iter = this.added_chunk_centers.longIterator();

               while(true) {
                  long coord;
                  do {
                     if (!added_iter.hasNext()) {
                        return;
                     }

                     coord = added_iter.next();
                  } while(coordinates.contains(coord));

                  added_iter.remove();
                  mx = MathUtil.longHashMsw(coord);
                  mz = MathUtil.longHashLsw(coord);

                  for(cx = -2; cx <= 2; ++cx) {
                     for(int cz = -2; cz <= 2; ++cz) {
                        long ownedCoord = MathUtil.longHashToLong(mx + cx, mz + cz);
                        ChunkArea.OwnedChunk ownedChunk = (ChunkArea.OwnedChunk)this.chunks.get(ownedCoord);
                        if (ownedChunk != null) {
                           ownedChunk.removeChunk(coord, mx, mz);
                           if (ownedChunk.isEmpty()) {
                              ownedChunk.forcedChunk.close();
                              this.removed_chunks.add(ownedChunk);
                              this.chunks.remove(ownedCoord);
                              this.all_chunks.remove(ownedChunk);
                           }
                        }
                     }
                  }
               }
            }

            coord = iter.next();
         } while(!this.added_chunk_centers.add(coord));

         int mx = MathUtil.longHashMsw(coord);
         mx = MathUtil.longHashLsw(coord);

         for(mz = -2; mz <= 2; ++mz) {
            for(cx = -2; cx <= 2; ++cx) {
               long ownedCoord = MathUtil.longHashToLong(mx + mz, mx + cx);
               ChunkArea.OwnedChunk ownedChunk = (ChunkArea.OwnedChunk)this.chunks.get(ownedCoord);
               if (ownedChunk == null) {
                  ownedChunk = new ChunkArea.OwnedChunk(world, mx + mz, mx + cx);
                  ownedChunk.addChunk(coord, mx, mx);
                  this.all_chunks.add(ownedChunk);
                  this.chunks.put(ownedCoord, ownedChunk);
                  this.added_chunks.add(ownedChunk);
               } else {
                  ownedChunk.addChunk(coord, mx, mx);
               }
            }
         }
      }
   }

   public final void getForcedChunks(List<ForcedChunk> forcedChunks) {
      Iterator var2 = this.all_chunks.iterator();

      while(var2.hasNext()) {
         ChunkArea.OwnedChunk chunk = (ChunkArea.OwnedChunk)var2.next();
         if (!chunk.forcedChunk.isNone()) {
            forcedChunks.add(chunk.forcedChunk.clone());
         }
      }

   }

   public final LongHashSet getAllCenters() {
      return this.added_chunk_centers;
   }

   public final Collection<ChunkArea.OwnedChunk> getAll() {
      return this.all_chunks;
   }

   public final List<ChunkArea.OwnedChunk> getRemoved() {
      return this.removed_chunks;
   }

   public final List<ChunkArea.OwnedChunk> getAdded() {
      return this.added_chunks;
   }

   public boolean containsChunk(long chunkLongCoord) {
      return this.chunks.contains(chunkLongCoord);
   }

   public boolean containsChunk(int chunkX, int chunkZ) {
      return this.containsChunk(MathUtil.longHashToLong(chunkX, chunkZ));
   }

   public ForwardChunkArea getForwardChunkArea() {
      return this.forward_chunk_area;
   }

   public static final class OwnedChunk {
      private final int cx;
      private final int cz;
      private final World world;
      private final LongHashSet chunks = new LongHashSet();
      private int distance;
      private int distance_previous;
      private final ForcedChunk forcedChunk = ForcedChunk.none();

      public OwnedChunk(World world, int cx, int cz) {
         this.world = world;
         this.cx = cx;
         this.cz = cz;
         this.distance = Integer.MAX_VALUE;
         this.distance_previous = Integer.MAX_VALUE;
      }

      public boolean isLoaded() {
         return this.world.isChunkLoaded(this.cx, this.cz);
      }

      public void keepLoaded(boolean keepLoaded) {
         if (keepLoaded) {
            this.forcedChunk.move(ChunkUtil.forceChunkLoaded(this.world, this.cx, this.cz));
         } else {
            this.forcedChunk.close();
         }

      }

      public void loadChunk() {
         this.world.getChunkAt(this.cx, this.cz);
      }

      public World getWorld() {
         return this.world;
      }

      public int getX() {
         return this.cx;
      }

      public int getZ() {
         return this.cz;
      }

      public int getDistance() {
         return this.distance;
      }

      public int getPreviousDistance() {
         return this.distance_previous;
      }

      public boolean isAdded() {
         return this.distance < Integer.MAX_VALUE && this.distance_previous == Integer.MAX_VALUE;
      }

      public boolean isRemoved() {
         return this.distance == Integer.MAX_VALUE && this.distance_previous < Integer.MAX_VALUE;
      }

      private void addChunk(long key, int cx, int cz) {
         if (this.chunks.add(key)) {
            this.distance = Math.min(this.distance, this.calcDistance(cx, cz));
         }

      }

      private void removeChunk(long key, int cx, int cz) {
         if (this.chunks.remove(key)) {
            int oldDistance = this.calcDistance(cx, cz);
            if (oldDistance <= this.distance) {
               this.distance = Integer.MAX_VALUE;
               LongIterator iter = this.chunks.longIterator();

               while(iter.hasNext()) {
                  long storedChunk = iter.next();
                  int distance = this.calcDistance(MathUtil.longHashMsw(storedChunk), MathUtil.longHashLsw(storedChunk));
                  if (distance < this.distance) {
                     this.distance = distance;
                  }
               }
            }
         }

      }

      public boolean isEmpty() {
         return this.chunks.isEmpty();
      }

      private final int calcDistance(int cx, int cz) {
         return Math.max(Math.abs(cx - this.cx), Math.abs(cz - this.cz));
      }
   }
}
