package com.example.data.local

import com.example.data.model.WordStudy

object CuratedWordStudies {

    fun getCuratedStudy(queryWord: String, preferredTestament: String = ""): WordStudy? {
        val clean = queryWord.lowercase().trim()
        val lowerTestament = preferredTestament.lowercase().trim()

        // 1. Direct or alias match
        val matchedKey = when (clean) {
            "euaggelion", "euangelion", "gospel", "good news" -> "euaggelion"
            "grace", "charis" -> "grace"
            "adoption", "huiothesia" -> "adoption"
            "redemption", "apolytrosis" -> "redemption"
            "faith", "pistis" -> "faith"
            "agape", "love", "charity" -> "agape"
            "logos", "the word" -> "logos"
            "koinonia", "fellowship" -> "koinonia"
            "ekklesia", "ecclesia", "church", "assembly" -> "ekklesia"
            "covenant", "berit", "testament" -> "covenant"
            "lovingkindness", "chesed", "hesed", "steadfast love", "unfailing love" -> "lovingkindness"
            "redeemer", "goel", "kinsman" -> "redeemer"
            "righteousness", "tsedakah", "zedakah", "justice" -> "righteousness"
            "shalom", "peace", "wholeness" -> "shalom"
            "kadosh", "holy", "holiness", "sanctified" -> "kadosh"
            "shema", "hear", "listen", "obey" -> "shema"
            else -> clean
        }

        val all = allCuratedMap
        // If matched key exists, return it
        val study = all[matchedKey] ?: all.values.firstOrNull {
            it.transliteration.equals(clean, ignoreCase = true) ||
            it.originalWord.equals(clean, ignoreCase = true) ||
            it.word.equals(clean, ignoreCase = true) ||
            clean.contains(it.word, ignoreCase = true) ||
            clean.contains(it.transliteration, ignoreCase = true)
        }

        return study?.let {
            if (it.searchGroundingSources.isEmpty()) {
                val authentic = com.example.data.verification.SourceTrustVerifier.getCuratedAuthenticSources(it.word)
                    .map { s -> "${s.title} | ${s.domain} [Trust: ${s.trustScore}%]" }
                it.copy(searchGroundingSources = authentic)
            } else {
                it
            }
        }
    }

    fun getAllCuratedStudies(): List<WordStudy> = allCuratedMap.values.map { study ->
        if (study.searchGroundingSources.isEmpty()) {
            val authentic = com.example.data.verification.SourceTrustVerifier.getCuratedAuthenticSources(study.word)
                .map { "${it.title} | ${it.domain} [Trust: ${it.trustScore}%]" }
            study.copy(searchGroundingSources = authentic)
        } else {
            study
        }
    }

    private val allCuratedMap: Map<String, WordStudy> = listOf(
        WordStudy(
            word = "euaggelion",
            testament = "New Testament",
            originalWord = "εὐαγγέλιον",
            transliteration = "euangelion",
            strongsNumber = "G2098",
            literalMeaning = "good news, royal proclamation of victory, glad tidings",
            thenMeaning = "In the first-century Greco-Roman world, 'euaggelion' was not a church term; it was an official imperial decree of state magnitude. When a Roman Caesar ascended the throne, had an imperial heir, or won a decisive military battle, imperial heralds (kērykes) were dispatched throughout the cities of the empire proclaiming the 'euaggelion'—the good news that Caesar was sovereign Lord, enemies were subdued, and Pax Romana (Roman Peace) was established. Citizens were summoned to bow, acknowledge Caesar as Lord, and rejoice in the new world order. When the Apostles declared the 'euaggelion of Jesus Christ, Son of God' (Mark 1:1; Romans 1:1-4), they co-opted Rome's supreme political vocabulary to declare that the crucified and resurrected Jesus of Nazareth—not Caesar—is the true King, Judge, and Savior of the entire cosmos.",
            nowMeaning = "Modern readers often strip 'euaggelion' of its public, cosmic, and royal majesty, reducing it to a private, transactional formula for going to heaven after death, or sentimental advice for personal self-improvement. We miss the explosive truth that the gospel is an imperial victory announcement calling every human being, nation, and empire to bend the knee in active allegiance to the living, enthroned Christ.",
            historicalBackground = "The Priene Calendar Inscription (9 BC) provides stunning historical evidence of how Rome used this word for Emperor Augustus: 'the birthday of the god Augustus was the beginning for the world of the good news (euaggelia) that have come to men on his account.' Imperial decrees across Rome, Asia Minor, and Egypt used euaggelion for Caesar's victories. The early church boldly declared this exact political vocabulary under emperors like Tiberius, Claudius, and Nero, knowing it directly challenged the imperial cult.",
            culturalContext = "In the ancient Mediterranean world, news travelled via public heralds rather than newspapers or digital media. To receive an imperial 'euaggelion' meant public civic festivals, animal sacrifices, and oaths of loyalty to the Emperor. Disregarding an imperial euaggelion was seen as sedition. In Jewish culture, this resonated deeply with the Septuagint (LXX) translation of Isaiah 40:9 and Isaiah 52:7 ('How beautiful upon the mountains are the feet of him who brings good news... who announces peace... who says to Zion, Your God reigns!').",
            legalDimension = "Under Roman treason laws (Lex Maiestas) and the Roman imperial cult, declaring another king or proclaiming rival good news of a different world sovereign was a capital offense punishable by crucifixion, beheading, or exile to penal colonies. Paul and the apostles were frequently arrested precisely because they were accused of 'acting contrary to the decrees of Caesar, saying that there is another king, Jesus' (Acts 17:7).",
            theologicalWeight = "When Paul, Mark, and Peter proclaim the euaggelion, they reveal that God's covenant promises to Israel and humanity have reached their triumphant climax. Jesus took the full hostility of Rome, the powers of darkness, and sin onto the cross, and through the resurrection was publicly declared to be the Son of God in power (Romans 1:4). The Gospel is the announcement that the true King has conquered death and inaugurates the kingdom of God.",
            keyScriptures = listOf("Mark 1:1", "Mark 1:14-15", "Romans 1:1-4", "Romans 1:16-17", "1 Corinthians 15:1-4", "Isaiah 52:7"),
            closingInsight = "When you realize the gospel is a royal coronation announcement rather than just religious advice, your entire Christian life changes. You are not merely a customer accepting an offer; you are a citizen of a cosmic kingdom, dispatched into the world to live out loyal allegiance to King Jesus whose victory over death is final and unquestioned."
        ),
        WordStudy(
            word = "grace",
            testament = "New Testament",
            originalWord = "χάρις",
            transliteration = "charis",
            strongsNumber = "G5485",
            literalMeaning = "favor, beauty, reciprocal goodwill, patronal benefaction",
            thenMeaning = "In the first-century Roman Empire, 'charis' was the foundational social glue that governed society: the Patron-Client system. A wealthy Patron bestowed an unearned, life-altering gift or civic protection upon a poor Client. The gift was free and unmerited, but it established a permanent, reciprocal bond. The Client was legally and socially bound to express lifelong gratitude (charis), public loyalty (pistis), and vocal honor to their Patron. It was never a license for passive inactivity; it was a relational covenant of allegiance.",
            nowMeaning = "Modern readers often misunderstand grace as 'cheap sentimentality' or unconditional permissiveness—an attitude that requires nothing of the recipient and changes nothing about their allegiance. We confuse unearned favor with zero-expectation apathy, treating grace as an abstract theological doctrine rather than a life-transforming covenant relationship.",
            historicalBackground = "Greco-Roman philosophers like Seneca wrote entire treatises ('De Beneficiis') strictly regulating the giving and receiving of charis, stating that ungratefulness was the most dishonorable vice in Roman society.",
            culturalContext = "Patronage governed every tier of Roman life. Clients would perform the morning greeting (salutatio) at their patron's villa, accompany him in the forum, and vote in accordance with his wishes.",
            legalDimension = "Under Roman social contract law, failing to honor a patron's benefaction could lead to public disgrace (infamia) or formal revocation of citizenship and privileges.",
            theologicalWeight = "When biblical writers say we are saved by 'grace,' they declare that God is the ultimate Cosmic Patron who has bestowed the infinite gift of His Son upon undeserving humanity. Our response is not earning the gift, but pledging our lifelong fidelity and obedience.",
            keyScriptures = listOf("Ephesians 2:8-9", "Titus 2:11-12", "Romans 5:1-2"),
            closingInsight = "Grace is not cheap; it cost the Patron everything and demands our complete allegiance. We do not work to earn God's favor, but because of His staggering benefaction, our entire life becomes an active hymn of loyalty."
        ),
        WordStudy(
            word = "adoption",
            testament = "New Testament",
            originalWord = "υἱοθεσία",
            transliteration = "huiothesia",
            strongsNumber = "G5206",
            literalMeaning = "placement as a legal adult son and heir",
            thenMeaning = "In ancient Roman law, huiothesia was not about adopting orphaned infants as in modern times. It was an elite legal ceremony where a wealthy, powerful patriarch adopted a mature man to be his official legal heir and carry on the family name, estate, and authority. Under Roman law, the adopted son underwent a complete severance of all legal, financial, and filial ties to his biological past: all previous debts were legally erased as if they never existed, and he received full authority as a legitimate heir (haeres suus). Julius Caesar adopted Octavian (Emperor Augustus) via huiothesia.",
            nowMeaning = "Modern readers think of adoption in purely emotional or social welfare terms—taking in a helpless child. We miss the staggering legal sovereignty: God did not just welcome us into a house; He legally wiped out all our past spiritual debts, revoked our old master's claim, and granted us full co-heirship with Christ.",
            historicalBackground = "Roman adoption was practiced among patrician families to ensure dynastic continuity. Augustus used huiothesia to secure the Roman imperial line, adopting Tiberius to guarantee smooth succession.",
            culturalContext = "In Roman society, the Patria Potestas (absolute power of the father) was immense. An adoption required a formal ritual of simulated sale (mancipatio) three times to sever the old father's power.",
            legalDimension = "Under the Praetorian edicts, adoption legally cancelled all prior civil obligations, obligations of servitude, and private debts. The adoptee legally possessed the right to inherit the family patrimony.",
            theologicalWeight = "Paul's use of huiothesia in Romans and Galatians emphasizes that believers have received the 'Spirit of adoption' (Romans 8:15). We are not second-class citizens or spiritual foster children; we are full legal heirs of God.",
            keyScriptures = listOf("Romans 8:15", "Galatians 4:4-5", "Ephesians 1:5"),
            closingInsight = "If you live with spiritual insecurity, carrying the guilt of past debts, you are living like a slave, not an heir. Under Roman huiothesia, your old debts are legally non-existent. You are a full heir of God, clothed in absolute security."
        ),
        WordStudy(
            word = "redemption",
            testament = "New Testament",
            originalWord = "ἀπολύτρωσις",
            transliteration = "apolytrosis",
            strongsNumber = "G629",
            literalMeaning = "buying back, releasing by paying a ransom",
            thenMeaning = "In the first-century Roman Empire, there were over 60 million slaves. 'Apolytrosis' was the technical legal term for purchasing a slave's freedom from the auction block. A benefactor would pay the full market ransom (lytron) to the slave owner in a temple court. The slave was then legally declared 'the property of the deity'—which was the ancient legal mechanism to make them permanently free from human masters.",
            nowMeaning = "Today, redemption is treated as a vague religious buzzword meaning 'becoming a better person' or 'getting a second chance.' We miss the legal, commercial reality: we were captives on the auction block of sin and death, unable to free ourselves, and a literal, infinite ransom was paid to secure our permanent release.",
            historicalBackground = "Manumission of slaves was a common legal practice in the Greco-Roman world, often performed through sacral manumission where the slave saved money or a patron paid the temple treasury.",
            culturalContext = "Being a slave in Rome meant having zero legal rights, being treated as 'living tools' (instrumentum vocale). Freedom was not just an emotional relief; it was the recovery of human dignity, legal standing, and citizenship.",
            legalDimension = "The legal process of apolytrosis involved a commercial exchange and a formal change of ownership. Because the ransom was paid to the god of the temple, the freed person could never be re-enslaved; they were legally protected under divine custody.",
            theologicalWeight = "Jesus and Paul used this term to depict the cross. Jesus' life was the ransom (lytron) paid to buy us back from the power of darkness (Colossians 1:13). Our redemption means we are no longer slaves to sin, but now belong to God, our protector and liberator.",
            keyScriptures = listOf("Ephesians 1:7", "Colossians 1:13-14", "Romans 3:24"),
            closingInsight = "Without knowing this context, we try to earn our freedom or live in fear of being dragged back to the auction block. Your ransom has been paid in full at the highest legal level. You are legally, permanently free from human and spiritual bondage."
        ),
        WordStudy(
            word = "faith",
            testament = "New Testament",
            originalWord = "πίστις",
            transliteration = "pistis",
            strongsNumber = "G4102",
            literalMeaning = "trust, allegiance, active faithfulness",
            thenMeaning = "In the Greco-Roman world, 'pistis' was not merely mental agreement with a set of facts. It was a social and legal term representing absolute trust, fidelity, and sworn allegiance. To have 'pistis' in a ruler or a covenant partner meant aligning your entire life to support them. It was a term of personal and political loyalty, establishing mutual obligations of protection and fidelity.",
            nowMeaning = "Modern readers often reduce 'faith' to a purely intellectual belief ('I agree that God exists') or a blind, emotional feeling. This strips the word of its active, loyal nature, separating belief from obedience and personal alignment with the Sovereign.",
            historicalBackground = "In treaties and social contracts, 'pistis' was the mutual trust that allowed trade, diplomatic alliances, and legal contracts to exist across different regions of the Roman Empire.",
            culturalContext = "In ancient client-patron relationships, 'pistis' was the client's public response of trust and absolute loyalty to a generous patron, reflecting their ongoing honor and commitment.",
            legalDimension = "Legally, 'pistis' was a binding pledge of reliability. If an ally broke 'pistis,' they were legally and socially declared treaty-breakers, which justified military or economic intervention under Roman international law.",
            theologicalWeight = "When biblical writers speak of 'faith in Christ,' they mean entering a covenant of absolute trust and lifelong allegiance to Jesus as the true King of the world, contrasting directly with swearing 'pistis' to Caesar.",
            keyScriptures = listOf("Hebrews 11:1", "Romans 1:17", "Galatians 2:16"),
            closingInsight = "Believing in God is not just about holding correct opinions in your mind; it is about pledging your life's allegiance. Biblical faith is active loyalty that finds its security in God's absolute faithfulness to us."
        ),
        WordStudy(
            word = "agape",
            testament = "New Testament",
            originalWord = "ἀγάπη",
            transliteration = "agape",
            strongsNumber = "G26",
            literalMeaning = "self-sacrificing, covenantal, unconditional love",
            thenMeaning = "In classical Greek, several words denoted love: 'eros' (romantic/passionate desire), 'philos' (affection between friends), and 'storge' (family bond). Biblical authors elevated the rare word 'agape' to describe a deliberate, self-emptying, unconditional commitment to seek the highest good of another, regardless of whether the recipient deserves it, reciprocal benefit, or emotional inclination.",
            nowMeaning = "Modern culture conflates love with fleeting emotional attraction, sentimentality, or romantic infatuation. Agape is radically distinct: it is not a feeling that happens to you, but a determined covenant decision of the will to sacrifice for the beloved.",
            historicalBackground = "In first-century pagan society, charity was typically transactional—given to gain public prestige or curry political favor. Agape shocked the Greco-Roman world by commanding love for enemies, outcasts, lepers, and slaves.",
            culturalContext = "The early Christian love-feasts (agapai) gathered masters and slaves at the same communion table, dissolving ancient rigid social hierarchies and provoking astonishment among pagan observers.",
            legalDimension = "Agape fulfills the royal law (James 2:8). It is the moral and legal distillation of the entire Torah, binding the believer into ethical responsibility toward neighbor and enemy alike.",
            theologicalWeight = "Agape defines the very essence of God's character ('God is agape', 1 John 4:8) and finds its supreme manifestation on the Cross (Romans 5:8). It is the signature badge of authentic discipleship.",
            keyScriptures = listOf("1 Corinthians 13:4-8", "John 3:16", "Romans 5:8", "1 John 4:7-12"),
            closingInsight = "Agape love does not wait for feelings or worthy recipients; it initiates sacrifice. When you love with agape, you manifest the very heartbeat and character of the Triune God in a broken, transactional world."
        ),
        WordStudy(
            word = "logos",
            testament = "New Testament",
            originalWord = "λόγος",
            transliteration = "logos",
            strongsNumber = "G3056",
            literalMeaning = "word, rational principle, divine speech, wisdom",
            thenMeaning = "To Greek philosophers (Heraclitus, the Stoics), the 'Logos' was the cosmic reason and rational architecture that held the universe together in intelligible order. To Jewish minds, 'Dabar Yahweh' (the Word of the Lord) was God's dynamic, creative, world-forming utterance that spoke creation into being and inspired the prophets. John 1:1 boldly bridges both worlds: the ultimate Cosmic Reason and the Creative Divine Speech is not an abstract force, but a living human person: Jesus of Nazareth.",
            nowMeaning = "Modern readers casually read 'Word' as printed ink on paper or a spoken sentence. This misses the breathtaking philosophical and theological claim: the ultimate meaning of the cosmos has taken on flesh and pitched His tent among us.",
            historicalBackground = "Hellenistic Jewish philosopher Philo of Alexandria (20 BC - 50 AD) had written extensively about the Logos as the mediator between the transcendent God and creation, setting the stage for John's definitive revelation.",
            culturalContext = "In Jewish Targums (Aramaic translations), whenever God interacted directly with humanity, they used 'Memra' (the Word of the Lord) to avoid anthropomorphism. John reveals the Memra has become incarnate.",
            legalDimension = "In ancient legal decree, the 'logos' of a monarch was irrevocable law. The incarnate Logos represents the ultimate sovereign authority and divine standard by which the world is judged and saved.",
            theologicalWeight = "The doctrine of the Incarnation (John 1:14) centers on Logos: God did not merely send a message; He came in person. All creation was made through Him, coheres in Him, and is redeemed in Him.",
            keyScriptures = listOf("John 1:1-3", "John 1:14", "Colossians 1:16-17", "Hebrews 1:1-3"),
            closingInsight = "If you want to know what the Creator is like, look at Jesus. In Christ, the cosmic principle that sustains galaxies became a beating human heart."
        ),
        WordStudy(
            word = "koinonia",
            testament = "New Testament",
            originalWord = "κοινωνία",
            transliteration = "koinonia",
            strongsNumber = "G2842",
            literalMeaning = "joint participation, partnership, shared life, commercial venture",
            thenMeaning = "In first-century Greek, 'koinonia' was a legal and commercial term used for business partnerships (such as Peter, James, and John's fishing enterprise in Luke 5:10). Partners in koinonia pooled their assets, shared all liabilities, and committed their personal resources toward a singular common objective. When applied to the Church, it meant radical, mutual co-ownership of life, spiritual mission, and material burdens.",
            nowMeaning = "Today, 'fellowship' is often reduced to coffee and donuts in a church basement, or casual social pleasantries. We miss the life-or-death, shared-pocketbook partnership of the early church.",
            historicalBackground = "Greco-Roman voluntary associations (collegia) had communal funds and mutual burial pacts, but Christian koinonia uniquely united diverse ethnicities, social strata, and economic classes in complete equality.",
            culturalContext = "Acts 2:42-45 and Acts 4:32 record that believers held all things in common, actively selling property so that no one among them had need—a literal enactment of economic koinonia.",
            legalDimension = "Under Roman partnership law (societas), partners were jointly and severally liable for obligations undertaken on behalf of the partnership, requiring absolute fiduciary trust.",
            theologicalWeight = "Koinonia begins with participation in the life of the Trinity (1 John 1:3) and the suffering of Christ (Philippians 3:10), which overflows into shared material and spiritual partnership among believers.",
            keyScriptures = listOf("Acts 2:42", "1 John 1:3", "Philippians 1:5", "2 Corinthians 13:14"),
            closingInsight = "Biblical fellowship is not casual hanging out; it is joint venture in the kingdom of God where our lives, resources, and hearts are inextricably bound together."
        ),
        WordStudy(
            word = "ekklesia",
            testament = "New Testament",
            originalWord = "ἐκκλησία",
            transliteration = "ekklesia",
            strongsNumber = "G1577",
            literalMeaning = "called-out gathering, civic democratic assembly",
            thenMeaning = "In ancient Greek city-states (poleis), the 'ekklesia' was the official assembly of citizens summoned out of their homes by a herald to make binding civic decisions, vote on laws, and govern the city. In the Septuagint (Greek Old Testament), it translated the Hebrew 'qahal'—the solemn assembly of Israel standing before God at Mount Sinai. When Jesus declared 'I will build my ekklesia' (Matthew 16:18), He was establishing a cosmic, counter-cultural civic embassy of heaven on earth.",
            nowMeaning = "Modern culture identifies 'church' as a physical building with a steeple, or a weekly religious spectator performance. The biblical ekklesia was never a building; it is a mobilized body of royal citizens executing kingdom governance on earth.",
            historicalBackground = "The Athenian Ekklesia was the supreme legislative assembly of Athens. In Roman provinces, Greek cities retained local ekklesiai to discuss municipal matters (as in Acts 19:39).",
            culturalContext = "Meeting as an ekklesia of King Jesus subverted the civic religion of pagan cities, replacing loyalty to Artemis or Caesar with the assembly of the saints.",
            legalDimension = "The decisions of an ekklesia were legally binding upon the community. Jesus bestowed the 'keys of the kingdom' to bind and loose upon this newly constituted assembly.",
            theologicalWeight = "The ekklesia is the body and bride of Christ, the temple of the Holy Spirit, and God's chosen agency for displaying His manifold wisdom to cosmic rulers and authorities (Ephesians 3:10).",
            keyScriptures = listOf("Matthew 16:18", "Ephesians 1:22-23", "Ephesians 3:10", "Acts 2:47"),
            closingInsight = "You do not 'go to church'; you ARE the ekklesia—the royal assembly summoned out of darkness to govern your sphere with the love, truth, and authority of King Jesus."
        ),
        WordStudy(
            word = "covenant",
            testament = "Old Testament",
            originalWord = "בְּרִית",
            transliteration = "berit",
            strongsNumber = "H1285",
            literalMeaning = "shackle, bond, covenant treaty, solemn pact",
            thenMeaning = "In the Ancient Near East, a 'berit' was not a standard business agreement. It was a solemn, life-and-death treaty that bound two parties together as sacred family. It was sealed by cutting sacrificial animals in half, with the parties walking between the pieces, essentially declaring: 'If I break this covenant, may I be slaughtered like these animals.' It established an unbreakable bond of kinship, loyalty, and mutual defense.",
            nowMeaning = "Today, covenants are often confused with commercial contracts. A contract is a temporary, self-serving agreement based on mutual distrust ('if you do your part, I will do mine'). If one party fails, the contract is broken. But a biblical covenant is a permanent, sacrificial commitment based on love and kinship.",
            historicalBackground = "Suzerain-Vassal treaties of the Hittite and Assyrian Empires (2nd millennium BC) heavily influenced biblical covenant structures, including the division of responsibilities, historical prologues, and lists of blessings and curses.",
            culturalContext = "In ancient nomadic societies, survival depended entirely on tribal kinship. Covenants allowed non-relatives to be adopted into the tribe, receiving the full protection and inheritance rights of blood brothers.",
            legalDimension = "The legal framework of 'berit' was sealed with blood oaths and binding stipulations. God's covenants are structurally asymmetrical (initiated by the Sovereign) yet legally bind God Himself to His promises, showing His infinite faithfulness to His people.",
            theologicalWeight = "The entire biblical narrative revolves around covenants (Abrahamic, Mosaic, Davidic, and New). It shows God's relentless drive to bring humanity back into His royal household, culminating in Jesus' sacrifice—the ultimate sealing of the New Covenant in His own blood.",
            keyScriptures = listOf("Genesis 15:17-18", "Jeremiah 31:31", "Hebrews 9:15"),
            closingInsight = "When we see our relationship with God as a contract, we live in constant fear of failure, thinking our mistakes annul the deal. Recognizing it as a blood-sealed covenant reveals that God's commitment to us is unconditional and family-based, anchored in His absolute faithfulness."
        ),
        WordStudy(
            word = "lovingkindness",
            testament = "Old Testament",
            originalWord = "חֶסֶד",
            transliteration = "chesed",
            strongsNumber = "H2617",
            literalMeaning = "covenant loyalty, steadfast love, active mercy",
            thenMeaning = "In the Old Testament, 'chesed' is the active, passionate commitment to fulfill one's covenant obligations. It is not an emotion or a passive feeling; it is a relentless, action-oriented loyalty. When God exercises 'chesed' toward His people, He is acting in accordance with His covenant promises—even when His people fail. It is love that acts, rescues, and remains loyal when there is absolutely no benefit to the giver.",
            nowMeaning = "We often translate 'chesed' as 'mercy' or 'love' in a modern romantic or sentimental sense. This strips the word of its steel. Sentimental love can fade when feelings change, but 'chesed' is a rock-solid, covenantal decision to remain loyal and supportive through adversity.",
            historicalBackground = "In the rugged tribal realities of ancient Israel, loyalty (chesed) within a family or treaty was a matter of physical survival. David and Jonathan's covenant (1 Samuel 20) is a prime example of human 'chesed' overriding royal rivalry.",
            culturalContext = "Ancient Semitic cultures highly valued hospitality and tribal loyalty. Showing 'chesed' to a stranger or ally was a sacred duty, establishing a bond of protective responsibility.",
            legalDimension = "Legally, 'chesed' resides in the interface between law and relationship. It is the legal obligation of a covenant elevated and animated by deep, family-like devotion. It means doing far more than the letter of the law requires.",
            theologicalWeight = "This is the primary word used to describe God's character in Exodus 34:6 ('abundant in lovingkindness and truth'). It is the foundation of biblical hope—God will not abandon His people because His covenant loyalty (chesed) is everlasting, outlasting human unfaithfulness.",
            keyScriptures = listOf("Exodus 34:6", "Psalm 136:1", "Lamentations 3:22-23"),
            closingInsight = "When you feel like God's love for you depends on your performance, you are confusing His love with human affection. God's 'chesed' is anchored in His covenant faithfulness. He remains loyal to you because of who He is, not because of what you do."
        ),
        WordStudy(
            word = "redeemer",
            testament = "Old Testament",
            originalWord = "גּוֹאֵל",
            transliteration = "goel",
            strongsNumber = "H1350",
            literalMeaning = "kinsman-redeemer, family champion/protector",
            thenMeaning = "In ancient Israel, a 'goel' was the nearest male relative responsible for defending and restoring the family's honor, blood, and property. If a family member fell into debt and had to sell their ancestral land or sell themselves into slavery, the 'goel' was legally obligated to pay the debt, buy back the land, or purchase the relative's freedom. He stepped in as the family's legal champion.",
            nowMeaning = "Today, 'redeemer' is viewed as a purely spiritual Savior who takes us to heaven when we die. This misses the raw, legal, and relational reality of the 'goel' as a family champion who steps into our physical, economic, and social brokenness to restore our inheritance and dignity.",
            historicalBackground = "The Book of Ruth is the classic historical narrative of the kinsman-redeemer in action, where Boaz legally redeems Elimelech's land and marries Ruth to preserve the family line.",
            culturalContext = "In tribal Israel, land was a sacred, permanent trust from God that could never be permanently sold out of the family. The 'goel' was the legal guardian who prevented the permanent loss of ancestral heritage.",
            legalDimension = "Under Levitical law, the duties of the 'goel' were strictly codified, outlining the order of kinship eligibility and the precise calculations for redeeming land, property, or persons.",
            theologicalWeight = "Job famously cried, 'I know that my Redeemer (Goel) lives!' indicating his supreme confidence that God Himself would act as his legal champion and restore him. When Isaiah calls God the 'Redeemer of Israel,' he is declaring that God is our nearest Kin who has taken legal responsibility for our restoration.",
            keyScriptures = listOf("Leviticus 25:25", "Job 19:25", "Ruth 4:9-10"),
            closingInsight = "Knowing God is your 'Goel' means realizing He is not a distant judge, but your closest Relative. He is legally, passionately committed to buying back everything you have lost, restoring your heritage, and championing your cause."
        ),
        WordStudy(
            word = "righteousness",
            testament = "Old Testament",
            originalWord = "צְדָקָה",
            transliteration = "tsedakah",
            strongsNumber = "H6666",
            literalMeaning = "justice, covenant-standard, relational rightness",
            thenMeaning = "In ancient Hebrew thought, 'tsedakah' was not an abstract moral perfection or compliance with a detached legal code. It was a relational term meaning 'faithfulness to the expectations of a relationship.' To be righteous meant fulfilling your relational obligations to both God and fellow human beings, especially caring for the vulnerable (widow, orphan, stranger) in accordance with the covenant.",
            nowMeaning = "Modern readers often think of 'righteousness' as self-righteous moralism, legalistic perfection, or an individualistic, private holiness that is disconnected from social justice, community responsibility, and active relational care.",
            historicalBackground = "In Hebrew poetry, 'tsedakah' is often paired with 'mishpat' (justice) to describe the foundational pillars of God's throne and the ideal standard for Israelite leaders.",
            culturalContext = "Ancient Israelite culture was highly communal. Relational rightness (tsedakah) was measured by how well a citizen contributed to the wholeness and legal defense of the community's weakest members.",
            legalDimension = "The legal framework of 'tsedakah' was covenantal. It is the active legal vindication of the oppressed, where the judge does not just decide a case neutrally but actively steps in to deliver and restore the victim of injustice.",
            theologicalWeight = "God's righteousness is His active, saving faithfulness to His covenant promises. He acts righteously by rescuing His people from bondage and restoring order, showing that His holiness is dynamically aligned with mercy and rescue.",
            keyScriptures = listOf("Genesis 15:6", "Amos 5:24", "Isaiah 58:6-7"),
            closingInsight = "If you think righteousness is only about following rules and avoiding sin, you miss its heartbeat. Biblical righteousness is the active restoration of right relationships, calling us to be channels of God's covenant loyalty and justice to the world."
        ),
        WordStudy(
            word = "shalom",
            testament = "Old Testament",
            originalWord = "שָׁלוֹם",
            transliteration = "shalom",
            strongsNumber = "H7965",
            literalMeaning = "wholeness, completeness, sound health, restored harmony",
            thenMeaning = "In ancient Hebrew thought, 'shalom' is derived from the root 'shalam' (to make whole, pay debts, or restore what is broken). Shalom is far more than the mere absence of military conflict; it is the active presence of flourishing, comprehensive wellness, justice, economic wholeness, and relational harmony in the community under the blessing of Yahweh.",
            nowMeaning = "Modern minds treat 'peace' as a temporary truce between warring nations or a calm emotional state of tranquility. Biblical shalom is a dynamic, structural reality: everything in creation functioning as God designed it.",
            historicalBackground = "In Ancient Near Eastern monarchies, peace was enforced through tyrannical military suppression (Pax Romana, Pax Assyriaca). Biblical shalom subverted this by rooting true peace in divine covenant righteousness.",
            culturalContext = "Greeting someone with 'Shalom aleikhem' (peace be upon you) was a prayer asking God to bring healing, prosperity, debt-relief, and wholeness to their entire household.",
            legalDimension = "In Mosaic civil law, 'shalam' was the legal restitution required to make a victim whole after damage or theft (Exodus 22:1). Shalom cannot exist without restitution and justice.",
            theologicalWeight = "God makes a 'covenant of shalom' with His people (Numbers 25:12, Isaiah 54:10). The Messiah is the Prince of Peace (Sar Shalom, Isaiah 9:6) who reconciles all things to God.",
            keyScriptures = listOf("Numbers 6:24-26", "Isaiah 9:6", "Jeremiah 29:7", "Psalm 122:6-7"),
            closingInsight = "God does not merely want you to experience quiet moments; He invites you into shalom—the deep, restorative wholeness where all brokenness, debts, and fractures are healed under His loving sovereignty."
        ),
        WordStudy(
            word = "kadosh",
            testament = "Old Testament",
            originalWord = "קָדוֹשׁ",
            transliteration = "kadosh",
            strongsNumber = "H6918",
            literalMeaning = "set apart, consecrated, utterly other, sacred radiant purity",
            thenMeaning = "In ancient Semitic theology, 'kadosh' denotes that which is fundamentally unique, set apart from the ordinary or profane, and infused with divine presence. God's holiness is not just moral purity; it is His uncreated, blazing, matchless otherness. Coming into contact with the holy required ritual cleansing and reverence, like standing near the sun's surface.",
            nowMeaning = "Modern culture often views 'holy' as synonymous with prudishness, self-righteous scrupulosity, or stern religious rule-following. Holiness is the awe-inspiring, vibrant radiance of the Creator's very being.",
            historicalBackground = "In Ancient Near Eastern religions, holy sanctuaries were isolated behind multiple physical barriers to protect mortals from chaotic deity wrath. In Israel, the Tabernacle brought the Holy God into the midst of the camp.",
            culturalContext = "The Levitical system divided reality into clean, unclean, common, and holy. Sanctification (kiddush) was the active dedication of people, days (Sabbath), and space to the service of Yahweh.",
            legalDimension = "Under the Holiness Code (Leviticus 17-26), holiness had immediate social and judicial implications: honest weights and measures, leaving field corners for the poor, and fair wages.",
            theologicalWeight = "Isaiah saw the seraphim crying 'Kadosh, Kadosh, Kadosh' (Isaiah 6:3). The threefold repetition indicates the infinite perfection of God's transcendent majesty and moral brilliance.",
            keyScriptures = listOf("Isaiah 6:3", "Leviticus 19:2", "Exodus 3:5", "Psalm 99:9"),
            closingInsight = "Holiness is not cold religious detachment; it is being caught up in the blazing beauty of the living God, setting you free from the mundane to live a life of sacred purpose."
        ),
        WordStudy(
            word = "shema",
            testament = "Old Testament",
            originalWord = "שְׁמַע",
            transliteration = "shema",
            strongsNumber = "H8085",
            literalMeaning = "hear, listen attentively, heed, actively obey",
            thenMeaning = "In ancient Hebrew linguistics, there is no separate word for 'obey.' To 'shema' is simultaneously to hear and to act upon what is heard. If you hear God's voice but do not do what He says, in Hebrew thought you have not truly 'shema'd'. It represents responsive, active listening that results in covenantal obedience.",
            nowMeaning = "In modern English, hearing is a passive sensory reception ('I heard the sound in the background') distinct from obedience. This creates a tragic divide between listening to sermons and actually obeying God's commands.",
            historicalBackground = "The Shema (Deuteronomy 6:4-9) was recited twice daily by observant Jews as the supreme confession of faith: 'Hear, O Israel: Yahweh our God, Yahweh is one!'",
            culturalContext = "Written in mezuzot on doorposts and tefillin on foreheads, the Shema bound Jewish daily life, family pedagogy, and economic ethics to the singular allegiance of Yahweh.",
            legalDimension = "In covenant treaties, the 'hearing' of the treaty terms was the formal acceptance of legal stipulations, binding the hearer to its blessings and sanctions.",
            theologicalWeight = "Jesus identified the Shema as the greatest commandment in all of Scripture (Mark 12:29-30), uniting total love for God with practical love for neighbor.",
            keyScriptures = listOf("Deuteronomy 6:4-5", "James 1:22", "Mark 12:29-30", "Exodus 19:5"),
            closingInsight = "True biblical listening is measured by the movement of your hands and feet, not the nod of your head. When God speaks, to hear is to love, and to love is to obey."
        )
    ).associateBy { it.word }

    fun getCuratedChatResponse(wordStudy: WordStudy, userQuestion: String): String {
        val q = userQuestion.lowercase().trim()
        val w = wordStudy.word
        val orig = wordStudy.originalWord
        val translit = wordStudy.transliteration

        return when {
            q.contains("legal") || q.contains("law") || q.contains("court") || q.contains("contract") || q.contains("decree") -> {
                "Regarding the legal dimension: ${wordStudy.legalDimension}\n\nThis legal underpinning changes everything: it shows that God's work in '$w' is grounded in binding, unshakeable covenantal and judicial reality."
            }
            q.contains("history") || q.contains("historical") || q.contains("background") || q.contains("priene") || q.contains("empire") || q.contains("caesar") || q.contains("rome") || q.contains("greek") -> {
                "In historical depth: For '${wordStudy.word}' ($orig - $translit), the historical setting is essential. ${wordStudy.historicalBackground}\n\nThis historical reality proves that biblical authors were not operating in a vacuum, but directly addressing the empires, rulers, and events of their day."
            }
            q.contains("culture") || q.contains("cultural") || q.contains("society") || q.contains("honor") || q.contains("shame") || q.contains("patron") || q.contains("family") || q.contains("household") -> {
                "In cultural context: For '${wordStudy.word}' ($orig - $translit):\n\n${wordStudy.culturalContext}\n\nUnderstanding the first-century cultural framework (such as honor/shame dynamics, patron-client reciprocity, and ancient household governance) unlocks the true relational heartbeat of this term."
            }
            q.contains("meaning") || q.contains("define") || q.contains("definition") || q.contains("literal") || q.contains("mean") -> {
                "Scholarly definition of '${wordStudy.word}' ($orig - $translit):\n\n• Literal Linguistic Root: ${wordStudy.literalMeaning}\n• First-Century Historical & Legal Reality: ${wordStudy.thenMeaning}\n\n• Legal Dimension: ${wordStudy.legalDimension}\n\nThis reveals that the original biblical meaning carries far greater judicial and covenantal weight than modern English translations convey."
            }
            q.contains("theology") || q.contains("theological") || q.contains("god") || q.contains("faith") || q.contains("jesus") || q.contains("christ") || q.contains("salvation") -> {
                "Theological depth of '${wordStudy.word}' ($orig - $translit):\n\n${wordStudy.theologicalWeight}\n\nKey Biblical Passages:\n" +
                wordStudy.keyScriptures.joinToString("\n") { "• $it" } +
                "\n\nClosing Insight: ${wordStudy.closingInsight}"
            }
            q.contains("today") || q.contains("now") || q.contains("modern") || q.contains("misunderstand") || q.contains("mistake") -> {
                "How modern readers misunderstand this: ${wordStudy.nowMeaning}\n\nClosing Insight: ${wordStudy.closingInsight}"
            }
            q.contains("scripture") || q.contains("verse") || q.contains("bible") || q.contains("passage") -> {
                "Key scriptures highlighting '$w' ($orig) include:\n" +
                wordStudy.keyScriptures.joinToString("\n") { "• $it" } +
                "\n\nTheological significance: ${wordStudy.theologicalWeight}"
            }
            else -> {
                "Regarding your question about '$w' ($orig / $translit):\n\n" +
                "• Literal Meaning: ${wordStudy.literalMeaning}\n\n" +
                "• Original 1st-Century Historical Context: ${wordStudy.thenMeaning}\n\n" +
                "• Cultural Environment: ${wordStudy.culturalContext}\n\n" +
                "• Legal Dimension: ${wordStudy.legalDimension}\n\n" +
                "• Transformative Insight: ${wordStudy.closingInsight}"
            }
        }
    }
}
