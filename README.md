# Compose Auth Demo

A Jetpack Compose Android app demonstrating a complete beginner-friendly authentication flow with registration, login, session persistence, validation, and multi-screen navigation.

This project is a solid junior Android portfolio sample because it shows practical app flow rather than a single static screen. The app includes a splash experience, an authentication gate, form validation, local persistence, a home screen, and an example of launching an email intent.

# Project Summary

Compose Auth Demo is a small Android application built with Kotlin and Jetpack Compose. It walks the user through a basic registration and login flow using local storage and Compose navigation.

The public code shows the following screens and flows:

* splash screen
* authentication gateway
* login screen
* registration screen
* home screen
* support email intent
* logout and session check flow

# Core Features

## Splash and Session Routing

* splash screen at app startup
* automatic routing based on whether the user is already marked as logged in

## Registration Flow

* first name field
* family name field
* date of birth field
* email validation
* password input
* success dialog after registration

## Login Flow

* email and password login
* validation of required fields
* error handling for missing or incorrect credentials

## Local Persistence

* stores user data locally with `SharedPreferences`
* stores a logged-in session flag to route users directly to the home screen

## Home Screen

* displays logged-in email
* allows logout
* includes an email intent to contact support

# Tech Stack

* Kotlin
* Android
* Jetpack Compose
* Material 3
* Navigation Compose
* SharedPreferences

# What This Project Demonstrates

This project is useful for junior Android applications because it shows:

* Compose-based UI development
* state handling with Compose
* form validation
* screen navigation
* local storage basics
* simple Android intent usage

# Current Architecture Notes

The main visible application flow is built inside Compose-based screen functions and a navigation graph. The public code also shows:

* email validation with regex
* DOB validation in `YYYY-MM-DD` format
* login state persistence
* an app structure centered around `MainActivity.kt`

# How to Run

1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Run on an emulator or Android device.
4. Register a user account locally.
5. Log in and test navigation, persistence, logout, and support-email intent flow.

# Important Improvement Note

This is a good demo project, but for a stronger production-style portfolio presentation it should be improved by:

* replacing plain `SharedPreferences` password storage with secure handling
* adding a README screenshot section
* separating screens and logic into cleaner files or layers
* adding tests for validation and navigation logic
* optionally replacing local auth with Firebase Auth or a backend API

# What I Learned

This project helped strengthen:

* Jetpack Compose layout and state management
* navigation between screens
* Android local persistence
* validation and form feedback
* building a more complete user flow in a mobile app

# Future Improvements

* secure credential storage
* MVVM structure with ViewModel classes
* unit tests for validation
* dark mode polish and UI refinement
* remote auth integration

# Portfolio Positioning

If I were presenting this on a project CV, I would describe it as:

"A Kotlin Jetpack Compose Android authentication demo with registration, login, session persistence, validation, navigation, and support-intent integration."
