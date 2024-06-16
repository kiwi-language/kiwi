package valuetypes;

import org.metavm.api.ValueType;

@ValueType
public record ChannelPrice(String channel, Currency price) {
}
