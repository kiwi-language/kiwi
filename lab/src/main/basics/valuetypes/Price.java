package valuetypes;

import org.metavm.api.ValueList;
import org.metavm.api.Value;

@Value
public record Price(
        Currency defaultPrice,
        ValueList<ChannelPrice> channelPrices
) {
}
