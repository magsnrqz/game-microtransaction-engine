package one;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Exer4_IAPSystem extends JFrame {

    private static final long serialVersionUID = 1L;

    private IAPSystem iapEngine;
    private Gamer currentGamer;
    private JLabel balanceLabel;
    private Item selectedItem = null;
    private JPanel selectedTile = null;

    // Colours
    private final Color BG_DARK = new Color(18, 18, 24);
    private final Color CARD_BG = new Color(28, 28, 38);
    private final Color CARD_HOVER = new Color(42, 42, 58);
    private final Color ACCENT_BLUE = new Color(0, 112, 239);
    private final Color ACCENT_GREEN = new Color(46, 204, 113);
    private final Color ACCENT_RED = new Color(231, 76, 60);
    private final Color ACCENT_AMBER = new Color(241, 196, 15);
    private final Color TEXT_WHITE = new Color(240, 240, 245);
    private final Color TEXT_MUTED = new Color(150, 150, 165);

    public Exer4_IAPSystem() {
        iapEngine = new IAPSystem();
        currentGamer = new Gamer("PSN_User1", "2026100123", 150.00);

        setTitle("In-Game Store - Microtransactions");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // Top bar with the title and wallet balance
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(12, 12, 16));
        topBar.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("IN-GAME STORE", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_WHITE);

        balanceLabel = new JLabel(balanceText());
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        balanceLabel.setForeground(ACCENT_GREEN);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(balanceLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Middle area with one card per item
        JPanel gridPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        gridPanel.setBackground(BG_DARK);
        gridPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        for (Item item : iapEngine.getCatalog()) {
            gridPanel.add(createProductCard(item));
        }
        add(gridPanel, BorderLayout.CENTER);

        // Bottom bar with the checkout button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomBar.setBackground(new Color(12, 12, 16));

        JButton checkoutBtn = new JButton("Proceed to Checkout");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutBtn.setBackground(ACCENT_BLUE);
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.setPreferredSize(new Dimension(200, 40));
        checkoutBtn.addActionListener(e -> initiateCheckoutProcess());

        bottomBar.add(checkoutBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    // Builds one clickable item card.
    private JPanel createProductCard(Item item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 60), 1),
                new EmptyBorder(20, 15, 20, 15)));

        JLabel iconLabel = new JLabel(item.getIconSymbol(), SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("<html><center>" + item.getName() + "</center></html>",
                SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel typeLabel = new JLabel(item.getItemType().toUpperCase());
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        typeLabel.setForeground(TEXT_MUTED);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("$" + String.format("%.2f", item.getPriceUSD()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLabel.setForeground(ACCENT_BLUE);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(typeLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(priceLabel);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Un-highlight the old card
                if (selectedTile != null) {
                    selectedTile.setBackground(CARD_BG);
                    selectedTile.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 60), 1));
                }
                // Highlight this one
                selectedItem = item;
                selectedTile = card;
                card.setBackground(CARD_HOVER);
                card.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2));
            }
        });

        return card;
    }

    // Runs when the checkout button is pressed.
    private void initiateCheckoutProcess() {

        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the storefront first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Make sure the player has enough money first.
        if (!currentGamer.canAfford(selectedItem.getPriceUSD())) {
            JOptionPane.showMessageDialog(this,
                    "Not enough wallet balance.\n\n"
                    + "Needed: $" + String.format("%.2f", selectedItem.getPriceUSD()) + "\n"
                    + "You have: $" + String.format("%.2f", currentGamer.getWalletBalance()),
                    "Purchase Declined", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = JOptionPane.showInputDialog(this,
                "Selected: " + selectedItem.getName()
                + " ($" + String.format("%.2f", selectedItem.getPriceUSD()) + ")\n\n"
                + "Enter User ID\n"
                + "(Type SUSPECT999 to trigger the fraud check,\n"
                + "or buy 6 times quickly to trigger it too):",
                "Step 2: User Identification", JOptionPane.QUESTION_MESSAGE);

        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        userId = userId.trim();

        String pin = JOptionPane.showInputDialog(this,
                "Enter Wallet Security PIN (Default: 1234):",
                "Step 4: PIN Verification", JOptionPane.QUESTION_MESSAGE);

        if (pin == null || !pin.equals("1234")) {
            JOptionPane.showMessageDialog(this,
                    "Payment Authorization Failed: Invalid PIN.",
                    "Step 5 Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ask if we should fake a tampered receipt, so we can show
        // the validation step actually catching something.
        int answer = JOptionPane.showConfirmDialog(this,
                "Test a FAKE receipt?\n\n"
                + "Yes = change the price after the receipt is made,\n"
                + "so the check should reject it.",
                "Demo Option", JOptionPane.YES_NO_OPTION);

        showLiveProcessingDialog(userId, answer == JOptionPane.YES_OPTION);
    }

    // Shows the step-by-step processing window.
    private void showLiveProcessingDialog(String userId, boolean useFakeReceipt) {

        JDialog processDialog = new JDialog(this, "Transaction Processing", true);
        processDialog.setSize(540, 440);
        processDialog.setLocationRelativeTo(this);
        processDialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("PROCESSING TRANSACTION", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(ACCENT_BLUE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setForeground(ACCENT_BLUE);
        progressBar.setBackground(CARD_BG);
        progressBar.setStringPainted(true);
        progressBar.setMaximumSize(new Dimension(480, 25));

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(CARD_BG);
        logArea.setForeground(TEXT_WHITE);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(480, 240));

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(scrollPane);
        processDialog.add(mainPanel, BorderLayout.CENTER);

        final String user = userId;
        final Item item = selectedItem;

        // Runs the steps in the background so the window stays responsive.
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {

                // Step 5 - make the receipt
                log(logArea, "[Step 5] Authorizing wallet payment...");
                updateProgress(progressBar, 20, ACCENT_BLUE);
                Thread.sleep(800);

                String token = AppStoreAPI.generateReceiptToken(
                        item.getItemId(), user, item.getPriceUSD());
                log(logArea, "  -> Receipt: " + token);

                if (useFakeReceipt) {
                    token = AppStoreAPI.tamperWith(token);
                    log(logArea, "  -> [DEMO] Price changed to $0.01 after signing");
                }

                // Steps 6 and 7 - check the receipt
                log(logArea, "");
                log(logArea, "[Step 6-7] Checking if the receipt is genuine...");
                updateProgress(progressBar, 45, ACCENT_BLUE);
                Thread.sleep(900);

                if (!AppStoreAPI.validateReceiptToken(token)) {
                    updateProgress(progressBar, 100, ACCENT_RED);
                    log(logArea, "  -> REJECTED: receipt was changed");
                    log(logArea, "");
                    log(logArea, "[Step 8b] Receipt invalid. Purchase cancelled.");
                    return null;
                }
                log(logArea, "  -> OK: receipt is genuine");

                // Step 9 - fraud check
                log(logArea, "");
                log(logArea, "[Step 9] Running fraud check...");
                updateProgress(progressBar, 70, ACCENT_BLUE);
                Thread.sleep(1000);

                String problem = AntiCheatEngine.audit(user, item.getPriceUSD());
                log(logArea, "  -> Purchases in last 2 min: " + AntiCheatEngine.tryCount(user));

                if (problem != null) {
                    updateProgress(progressBar, 100, ACCENT_AMBER);
                    log(logArea, "");
                    log(logArea, "[Step 10a] FLAGGED");
                    log(logArea, "  -> " + problem);
                    log(logArea, "[Step 11a-14a] Purchase stopped and logged.");
                    return null;
                }
                log(logArea, "  -> OK: nothing suspicious");

                // Step 11 - save the purchase
                log(logArea, "");
                log(logArea, "[Step 11b] Updating wallet...");
                updateProgress(progressBar, 90, ACCENT_BLUE);
                Thread.sleep(800);

                double newBalance = currentGamer.getWalletBalance() - item.getPriceUSD();
                currentGamer.setWalletBalance(newBalance);
                updateBalanceLabel();

                updateProgress(progressBar, 100, ACCENT_GREEN);
                log(logArea, "  -> Saved");
                log(logArea, "");
                log(logArea, "[Step 14b] SUCCESS: " + item.getName() + " granted!");
                log(logArea, "  -> New balance: $" + String.format("%.2f", newBalance));

                return null;
            }
        };

        worker.execute();
        processDialog.setVisible(true);
    }

    // The text shown in the top-right corner.
    private String balanceText() {
        return "Wallet Balance: $" + String.format("%.2f", currentGamer.getWalletBalance());
    }

    // Swing parts must only be changed on the main UI thread,
    // so these three helpers use invokeLater to do that safely.

    private void updateBalanceLabel() {
        SwingUtilities.invokeLater(() -> balanceLabel.setText(balanceText()));
    }

    private void log(JTextArea area, String text) {
        SwingUtilities.invokeLater(() -> {
            area.append(text + "\n");
            area.setCaretPosition(area.getDocument().getLength());
        });
    }

    private void updateProgress(JProgressBar bar, int value, Color colour) {
        SwingUtilities.invokeLater(() -> {
            bar.setValue(value);
            bar.setForeground(colour);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Exer4_IAPSystem().setVisible(true));
    }
}