package valuetypes;

import tech.metavm.entity.ValueList;
import tech.metavm.entity.ValueType;

@ValueType("Price")
public record Price(
        Currency defaultPrice,
        ValueList<ChannelPrice> channelPrices
) {
}
