package one;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// A product in the store.
class Item {
    private String itemId;
    private String name;
    private double priceUSD;
    private String itemType;
    private String iconSymbol;

    public Item(String itemId, String name, double priceUSD, String itemType, String iconSymbol) {
        this.itemId = itemId;
        this.name = name;
        this.priceUSD = priceUSD;
        this.itemType = itemType;
        this.iconSymbol = iconSymbol;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public double getPriceUSD() { return priceUSD; }
    public String getItemType() { return itemType; }
    public String getIconSymbol() { return iconSymbol; }
}

// The player and their wallet.
class Gamer {
    private String gamerTag;
    private String userId;
    private double walletBalance;

    public Gamer(String gamerTag, String userId, double walletBalance) {
        this.gamerTag = gamerTag;
        this.userId = userId;
        this.walletBalance = walletBalance;
    }

    public String getGamerTag() { return gamerTag; }
    public String getUserId() { return userId; }
    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double newBalance) { this.walletBalance = newBalance; }

    // Can the player pay this amount?
    public boolean canAfford(double amount) {
        return walletBalance >= amount;
    }
}

// Pretends to be the app store that handles payment.
//
// The receipt token looks like:  PSN-TX-user|item|price-a1b2c3d4
// The last part is a fingerprint made from the rest plus a secret word.
// To check a receipt, we make the fingerprint again and see if it matches.
// Someone who changes the price can't produce the right fingerprint,
// because they don't know the secret word.
class AppStoreAPI {

    private static final String SECRET = "GRP6SECRET";

    // Creates a receipt token for a purchase.
    public static String generateReceiptToken(String itemId, String userId, double price) {
        String data = userId + "|" + itemId + "|" + String.format("%.2f", price);
        return "PSN-TX-" + data + "-" + fingerprint(data);
    }

    // Checks whether a receipt token is genuine.
    public static boolean validateReceiptToken(String token) {
        if (token == null || !token.startsWith("PSN-TX-")) {
            return false;
        }

        // Cut off the "PSN-TX-" prefix.
        String rest = token.substring(7);

        // The fingerprint is after the last dash.
        int split = rest.lastIndexOf('-');
        if (split < 0) {
            return false;
        }

        String data = rest.substring(0, split);
        String givenFingerprint = rest.substring(split + 1);

        // Make the fingerprint again and compare.
        return fingerprint(data).equals(givenFingerprint);
    }

    // Changes the price in a token without fixing the fingerprint,
    // so we can show that validation catches it.
    public static String tamperWith(String token) {
        int split = token.lastIndexOf('-');
        String data = token.substring(0, split);
        String fingerprint = token.substring(split + 1);

        // Replace the price with 0.01 but keep the old fingerprint.
        int lastPipe = data.lastIndexOf('|');
        return data.substring(0, lastPipe) + "|0.01-" + fingerprint;
    }

    // Turns text + secret into a short scrambled code.
    private static String fingerprint(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((data + SECRET).getBytes("UTF-8"));

            String result = "";
            for (int i = 0; i < 4; i++) {
                result += String.format("%02x", hash[i]);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

// Pretends to be the fraud detection system.
//
// Rule 1: more than 5 purchases in 2 minutes is suspicious.
// Rule 2: any purchase over $150 is suspicious.
class AntiCheatEngine {

    private static final int MAX_TRIES = 5;
    private static final long TWO_MINUTES = 120000;
    private static final double BIG_PURCHASE = 150.00;

    // Remembers when each user tried to buy something.
    private static Map<String, List<Long>> tryLog = new HashMap<>();

    // Returns null if everything looks fine.
    // Returns a reason text if the purchase is suspicious.
    public static String audit(String userId, double amount) {

        // Kept so we can trigger a flag on demand during a demo.
        if (userId.equalsIgnoreCase("SUSPECT999")) {
            return "Account is on the watchlist";
        }

        long now = System.currentTimeMillis();

        // Get this user's list of past tries, or start a new one.
        List<Long> tries = tryLog.get(userId);
        if (tries == null) {
            tries = new ArrayList<>();
            tryLog.put(userId, tries);
        }

        // Keep only the tries from the last 2 minutes.
        List<Long> recent = new ArrayList<>();
        for (long t : tries) {
            if (now - t <= TWO_MINUTES) {
                recent.add(t);
            }
        }
        recent.add(now);
        tryLog.put(userId, recent);

        // Rule 1
        if (recent.size() > MAX_TRIES) {
            return "Too many purchases: " + recent.size() + " in 2 minutes";
        }

        // Rule 2
        if (amount > BIG_PURCHASE) {
            return "Purchase of $" + String.format("%.2f", amount) + " is unusually large";
        }

        return null;
    }

    // How many tries this user has made in the last 2 minutes.
    public static int tryCount(String userId) {
        List<Long> tries = tryLog.get(userId);
        if (tries == null) {
            return 0;
        }
        return tries.size();
    }
}

// Holds the list of items for sale.
public class IAPSystem {

    private List<Item> storeCatalog;

    public IAPSystem() {
        storeCatalog = new ArrayList<>();
        seedCatalog();
    }

    private void seedCatalog() {
        storeCatalog.add(new Item("IAP101", "6,480 Virtual Crystals", 99.99, "Currency", "\uD83D\uDC8E"));
        storeCatalog.add(new Item("IAP102", "Cyberpunk Phantom Skin", 24.99, "Cosmetic", "\uD83C\uDFAD"));
        storeCatalog.add(new Item("IAP103", "Battle Pass (Season 8)", 9.99, "Pass", "\u26A1"));
        storeCatalog.add(new Item("IAP104", "500 Points Starter Pack", 4.99, "Currency", "\uD83E\uDE99"));
    }

    public List<Item> getCatalog() { return storeCatalog; }
}
