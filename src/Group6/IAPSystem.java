package Group6;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

class Gamer {
    private String gamerTag;
    private String userId;
    private double walletBalance;
    private String role;

    public Gamer(String gamerTag, String userId, double walletBalance) {
        this(gamerTag, userId, walletBalance, "PLAYER");
    }

    public Gamer(String gamerTag, String userId, double walletBalance, String role) {
        this.gamerTag = gamerTag;
        this.userId = userId;
        this.walletBalance = walletBalance;
        this.role = role;
    }

    public String getGamerTag() { return gamerTag; }
    public String getUserId() { return userId; }
    public double getWalletBalance() { return walletBalance; }
    public String getRole() { return role; }
    public void setWalletBalance(double newBalance) { this.walletBalance = newBalance; }

    public boolean canAfford(double amount) {
        return walletBalance >= amount;
    }

    public static double fetchBalanceFromDB(String userId) {
        String sql = "SELECT currency_balance FROM USER WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("currency_balance");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch balance from DB: " + e.getMessage(), e);
        }
        throw new RuntimeException("User " + userId + " not found in database");
    }
}

class AppStoreAPI {
    private static final String SECRET = "GRP6SECRET";

    public static String generateReceiptToken(String purchaseId, String itemId, String userId, double price) {
        String data = purchaseId + "|" + userId + "|" + itemId + "|" + String.format("%.2f", price);
        return "PSN-TX-" + data + "-" + fingerprint(data);
    }

    public static boolean validateReceiptToken(String token) {
        if (token == null || !token.startsWith("PSN-TX-")) return false;
        String rest = token.substring(7);
        int split = rest.lastIndexOf('-');
        if (split < 0) return false;

        String data = rest.substring(0, split);
        String givenFingerprint = rest.substring(split + 1);

        return fingerprint(data).equals(givenFingerprint);
    }

    public static String tamperWith(String token) {
        int split = token.lastIndexOf('-');
        String data = token.substring(0, split);
        String fingerprint = token.substring(split + 1);
        int lastPipe = data.lastIndexOf('|');
        return data.substring(0, lastPipe) + "|0.01-" + fingerprint;
    }

    private static String fingerprint(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((data + SECRET).getBytes("UTF-8"));
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                result.append(String.format("%02x", hash[i]));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class AntiCheatEngine {
    private static final int MAX_TRIES = 5;
    private static final long TWO_MINUTES = 120000;
    private static final double BIG_PURCHASE = 150.00;

    private static Map<String, List<Long>> tryLog = new HashMap<>();

    public static String audit(String userId, double amount) {
        if (userId.equalsIgnoreCase("SUSPECT999")) {
            logFraudToDB(userId, "Account is on the watchlist");
            return "Account is on the watchlist";
        }

        long now = System.currentTimeMillis();
        List<Long> tries = tryLog.computeIfAbsent(userId, k -> new ArrayList<>());

        List<Long> recent = new ArrayList<>();
        for (long t : tries) {
            if (now - t <= TWO_MINUTES) {
                recent.add(t);
            }
        }
        recent.add(now);
        tryLog.put(userId, recent);

        if (recent.size() > MAX_TRIES) {
            String reason = "Too many purchases: " + recent.size() + " in 2 minutes";
            logFraudToDB(userId, reason);
            return reason;
        }

        if (amount > BIG_PURCHASE) {
            String reason = "Purchase of $" + String.format("%.2f", amount) + " is unusually large";
            logFraudToDB(userId, reason);
            return reason;
        }

        return null;
    }

    public static int tryCount(String userId) {
        List<Long> tries = tryLog.get(userId);
        return tries == null ? 0 : tries.size();
    }

    public static void logFraudToDB(String userId, String reason) {
        String sql = "INSERT INTO FRAUD_LOG (user_id, reason, logged_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, reason);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Could not log fraud to DB: " + e.getMessage());
        }
    }

    public static void logAuditToDB(String purchaseId, String metadata, String decision) {
        String sql = "INSERT INTO AUDIT_LOG (purchase_id, metadata, decision) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, purchaseId);
            stmt.setString(2, metadata);
            stmt.setString(3, decision);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Could not log audit to DB: " + e.getMessage());
        }
    }
}

public class IAPSystem {

    public List<Item> getCatalog() {
        List<Item> storeCatalog = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCT";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                storeCatalog.add(new Item(
                        rs.getString("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("type"),
                        rs.getString("icon_symbol")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load products from the database: " + e.getMessage(), e);
        }

        return storeCatalog;
    }

    // Reads what a player owns, joined with product names. Used by the inventory window.
    public List<Object[]> getInventory(String userId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT p.name, p.type, i.quantity, i.granted_at " +
                     "FROM INVENTORY i JOIN PRODUCT p ON i.product_id = p.product_id " +
                     "WHERE i.user_id = ? ORDER BY i.granted_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new Object[] {
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("granted_at")
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load inventory: " + e.getMessage(), e);
        }
        return rows;
    }
}