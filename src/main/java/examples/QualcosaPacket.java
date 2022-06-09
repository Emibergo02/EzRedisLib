package examples;

import ezredislib.packet.MessagingPacket;
import org.jetbrains.annotations.Nullable;

public class QualcosaPacket implements MessagingPacket {

        private final long timestamp;
        private final String from;
        private final String to;

        public QualcosaPacket(String from,String to) {
            this.from= from;
            this.to= to;
            this.timestamp = System.currentTimeMillis();
        }


        public @Nullable String getTarget() {
            return this.to;
        }

        public String getSender() {
            return this.from;
        }

        public long getTimestamp() {
            return timestamp;
        }
}
