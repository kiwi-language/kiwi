import org.manul.api.Value;

@Value
public record Price(double amount, Currency currency) {
}
