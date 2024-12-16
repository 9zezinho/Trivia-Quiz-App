# Trivia Quiz App 🎮

Welcome to the **Trivia Quiz App**! This app allows users to play exciting trivia quizzes on various topics and challenge their knowledge.

---

## Libraries Used 📚

- **[Ion Library](https://search.maven.org/artifact/com.koushikdutta.ion/ion/3.1.0/jar)** - For handling asynchronous HTTP requests.
- **[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)** - For rendering beautiful and interactive pie charts.
- **[Open Trivia Database (API)](https://opentdb.com/api_config.php)** - Provides trivia questions from different categories.
- **[Firebase Realtime Database](https://firebase.google.com)** - For storing and syncing data in real-time, including user scores and game history.

---

## Features ✨

1. **User Authentication:**
   - Users can create an account and log in using their credentials.
   - Option to set a daily reminder to play the quiz.

2. **Quiz Customization:**
   - Choose from a variety of quiz categories (e.g., General Knowledge, Science, Entertainment....).
   - Select the difficulty level of question(e.g., easy, medium, hard)
   - Select the type of question(e.g., true/false, multiple-choice) or you can choose "any" to have both types of questions.

3. **Lifelines and Music:**
   - Use lifelines like **Skip** and **50-50** to make the quiz easier.
   - Option to play or stop background music from the settings.

4. **In-App Store:**
   - Redeem lifelines using coins, which can be earned by playing.
   - Track your quiz performance and view your history of answered questions in the History section.

5. **Leaderboard and Ranking:**
   - View the top 10 players on the leaderboard.
   - A live ranking system refreshes every 5 minutes. Users who earn the most points in that time get coins as a reward.

6. **Leveling Up:**
   - As users level up, they can use earned coins to unlock new categories.
   - Users must reach the required level to unlock certain categories and can purchase them using coins.

---

## Screenshots 📸

Here’s a screenshot of the app in action:

<img width="208" alt="image" src="https://github.com/user-attachments/assets/af6da6e0-0041-4491-8284-d4c9dc1f4151"/>

<img width="208" alt="image" src="https://github.com/user-attachments/assets/0e55c993-9f8f-4b98-8bfd-7079ceae78a7"/>

<img width="208" alt="image" src="https://github.com/user-attachments/assets/560c5ff1-511a-44b9-8cf4-4a33e266b9c5"/>

<img width="208" alt="image" src="https://github.com/user-attachments/assets/36b4ccc5-1d8d-438b-8b22-0e670a5873b7"/>

<img width="208" alt="image" src="https://github.com/user-attachments/assets/5dca78d5-0c8e-4447-bc2d-0b3b8d2efe18"/>

<img width="208" alt="image" src="https://github.com/user-attachments/assets/2fedec7b-7d16-442f-9cc1-61778ac64c22"/>

<img width="208" alt="image" src="https://github.com/user-attachments/assets/dbb883f0-ba70-4457-80ce-a3249d822822"/>

<img width="208" alt="image" src="https://github.com/user-attachments/assets/d7a81645-3213-4255-b6df-51ebbb571db6"/>

---
## Known Issues & Troubleshooting 🐛

- **Issue**: The app crashes on launch.
  - **Solution**: Make sure you have the correct Firebase configuration. Check the Firebase setup guide and make sure to connect your project to Firebase. And in FirebaseManager.kt, change this "path-to-firebase-real-time-database" to your database path.

If you encounter any other issues, feel free to create an issue in the repository.



## Contribution 🤝

We welcome contributions to the project! If you have suggestions or would like to improve the app, feel free to fork this repository and submit a pull request. Make sure to follow the steps below:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature-name`).
3. Commit your changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-name`).
5. Open a pull request.

---

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
