package valuetypes;

import tech.metavm.entity.ValueType;

@ValueType("ChannelPrice")
public record ChannelPrice(String channel, Currency price) {
}
