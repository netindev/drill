package tk.netindev.drill.miner;

/**
 *
 * @author netindev
 *
 */
public class Job {

   private String id, jobId;
   private byte[] blob;
   private int target;

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public byte[] getBlob() {
      return this.blob;
   }

   public void setBlob(byte[] blob) {
      this.blob = blob;
   }

   public String getJobId() {
      return this.jobId;
   }

   public void setJobId(String jobId) {
      this.jobId = jobId;
   }

   public int getTarget() {
      return this.target;
   }

   public void setTarget(int target) {
      this.target = target;
   }

}