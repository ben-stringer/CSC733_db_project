package csc733.group5;

import java.util.Random;

public class RandomDataGenerator {

    private static final char[] characters = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'u', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'U', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    };

    private final Random rand;

    public RandomDataGenerator() { rand = new Random(); }

    public RandomDataGenerator(final int seed) { rand = new Random(seed); }

    public boolean nextBoolean() { return rand.nextBoolean(); }

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

    public String randomWarehouse(final int id) {
        return new StringBuilder()
                .append("{ w_id : 'w_").append(id)
                .append("', w_name : '").append(randomWord(6,16))
                .append("', w_street_1 : '").append(randomStreet())
                .append("', w_city : '").append(randomWord())
                .append("', w_state : '").append(randomWord(2))
                .append("', w_zip : ").append(randomZip())
                .append("}")
                .toString();
    }

    public String randomDistrict(final int id) {
        return new StringBuilder()
                .append('{')
                .append("d_id : 'd_").append(id)
                .append("', d_name : '").append(randomWord(6,16))
                .append("', d_street_1 : '").append(randomStreet())
                .append("', d_city : '").append(randomWord())
                .append("', d_state : '").append(randomWord(2))
                .append("', d_zip : ").append(randomZip())
                .append("}")
                .toString();
    }

    public String randomItem(final int id) {
        return new StringBuilder()
                .append('{')
                .append("i_id : 'i_").append(id)
                .append("', i_name : '").append(randomWord(6,16))
                .append("', i_price : ").append(Math.abs(rand.nextInt(10000)))
                .append("}")
                .toString();
    }

    public String randomItemQuantity() {
        return new StringBuilder().append("{ s_quantity : ")
                .append(Math.abs(rand.nextInt(200))+1)
                .append("}")
                .toString();
    }
}
