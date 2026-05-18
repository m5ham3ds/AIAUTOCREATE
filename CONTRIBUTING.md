# 🤝 دليل المساهمة – AI AutoCreate

شكراً جزيلاً لاهتمامك بالمساهمة في هذا المشروع! 🎉  
هذا الدليل يوضح كيفية المساهمة بطريقة منظّمة وفعّالة.

---

## 📜 قواعد السلوك

يرجى التعامل مع الجميع باحترام ولطف.  
نحن نتبع [Contributor Covenant](https://www.contributor-covenant.org/).

---

## 🏗️ هيكل المشروع

لفهم المشروع بشكل أفضل، إليك الهيكل العام:

```text
AIAutoCreate/
├── app/src/main/java/com/aiautocreate/
│   ├── agent/                          # 8 وكلاء أذكياء (Agents)
│   ├── data/
│   │   ├── datasource/local/           # Room, DataStore, FileStorage
│   │   ├── datasource/remote/          # APIs (Gemini, HuggingFace)
│   │   └── repository/                 # مستودعات البيانات
│   ├── di/                             # حقن التبعيات (Hilt)
│   ├── domain/
│   │   ├── model/                      # نماذج المجال
│   │   ├── pipeline/                   # PipelineOrchestrator
│   │   ├── service/                    # FFmpegCommandBuilder
│   │   └── usecase/                    # حالات الاستخدام
│   ├── presentation/
│   │   ├── common/                     # مكونات مشتركة، حوارات، إشعارات
│   │   └── ui/screens/                 # 12 شاشة
│   ├── sync/                           # مزامنة
│   ├── util/                           # أدوات مساعدة (15 أداة)
│   └── worker/                         # عمال الخلفية (6 عمال)
```

الطبقات الرئيسية

الطبقة الوصف
agent/ وكلاء أذكياء يستخدمون Gemini و HuggingFace لتنفيذ مهام محددة
data/datasource/ مصادر البيانات المحلية والبعيدة
data/repository/ مستودعات تطبق Clean Architecture
di/ وحدات Hilt لحقن التبعيات
domain/pipeline/ منسق العمليات المركزي (PipelineOrchestrator)
domain/service/ خدمات المجال (FFmpegCommandBuilder)
presentation/ui/screens/ 12 شاشة Compose مع ViewModels
sync/ نظام المزامنة مع ConflictResolver
util/ 15 أداة مساعدة
worker/ 6 عمال خلفية (WorkManager)

---

🚀 كيف تساهم؟

1️⃣ 🐛 الإبلاغ عن خطأ

إذا وجدت خطأ، افتح Issue جديد يحتوي على:

المعلومة الوصف
📱 الجهاز نوع الهاتف وإصدار Android
📦 إصدار التطبيق رقم الإصدار (مثلاً 1.0.0)
📝 وصف المشكلة شرح واضح لما حدث
🔁 خطوات إعادة الإنتاج كيف يمكننا تكرار المشكلة
📸 لقطة شاشة (اختياري) صورة توضيحية

---

2️⃣ 💡 اقتراح ميزة جديدة

· افتح Issue بعنوان [اقتراح] وصف الميزة.
· صف الميزة بالتفصيل: كيف تعمل؟ لماذا مفيدة؟
· ناقش الفكرة مع المشرفين قبل البدء في تنفيذها.

---

3️⃣ 🔧 المساهمة بالكود (Pull Request)

🛠️ إعداد بيئة التطوير

```bash
# 1. قم بعمل Fork للمستودع من GitHub

# 2. استنساخ المستودع إلى جهازك
git clone https://github.com/your-username/AIAutoCreate.git
cd AIAutoCreate

# 3. أضف المستودع الأصلي كـ upstream
git remote add upstream https://github.com/original-username/AIAutoCreate.git

# 4. افتح المشروع في Android Studio
# تأكد من تثبيت JDK 17 و Gradle 8.9
```

✅ قواعد الكود

· نستخدم Kotlin كلغة برمجة أساسية.
· نتبع نمط Clean Architecture مع MVVM للشاشات.
· نستخدم Jetpack Compose لبناء الواجهات.
· نستخدم Hilt لحقن التبعيات.
· نستخدم DataStore لتخزين التفضيلات.
· جميع المكونات المشتركة في presentation/common/components/.
· لا نستخدم بيانات وهمية (Hardcoded) في ViewModels – دائماً اربطها بـ Repository حقيقي.
· تأكد من تشغيل ./gradlew detekt قبل رفع الكود.

📝 تسمية الفروع

· feature/اسم-الميزة – للميزات الجديدة.
· bugfix/وصف-المشكلة – لإصلاح الأخطاء.
· docs/اسم-التوثيق – لتحديث التوثيق.

---

📞 التواصل

· افتح Issue على GitHub للأسئلة والمناقشات.

```