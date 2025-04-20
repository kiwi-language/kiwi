import org.metavm.api.Value;

@Value
public record Price(double amount, Currency currency) {
}
