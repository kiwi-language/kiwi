package valuetypes;

import org.metavm.entity.ValueList;
import org.metavm.entity.ValueType;

@ValueType
public record Price(
        Currency defaultPrice,
        ValueList<ChannelPrice> channelPrices
) {
}
