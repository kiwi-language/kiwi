package valuetypes;

import org.metavm.api.ValueList;
import org.metavm.api.ValueType;

@ValueType
public record Price(
        Currency defaultPrice,
        ValueList<ChannelPrice> channelPrices
) {
}
