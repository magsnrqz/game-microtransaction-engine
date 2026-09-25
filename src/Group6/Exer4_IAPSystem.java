package Group6;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class Exer4_IAPSystem extends JFrame {

    private static final long serialVersionUID = 1L;

    private IAPSystem iapEngine;
    private Gamer currentGamer;
    private JLabel balanceLabel;
    private Item selectedItem = null;
    private JPanel selectedTile = null;

    private final Color BG_DARK = new Color(18, 18, 24);
    private final Color CARD_BG = new Color(28, 28, 38);
    private final Color CARD_HOVER = new Color(42, 42, 58);
    private final Color ACCENT_BLUE = new Color(0, 112, 239);
    private final Color ACCENT_GREEN = new Color(46, 204, 113);
    private final Color ACCENT_RED = new Color(231, 76, 60);
    private final Color ACCENT_AMBER = new Color(241, 196, 15);
    private final Color TEXT_WHITE = new Color(240, 240, 245);
    private final Color TEXT_MUTED = new Color(150, 150, 165);
    private final Color CARD_HOVER_SOFT = new Color(35, 35, 48);
    private final Color LINE = new Color(45, 45, 60);

    public Exer4_IAPSystem(Gamer loggedInGamer) {
        iapEngine = new IAPSystem();
        currentGamer = loggedInGamer;

        setTitle("In-Game Store");
        getContentPane().setPreferredSize(new Dimension(950, 600)); // size of the inside, not counting the title bar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // Header Section - PLAYSTATION STORE
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(12, 12, 16));
        topBar.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("PLAYSTATION STORE", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_WHITE);

        balanceLabel = new JLabel(balanceText());
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        balanceLabel.setForeground(ACCENT_GREEN);

        // Players just see their name. Admins get a small tag so the role is visible.
        String roleTag = "ADMIN".equals(currentGamer.getRole()) ? " (Admin)" : "";
        JLabel userLabel = new JLabel("Signed in as " + currentGamer.getGamerTag() + roleTag, SwingConstants.CENTER);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(TEXT_MUTED);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(userLabel, BorderLayout.CENTER);
        topBar.add(balanceLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Catalog Cards Grid
        JPanel gridPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        gridPanel.setBackground(BG_DARK);
        gridPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        try {
            for (Item item : iapEngine.getCatalog()) {
                gridPanel.add(createProductCard(item));
            }
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage()); // technical details go to the Eclipse console
            JOptionPane.showMessageDialog(null, "Couldn't load the store right now.\nPlease try again later.",
                    "Store Unavailable", JOptionPane.ERROR_MESSAGE);
        }
        add(gridPanel, BorderLayout.CENTER);

        // Bottom Checkout Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomBar.setBackground(new Color(12, 12, 16));

        JButton checkoutBtn = new JButton("Proceed to Checkout");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        checkoutBtn.setBackground(ACCENT_BLUE);
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkoutBtn.setPreferredSize(new Dimension(180, 38));
        checkoutBtn.addActionListener(e -> initiateCheckoutProcess());

        JButton inventoryBtn = new JButton("My Inventory");
        inventoryBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inventoryBtn.setBackground(CARD_HOVER);
        inventoryBtn.setForeground(TEXT_WHITE);
        inventoryBtn.setFocusPainted(false);
        inventoryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        inventoryBtn.setPreferredSize(new Dimension(150, 38));
        inventoryBtn.addActionListener(e -> showInventory());

        bottomBar.add(inventoryBtn);
        bottomBar.add(checkoutBtn);
        add(bottomBar, BorderLayout.SOUTH);

        pack();                       // builds the window around the inside, so nothing gets cut off
        setLocationRelativeTo(null);  // centre it on screen (must come after pack)
    }

    private JPanel createProductCard(Item item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 60), 1),
                new EmptyBorder(25, 15, 25, 15)));

        JLabel iconLabel = new JLabel(item.getIconSymbol(), SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconLabel.setForeground(TEXT_WHITE);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("<html><center>" + item.getName() + "</center></html>", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(TEXT_WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel typeLabel = new JLabel(item.getItemType().toUpperCase());
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        typeLabel.setForeground(TEXT_MUTED);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("$" + String.format("%.2f", item.getPriceUSD()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        priceLabel.setForeground(ACCENT_BLUE);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue()); // pushes the content to the middle of the card
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(typeLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(priceLabel);
        card.add(Box.createVerticalGlue());

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            // Light up a card when the mouse is over it, unless it's already selected.
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (card != selectedTile) card.setBackground(CARD_HOVER_SOFT);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (card != selectedTile) card.setBackground(CARD_BG);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectedTile != null) {
                    selectedTile.setBackground(CARD_BG);
                    selectedTile.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(45, 45, 60), 1),
                            new EmptyBorder(25, 15, 25, 15)));
                }
                selectedItem = item;
                selectedTile = card;
                card.setBackground(CARD_HOVER);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                        new EmptyBorder(24, 14, 24, 14))); // 1px less padding to make room for the thicker line
            }
        });

        return card;
    }

    private void initiateCheckoutProcess() {
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the storefront first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!currentGamer.canAfford(selectedItem.getPriceUSD())) {
            JOptionPane.showMessageDialog(this,
                    "Not enough wallet balance.\n\nNeeded: $" + String.format("%.2f", selectedItem.getPriceUSD()) +
                            "\nYou have: $" + String.format("%.2f", currentGamer.getWalletBalance()),
                    "Purchase Declined", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // The user is already known from the login screen.
        // Sign in as the "suspect" account to demo the fraud check,
        // or buy 6 times quickly on any account to trigger it too.
        String userId = currentGamer.getUserId();

        String pin = JOptionPane.showInputDialog(this, "Enter your wallet PIN to confirm this purchase:",
                "Confirm Purchase", JOptionPane.QUESTION_MESSAGE);
        if (pin == null) return; // player cancelled
        if (!pin.equals("1234")) {
            JOptionPane.showMessageDialog(this, "Incorrect PIN. Your purchase was not completed.",
                    "Purchase Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Staff-only test purchase: alters the payment to prove tampered payments are rejected.
        // Players never see this option.
        boolean testMode = false;
        if ("ADMIN".equals(currentGamer.getRole())) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Run this as a test purchase?\n\nThe payment will be altered on purpose\nto check that it gets rejected.",
                    "Staff Test Purchase", JOptionPane.YES_NO_OPTION);
            testMode = (answer == JOptionPane.YES_OPTION);
        }

        showLiveProcessingDialog(userId, testMode);
    }

    private void showLiveProcessingDialog(String userId, boolean useFakeReceipt) {
        JDialog processDialog = new JDialog(this, "Checkout", true);
        processDialog.setLayout(new BorderLayout());
        processDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // can't close halfway through paying

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("PROCESSING PURCHASE", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(ACCENT_BLUE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setForeground(ACCENT_BLUE);
        progressBar.setBackground(CARD_BG);
        progressBar.setStringPainted(false); // no percentage text, like a real store
        progressBar.setBorderPainted(false);
        progressBar.setMaximumSize(new Dimension(480, 25));

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(CARD_BG);
        logArea.setForeground(TEXT_WHITE);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(480, 240));
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(scrollPane);

        // Done button stays greyed out until the purchase has finished.
        JButton doneBtn = new JButton("Done");
        doneBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        doneBtn.setBackground(ACCENT_BLUE);
        doneBtn.setForeground(Color.WHITE);
        doneBtn.setFocusPainted(false);
        doneBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        doneBtn.setPreferredSize(new Dimension(120, 36));
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneBtn.setEnabled(false);
        doneBtn.addActionListener(e -> processDialog.dispose());

        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(doneBtn);
        processDialog.add(mainPanel, BorderLayout.CENTER);

        final String user = userId;
        final Item item = selectedItem;

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                String purchaseId = "PURCHASE-" + UUID.randomUUID().toString().substring(0, 8);

                String orderId = purchaseId.substring(9).toUpperCase(); // e.g. F3762DDB
                log(logArea, "Contacting payment provider...");
                updateProgress(progressBar, 20, ACCENT_BLUE);
                Thread.sleep(800);

                String token = AppStoreAPI.generateReceiptToken(purchaseId, item.getItemId(), user, item.getPriceUSD());
                log(logArea, "  Order ID: " + orderId);

                if (useFakeReceipt) {
                    token = AppStoreAPI.tamperWith(token);
                    log(logArea, "  (Staff test purchase)");
                }

                log(logArea, "");
                log(logArea, "Verifying payment...");
                updateProgress(progressBar, 45, ACCENT_BLUE);
                Thread.sleep(900);

                if (!AppStoreAPI.validateReceiptToken(token)) {
                    updateProgress(progressBar, 100, ACCENT_RED);
                    log(logArea, "");
                    setHeading(title, "PAYMENT FAILED", ACCENT_RED);
                    log(logArea, "We couldn't verify this payment.");
                    log(logArea, "Your purchase was cancelled and you have not been charged.");
                    recordFailedPurchase(purchaseId, user, item, "FAILED");
                    AntiCheatEngine.logAuditToDB(purchaseId, "Token: " + token, "REJECTED_RECEIPT");
                    return null;
                }
                log(logArea, "  Payment verified.");

                log(logArea, "");
                log(logArea, "Running security check...");
                updateProgress(progressBar, 70, ACCENT_BLUE);
                Thread.sleep(1000);

                String problem = AntiCheatEngine.audit(user, item.getPriceUSD());

                if (problem != null) {
                    updateProgress(progressBar, 100, ACCENT_AMBER);
                    // The real reason is saved to FRAUD_LOG. Players are never told which rule caught them.
                    System.err.println("Fraud flag for " + user + ": " + problem);
                    log(logArea, "");
                    setHeading(title, "PURCHASE ON HOLD", ACCENT_AMBER);
                    log(logArea, "This purchase couldn't be completed.");
                    log(logArea, "For your account's security, purchases are paused for now.");
                    log(logArea, "You have not been charged. Please try again later.");
                    recordFailedPurchase(purchaseId, user, item, "FLAGGED");
                    AntiCheatEngine.logAuditToDB(purchaseId, "Reason: " + problem, "FLAGGED_FRAUD");
                    return null;
                }
                log(logArea, "  Security check passed.");

                log(logArea, "");
                log(logArea, "Completing your purchase...");
                updateProgress(progressBar, 90, ACCENT_BLUE);
                Thread.sleep(800);

                boolean transactionSuccess = executePurchaseTransaction(purchaseId, user, item, token);

                if (transactionSuccess) {
                    // Read the balance back from MySQL, proving the purchase was really saved.
                    double newBalance;
                    try {
                        newBalance = Gamer.fetchBalanceFromDB(user);
                    } catch (RuntimeException ex) {
                        newBalance = currentGamer.getWalletBalance() - item.getPriceUSD();
                        System.err.println("Could not re-read balance: " + ex.getMessage());
                    }
                    currentGamer.setWalletBalance(newBalance);
                    updateBalanceLabel();

                    updateProgress(progressBar, 100, ACCENT_GREEN);
                    log(logArea, "");
                    setHeading(title, "PURCHASE COMPLETE", ACCENT_GREEN);
                    log(logArea, "Purchase complete!");
                    log(logArea, item.getName() + " has been added to your inventory.");
                    log(logArea, "Wallet balance: $" + String.format("%.2f", newBalance));
                    AntiCheatEngine.logAuditToDB(purchaseId, "Receipt: " + token, "APPROVED");
                } else {
                    updateProgress(progressBar, 100, ACCENT_RED);
                    log(logArea, "");
                    setHeading(title, "PURCHASE FAILED", ACCENT_RED);
                    log(logArea, "Something went wrong while completing your purchase.");
                    log(logArea, "You have not been charged. Please try again.");
                    recordFailedPurchase(purchaseId, user, item, "FAILED");
                    AntiCheatEngine.logAuditToDB(purchaseId, "Error: DB Exception", "ROLLED_BACK");
                }

                return null;
            }

            // Runs once the purchase has finished, whether it worked or not.
            @Override
            protected void done() {
                doneBtn.setEnabled(true);
                processDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            }
        };

        processDialog.pack();                      // size the window to fit everything in it
        processDialog.setLocationRelativeTo(this); // then centre it over the store
        worker.execute();
        processDialog.setVisible(true);
    }

    private boolean executePurchaseTransaction(String purchaseId, String userId, Item item, String receiptToken) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Enable ACID Transaction

            String sqlPurchase = "INSERT INTO PURCHASE (purchase_id, user_id, product_id, base_price, discount_amount, final_amount, status) " +
                    "VALUES (?, ?, ?, ?, 0, ?, 'COMPLETED')";
            try (PreparedStatement stmt = conn.prepareStatement(sqlPurchase)) {
                stmt.setString(1, purchaseId);
                stmt.setString(2, userId);
                stmt.setString(3, item.getItemId());
                stmt.setDouble(4, item.getPriceUSD());
                stmt.setDouble(5, item.getPriceUSD());
                stmt.executeUpdate();
            }

            String sqlReceipt = "INSERT INTO PAYMENT_RECEIPT (purchase_id, receipt_token, signed_data, validated, validated_at) " +
                    "VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlReceipt)) {
                stmt.setString(1, purchaseId);
                stmt.setString(2, receiptToken);
                stmt.setString(3, receiptToken.substring(receiptToken.lastIndexOf('-') + 1));
                stmt.executeUpdate();
            }

            String sqlUser = "UPDATE USER SET currency_balance = currency_balance - ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUser)) {
                stmt.setDouble(1, item.getPriceUSD());
                stmt.setString(2, userId);
                stmt.executeUpdate();
            }

            String sqlInventory = "INSERT INTO INVENTORY (user_id, product_id, quantity) VALUES (?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE quantity = quantity + 1";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInventory)) {
                stmt.setString(1, userId);
                stmt.setString(2, item.getItemId());
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            System.err.println("Transaction Failed: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Saves a purchase that did not go through, so the audit log has a record to point to.
    private void recordFailedPurchase(String purchaseId, String userId, Item item, String status) {
        String sql = "INSERT INTO PURCHASE (purchase_id, user_id, product_id, base_price, discount_amount, final_amount, status) " +
                "VALUES (?, ?, ?, ?, 0, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, purchaseId);
            stmt.setString(2, userId);
            stmt.setString(3, item.getItemId());
            stmt.setDouble(4, item.getPriceUSD());
            stmt.setDouble(5, item.getPriceUSD());
            stmt.setString(6, status);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not record failed purchase: " + e.getMessage());
        }
    }

    // Opens a window listing what the signed-in player owns, read from MySQL.
    private void showInventory() {
        java.util.List<Object[]> rows;
        try {
            rows = iapEngine.getInventory(currentGamer.getUserId());
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage()); // technical details go to the Eclipse console
            JOptionPane.showMessageDialog(this, "Couldn't load your inventory right now.\nPlease try again later.",
                    "Inventory Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] columns = {"Item", "Type", "Quantity", "Date Added"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        // Show dates like "Sep 24, 2026" instead of a raw timestamp.
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH);
        for (Object[] row : rows) {
            String date = (row[3] instanceof java.util.Date) ? dateFormat.format((java.util.Date) row[3]) : "";
            model.addRow(new Object[] { row[0], row[1], row[2], date });
        }

        JDialog dialog = new JDialog(this, "My Inventory", true);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("MY INVENTORY");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(TEXT_WHITE);
        panel.add(heading, BorderLayout.NORTH);

        if (rows.isEmpty()) {
            JLabel empty = new JLabel("No items yet. Buy something from the store!", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(TEXT_MUTED);
            panel.add(empty, BorderLayout.CENTER);
        } else {
            JTable table = new JTable(model);
            table.setRowHeight(32);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setBackground(CARD_BG);
            table.setForeground(TEXT_WHITE);
            table.setGridColor(LINE);
            table.setShowVerticalLines(false);
            table.setSelectionBackground(CARD_HOVER);
            table.setSelectionForeground(TEXT_WHITE);
            table.setFillsViewportHeight(true); // keeps the empty area below the rows dark too

            table.getTableHeader().setBackground(new Color(12, 12, 16));
            table.getTableHeader().setForeground(TEXT_MUTED);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getTableHeader().setReorderingAllowed(false);

            // Give the item name the most room, and centre the quantity.
            table.getColumnModel().getColumn(0).setPreferredWidth(240);
            table.getColumnModel().getColumn(1).setPreferredWidth(90);
            table.getColumnModel().getColumn(2).setPreferredWidth(70);
            table.getColumnModel().getColumn(3).setPreferredWidth(110);
            javax.swing.table.DefaultTableCellRenderer centre = new javax.swing.table.DefaultTableCellRenderer();
            centre.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(2).setCellRenderer(centre);

            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(CARD_BG);
            scroll.setBorder(BorderFactory.createLineBorder(LINE));
            panel.add(scroll, BorderLayout.CENTER);
        }

        dialog.setContentPane(panel);
        dialog.setSize(580, 380);
        dialog.setLocationRelativeTo(this); // centre it after everything is built
        dialog.setVisible(true);
    }

    private String balanceText() {
        return "Wallet Balance: $" + String.format("%.2f", currentGamer.getWalletBalance());
    }

    private void updateBalanceLabel() {
        SwingUtilities.invokeLater(() -> balanceLabel.setText(balanceText()));
    }

    private void log(JTextArea area, String text) {
        SwingUtilities.invokeLater(() -> {
            area.append(text + "\n");
            area.setCaretPosition(area.getDocument().getLength());
        });
    }

    // Changes the checkout heading to show how the purchase ended.
    private void setHeading(JLabel label, String text, Color colour) {
        SwingUtilities.invokeLater(() -> {
            label.setText(text);
            label.setForeground(colour);
        });
    }

    private void updateProgress(JProgressBar bar, int value, Color colour) {
        SwingUtilities.invokeLater(() -> {
            bar.setValue(value);
            bar.setForeground(colour);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gamer gamer = LoginDialog.showLogin();
            if (gamer == null) {
                System.exit(0); // login window was closed
            }
            new Exer4_IAPSystem(gamer).setVisible(true);
        });
    }
}