package com.example.performance

/**
 * This object helps structure and manage the game categories by providing
 * the necessary mappings between category names, level and
 * cost requirements.
 */
object CategoryData {
    /**
     * categoryMap maps each category name to specific numbers for retrieving the
     * trivia questions from the Open API
     */
    val categoryMap = mapOf(
        "Entertainment: Books" to "10",
        "Entertainment: Film" to "11",
        "Entertainment: Music" to "12",
        "Entertainment: Television" to "14",
        "Entertainment: Comics" to "29",
        "Entertainment: Video Games" to "15",
        "Entertainment: Musicals & Theatres" to "13",
        "Entertainment: Board Games" to "16",
        "Entertainment: Japanese Anime & Manga" to "31",
        "Entertainment: Cartoon & Animations" to "32",
        "General Knowledge" to "9",
        "Science: Gadgets" to "30",
        "Science: Mathematics" to "19",
        "Science: Computers" to "18",
        "Mythology" to "20",
        "Sports" to "21",
        "Geography" to "22",
        "History" to "23",
        "Politics" to "24",
        "Art" to "25",
        "Celebrities" to "26",
        "Animals" to "27",
        "Vehicles" to "28",
        "Science & Nature" to "17"
    )

    //Map to define which level unlocks which categories
    val levelRequirements = mapOf(
        "Entertainment: Books" to 1,
        "Entertainment: Film" to 15,
        "Entertainment: Music" to 10,
        "Entertainment: Television" to 20,
        "Entertainment: Comics" to 25,
        "Entertainment: Video Games" to 18,
        "Entertainment: Musicals & Theatres" to 30,
        "Entertainment: Board Games" to 5,
        "Entertainment: Japanese Anime & Manga" to 35,
        "Entertainment: Cartoon & Animations" to 40,
        "General Knowledge" to 1,
        "Science: Gadgets" to 2,
        "Science: Mathematics" to 27,
        "Science: Computers" to 30,
        "Mythology" to 33,
        "Sports" to 12,
        "Geography" to 8,
        "History" to 17,
        "Politics" to 26,
        "Art" to 45,
        "Celebrities" to 20,
        "Animals" to 6,
        "Vehicles" to 9,
        "Science & Nature" to 24
    )
    // Map to define cost of each categories
    val categoryUnlockCost = mapOf(
        "Entertainment: Books" to 0L,
        "Entertainment: Film" to 50L,
        "Entertainment: Music" to 40L,
        "Entertainment: Television" to 60L,
        "Entertainment: Comics" to 70L,
        "Entertainment: Video Games" to 45L,
        "Entertainment: Musicals & Theatres" to 65L,
        "Entertainment: Board Games" to 30L,
        "Entertainment: Japanese Anime & Manga" to 80L,
        "Entertainment: Cartoon & Animations" to 90L,
        "General Knowledge" to 0L,
        "Science: Gadgets" to 20L,
        "Science: Mathematics" to 75L,
        "Science: Computers" to 70L,
        "Mythology" to 85L,
        "Sports" to 35L,
        "Geography" to 25L,
        "History" to 55L,
        "Politics" to 60L,
        "Art" to 95L,
        "Celebrities" to 50L,
        "Animals" to 15L,
        "Vehicles" to 25L,
        "Science & Nature" to 65L
    )

}