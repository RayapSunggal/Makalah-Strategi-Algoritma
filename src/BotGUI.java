import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class BotGUI extends JFrame {

    static class Card {
        char suit;
        String rankStr;
        int value;

        public Card(String input) {
            String[] parts = input.split("-");
            this.suit = parts[0].toUpperCase().charAt(0);
            this.rankStr = parts[1].toUpperCase();
            this.value = parseValue(this.rankStr);
        }

        private int parseValue(String rank) {
            switch (rank) {
                case "A": return 14;
                case "K": return 13;
                case "Q": return 12;
                case "J": return 11;
                default: return Integer.parseInt(rank);
            }
        }

        @Override
        public String toString() {
            return suit + "-" + rankStr;
        }
    }

    private JTextArea logArea;
    private JTextField inputField;
    private JButton submitButton;
    private JPanel gridPanel;
    private Map<String, JToggleButton> cardButtonsMap = new LinkedHashMap<>();

    private enum GamePhase {
        INPUT_MY_HAND, BIDDING, CONTRACT_ROLE, CONTRACT_TRUMP, INPUT_DUMMY_HAND, TRICK_LEADER, PLAYING_TRICK, GAME_OVER
    }

    private GamePhase currentPhase = GamePhase.INPUT_MY_HAND;
    
    private Set<String> myHandSet = new HashSet<>();
    private Set<String> partnerHandSet = new HashSet<>();
    private Set<String> playedCardsSet = new HashSet<>();
    
    private List<Card> myHand = new ArrayList<>();
    private List<Card> partnerHand = new ArrayList<>();
    
    private int hcp = 0;
    private int role = -1;
    private char trump = ' ';
    
    private int trickKitaMenang = 0;
    private int trickLawanMenang = 0;
    private int currentTrick = 1;
    private int leader = -1;
    private int currentTurn = 0;
    
    private char ledSuit = ' ';
    private int highestValue = 0;
    private int winningPlayer = -1;
    private Card winningCard = null;

    static final int KITA = 0;
    static final int LAWAN_KIRI = 1;
    static final int TEMAN = 2;
    static final int LAWAN_KANAN = 3;

    public BotGUI() {
        setTitle("Bot Bridge Greedy Murni (Pro Visual GUI)");
        setSize(850, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(850, 300));
        add(scrollPane, BorderLayout.NORTH);

        setupCardGrid();
        add(gridPanel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        submitButton = new JButton("Konfirmasi / Lanjut");
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitButton.setBackground(new Color(70, 130, 180));
        submitButton.setForeground(Color.WHITE);
        
        inputPanel.add(new JLabel(" Teks Input (Jika diminta): "), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(inputPanel, BorderLayout.SOUTH);

        ActionListener submitAction = e -> processSubmit();
        submitButton.addActionListener(submitAction);
        inputField.addActionListener(submitAction);

        startGame();
    }

    private void setupCardGrid() {
        gridPanel = new JPanel(new GridLayout(4, 13, 2, 2));
        gridPanel.setBorder(BorderFactory.createTitledBorder("Papan Kartu (Visual)"));
        
        String[] suits = {"S", "H", "D", "C"};
        String[] ranks = {"A", "K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2"};
        
        for (String s : suits) {
            for (String r : ranks) {
                String cardKey = s + "-" + r;
                JToggleButton btn = new JToggleButton(getSymbol(s) + r);
                btn.setFont(new Font("SansSerif", Font.BOLD, 14));
                btn.setFocusPainted(false);
                
                if (s.equals("H") || s.equals("D")) {
                    btn.setForeground(Color.RED);
                }
                else {
                    btn.setForeground(Color.BLACK);
                }
                
                btn.addActionListener(e -> handleCardClick(btn, cardKey));
                
                cardButtonsMap.put(cardKey, btn);
                gridPanel.add(btn);
            }
        }
    }

    private String getSymbol(String suit) {
        switch (suit) {
            case "S": return "♠ ";
            case "H": return "♥ ";
            case "D": return "♦ ";
            case "C": return "♣ ";
            default: return "";
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void handleCardClick(JToggleButton btn, String cardKey) {
        int selectedCount = getSelectedCardsFromGrid().size();
        
        if (currentPhase == GamePhase.INPUT_MY_HAND || currentPhase == GamePhase.INPUT_DUMMY_HAND) {
            if (btn.isSelected() && selectedCount > 13) {
                btn.setSelected(false);
                JOptionPane.showMessageDialog(this, "Anda hanya bisa memilih maksimal 13 kartu!");
            }
        }
        else if (currentPhase == GamePhase.PLAYING_TRICK) {
            if (btn.isSelected() && selectedCount > 1) {
                btn.setSelected(false);
                JOptionPane.showMessageDialog(this, "Anda hanya bisa memilih 1 kartu untuk dikeluarkan!");
            }
        }
    }

    private List<String> getSelectedCardsFromGrid() {
        List<String> selected = new ArrayList<>();
        for (Map.Entry<String, JToggleButton> entry : cardButtonsMap.entrySet()) {
            if (entry.getValue().isSelected() && entry.getValue().isEnabled()) {
                selected.add(entry.getKey());
            }
        }
        return selected;
    }

    private void resetGridVisuals() {
        for (JToggleButton btn : cardButtonsMap.values()) {
            btn.setSelected(false);
            btn.setBackground(null);
            btn.setOpaque(false);
            btn.setEnabled(false);
        }
    }

    private void updateGridForPhase() {
        resetGridVisuals();
        
        for (Map.Entry<String, JToggleButton> entry : cardButtonsMap.entrySet()) {
            String cardKey = entry.getKey();
            JToggleButton btn = entry.getValue();
            
            boolean isPlayed = playedCardsSet.contains(cardKey);
            boolean isMyHand = myHandSet.contains(cardKey);
            boolean isDummyHand = partnerHandSet.contains(cardKey);

            if (isPlayed) {
                btn.setEnabled(false);
                continue;
            }

            switch (currentPhase) {
                case INPUT_MY_HAND:
                    btn.setEnabled(true);
                    break;
                case INPUT_DUMMY_HAND:
                    if (!isMyHand) btn.setEnabled(true);
                    break;
                case PLAYING_TRICK:
                    int currentPlayer = (leader + currentTurn) % 4;
                    if (currentPlayer == KITA) {
                        if (isMyHand) btn.setEnabled(true);
                    }
                    else if (currentPlayer == TEMAN && role == 1) {
                        if (isDummyHand) btn.setEnabled(true);
                    }
                    else {
                        if (!isMyHand && !isDummyHand) btn.setEnabled(true);
                    }
                    break;
                default:
                    btn.setEnabled(false);
                    break;
            }
        }
    }

    private void highlightSuggestedCard(Card c) {
        if (c != null && cardButtonsMap.containsKey(c.toString())) {
            JToggleButton btn = cardButtonsMap.get(c.toString());
            btn.setBackground(Color.YELLOW);
            btn.setOpaque(true);
        }
    }

    private void startGame() {
        log("=== BOT BRIDGE GREEDY ===");
        log("\n[FASE 1] KARTU ANDA");
        log("Silakan klik (centang) tepat 13 kartu Anda di papan atas, lalu tekan 'Konfirmasi'.");
        updateGridForPhase();
        inputField.setEnabled(false);
    }

    private void processSubmit() {
        String inputText = inputField.getText().trim().toUpperCase();
        
        try {
            switch (currentPhase) {
                case INPUT_MY_HAND:
                    List<String> mySel = getSelectedCardsFromGrid();
                    if (mySel.size() != 13) throw new IllegalArgumentException("Harus memilih tepat 13 kartu!");
                    for (String s : mySel) {
                        myHand.add(new Card(s));
                        myHandSet.add(s);
                    }
                    log("> 13 Kartu Anda telah disimpan.");
                    
                    hcp = calculateHCP(myHand);
                    String bestSuit = suggestBestSuit(myHand);
                    
                    log("[Sistem] High Card Points (HCP) Anda: " + hcp);
                    log("[Sistem] Suit terkuat/terpanjang Anda: " + bestSuit);
                    
                    log("\n[FASE 2] BIDDING / BETTING");
                    log("Ketik bid terakhir teman Anda (Ketik 'PASS', '1H', '1NT', dll) di kolom teks bawah.");
                    
                    inputField.setEnabled(true);
                    inputField.setText("");
                    currentPhase = GamePhase.BIDDING;
                    updateGridForPhase();
                    break;

                case BIDDING:
                    if (inputText.isEmpty()) throw new IllegalArgumentException("Teks tidak boleh kosong.");
                    log("> Teman Bid: " + inputText);
                    
                    String recommendedSuit = suggestBestSuit(myHand);
                    
                    if (inputText.equals("PASS")) {
                        if (hcp >= 13) {
                            log("-> Saran Bet Bot: OPEN BID 1 " + recommendedSuit + " (Poin Anda " + hcp + ").");
                        }
                        else {
                            log("-> Saran Bet Bot: PASS.");
                        }
                    }
                    else {
                        int estimatedTotalHCP = hcp + 13;
                        log("-> Estimasi Total Poin Tim: ~" + estimatedTotalHCP);
                        
                        if (hcp < 6) {
                            log("-> Saran Bet Bot: PASS. Poin Anda terlalu kecil.");
                        }
                        else if (hcp >= 6 && hcp <= 9) {
                            log("-> Saran Bet Bot: RESPONS MINIMUM. (Dukung suit teman di level 2, atau bid 1NT).");
                        }
                        else if (hcp >= 10 && hcp <= 12) {
                            log("-> Saran Bet Bot: INVITE GAME. (Dukung suit teman di level 3, atau bid " + recommendedSuit + " di level 2).");
                        }
                        else {
                            log("-> Saran Bet Bot: GAME FORCING! (Misal 3NT, atau 4 " + recommendedSuit + ").");
                        }
                    }

                    log("\n[FASE 3] KONTRAK FINAL");
                    log("Ketik '1' jika Tim Kita Menyerang (Declarer), atau '2' jika Bertahan.");
                    currentPhase = GamePhase.CONTRACT_ROLE;
                    inputField.setText("");
                    break;

                case CONTRACT_ROLE:
                    int r = Integer.parseInt(inputText);
                    if (r != 1 && r != 2) throw new IllegalArgumentException("Ketik angka 1 atau 2.");
                    role = r;
                    log("> Tim Kita: " + (role == 1 ? "Menyerang" : "Bertahan"));
                    log("Ketik Trump Suit (C/D/H/S) atau 'N' untuk No-Trump:");
                    currentPhase = GamePhase.CONTRACT_TRUMP;
                    inputField.setText("");
                    break;

                case CONTRACT_TRUMP:
                    char t = inputText.charAt(0);
                    if ("CDHSN".indexOf(t) == -1) throw new IllegalArgumentException("Harus C, D, H, S, atau N.");
                    trump = t;
                    log("> Trump Suit: " + trump);

                    if (role == 1) {
                        log("\n[FASE 4] TANGAN DUMMY");
                        log("Silakan centang tepat 13 kartu Dummy di papan atas, lalu tekan Konfirmasi.");
                        inputField.setEnabled(false);
                        currentPhase = GamePhase.INPUT_DUMMY_HAND;
                        updateGridForPhase();
                    }
                    else {
                        startTrickPhase();
                    }
                    inputField.setText("");
                    break;

                case INPUT_DUMMY_HAND:
                    List<String> dumSel = getSelectedCardsFromGrid();
                    if (dumSel.size() != 13) throw new IllegalArgumentException("Harus memilih tepat 13 kartu dummy!");
                    for (String s : dumSel) {
                        partnerHand.add(new Card(s));
                        partnerHandSet.add(s);
                    }
                    log("> 13 Kartu Dummy telah disimpan.");
                    startTrickPhase();
                    break;

                case TRICK_LEADER:
                    int l = Integer.parseInt(inputText);
                    if (l < 0 || l > 3) throw new IllegalArgumentException("Ketik 0, 1, 2, atau 3.");
                    leader = l;
                    currentTrick = 1;
                    startNewTrickUI();
                    currentPhase = GamePhase.PLAYING_TRICK;
                    inputField.setEnabled(false);
                    promptNextPlayer();
                    break;

                case PLAYING_TRICK:
                    List<String> playSel = getSelectedCardsFromGrid();
                    if (playSel.size() != 1) throw new IllegalArgumentException("Pilih tepat 1 kartu di papan!");
                    
                    String playedStr = playSel.get(0);
                    Card playedCard = new Card(playedStr);
                    playedCardsSet.add(playedStr);
                    
                    int currentPlayer = (leader + currentTurn) % 4;
                    
                    if (currentPlayer == KITA) myHand.removeIf(c -> c.toString().equals(playedStr));
                    if (currentPlayer == TEMAN && role == 1) partnerHand.removeIf(c -> c.toString().equals(playedStr));

                    log("> " + getNamaPemain(currentPlayer) + " mengeluarkan: " + playedCard);

                    if (currentTurn == 0) {
                        ledSuit = playedCard.suit;
                        highestValue = playedCard.value;
                        winningCard = playedCard;
                        winningPlayer = currentPlayer;
                    }
                    else {
                        boolean beats = false;
                        if (playedCard.suit == trump && winningCard.suit != trump) beats = true;
                        else if (playedCard.suit == winningCard.suit && playedCard.value > winningCard.value) beats = true;

                        if (beats) {
                            winningCard = playedCard;
                            highestValue = playedCard.value;
                            winningPlayer = currentPlayer;
                        }
                    }

                    currentTurn++;

                    if (currentTurn == 4) {
                        log("\n=> Pemenang Trick " + currentTrick + ": " + getNamaPemain(winningPlayer) + " (" + winningCard + ")");
                        if (winningPlayer == KITA || winningPlayer == TEMAN) trickKitaMenang++;
                        else trickLawanMenang++;
                        
                        leader = winningPlayer;
                        currentTrick++;
                        
                        if (currentTrick > 13) {
                            log("\n================================================");
                            log("PERMAINAN SELESAI!");
                            log("Skor Akhir -> Kita: " + trickKitaMenang + " | Lawan: " + trickLawanMenang);
                            log("================================================");
                            currentPhase = GamePhase.GAME_OVER;
                            updateGridForPhase();
                        }
                        else {
                            startNewTrickUI();
                            promptNextPlayer();
                        }
                    }
                    else {
                        promptNextPlayer();
                    }
                    break;

                case GAME_OVER:
                    log("Game Over. Restart aplikasi untuk main lagi.");
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "[ERROR] " + ex.getMessage(), "Input Salah", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startTrickPhase() {
        log("\n=== PERMAINAN DIMULAI (13 TRICKS) ===");
        log("Daftar: 0=Kita, 1=Lawan Kiri, 2=Teman/Dummy, 3=Lawan Kanan");
        log("Ketik siapa yang jalan pertama di Trick 1? (0/1/2/3) di kolom teks.");
        inputField.setEnabled(true);
        currentPhase = GamePhase.TRICK_LEADER;
        updateGridForPhase();
    }

    private void startNewTrickUI() {
        log("\n------------------------------------------------");
        log("TRICK " + currentTrick + " | Skor -> Kita: " + trickKitaMenang + " | Lawan: " + trickLawanMenang);
        log("------------------------------------------------");
        ledSuit = ' ';
        highestValue = 0;
        winningPlayer = leader;
        winningCard = null;
        currentTurn = 0;
    }

    private void promptNextPlayer() {
        int currentPlayer = (leader + currentTurn) % 4;
        String namaPemain = getNamaPemain(currentPlayer);
        boolean isPartnerWinning = (winningPlayer == (currentPlayer + 2) % 4);

        updateGridForPhase();

        if (currentPlayer == KITA) {
            Card suggested = suggestCard(myHand, ledSuit, trump, highestValue, isPartnerWinning);
            log("\n[Giliran KITA] Bot Menyarankan: " + suggested);
            log("Silakan klik 1 kartu KITA di papan dan Konfirmasi.");
            highlightSuggestedCard(suggested);
        }
        else if (currentPlayer == TEMAN && role == 1) {
            Card suggested = suggestCard(partnerHand, ledSuit, trump, highestValue, isPartnerWinning);
            log("\n[Giliran DUMMY] Bot Menyarankan: " + suggested);
            log("Silakan klik 1 kartu DUMMY di papan dan Konfirmasi.");
            highlightSuggestedCard(suggested);
        }
        else {
            log("\n[Giliran " + namaPemain + "]");
            log("Silakan klik 1 kartu LAWAN di papan dan Konfirmasi.");
        }
    }

    private String getNamaPemain(int id) {
        switch(id) {
            case KITA: return "Kita";
            case LAWAN_KIRI: return "Lawan Kiri";
            case TEMAN: return "Teman/Dummy";
            case LAWAN_KANAN: return "Lawan Kanan";
            default: return "Unknown";
        }
    }

    private int calculateHCP(List<Card> hand) {
        int p = 0;
        for (Card c : hand) {
            if (c.value == 14) p += 4;
            else if (c.value == 13) p += 3;
            else if (c.value == 12) p += 2;
            else if (c.value == 11) p += 1;
        }
        return p;
    }

    private String suggestBestSuit(List<Card> hand) {
        int s = 0, h = 0, d = 0, c = 0;
        
        for (Card card : hand) {
            if (card.suit == 'S') s++;
            else if (card.suit == 'H') h++;
            else if (card.suit == 'D') d++;
            else if (card.suit == 'C') c++;
        }
        
        int max = Math.max(Math.max(s, h), Math.max(d, c));
        
        if (s == max) return "Sekop (S)";
        if (h == max) return "Hati (H)";
        if (d == max) return "Wajik (D)";
        return "Keriting (C)";
    }

    private Card suggestCard(List<Card> hand, char lSuit, char tSuit, int highestVal, boolean partnerWinning) {
        if (hand.isEmpty()) return null;

        if (lSuit == ' ') {
            hand.sort((a, b) -> b.value - a.value); 
            return hand.get(0); 
        }

        List<Card> followSuitCards = new ArrayList<>();
        for (Card c : hand) if (c.suit == lSuit) followSuitCards.add(c);

        if (!followSuitCards.isEmpty()) {
            followSuitCards.sort((a, b) -> a.value - b.value); 
            if (partnerWinning) return followSuitCards.get(0);
            for (Card c : followSuitCards) {
                if (c.value > highestVal) return c;
            }
            return followSuitCards.get(0);
        }

        List<Card> trumpCards = new ArrayList<>();
        for (Card c : hand) if (c.suit == tSuit) trumpCards.add(c);

        if (!trumpCards.isEmpty() && !partnerWinning) {
            trumpCards.sort((a, b) -> a.value - b.value);
            return trumpCards.get(0);
        }

        hand.sort((a, b) -> a.value - b.value);
        return hand.get(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BotGUI().setVisible(true);
        });
    }
}