package valuetypes;

import org.metavm.api.Value;

@Value
public record ChannelPrice(String channel, Currency price) {
}
