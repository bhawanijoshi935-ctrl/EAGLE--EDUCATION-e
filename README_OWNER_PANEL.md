# EAGLE EDUCATION — Owner Panel

इस version में Owner Panel जोड़ा गया है।

## Demo Owner Login
- Owner ID: `owner`
- Password: `eagle123`

## Features
- Owner Login / Logout
- Video upload
- PDF/TXT Notes upload
- Title और description/notes
- Uploaded content list
- Delete content
- Student panel में uploaded video/notes दिखाना
- Android WebView file picker support

## Important
यह version local/demo storage (IndexedDB) इस्तेमाल करता है। इसका मतलब content उसी device/app के local storage में रहता है। सभी विद्यार्थियों के अलग-अलग phones पर content दिखाने के लिए Firebase या किसी secure backend/storage की आवश्यकता होगी।

Production में Owner password को APK/HTML में hard-code न रखें। Authentication backend पर रखें।
