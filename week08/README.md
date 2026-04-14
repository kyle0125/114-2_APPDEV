# W08 進階佈局與事件處理 ＋ 課後作業：海洋生物小百科 Mini APP

> **APP 開發課程** ｜ 第 8 週 ｜ 4/16
> **教科書**：Ch04 使用介面設計 ＋ Ch05 使用者互動設計
> **課後作業繳交期限**：W09 期中考前（4/23）

---

## 一、使用介面設計（Ch04）

> **PPT：Android_Ch04.pdf（78 頁，選用重點節次）**

### 4-1 介面元件與佈局元件（P2-5）

Android UI 由兩種元素組成：

| 類型 | 說明 | 範例 |
|------|------|------|
| **View**（介面元件） | 使用者看到、互動的物件 | Button、TextView、EditText、ImageView |
| **ViewGroup**（佈局元件） | 看不見的容器，負責排列 View | LinearLayout、ConstraintLayout |

```
ViewGroup（容器）
├── View（按鈕）
├── View（文字）
└── ViewGroup（巢狀容器）
    ├── View（圖片）
    └── View（文字）
```

#### Android 尺寸單位

| 單位 | 說明 | 用途 |
|------|------|------|
| `dp` | 密度無關像素 | 元件大小、間距（**最常用**） |
| `sp` | 可縮放像素 | 文字大小（**文字專用**） |
| `px` | 實際像素 | 不建議使用（不同螢幕會變形） |

### 4-2 ConstraintLayout 佈局（P6-30）

最靈活的佈局方式，用**約束條件（Constraint）**定位元件。

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="海洋生物小百科"
        android:textSize="28sp"
        android:textStyle="bold"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <ImageView
        android:id="@+id/imgCreature"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_marginTop="16dp"
        android:scaleType="centerCrop"
        app:layout_constraintTop_toBottomOf="@id/tvTitle"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**約束語法規則**：
- `app:layout_constraintTop_toBottomOf="@id/tvTitle"` → 我的上邊 對齊 tvTitle 的下邊
- `app:layout_constraintStart_toStartOf="parent"` → 我的左邊 對齊 父容器的左邊

### 4-3 LinearLayout 佈局（P31-45）

最簡單的佈局方式，元件**依序排列**。

```xml
<!-- 垂直排列 -->
<LinearLayout
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView ... />   <!-- 第 1 個：最上面 -->
    <Button ... />     <!-- 第 2 個：在下面 -->
    <TextView ... />   <!-- 第 3 個：最下面 -->
</LinearLayout>

<!-- 水平排列 -->
<LinearLayout
    android:orientation="horizontal" ...>

    <Button ... />     <!-- 左邊 -->
    <Button ... />     <!-- 中間 -->
    <Button ... />     <!-- 右邊 -->
</LinearLayout>
```

| 佈局 | 特點 | 適合場景 |
|------|------|---------|
| `LinearLayout` | 簡單、元件依序排列 | 簡單表單、按鈕列 |
| `ConstraintLayout` | 靈活、任意定位 | 複雜畫面、精確排版 |

### 4-4 更改介面元件的外觀

```xml
<!-- 文字樣式 -->
android:textSize="20sp"
android:textStyle="bold"
android:textColor="#FF6600"

<!-- 元件大小 -->
android:layout_width="match_parent"   <!-- 填滿父容器 -->
android:layout_width="wrap_content"   <!-- 依內容大小 -->
android:layout_width="200dp"          <!-- 固定大小 -->

<!-- 間距 -->
android:padding="16dp"                <!-- 內距（元件內部） -->
android:layout_margin="8dp"           <!-- 外距（元件之間） -->
android:layout_marginTop="16dp"       <!-- 只設上方外距 -->
```

### 4-6 在實機測試（P70-78）

除了模擬器，也可以用**真實手機**測試：

1. 手機開啟「開發人員選項」（設定→關於手機→連點版本號碼 7 次）
2. 開啟「USB 偵錯」
3. 用 USB 連接電腦
4. Android Studio 上方裝置列表會出現你的手機

---

## 二、使用者互動設計（Ch05）

> **PPT：Android_Ch05.pdf（42 頁，重點 §5-1、§5-2）**

### 5-1 事件處理機制（P2-5）

Android 使用**委託事件處理模型**：

```
事件來源（Button）──觸發事件──→ 傾聽者（Listener）──執行處理──→ 結果
```

#### 5-1-2 Java 介面（Interface）在 Android 的應用

> 還記得 Java OOP 的 Interface 嗎？Android 的事件處理就是用 Interface！

```java
// View.OnClickListener 就是一個 Java Interface
public interface OnClickListener {
    void onClick(View v);
}

// 方法 1：Lambda 表達式（最簡潔，W7 已學）
btn.setOnClickListener(v -> {
    // 處理點擊事件
});

// 方法 2：匿名內部類別（教科書寫法）
btn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // 處理點擊事件
    }
});

// 方法 3：Activity 實作 Interface
public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        // 根據 v.getId() 判斷是哪個按鈕
    }
}
```

### 5-2 按鈕元件的事件處理實習（P6-15）

#### 多按鈕共用事件處理

當畫面有很多按鈕，可以用 `getId()` 判斷：

```java
// Activity 實作 OnClickListener
public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInfo = findViewById(R.id.tvInfo);

        // 多個按鈕共用同一個 Listener（this）
        findViewById(R.id.btnShark).setOnClickListener(this);
        findViewById(R.id.btnTurtle).setOnClickListener(this);
        findViewById(R.id.btnDolphin).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnShark) {
            tvInfo.setText("大白鯊：深海頂級掠食者");
        } else if (id == R.id.btnTurtle) {
            tvInfo.setText("綠蠵龜：珊瑚礁的溫柔居民");
        } else if (id == R.id.btnDolphin) {
            tvInfo.setText("海豚：聰明的海洋社交家");
        }
    }
}
```

> 這就是 **方案 C** — Java 的 Interface 和 Override 在 Android 事件處理中自然學會，不需要額外教。

---

## 三、本週 PPT 清單

| PPT 檔案 | 使用範圍 | 頁數 |
|---------|---------|------|
| Android_Ch04.pdf | §4-1~§4-4、§4-6 選用 | ~50 頁 |
| Android_Ch05.pdf | §5-1~§5-2 | ~15 頁 |

---

## 四、課後作業：海洋生物小百科 Mini APP

> **繳交期限**：W09 期中考前（4/23）
> **繳交方式**：將 Android 專案放在 `week08/` 內，push 到 Fork（同一個 PR）

### 作業說明

運用 W07-W08 學到的所有技能，獨立完成一個**海洋生物小百科** App。
延續 W03-W05 建立的海洋生物（Shark、Turtle、Dolphin 等），把它們「穿上 Android 的衣服」。

**請從以下兩個版本擇一實作：**

---

### 版本 A：單頁版（One Page）

一個畫面完成所有功能。

#### 功能需求

1. 畫面上方顯示標題「海洋生物小百科」
2. 提供 **4 個以上按鈕**，每個代表一種海洋生物
3. 點擊按鈕後，下方區域顯示該生物的：名稱、棲息地、移動方式、覓食方式
4. 搭配圖片（放在 `res/drawable/`）
5. 提供一個 **EditText 搜尋框**：輸入生物名稱並按「搜尋」按鈕，顯示對應資訊

#### 畫面示意

```
┌──────────────────────────┐
│     海洋生物小百科         │
│                          │
│  [鯊魚] [海龜] [海豚] [章魚] │
│                          │
│  [________搜尋________]  │
│  [搜尋]                  │
│                          │
│  ┌──────────────────┐    │
│  │   (生物圖片)       │    │
│  └──────────────────┘    │
│                          │
│  名稱：大白鯊             │
│  棲息地：深海             │
│  移動：高速衝刺獵食        │
│  覓食：撕咬獵物           │
└──────────────────────────┘
```

#### 關鍵程式碼提示

**MainActivity.java**：

```java
public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private ImageView imgCreature;
    private TextView tvName, tvHabitat, tvMove, tvEat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgCreature = findViewById(R.id.imgCreature);
        tvName = findViewById(R.id.tvName);
        tvHabitat = findViewById(R.id.tvHabitat);
        tvMove = findViewById(R.id.tvMove);
        tvEat = findViewById(R.id.tvEat);

        // 多按鈕共用 Listener
        findViewById(R.id.btnShark).setOnClickListener(this);
        findViewById(R.id.btnTurtle).setOnClickListener(this);
        findViewById(R.id.btnDolphin).setOnClickListener(this);
        findViewById(R.id.btnOctopus).setOnClickListener(this);

        // 搜尋功能
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString();
            searchCreature(keyword);
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnShark) {
            showCreature("大白鯊", "深海", "高速衝刺獵食", "撕咬獵物", R.drawable.shark);
        } else if (id == R.id.btnTurtle) {
            showCreature("綠蠵龜", "珊瑚礁", "緩慢划動四肢", "啃食海草", R.drawable.turtle);
        } else if (id == R.id.btnDolphin) {
            showCreature("瓶鼻海豚", "近海", "躍出水面", "合作圍捕魚群", R.drawable.dolphin);
        } else if (id == R.id.btnOctopus) {
            showCreature("章魚", "岩礁", "噴射水流推進", "用觸手捕捉獵物", R.drawable.octopus);
        }
    }

    private void showCreature(String name, String habitat,
                              String move, String eat, int imageRes) {
        tvName.setText("名稱：" + name);
        tvHabitat.setText("棲息地：" + habitat);
        tvMove.setText("移動：" + move);
        tvEat.setText("覓食：" + eat);
        imgCreature.setImageResource(imageRes);
    }

    private void searchCreature(String keyword) {
        // 根據關鍵字搜尋（簡單版：用 if-else 比對）
        if (keyword.contains("鯊")) {
            showCreature("大白鯊", "深海", "高速衝刺獵食", "撕咬獵物", R.drawable.shark);
        } else if (keyword.contains("龜")) {
            showCreature("綠蠵龜", "珊瑚礁", "緩慢划動四肢", "啃食海草", R.drawable.turtle);
        } else {
            tvName.setText("找不到「" + keyword + "」相關的海洋生物");
            tvHabitat.setText("");
            tvMove.setText("");
            tvEat.setText("");
        }
    }
}
```

---

### 版本 B：雙頁版（Two Pages）

主畫面選擇生物 → 用 **Intent** 跳轉到詳細頁面。

#### 功能需求

1. **主畫面（MainActivity）**：
   - 顯示標題和 4 個以上生物按鈕
   - 提供 EditText 搜尋框
   - 點擊按鈕或搜尋 → 用 Intent 跳轉到詳細頁面
2. **詳細畫面（DetailActivity）**：
   - 接收 Intent 資料，顯示生物完整資訊與圖片
   - 使用 `ConstraintLayout` 排版
   - 提供「返回」按鈕

#### 畫面示意

```
主畫面                          詳細畫面
┌──────────────────┐           ┌──────────────────┐
│  海洋生物小百科    │           │  [← 返回]         │
│                  │           │                  │
│  [鯊魚] [海龜]   │  Intent   │  ┌──────────┐    │
│  [海豚] [章魚]   │ ───────> │  │ (生物圖片) │    │
│                  │           │  └──────────┘    │
│  [____搜尋____]  │           │                  │
│  [搜尋]          │           │  大白鯊           │
│                  │           │  ──────────      │
│                  │           │  棲息地：深海      │
│                  │           │  移動：高速衝刺    │
│                  │           │  覓食：撕咬獵物    │
└──────────────────┘           └──────────────────┘
```

#### 關鍵程式碼提示

**MainActivity.java**（用 Intent 傳資料）：

```java
public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnShark).setOnClickListener(this);
        findViewById(R.id.btnTurtle).setOnClickListener(this);
        findViewById(R.id.btnDolphin).setOnClickListener(this);
        findViewById(R.id.btnOctopus).setOnClickListener(this);

        // 搜尋功能
        EditText etSearch = findViewById(R.id.etSearch);
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            String keyword = etSearch.getText().toString();
            searchAndOpen(keyword);
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnShark) {
            openDetail("大白鯊", "深海", "高速衝刺獵食", "撕咬獵物", "shark");
        } else if (id == R.id.btnTurtle) {
            openDetail("綠蠵龜", "珊瑚礁", "緩慢划動四肢", "啃食海草", "turtle");
        } else if (id == R.id.btnDolphin) {
            openDetail("瓶鼻海豚", "近海", "躍出水面", "合作圍捕魚群", "dolphin");
        } else if (id == R.id.btnOctopus) {
            openDetail("章魚", "岩礁", "噴射水流推進", "用觸手捕捉獵物", "octopus");
        }
    }

    private void openDetail(String name, String habitat,
                            String move, String eat, String imageName) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("habitat", habitat);
        intent.putExtra("move", move);
        intent.putExtra("eat", eat);
        intent.putExtra("image", imageName);
        startActivity(intent);
    }

    private void searchAndOpen(String keyword) {
        if (keyword.contains("鯊")) {
            openDetail("大白鯊", "深海", "高速衝刺獵食", "撕咬獵物", "shark");
        } else if (keyword.contains("龜")) {
            openDetail("綠蠵龜", "珊瑚礁", "緩慢划動四肢", "啃食海草", "turtle");
        }
        // 可擴充更多生物...
    }
}
```

**DetailActivity.java**（接收資料 + ConstraintLayout）：

```java
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 取得 Intent 傳來的資料
        String name = getIntent().getStringExtra("name");
        String habitat = getIntent().getStringExtra("habitat");
        String move = getIntent().getStringExtra("move");
        String eat = getIntent().getStringExtra("eat");
        String imageName = getIntent().getStringExtra("image");

        // 顯示資料
        ((TextView) findViewById(R.id.tvName)).setText(name);
        ((TextView) findViewById(R.id.tvHabitat)).setText("棲息地：" + habitat);
        ((TextView) findViewById(R.id.tvMove)).setText("移動：" + move);
        ((TextView) findViewById(R.id.tvEat)).setText("覓食：" + eat);

        // 動態載入圖片
        ImageView img = findViewById(R.id.imgCreature);
        int imageRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
        img.setImageResource(imageRes);

        // 返回按鈕
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
```

**activity_detail.xml**（使用 ConstraintLayout）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <Button
        android:id="@+id/btnBack"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="← 返回"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <ImageView
        android:id="@+id/imgCreature"
        android:layout_width="250dp"
        android:layout_height="250dp"
        android:layout_marginTop="16dp"
        android:scaleType="centerCrop"
        app:layout_constraintTop_toBottomOf="@id/btnBack"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/tvName"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="28sp"
        android:textStyle="bold"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/imgCreature"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/tvHabitat"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:layout_marginTop="12dp"
        app:layout_constraintTop_toBottomOf="@id/tvName"
        app:layout_constraintStart_toStartOf="parent" />

    <TextView
        android:id="@+id/tvMove"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@id/tvHabitat"
        app:layout_constraintStart_toStartOf="parent" />

    <TextView
        android:id="@+id/tvEat"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@id/tvMove"
        app:layout_constraintStart_toStartOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**AndroidManifest.xml**（別忘記註冊 DetailActivity）：

```xml
<!-- 在 <application> 標籤內加入 -->
<activity android:name=".DetailActivity" />
```

---

## 驗收標準（通過／不通過）

| 項目 | 通過條件 |
|------|----------|
| 專案可執行 | Build 成功並正常執行 |
| UI 佈局完整 | 標題、按鈕、圖片、文字資訊、搜尋框皆呈現 |
| 互動功能正確 | 按鈕切換 + 搜尋功能正確運作 |
| 程式碼品質 | 使用 Interface 共用事件處理、命名清晰 |

### 額外挑戰（選做，不影響通過與否）

- 使用 `ScrollView` 讓內容可捲動
- 使用 `CardView` 美化資訊卡片
- 加入 `Toast` 訊息提示（Ch08 預習）
- 新增超過 6 種海洋生物
- 搜尋支援模糊比對（如輸入「海」找到所有含「海」的生物）

---

## 繳交方式

1. 在你的 Fork 中建立 `week08/` 資料夾
2. 將整個 Android 專案放入（至少包含 `app/src/main/` 下的 java 和 res 資料夾）
3. **Push 到你的 Fork**，原有的 PR 會自動包含此次 commit
4. 繳交期限：**W09 上課前（4/23）**

---

## 與 Java OOP 的完整對照

| W03-W05 Java OOP | W07-W08 Android APP |
|-------------------|---------------------|
| `class Creature` 定義屬性 | 生物資訊顯示在 TextView 上 |
| `extends` 繼承 | `extends AppCompatActivity` |
| `@Override` 覆寫方法 | `@Override onClick(View v)` |
| `interface` 介面 | `implements View.OnClickListener` |
| `move()` `eat()` 方法 | 點擊按鈕顯示移動/覓食描述 |
| 多型 `Creature[]` 陣列 | 多按鈕共用 `onClick` + `getId()` 判斷 |
| **Console 文字輸出** | **App 圖形介面輸出** |
