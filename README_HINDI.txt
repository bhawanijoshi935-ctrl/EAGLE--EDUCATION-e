EAGLE EDUCATION Android Studio Project

इस ZIP में Android WebView ऐप का पूरा source project है।
अभी इस वातावरण में Android SDK / Gradle उपलब्ध नहीं है, इसलिए compiled APK बनाना संभव नहीं हुआ।

APK बनाने के लिए:
1. कंप्यूटर में Android Studio इंस्टॉल करें।
2. इस ZIP को Extract करें।
3. Android Studio में 'Open' चुनकर EagleEducation फ़ोल्डर खोलें।
4. Gradle Sync पूरा होने दें और Android SDK Platform 35 इंस्टॉल करें।
5. Build > Build Bundle(s) / APK(s) > Build APK(s) चुनें।
6. APK app/build/outputs/apk/debug/app-debug.apk में बनेगा।

यह ऐप अभी फोटो के अनुरूप तैयार किए गए वेब-ऐप डेमो को Android WebView में चलाता है।
Live class, cloud login, admin uploads और online student data के लिए अलग सुरक्षित backend चाहिए।


नई सुविधा:
- Home और bottom navigation में Owner Panel जोड़ा गया है।
- Owner Name और Video Notes दर्ज किए जा सकते हैं।
- Video file चुनी और local IndexedDB में save की जा सकती है।
- Saved videos Owner Panel में preview/play किए जा सकते हैं।
- यह अभी local-device feature है; सभी विद्यार्थियों के लिए online upload हेतु Firebase/secure backend आवश्यक है।
