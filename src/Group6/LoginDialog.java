package Group6;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Sign-in window shown before the store opens (FR-01 and FR-02).
// Checks the username and the scrambled password against the USER table.
public class LoginDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Color BG_DARK = new Color(18, 18, 24);
    private final Color CARD_BG = new Color(28, 28, 38);
    private final Color ACCENT_BLUE = new Color(0, 112, 239);
    private final Color TEXT_WHITE = new Color(240, 240, 245);
    private final Color TEXT_MUTED = new Color(150, 150, 165);

    private JTextField usernameField = new JTextField(18);
    private JPasswordField passwordField = new JPasswordField(18);
    private Gamer loggedIn = null;

    private LoginDialog() {
        super((Frame) null, "Sign In", true);
        setSize(380, 290);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("SIGN IN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(ACCENT_BLUE);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());

        panel.add(title);
        panel.add(Box.createVerticalStrut(18));
        panel.add(fieldLabel("Username"));
        panel.add(styled(usernameField));
        panel.add(Box.createVerticalStrut(12));
        panel.add(fieldLabel("Password"));
        panel.add(styled(passwordField));
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginBtn);

        getRootPane().setDefaultButton(loginBtn); // pressing Enter signs in
        add(panel);
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField styled(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBackground(CARD_BG);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 60)),
                new EmptyBorder(4, 8, 4, 8)));
        return field;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a username and password.",
                    "Sign In", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT user_id, username, currency_balance, role FROM USER "
                + "WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hash(password));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loggedIn = new Gamer(rs.getString("username"), rs.getString("user_id"),
                        rs.getDouble("currency_balance"), rs.getString("role"));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Wrong username or password.",
                        "Sign In Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        } catch (Exception e) {
            // This is the first thing that touches the database,
            // so any connection problem shows up here.
            System.err.println("Login failed to reach database: " + e.getMessage()); // details for developers
            JOptionPane.showMessageDialog(this, "Can't reach the server right now.\nPlease try again later.",
                    "Connection Problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Scrambles the password with SHA-256. Only the scrambled version is stored or compared.
    static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Shows the window and waits. Returns the signed-in player, or null if the window was closed.
    public static Gamer showLogin() {
        LoginDialog dialog = new LoginDialog();
        dialog.setVisible(true); // blocks here until the dialog closes
        return dialog.loggedIn;
    }
}