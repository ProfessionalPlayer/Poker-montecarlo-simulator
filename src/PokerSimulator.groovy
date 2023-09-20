import groovy.swing.SwingBuilder
import javax.swing.JFrame
import java.awt.BorderLayout
import java.awt.Dimension

class PokerSimulator {
    static final int NUM_SIMULATIONS = 1000

    static void startSimulationWithGUI() {
        def swing = new SwingBuilder()
        swing.edt {
            def frame = swing.frame(title: 'Poker Simulator', size: new Dimension(400, 300), defaultCloseOperation: JFrame.EXIT_ON_CLOSE) {
                vbox {
                    label(text: "Scegli le carte:")

                    def suits = ['diamonds', 'hearts', 'clubs', 'spades']
                    def ranks = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A']

                    def card1Suit  = comboBox(items: suits)
                    def card1Rank = comboBox(items: ranks)

                    def card2Suit = comboBox(items: suits)
                    def card2Rank = comboBox(items: ranks)

                    label(text: "Inserisci il numero di simulazioni:")
                    def simulationCount = textField(columns: 5, text: "50") // Default value set to 1000

                    def resultsTextArea = textArea(columns: 30, rows: 10, editable: false, text: "Inizio Simulazione" + "\n")
                    scrollPane(constraints: BorderLayout.CENTER, viewportView: resultsTextArea)


                    button(text: 'Avvia Simulatore', actionPerformed: {
                        def handSet = [
                                [suit: card1Suit.selectedItem, rank: card1Rank.selectedItem],
                                [suit: card2Suit.selectedItem, rank: card2Rank.selectedItem]
                        ].toSet()
                        def handList = new ArrayList(handSet)

                        // Retrieve simulation count from the text field
                        int count = simulationCount.text.toInteger()
                        for(int i = 0; i < count; i++) {
                            def results = simulate(handList)
                            resultsTextArea.append("Vittorie: ${results.wins}\n")
                            resultsTextArea.append("Sconfitte: ${results.losses}\n")
                            resultsTextArea.append("Pareggi: ${results.ties}\n\n")
                        }
                    })
                }
            }
            frame.pack()
            frame.setVisible(true)
        }
    }


    static Map simulate(playerHand) {
        int playerWins = 0
        int ties = 0

        for (int i = 0; i < NUM_SIMULATIONS; i++) {
            List<Map> deck = createDeck()

            List handSet = playerHand.toSet().toList()
            deck.removeAll(handSet)


            Collections.shuffle(deck)
            List<Map> opponentHand = deck[0..1]
            deck.removeAll(opponentHand)
            List<Map> tableCards = deck[2..6]


            def playerBestHand = evaluateBestHand(playerHand + tableCards)
            def opponentBestHand = evaluateBestHand(opponentHand + tableCards)

            if (playerBestHand > opponentBestHand) {
                playerWins++
            } else if (playerBestHand == opponentBestHand) {
                ties++
            }
        }

        int playerLosses = NUM_SIMULATIONS - playerWins - ties

        return [wins: playerWins, losses: playerLosses, ties: ties]
    }

    def static createDeck() {
        def suits = ['hearts', 'diamonds', 'clubs', 'spades']
        def ranks = (2..10).toList() + ['J', 'Q', 'K', 'A']
        return [suits, ranks].combinations().collect { suit, rank -> [suit: suit, rank: rank] }
    }

    static evaluateBestHand(hand) {
        def rankValues = ['2': 2, '3': 3, '4': 4, '5': 5, '6': 6, '7': 7, '8': 8, '9': 9, '10': 10, 'J': 11, 'Q': 12, 'K': 13, 'A': 14]
        def ranks = hand.collect { rankValues[it.rank.toString()] }
        def rankCounts = ranks.groupBy { it }.collectEntries { key, value -> [(key): value.size()] }
        def suits = hand.collect { it.suit }
        def suitCounts = suits.groupBy { it }.collectEntries { key, value -> [(key): value.size()] }

        def sortedRanks = ranks.sort()

        def isRoyalFlush = sortedRanks == [10, 11, 12, 13, 14] && suitCounts.find { it.value == 5 }
        if (isRoyalFlush) {
            return 9000 + (ranks.sum() as Integer)
        }

        def isStraightFlush = (sortedRanks[4] - sortedRanks[0] == 4 || sortedRanks == [2, 3, 4, 5, 14]) && suitCounts.find { it.value == 5 }
        if (isStraightFlush) {
            return 8000 + (ranks.sum() as Integer)
        }

        if (rankCounts.find { it.value == 4 }) {
            return 7000 + (ranks.sum() as Integer)
        }

        if (rankCounts.find { it.value == 3 } && rankCounts.find { it.value == 2 }) {
            return 6000 + (ranks.sum() as Integer)
        }

        if (suitCounts.find { it.value == 5 }) {
            return 5000 + (ranks.sum() as Integer)
        }

        def isStraight = (sortedRanks[4] - sortedRanks[0] == 4 || sortedRanks == [2, 3, 4, 5, 14]) && sortedRanks.toSet().size() == 5
        if (isStraight) {
            return 4000 + (ranks.sum() as Integer)
        }

        if (rankCounts.find { it.value == 3 }) {
            return 3000 + (ranks.sum() as Integer)
        }

        if (rankCounts.findAll { it.value == 2 }.size() == 2) {
            return 2000 + (ranks.sum() as Integer)
        }

        if (rankCounts.find { it.value == 2 }) {
            return 1000 + (ranks.sum() as Integer)
        }

        return ranks.sum()
    }


}
