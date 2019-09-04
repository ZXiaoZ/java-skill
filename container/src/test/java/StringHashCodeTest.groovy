class StringHashCodeTest {
    static void main(String[] args) {
        println hashString("K1")
        println hashString("K2")
        println hashString("K3")
        println hashString("K4")
        println "${"K1".hashCode()}, ${"K2".hashCode()}, ${"K3".hashCode()}, ${"K4".hashCode()}"

    }

    static int hashString(String key) {
        int h = 0
        for (int i = 0; i < key.length(); i++) {
            h = 31 * h + key[i] as char
        }
        return h
    }
}