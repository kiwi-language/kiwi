package valuetypes;

import tech.metavm.entity.ValueType;

@ValueType
public record ChannelPrice(String channel, Currency price) {
}
