package valuetypes;

import org.manul.api.Value;

@Value
public record ChannelPrice(String channel, Currency price) {
}
