package gq.netin.drill.miner;

import gq.netin.drill.hasher.Hasher;

/**
 *
 * @author netindev
 *
 */
public class Worker implements Runnable {

	private final Miner miner;
	private final Job job;
	private final int startNonce;

	public Worker(Miner miner, Job job, int startNonce) {
		this.miner = miner;
		this.job = job;
		this.startNonce = startNonce;
	}

	@Override
	public void run() {
		int nonce = this.startNonce;
		final byte[] hashArray = new byte[32];
		final byte[] nonceArray = new byte[4];
		final byte[] blob = this.job.getBlob();
		final int target = this.job.getTarget();
		while (!Thread.interrupted()) {
			blob[39] = (byte) nonce;
			blob[40] = (byte) (nonce >> 8);
			blob[41] = (byte) (nonce >> 16);
			blob[42] = (byte) (nonce >> 24);
			Hasher.slowHash(blob, hashArray, (blob[0] - 6 < 0 ? 0 : blob[0] - 6));
			final int difficulty = (((hashArray[31] << 24) | ((hashArray[30] & 255) << 16)) | ((hashArray[29] & 255) << 8)) | (hashArray[28] & 255);
			if (difficulty >= 0 && difficulty <= target) {
				nonceArray[0] = (byte) nonce;
				nonceArray[1] = (byte) (nonce >> 8);
				nonceArray[2] = (byte) (nonce >> 16);
				nonceArray[3] = (byte) (nonce >> 24);
				this.miner.send(this.job, nonceArray, hashArray);
			}
			nonce++;
		}
	}

}
