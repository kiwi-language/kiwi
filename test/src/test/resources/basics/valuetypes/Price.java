package valuetypes;

import org.manul.api.Value;

import java.util.List;

@Value
public record Price(
        Currency defaultPrice,
        List<ChannelPrice> channelPrices
) {
}
