# UniRide 🚍

UniRide is a comprehensive transportation management app developed to provide students and drivers with real-time access to bus schedules, routes, live tracking, and communication. It features an issue reporting system, emergency contact options, live chat, driver reviews with AI-based summaries, dual language support (English and Bengali), and a fully functional admin panel.

This project was developed as part of my final year project for my university. The project report is available in the `docs` folder.

## Screenshots 📱

| Screenshot 1 | Screenshot 2 | Screenshot 3 | Screenshot 4 |
|-------------|-------------|-------------|-------------|
| <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/1.png" width="85%" /></div> | <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/2.png" width="85%" /></div> | <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/3.png" width="85%" /></div> | <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/4.png" width="85%" /></div> |
| <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/5.png" width="85%" /></div> | <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/6.png" width="85%" /></div> | <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/7.png" width="85%" /></div> | <div align="center"><img src="https://github.com/Mahmud0808/UniRide/blob/master/docs/previews/8.png" width="85%" /></div> |

## Table of Contents 📁

- [Features](#features-)
- [Getting Started](#getting-started-)
  - [Firebase Setup](#firebase-setup-)
  - [API Keys Configuration](#api-keys-configuration-)
- [Firestore Security Rules](#firestore-security-rules-)
- [How to Promote a User to Admin](#how-to-promote-a-user-to-admin-%EF%B8%8F)

## Features ⭐

- **Live Location Tracking**: Real-time GPS tracking for currently running buses, allowing students to view live bus locations.
- **Route and Schedule Management**: Easily view bus routes and schedules.
- **Chatbox**: A messaging system for communication between students and drivers.
- **Issue Reporting System**: Allows users to report issues directly from the app.
- **Emergency Helpline**: Quick access to emergency contact numbers.
- **Location Finder**: Helps users find their own location and nearby buses.
- **Driver Reviews and AI Summaries**: Students can rate drivers, with reviews summarized by Gemini AI.
- **Dual Language Support**: English and Bengali language support.
- **Admin Panel**: Full control panel for managing routes, buses, schedules, and handling driver accounts.

## Getting Started 🚀

### Firebase Setup 🔧

1. **Create a Firebase Project**: Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.

2. **Enable Authentication**: In the Firebase Console, go to **Authentication > Sign-in method** and enable:
   - **Email/Password**
   - **Phone Authentication**
   - **Google Authentication**

3. **Set up Firestore Database**:
   - Go to **Firestore Database** in the Firebase Console.
   - Set up the database in **production mode** for added security.
   - Use the Firestore rules provided below to set up access permissions for different user roles.

4. **Enable Storage**: Configure Firebase Storage to manage and store profile pictures and other documents.

5. **Enable Cloud Messaging**: Set up Firebase Cloud Messaging (FCM) for notifications to students and drivers.

6. **Set Up Google OAuth Client ID**:
   - Follow this [proccess](https://stackoverflow.com/questions/50507877/where-do-i-get-the-web-client-secret-in-firebase-google-login-for-android).
   - Obtain the **WEB_CLIENT_ID** and set it in [data/utils/Constant.kt](https://github.com/Mahmud0808/UniRide/blob/master/app/src/main/java/com/drdisagree/uniride/data/utils/Constant.kt) to enable Google OAuth sign-in.

### API Keys Configuration 🔑

1. Rename `native-lib.cpp.example` to `native-lib.cpp` and open it.
   - Add your **Google Maps API Key** and **Gemini API Key** in the file for location services and AI-based features.

2. Open `AndroidManifest.xml`.
   - Add your **Google Maps API Key** under the value of **com.google.android.geo.API_KEY**.

3. Rename `local.properties.example` to `local.properties` and open it.
   - Add your keystore information for secure app distribution.

4. **Update [data/utils/Constant.kt](https://github.com/Mahmud0808/UniRide/blob/master/app/src/main/java/com/drdisagree/uniride/data/utils/Constant.kt) with relevant constants:**
   - `STUDENT_MAIL_SUFFIX`
   - `PHONE_NUMBER_PREFIX`
   - `EMERGENCY_PHONE_NUMBERS`
   - `DRIVER_PRIVACY_POLICY_URL`
   - `ROAD_TRANSPORT_ACT_URL`

## Firestore Security Rules 🔒

To secure data access and permissions, use the following Firestore security rules:

```firestore
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
  	match /admin_list/{adminId} {
      allow read: if request.auth != null;
			allow write: if false;
    }
    
  	match /driver_list/{driverId} {
      allow read: if request.auth != null;
      allow write: if (request.auth != null &&
                      request.auth.uid == driverId &&
                      isDriverRegisteringOrUpdatingProfile()) ||
                      isUserAdmin();
    }
    
  	match /running_bus_list/{busId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && isDriverAuthorized(request.auth.uid, resource.data);
    }
    
  	match /drive_history_list/{busId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && isDriverAuthorized(request.auth.uid, resource.data);
    }
    
  	match /student_list/{studentId} {
      allow read, write: if request.auth.uid == studentId;
    }
    
  	match /route_list/{adminId} {
      allow read: if request.auth != null;
      allow write: if isUserAdmin();
    }
    
  	match /bus_list/{adminId} {
      allow read: if request.auth != null;
      allow write: if isUserAdmin();
    }
    
  	match /bus_category_list/{adminId} {
      allow read: if request.auth != null;
      allow write: if isUserAdmin();
    }
    
  	match /route_category_list/{adminId} {
      allow read: if request.auth != null;
      allow write: if isUserAdmin();
    }
    
  	match /schedule_list/{adminId} {
      allow read: if request.auth != null;
      allow write: if isUserAdmin();
    }
    
  	match /place_list/{adminId} {
      allow read: if request.auth != null;
      allow write: if isUserAdmin();
    }
    
  	match /announcement_list/{adminId} {
      allow read: if request.auth != null;
      allow write: if isUserAdmin();
    }
    
  	match /issue_list/{issueId} {
      allow read: if isUserAdmin();
      allow write: if isAddingNewIssue() || isUserAdmin();
    }
    
  	match /driver_review_list/{driverId} {
      allow read, write: if request.auth != null;
    }
    
  	match /chat_list/{chatId} {
      allow read, write: if request.auth != null;
    }
    
    function isUserAdmin() {
      return request.auth != null &&
      			 get(/databases/$(database)/documents/admin_list/$(request.auth.uid)).data != null;
    }
    
    function isDriverAuthorized(driverId, busData) {
      return (!exists(/databases/$(database)/documents/running_bus_list/$(request.resource.id)) ||
             busData.driver == null ||
             busData.driver.id == driverId ||
             busData.status == null ||
             busData.status == 'STOPPED') &&
             get(/databases/$(database)/documents/driver_list/$(driverId)).data.accountStatus == 'APPROVED';
    }
		
    function isDriverRegisteringOrUpdatingProfile() {
    return (
        (resource == null || resource.data.accountStatus == null) &&
        request.resource.data.accountStatus == 'PENDING') ||
        (resource != null &&
        resource.data.accountStatus != null &&
        request.resource.data.accountStatus == resource.data.accountStatus);
		}
		
    function isAddingNewIssue() {
      return request.auth != null &&
      			 resource.data.resolved == null &&
             request.resource.data.resolved == false;
    }
  }
}
```

## How to Promote a User to Admin 🎖️

To make a user an admin, follow these steps:

1. Go to the **Authentication** tab in the Firebase Console and locate the user you want to promote. Copy the UID of the user.

2. Navigate to the **Firestore Database** section.

3. Expand the **admin_list** collection in Firestore. Click on Add Document.

4. In the Document ID field, paste the UID of the user. Save the document.

Congratulations! The user is now an admin and has access to admin-specific functionalities in the app.

## Contact 📩

Wanna reach out to me? DM me at

Email: mahmudul15-13791@diu.edu.bd
