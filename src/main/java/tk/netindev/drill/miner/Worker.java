package tk.netindev.drill.miner;

import tk.netindev.drill.hasher.Hasher;

/**
 *
 * @author netindev
 *
 */
public class Worker extends Thread {

   private final Miner miner;
   private final Job job;
   private final int nonce, variant;

   public Worker(Miner miner, Job job, int nonce, int variant) {
      this.miner = miner;
      this.job = job;
      this.nonce = nonce;
      this.variant = variant;
   }

   @Override
   public void run() {
      int nonce = this.nonce;
      final byte[] hash = new byte[32];
      final byte[] array = new byte[4];
      final byte[] blob = this.job.getBlob();
      final int target = this.job.getTarget();
      while (!Thread.interrupted()) {
         blob[39] = (byte) nonce;
         blob[40] = (byte) (nonce >> 8);
         blob[41] = (byte) (nonce >> 16);
         blob[42] = (byte) (nonce >> 24);
         Hasher.slowHash(blob, hash,
               (this.variant == -1 ? (blob[0] - 6 < 0 ? 0 : blob[0] - 6)
                     : this.variant));
         final int difficulty = (((hash[31] << 24) | ((hash[30] & 255) << 16))
               | ((hash[29] & 255) << 8)) | (hash[28] & 255);
         if (difficulty >= 0 && difficulty <= target) {
            array[0] = (byte) nonce;
            array[1] = (byte) (nonce >> 8);
            array[2] = (byte) (nonce >> 16);
            array[3] = (byte) (nonce >> 24);
            this.miner.send(this.job, array, hash);
         }
         nonce++;
      }
   }

}