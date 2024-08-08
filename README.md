# Trello - Project Management App

## Overview
**Trello** is a project management app designed to help teams collaborate and manage their projects efficiently. Built with native Android development, the app allows users to create and manage boards, lists, and cards, making it easier to organize tasks and track progress.

## Features
- **User Authentication**: Users can sign up, sign in, and sign out securely.
- **Profile Management**: Update your profile with a name, image, email, and an optional mobile number.
- **Create Boards**: Start a new project by creating a board, where all tasks and activities can be organized.
- **Manage Lists**: Within a board, assigned members can add lists representing major features or phases of the project.
- **Card Management**: Members can add cards to lists, update card colors, and assign members to each card. However, only the project creator can add or remove members from the project.
- **Permissions Control**: The project creator has exclusive rights to add or delete members, while other members can modify cards but cannot remove the project maker.

## Technology
- **Firebase**: Used to store and manage app data in a real-time database.
- **Firestore**: Used to store images and associated data.
- **Kotlin**: The app is built with Kotlin, leveraging its modern features for native Android development.
- **Material Design**: The app follows Google's Material Design principles to provide a clean and intuitive user interface.
- **Latest Android APIs**: The app is built using the latest code patterns and APIs to ensure compatibility across all Android versions.
- **Activity Management**: Proper handling of activities and the activity stack to ensure a smooth and user-friendly experience.

## Code Structure
The app follows a modular architecture to maintain clean and maintainable code:
- **Activities**: Handle different screens and manage the user flow throughout the app.
- **Fragments**: Used within activities to modularize UI components and enhance reusability.
- **Firebase Integration**: Manages real-time data synchronization, ensuring that all users see the latest updates instantly.

## Screenshots

| Welcome Screen | SignIn screen | Home Screen |
|-------------|----------------|--------------------------|
| ![1](https://github.com/user-attachments/assets/ba32e6ea-46b4-4bd6-8c36-9fc57c35c806) | ![2](https://github.com/user-attachments/assets/950fa80a-0b80-49e7-a659-cec52deec590) | ![3](https://github.com/user-attachments/assets/30664eb3-38fb-4a99-8eec-17d154877fe1) |

| Profile Update Screen | Create Board | Manage members |
|----------------|-------------------|------------------|
| ![4](https://github.com/user-attachments/assets/c6538b0c-75db-49eb-8f32-8142eb86ef5b) | ![5](https://github.com/user-attachments/assets/ec8108fa-36f3-4cc5-bc18-eb73197b68a8) | ![6](https://github.com/user-attachments/assets/2703fb3f-3579-4325-806c-a73ec9eab672) |

| Add List and Cards | Update Card | Updated Board |
|----------------|-------------------|------------------|
| ![7](https://github.com/user-attachments/assets/b1785549-afed-49df-bd56-90e59cc8d9c0) | ![8](https://github.com/user-attachments/assets/6492883e-33db-47fa-9396-d1a85ea4f182) | ![9](https://github.com/user-attachments/assets/899da7d4-0bad-45dd-b3d0-ab26fc2a2522) |
