package valuetypes;

import org.metavm.entity.ValueType;

@ValueType
public record ChannelPrice(String channel, Currency price) {
}
