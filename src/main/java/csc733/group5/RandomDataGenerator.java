package csc733.group5;

import org.javatuples.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomDataGenerator {

    private static final char[] characters = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'u', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'U', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    };

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PHONE_TMPL = "(%d)%d-%d";




    private static final String O_TMPL =
            "{ O_ENTRY_D : '%s', " +
                    "O_OL_CNT : %d, " +
                    "O_ALL_LOCAL : %d }";
    private final Random rand;

    public RandomDataGenerator() { rand = new Random(); }

    public RandomDataGenerator(final int seed) { rand = new Random(seed); }

    public Random rand() { return rand; }

    public String randomWord(final int minLength, final int maxLength) {
        return randomWord(rand.nextInt(maxLength-minLength) + minLength);
    }

    public String randomWord(final int exactLength) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exactLength; i++) sb.append(characters[rand.nextInt(52)]);
        return sb.toString();
    }

    public String randomWord() { return randomWord(rand.nextInt(16)); }

    public String randomStreet() {
        return new StringBuilder()
                .append(rand.nextInt(9000)+100)
                .append(' ').append(randomWord(5, 16))
                .append(' ').append(randomWord(2,4))
                .toString();
    }

    public int randomZip() {
        return (int)(rand.nextDouble() * 100000);
    }

    public String randomPhone() {
        return String.format(PHONE_TMPL,
                (int)(rand.nextDouble() * 1000),
                (int)(rand.nextDouble() * 1000),
                (int)(rand.nextDouble() * 10000));
    }

    public String randomDate() {
        return DATE_FORMAT.format(LocalDateTime.of(
                rand.nextInt(50)+1970,
                rand.nextInt(12) + 1,
                rand.nextInt(27) + 1,
                rand.nextInt(24),
                rand.nextInt(60)));
    }

    public Pair<String, List<String>> randomOrder(final List<Integer> itemCosts) {
        final int numItems = rand.nextInt(10)+5;
        final String order = String.format(O_TMPL, randomDate(), numItems, 1);
        final List<String> orderLines = new ArrayList<>(numItems);
        for (int i = 0; i < numItems; i++) {

//            orderLines.add(String.format(OL_TMPL, randomDate()))
        }
        return Pair.with(order, orderLines);
    }
}
