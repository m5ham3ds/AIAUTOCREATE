# 📋 سجل التغييرات – AI AutoCreate

جميع التغييرات الملحوظة في هذا المشروع موثقة هنا.  
الصيغة مستوحاة من [Keep a Changelog](https://keepachangelog.com/ar/1.0.0/)،  
ويتبع المشروع [الإصدار الدلالي](https://semver.org/lang/ar/).

---

## [1.0.0] – 2026-05-05

### 🎉 الإصدار الأول – البنية الكاملة

#### 🚀 الميزات المضافة

**🧠 الذكاء الاصطناعي والإبداع**
- تكامل مع **Gemini API** لتوليد النصوص وتحليل الفيديو واقتراح السيناريوهات.
- تكامل مع **HuggingFace Inference API** لتوليد الصور (Stable Diffusion) وتحويل النص إلى كلام (TTS) وتحويل الكلام إلى نص (Whisper).
- نظام **الوكلاء الأذكياء (Agents)**:
  - `VideoAnalyzerAgent` – تحليل محتوى الفيديو واستخراج وصف هيكلي.
  - `ScriptGeneratorAgent` – توليد سيناريو إبداعي من موضوع أو فكرة.
  - `ImageInterpreterAgent` – توليد صور من نصوص وصفية.
  - `AudioContextAgent` – تحويل النصوص إلى كلام مع مراعاة السياق اللغوي.
  - `OrchestratorAgent` – تنسيق الوكلاء في سيرورة كاملة (تحليل → توليد → تركيب).
  - `ReviewerAgent` – تقييم جودة المخرجات.
  - `SanityCheckAgent` – فحص جاهزية النظام (مفاتيح، تخزين، اتصال).
- **استخراج فيديو مشابه**: تحليل فيديو مصدر وإنشاء سيناريو جديد لا ينتهك حقوق الملكية.
- **إعادة بناء الصوت**: تحويل مقطع صوتي إلى نص (ASR) ثم إعادة توليده بصوت جديد (TTS).
- **ملعب العمليات**: واجهة موحدة لاختبار Gemini و Stable Diffusion مباشرة.

**🎬 إدارة الفيديو والمشاريع**
- إنشاء مشاريع فيديو بعنوان ووصف وسيناريو.
- محاكاة توليد الفيديو باستخدام FFmpegKit مع تحديث مباشر لحالة المشروع (draft → generating → completed/failed).
- شاشة **FFmpeg** متكاملة لعرض المشاريع وتشغيل التوليد.
- تخزين الفيديوهات الناتجة في مجلد المشروع مع إمكانية الحفظ للمعرض عبر `MediaStore`.

**🎨 التصميم وتجربة المستخدم**
- **واجهة مستخدم حديثة** مبنية بالكامل بـ Jetpack Compose و Material Design 3.
- **نظام تصميم موحد (Dark Indigo)**: ألوان بنفسجية أساسية (#6C64FF)، خطوط وأبعاد متناسقة، مكونات قابلة لإعادة الاستخدام.
- **نظام مكونات مشتركة (AppComponents.kt)**: يحتوي على جميع المكونات الأساسية (`AppTopBar`, `AppCard`, `AppTextField`, `AppDropdown`, `AppButton`, `AppToggleCard`, `StatusCapsule`, `AppProgressSection`, `AppBottomBar`) لضمان تناسق الواجهات عبر جميع الشاشات.
- **12 شاشة كاملة** تم تحويلها من XML إلى Jetpack Compose مع ربطها بـ ViewModels الخاصة بها وإزالة جميع البيانات الوهمية (Hardcoded):
  1. `HomeScreen` – الشاشة الرئيسية لتوليد الفيديو مع Dropdowns تفاعلية، سجلات، معاينة فيديو، وشريط تقدم.
  2. `ResultsScreen` – لوحة مراقبة حية تستمع لأحداث `PipelineOrchestrator` وتعرض تقدم كل عملية (Script، Image، TTS، Video).
  3. `FfmpegScreen` – لوحة تحكم احترافية لإعدادات FFmpeg مع بروفايلات لكل نمط مونتاج (قصص وروايات، حماسي، احترافية، مخصص).
  4. `SubtitleStyleScreen` – ضبط كامل لتنسيق الترجمة (خط، حجم، لون، شفافية، ظل، موضع) مع معاينة حية.
  5. `ActivityLogScreen` – سجل النشاطات مع فلترة (الكل، Info، Warning، Error) وبطاقات قابلة للتوسيع.
  6. `AudioReconstructorScreen` – معالج الصوت الذكي مع رفع ملفات صوتية واختيار نوع المعالجة (عزل الضوضاء، ترميم الترددات، تحسين الوضوح، تعزيز الصوت).
  7. `ModelsSettingsScreen` – لوحة تحكم كاملة لإدارة مفاتيح API (Gemini، HuggingFace، ElevenLabs)، اختيار النماذج (صور، فيديو، صوت)، والاستنساخ الصوتي.
  8. `ModelsManagerScreen` – مدير النماذج مع إمكانية إضافة وحذف وتفعيل/تعطيل النماذج.
  9. `SettingsScreen` – إعدادات اللغة (العربية/الإنجليزية)، الثيم (داكن/فاتح)، والألوان الديناميكية.
  10. `AgentScreen` – الوكيل الذكي مع 3 تبويبات: دردشة (via Gemini API)، سجل التدخلات، الصلاحيات.
  11. `VideoReimaginerScreen` – تحسين جودة الفيديو مع رفع ملفات، إعدادات الدقة و FPS، وتحسينات إضافية.
  12. `SimilarVideoScreen` – استخراج فيديو مشابه مع تحليل النمط البصري وإعادة الإنشاء.
- **دعم ثيمات متعددة**: فاتح ☀️، داكن 🌙، تلقائي (حسب النظام) مع **ألوان ديناميكية** (Android 12+).
- **دعم لغتين كاملتين**: العربية 🇸🇦 والإنجليزية 🇬🇧، مع تبديل فوري وحفظ الإعداد.
- شريط جانبي احترافي مع أيقونات لجميع الشاشات.
- شاشة **إعدادات** كاملة لتغيير اللغة، الثيم، الألوان الديناميكية.
- مكونات واجهة موحدة: `AppButton`, `AppCard`, `AppTextField`, `AppDropdown`, `EmptyState`, `ErrorState`، حوارات جاهزة (`AppDialog`, `ConfirmDialog`, `ErrorDialog`, `LoadingDialog`).
- نظام إشعارات داخل التطبيق (`SnackbarManager`, `InAppNotification`) وإشعارات النظام (`NotificationHandler`).
- حالات UI موحدة (`UiState<T>`, `UiEvent`, `ErrorHandler`).

**⚙️ البنية التحتية (Core Pipeline)**
- **`AppSettingsRepository`**: مصدر وحيد لجميع إعدادات التطبيق (مفاتيح API، النماذج المختارة، قوائم النماذج CSV، الأساليب، اختيارات المستخدم، إعدادات الجودة والمونتاج) باستخدام DataStore. يدعم أكثر من 50 مفتاح إعداد مع دوال مساعدة للقراءة والكتابة وإدارة قوائم CSV.
- **`PipelineOrchestrator`**: منسق مركزي (بديل `PipelineManager.java`) يدير جميع مراحل توليد الفيديو:
  - استدعاء Gemini لتوليد السيناريو.
  - حفظ السكريبت واستخراج ملفات MSHHD و HAREKA و SSML.
  - معالجة الصور (HuggingFace) مع نظام محاولات متعددة و fallback.
  - معالجة الصوت (TTS) مع دعم استنساخ الصوت.
  - معالجة الفيديو (Img2Vid) مع دعم نماذج متعددة.
  - تجميع نهائي باستخدام FFmpegKit.
  - إصدار أحداث التقدم (`PipelineEvent`) عبر `SharedFlow`.
- **`FFmpegCommandBuilder`**: مولد أوامر FFmpeg ذكي يحول `MontagePlan` إلى أمر FFmpeg احترافي مع دعم:
  - إدخال متعدد (صور، فيديو، صوت).
  - تحويل الصور إلى فيديو بنفس الأبعاد.
  - انتقالات بين المشاهد (xfade: fade, cut, zoom, wipe).
  - تراكبات (نصوص، علامات مائية).
  - معالجة الصوت (دمج، ضبط مستوى، تلاشي).
  - إعدادات التصدير (دقة، FPS، جودة).
- **`MontagePlan`**: نموذج بيانات يمثل خطة المونتاج الكاملة (مدخلات، انتقالات، تراكبات، مسارات صوتية، إعدادات الإخراج).
- **`FfmpegPreset`**: نموذج بيانات للإعدادات المسبقة لعمليات FFmpeg (محذوف حالياً لصالح `MontagePlan` الديناميكي).

**🔄 إدارة الحالة (State Management)**
- جميع الشاشات تستخدم نمط **MVVM** مع `StateFlow` و `collectAsStateWithLifecycle`.
- **`AppSettingsRepository`** كمصدر وحيد للحقيقة (Single Source of Truth) للإعدادات.
- **`PipelineOrchestrator`** يستخدم `SharedFlow` لإصدار أحداث التقدم لجميع المشتركين.
- جميع الـ ViewModels تستخدم `viewModelScope` للعمليات غير المتزامنة.
- لا توجد بيانات وهمية (Hardcoded) في أي شاشة.

**📡 الشبكات والأمان**
- **تشفير AES-256** لمفاتيح API باستخدام Android KeyStore و EncryptedSharedPreferences.
- **ApiKeyInterceptor**: حقن تلقائي لمفاتيح Gemini و HuggingFace في الطلبات.
- **Certificate Pinning** لمنع هجمات الوسيط (MITM).
- **RetryInterceptor**: إعادة محاولة ذكية مع تأخير أسي.
- **ErrorHandlingInterceptor**: تسجيل مركزي لأخطاء HTTP.

**🔔 الإشعارات والخلفية**
- قنوات إشعارات مخصصة (توليد فيديو، مزامنة، نظام).
- إشعارات تفاعلية عند اكتمال أو فشل توليد الفيديو.
- نظام `SnackbarManager` للإشعارات داخل التطبيق.
- **WorkManager**: 6 عمال خلفية (`AnalyticsWorker`, `AudioGenerationWorker`, `FFmpegExecutionWorker`, `ImageGenerationWorker`, `SyncWorker`, `VideoCreationWorker`).

**💾 البيانات والتخزين**
- **Room Database**: 6 جداول (مشاريع، نماذج، وسائط، إعدادات ترجمة، سجل نشاطات، سجل مزامنة).
- **DataStore**: تفضيلات المستخدم (لغة، ثيم، ألوان ديناميكية) + جميع إعدادات التطبيق الأخرى.
- مستودعات (Repositories) تطبق نمط Clean Architecture.
- **20 حالة استخدام (Use Cases)** عبر 7 فئات: agent, media, model, script, subtitle, system, video.

**🔄 المزامنة (Sync)**
- **`SyncManager`**: مدير مزامنة مركزي ينسق بين `SyncRepository` و `ConflictResolver`.
- **`ConflictResolver`**: حل تعارضات المزامنة باستخدام استراتيجية "آخر تعديل ينتصر" (Last Write Wins).
- **`SyncStrategyManager`**: مدير استراتيجية المزامنة (يدوية، تلقائية، WiFi فقط).
- **`SyncNotifications`**: إشعارات متعلقة بالمزامنة.
- **`SyncWorker`**: عامل خلفي للمزامنة الدورية.

**🧰 الأدوات المساعدة (Utils – 15 ملفاً)**
- `CompressionUtils` – ضغط الصور.
- `Constants` – ثوابت التطبيق.
- `CryptoUtils` – تشفير SHA-256 و MD5.
- `DateTimeUtils` – تنسيق الوقت والتاريخ (باستخدام java.time).
- `Extensions` – دوال امتداد (Flow, Uri).
- `FFmpegRunner` – مشغل أوامر FFmpeg مبسط.
- `FileUtils` – أدوات الملفات (نسخ، إنشاء، امتدادات).
- `JsonLogger` – تسجيل JSON منسق.
- `LocaleHelper` – تغيير لغة التطبيق.
- `MediaUtils` – استخراج معلومات الوسائط.
- `NetworkUtils` – فحص الاتصال بالإنترنت.
- `NotificationUtils` – فحص حالة الإشعارات.
- `StoragePaths` – مسارات التخزين.
- `StringUtils` – معالجة النصوص.
- `ValidationUtils` – التحقق من صحة البيانات.

**🧪 الاختبارات والجودة**
- اختبارات وحدة مع **JUnit + MockK + Turbine**.
- اختبارات تكامل مع Room في الذاكرة.
- اختبارات واجهة Compose.
- **Detekt** و **Ktlint** لتحليل الكود الساكن.
- تغطية كود مستهدفة > 70%.

**⚙️ CI/CD**
- GitHub Actions: بناء تلقائي، اختبارات، فحص جودة لكل push و pull request.

**📄 ملفات المشروع**
- `README.md` شامل مع تعليمات تثبيت وتشغيل.
- `CHANGELOG.md` (هذا الملف).
- `CONTRIBUTING.md` لتنظيم المساهمة.
- `LICENSE` (MIT).
- سياسة خصوصية ثنائية اللغة.

---

### 📝 أنواع التغييرات

| الأيقونة | النوع |
|:---:|:---|
| 🚀 | ميزة جديدة |
| 🐛 | إصلاح خطأ |
| ⚡ | تحسين أداء |
| 🎨 | تغيير في التصميم أو التنسيق |
| 📝 | تغيير في التوثيق |
| 🔧 | تغيير في الإعدادات أو الأدوات |
| 🗑️ | إزالة ميزة أو كود |
| 🔒 | إصلاح أمني |