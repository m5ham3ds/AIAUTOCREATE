# 🎬 AI AutoCreate

> تطبيق Android احترافي لإنشاء محتوى إبداعي بالذكاء الاصطناعي.  
> توليد فيديوهات، صور، نصوص، وأصوات باستخدام Gemini و HuggingFace.  
> واجهة عصرية بـ Jetpack Compose، دعم عربي كامل، وأمان متقدم.

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-purple" alt="Language">
  <img src="https://img.shields.io/badge/Min%20SDK-26-orange" alt="Min SDK">
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue" alt="Target SDK">
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License">
  <img src="https://img.shields.io/badge/Architecture-Clean%20Architecture-brightgreen" alt="Architecture">
</p>

---

## ✨ الميزات الرئيسية

| الأيقونة | الميزة | الوصف |
|:---:|---|:---|
| 🧠 | **ذكاء اصطناعي متكامل** | Gemini لتوليد النصوص وتحليل الفيديو، HuggingFace للصور (Stable Diffusion) والصوت (TTS/ASR). |
| 🤖 | **وكلاء أذكياء** | 7 وكلاء متخصصين: تحليل فيديو، توليد سيناريو، توليد صور، توليد صوت، تنسيق، مراجعة، فحص سلامة. |
| 🎬 | **إنشاء فيديو تلقائي** | أدخل نصاً وصفياً واحصل على فيديو كامل باستخدام FFmpegKit مع تتبع حالة المشروع. |
| 🎨 | **واجهة عصرية** | Jetpack Compose + Material Design 3 مع ألوان بنفسجية أنيقة (Dark Indigo). |
| 🌓 | **ثيمات متعددة** | فاتح ☀️، داكن 🌙، تلقائي 📱 مع ألوان ديناميكية (Android 12+). |
| 🌐 | **دعم اللغات** | العربية 🇸🇦 والإنجليزية 🇬🇧 كاملة مع تبديل فوري. |
| 🔒 | **أمان متقدم** | تشفير AES-256 للمفاتيح، Certificate Pinning، تخزين آمن عبر Android KeyStore. |
| 🔔 | **إشعارات تفاعلية** | إشعارات عند اكتمال أو فشل العمليات مع إجراءات سريعة. |
| 💾 | **قاعدة بيانات محلية** | Room Database مع 6 جداول و 20 حالة استخدام. |
| 🧪 | **اختبارات شاملة** | Unit, Integration, UI tests مع CI/CD عبر GitHub Actions. |

---

## 🏗️ بنية المشروع

```text
AIAutoCreate/
├── app/src/main/java/com/aiautocreate/
│   ├── AIAutoCreateApp.kt              # تطبيق Hilt الرئيسي
│   ├── MainActivity.kt                 # النشاط الرئيسي مع شريط جانبي
│   ├── agent/                          # 8 وكلاء أذكياء
│   │   ├── AgentBase.kt               # الفئة الأساسية للوكلاء
│   │   ├── AudioContextAgent.kt        # تحويل النص إلى كلام
│   │   ├── ImageInterpreterAgent.kt    # توليد الصور
│   │   ├── OrchestratorAgent.kt        # تنسيق الوكلاء
│   │   ├── ReviewerAgent.kt           # مراجعة المخرجات
│   │   ├── SanityCheckAgent.kt         # فحص السلامة
│   │   ├── ScriptGeneratorAgent.kt     # توليد السيناريو
│   │   └── VideoAnalyzerAgent.kt       # تحليل الفيديو
│   ├── data/
│   │   ├── datasource/
│   │   │   ├── local/                 # Room Database, DataStore, FileStorage
│   │   │   └── remote/                # GeminiApi, HuggingFaceApi, DTOs
│   │   └── repository/                # 8 مستودعات (AppSettings, Project, MediaFile, ...)
│   ├── di/                            # حقن التبعيات (Hilt Modules)
│   ├── domain/
│   │   ├── model/                     # نماذج البيانات
│   │   ├── pipeline/                  # PipelineOrchestrator, PipelineConfig, PipelineEvent
│   │   ├── repository/                # واجهات المستودعات
│   │   ├── service/                   # FFmpegCommandBuilder, AnalyticsTracker
│   │   └── usecase/                   # 20 حالة استخدام
│   ├── presentation/
│   │   ├── common/
│   │   │   ├── components/            # ~15 مكون واجهة مشترك (AppComponents.kt)
│   │   │   ├── dialog/                # 4 حوارات جاهزة
│   │   │   ├── notification/          # SnackbarManager, NotificationHandler
│   │   │   └── state/                 # UiState, UiEvent, ErrorHandler
│   │   └── ui/
│   │       ├── navigation/            # NavGraph, Route, NavigationManager
│   │       ├── theme/                 # Color, Theme, Type, Shape, Dimensions, Animation
│   │       └── screens/               # 12 شاشة (كل منها Screen + ViewModel + State)
│   ├── sync/                          # SyncManager, ConflictResolver, SyncNotifications
│   ├── util/                          # 15 أداة مساعدة
│   └── worker/                        # 6 عمال خلفية (WorkManager)
```

---

📱 الشاشات الرئيسية

# الشاشة الوصف
1 الرئيسية (Home) إدخال فكرة الفيديو، اختيار الأساليب، بدء التوليد، عرض التقدم والسجلات، معاينة الفيديو الناتج.
2 النتائج والتجارب (Results) لوحة مراقبة حية لتقدم جميع مراحل التوليد (Script، Image، TTS، Video).
3 إعدادات FFmpeg لوحة تحكم احترافية لجودة الفيديو والمونتاج مع بروفايلات لكل نمط (قصص، حماسي، احترافية، مخصص).
4 تنسيق الترجمة (Subtitle) ضبط كامل للترجمة: خط، حجم، لون، شفافية، ظل، موضع مع معاينة حية.
5 سجل النشاطات (ActivityLog) عرض جميع الأحداث مع فلترة (الكل، Info، Warning، Error).
6 معالج الصوت الذكي رفع ملف صوتي ومعالجته: عزل الضوضاء، ترميم الترددات، تحسين الوضوح، تعزيز الصوت.
7 إعدادات النماذج (ModelsSettings) إدارة مفاتيح API (Gemini، HuggingFace، ElevenLabs)، اختيار النماذج، الاستنساخ الصوتي.
8 مدير النماذج (ModelsManager) إضافة وحذف وتفعيل/تعطيل النماذج.
9 الإعدادات (Settings) اللغة (عربي/إنجليزي)، الثيم (داكن/فاتح)، الألوان الديناميكية.
10 الوكيل الذكي (Agent) 3 تبويبات: دردشة مع Gemini، سجل التدخلات، صلاحيات الوكيل.
11 تحسين جودة الفيديو رفع فيديو وتحسينه: تغيير الدقة، FPS، تحسين الألوان، إزالة التشويش.
12 استخراج فيديو مشابه تحليل نمط فيديو مصدر وإنشاء فيديو جديد مشابه.

---

⚙️ البنية التحتية (Core Pipeline)

PipelineOrchestrator

منسق مركزي (بديل PipelineManager.java) يدير جميع مراحل توليد الفيديو:

· استدعاء Gemini لتوليد السيناريو.
· حفظ السكريبت واستخراج ملفات MSHHD و HAREKA و SSML.
· معالجة الصور (HuggingFace) مع نظام محاولات متعددة و fallback.
· معالجة الصوت (TTS) مع دعم استنساخ الصوت.
· معالجة الفيديو (Img2Vid) مع دعم نماذج متعددة.
· تجميع نهائي باستخدام FFmpegKit.
· إصدار أحداث التقدم (PipelineEvent) عبر SharedFlow.

FFmpegCommandBuilder

مولد أوامر FFmpeg ذكي يحول MontagePlan إلى أمر FFmpeg احترافي مع دعم:

· إدخال متعدد (صور، فيديو، صوت).
· تحويل الصور إلى فيديو بنفس الأبعاد.
· انتقالات بين المشاهد (xfade: fade, cut, zoom, wipe).
· تراكبات (نصوص، علامات مائية).
· معالجة الصوت (دمج، ضبط مستوى، تلاشي).
· إعدادات التصدير (دقة، FPS، جودة).

AppSettingsRepository

مصدر وحيد لجميع إعدادات التطبيق (أكثر من 50 مفتاح إعداد) مع دوال مساعدة للقراءة والكتابة وإدارة قوائم CSV. يدعم:

· مفاتيح API (Gemini، HuggingFace، ElevenLabs).
· النماذج المختارة (صور، فيديو، صوت).
· قوائم النماذج (CSV).
· الأساليب (صور، أغلفة، فيديو، مونتاج).
· اختيارات المستخدم.
· إعدادات الجودة والمونتاج (بروفايلات لكل نمط).
· إعدادات الاستنساخ الصوتي.

---

🎨 نظام المكونات المشتركة (AppComponents.kt)

جميع الشاشات تستخدم مكونات موحدة لضمان تناسق الواجهات:

· AppTopBar – الشريط العلوي مع الشعار وزر القائمة.
· AppCard – بطاقة موحدة بتصميم Dark Indigo.
· AppTextField – حقل نصي بتصميم موحد.
· AppDropdown – قائمة منسدلة بتصميم موحد.
· AppButton – زر رئيسي بتدرج بنفسجي.
· AppToggleCard – بطاقة تبديل مع مفتاح Switch.
· StatusCapsule – كبسولة عرض الحالة.
· AppProgressSection – شريط تقدم مع نسبة مئوية.
· AppBottomBar – شريط تنقل سفلي.

بالإضافة إلى مكونات مساعدة:

· EmptyState – عرض حالة فارغة.
· ErrorState – عرض حالة خطأ مع زر إعادة المحاولة.
· AppLoading – مؤشر تحميل.
· AppProgressBar – شريط تقدم مع رسالة.

---

🔔 نظام الإشعارات

· NotificationHandler: إشعارات النظام (Status Bar) مع 3 قنوات مخصصة (توليد فيديو، مزامنة، نظام).
· SnackbarManager: إشعارات داخل التطبيق (Snackbar) مع دعم 3 أنواع (نجاح، خطأ، معلومات).
· InAppNotification: نموذج بياني موحّد للإشعارات داخل التطبيق.

---

🤖 الوكلاء الأذكياء (Agents)

الوكيل الوصف
VideoAnalyzerAgent تحليل محتوى الفيديو باستخدام Gemini لاستخراج وصف هيكلي.
ScriptGeneratorAgent توليد سيناريو مخصص لفيديو بناءً على وصف أو فكرة.
ImageInterpreterAgent توليد صورة فنية من وصف نصي باستخدام Stable Diffusion.
AudioContextAgent توليد كلام (TTS) من نص مع مراعاة السياق اللغوي.
OrchestratorAgent تنسيق الوكلاء في سيرورة كاملة (تحليل ← توليد ← تركيب).
ReviewerAgent مراجعة مخرجات الوكلاء وإعادة درجات جودة.
SanityCheckAgent فحص جاهزية التطبيق (مفاتيح، تخزين، اتصال).

---

🔧 العمال الخلفية (Workers)

العامل الوصف
AnalyticsWorker إرسال الأحداث المجمعة إلى Firebase Analytics.
AudioGenerationWorker توليد الصوت (TTS) في الخلفية.
FFmpegExecutionWorker تنفيذ أوامر FFmpeg مخصصة (قص، دمج، ضغط).
ImageGenerationWorker توليد الصور في الخلفية.
VideoCreationWorker إنشاء الفيديو النهائي مع إشعارات التقدم.
SyncWorker مزامنة البيانات مع الخادم.

---

🔄 نظام المزامنة (Sync)

· SyncManager: مدير مزامنة مركزي ينسق بين المستودع ومحلل التعارضات.
· ConflictResolver: حل تعارضات المزامنة باستخدام استراتيجية "آخر تعديل ينتصر".
· SyncStrategyManager: مدير استراتيجية المزامنة (يدوية، تلقائية، WiFi فقط).
· SyncNotifications: إشعارات متعلقة بالمزامنة.

---

🧰 الأدوات المساعدة (Utils)

الأداة الوصف
CompressionUtils ضغط الصور.
Constants ثوابت التطبيق.
CryptoUtils تشفير SHA-256 و MD5.
DateTimeUtils تنسيق الوقت والتاريخ (java.time).
Extensions دوال امتداد (Flow, Uri).
FFmpegRunner مشغل أوامر FFmpeg مبسط.
FileUtils أدوات الملفات (نسخ، إنشاء، امتدادات).
JsonLogger تسجيل JSON منسق.
LocaleHelper تغيير لغة التطبيق.
MediaUtils استخراج معلومات الوسائط.
NetworkUtils فحص الاتصال بالإنترنت.
NotificationUtils فحص حالة الإشعارات.
StoragePaths مسارات التخزين.
StringUtils معالجة النصوص.
ValidationUtils التحقق من صحة البيانات.

---

🚀 البداية السريعة

المتطلبات الأساسية

الأداة الإصدار المطلوب
☕ JDK 17
🛠️ Android Studio Ladybug (2024.2) أو أحدث
📦 Gradle 8.9
📱 Android SDK API 35

التثبيت والتشغيل

```bash
# 1. استنساخ المستودع
git clone https://github.com/username/AIAutoCreate.git

# 2. فتح المشروع في Android Studio
# File → Open → اختر مجلد AIAutoCreate

# 3. مزامنة Gradle
./gradlew sync

# 4. بناء التطبيق
./gradlew assembleDebug

# 5. تثبيت التطبيق على جهاز متصل
adb install app/build/outputs/apk/debug/app-debug.apk
```

إعداد مفاتيح API

1. افتح التطبيق وانتقل إلى الإعدادات → إعدادات النماذج.
2. أدخل مفاتيح API الخاصة بك:
   · Gemini API Key: من Google AI Studio.
   · HuggingFace Token: من HuggingFace Settings.
3. احفظ الإعدادات.

---

📖 التوثيق

· سجل التغييرات (CHANGELOG.md)
· دليل المساهمة (CONTRIBUTING.md)
· الترخيص (LICENSE)

---

📄 الترخيص

هذا المشروع مرخص تحت MIT License.

```