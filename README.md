# Android Technical Test
Implementasi technical test Android menggunakan **Kotlin** dengan pendekatan **MVVM**, **StateFlow**, **Coroutines**, **Retrofit**, dan **RecyclerView**.

## Tech Stack
* Kotlin
* Android SDK
* MVVM Architecture
* ViewBinding
* Kotlin Coroutines
* StateFlow
* Retrofit
* Gson Converter
* RecyclerView

---

## Project Structure
```text
app/src/main/java/com/example/usertechnicaltest/
├── data/
│   ├── model/
│   │   ├── User.kt
│   │   └── UserResponse.kt
│   │
│   ├── remote/
│   │   ├── UserApi.kt
│   │   └── RetrofitClient.kt
│   │
│   └── repository/
│       └── UserRepository.kt
│
├── ui/
│   └── user/
│       ├── UserActivity.kt
│       ├── UserAdapter.kt
│       ├── UserUiState.kt
│       ├── UserViewModel.kt
│       └── UserViewModelFactory.kt
│
└── utils/
    └── UserUtils.kt
```

---

# 1. Active Adult Users
### Requirement
> Buat function untuk mendapatkan nama user yang aktif dan berusia minimal 18 tahun, kemudian urutkan berdasarkan nama.
### Implementation
```kotlin
fun getActiveAdultUserNames(
    users: List<User>
): List<String> {
    return users
        .filter { it.isActive && it.age >= 18 }
        .sortedBy { it.name }
        .map { it.name }
}
```

### Explanation
Function melakukan tiga proses:
1. `filter` untuk mengambil user yang:

   * `isActive == true`
   * `age >= 18`
2. `sortedBy` untuk mengurutkan berdasarkan nama.
3. `map` untuk mengambil nama user saja.

### Example
Input:
```kotlin
val users = listOf(
    User(1, "Andi", 25, true),
    User(2, "Budi", 17, true),
    User(3, "Citra", 30, false),
    User(4, "Deni", 22, true)
)
```

Output:

```text
Andi
Deni
```

### Presentation
Hasil function telah diintegrasikan ke presentation layer dan ditampilkan pada UI dengan label:
```text
Active Users Age ≥ 18

Andi
Deni
```

---

# 2. Coroutine + StateFlow
### Requirement
Diberikan function:
```kotlin
suspend fun getUsers(): List<User>
```

Buat ViewModel yang menggunakan Coroutine untuk mengambil data dan menangani state:
* Loading
* Success
* Error

### UI State
```kotlin
sealed interface UserUiState {
    data object Loading : UserUiState

    data class Success(
        val users: List<User>
    ) : UserUiState

    data class Error(
        val message: String
    ) : UserUiState
}
```

### ViewModel
```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<UserUiState>(
            UserUiState.Loading
        )

    val uiState: StateFlow<UserUiState> =
        _uiState.asStateFlow()

    fun getUsers() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading

            try {
                val users = repository.getUsers()

                _uiState.value =
                    UserUiState.Success(users)

            } catch (e: Exception) {
                _uiState.value =
                    UserUiState.Error(
                        e.message ?: "Terjadi kesalahan"
                    )
            }
        }
    }
}
```

### Explanation
`viewModelScope` digunakan untuk menjalankan coroutine yang lifecycle-nya mengikuti ViewModel.
`StateFlow` digunakan untuk menyimpan dan mengirimkan perubahan state dari ViewModel ke UI.
UI kemudian mengamati state:

```text
Loading
   ↓
Repository
   ↓
Success / Error
   ↓
UI
```

---

# 3. GET User by ID with Retrofit
### Requirement
Membuat API:

```text
GET /users/{id}
```

dengan response:
```json
{
    "id": 1,
    "name": "Andi",
    "email": "andi@example.com"
}
```

### Response Model
Karena response API memiliki struktur yang berbeda dari model `User`, digunakan model terpisah:
```kotlin
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)
```

### Retrofit Interface
```kotlin
interface UserApi {

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): UserResponse
}
```

### Retrofit Client

```kotlin
object RetrofitClient {

    private const val BASE_URL =
        "https://example.com/api/"

    val userApi: UserApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(UserApi::class.java)
    }
}
```

### Repository
```kotlin
class UserRepository(
    private val api: UserApi
) {

    suspend fun getUserById(
        id: Int
    ): UserResponse {
        return api.getUserById(id)
    }
}
```

### API Status
Endpoint API sebenarnya belum diberikan pada technical test.
Oleh karena itu:
* Retrofit interface sudah dibuat.
* Response model sudah dibuat.
* Repository function sudah dibuat.
* URL API sebenarnya belum dapat dikonfigurasi.
* Pengujian end-to-end terhadap server asli belum dilakukan.
Apabila diperlukan pengujian tanpa backend, endpoint dapat disimulasikan menggunakan **MockWebServer** dengan response JSON yang sesuai dengan requirement.

---

# 4. Singleton Activity Reference
### Problem
Contoh kode:
```kotlin
object UserManager {

    var activity: Activity? = null

    fun setActivity(activity: Activity) {
        this.activity = activity
    }
}
```

`UserManager` merupakan singleton sehingga memiliki lifecycle yang panjang, sedangkan `Activity` memiliki lifecycle yang lebih pendek.
Ketika `Activity` dihancurkan tetapi masih direferensikan oleh `UserManager`, object tersebut berpotensi tidak dapat di-garbage-collect.
Hal ini dapat menyebabkan **memory leak**.

### Risk
```text
UserManager
    │
    └── Activity
          │
          └── Context / View
```
Singleton tetap hidup dan mempertahankan reference terhadap Activity.

### Recommended Solution
Jangan menyimpan `Activity` pada singleton.
Jika hanya membutuhkan data user:
```kotlin
object UserManager {

    var userId: Int? = null
    var userName: String? = null
}
```
Jika membutuhkan `Context` untuk kebutuhan application-level, gunakan **Application Context**, bukan `Activity Context`.

---

# 5. Search User
### Requirement
Buat function:
```kotlin
fun searchUsers(
    users: List<User>,
    keyword: String
): List<User>
```

Dengan ketentuan:
* Search berdasarkan nama.
* Case-insensitive.
* Keyword kosong mengembalikan seluruh user.
* Hasil diurutkan berdasarkan nama.

### Implementation
```kotlin
fun searchUsers(
    users: List<User>,
    keyword: String
): List<User> {
    return users
        .filter {
            keyword.isBlank() ||
                it.name.contains(
                    keyword,
                    ignoreCase = true
                )
        }
        .sortedBy { it.name }
}
```

### Example
Keyword:
```text
"an"
```

Function akan mencari nama yang mengandung `an` tanpa memperhatikan uppercase/lowercase.
Search dilakukan secara lokal terhadap data yang sudah diterima sehingga tidak melakukan request API pada setiap perubahan keyword.

### Presentation
Search telah diintegrasikan dengan `SearchView`.

Alur:
```text
User mengetik keyword
        ↓
SearchView
        ↓
searchUsers()
        ↓
Filtered List
        ↓
RecyclerView
```

---

# Architecture
Project menggunakan pendekatan **MVVM**:
```text
┌─────────────────────┐
│       Activity      │
│    Presentation     │
└──────────┬──────────┘
           │
           │ observe StateFlow
           ↓
┌─────────────────────┐
│      ViewModel      │
│  State + Coroutine  │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│     Repository      │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│     Retrofit API    │
└─────────────────────┘
```
---
# User Model
Model untuk soal 1, 2, dan 5:
```kotlin
data class User(
    val id: Int,
    val name: String,
    val age: Int,
    val isActive: Boolean
)
```

Model untuk response API pada soal 3:
```kotlin
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)
```
Kedua model sengaja dipisahkan karena struktur datanya berbeda.
---

# Implementation Status
| Requirement                    | Status            |
| ------------------------------ | ----------------- |
| Active adult user filtering    | ✅ Implemented     |
| Sort users by name             | ✅ Implemented     |
| Display result in presentation | ✅ Implemented     |
| Coroutine                      | ✅ Implemented     |
| StateFlow                      | ✅ Implemented     |
| Loading state                  | ✅ Implemented     |
| Success state                  | ✅ Implemented     |
| Error state                    | ✅ Implemented     |
| Search user                    | ✅ Implemented     |
| Case-insensitive search        | ✅ Implemented     |
| Retrofit interface             | ✅ Implemented     |
| User response model            | ✅ Implemented     |
| Repository `getUserById()`     | ✅ Implemented     |

---
# How to Run
1. Clone atau buka project.
2. Pastikan Android Studio menggunakan versi Kotlin/Gradle yang sesuai dengan project.
3. Sync Gradle.
4. Jalankan aplikasi menggunakan emulator atau physical device.
5. Untuk pengujian Retrofit dengan server asli, ubah:

```kotlin
private const val BASE_URL =
    "https://example.com/api/"
```
menjadi Base URL API yang diberikan.

> **Note:** `https://example.com/api/` hanya digunakan sebagai placeholder karena endpoint API sebenarnya belum disediakan dalam requirement technical test.
---

# Evidence
Evidence yang dapat disertakan:

### Soal 1
Screenshot presentation yang menunjukkan:
```text
Active Users Age ≥ 18
Andi
Deni
```

### Soal 2
Screenshot aplikasi pada kondisi:
* Loading
* Success
* Error jika tersedia

### Soal 3
Screenshot kode:
* `UserResponse`
* `UserApi`
* `UserRepository`
Karena API URL sebenarnya belum tersedia, pengujian terhadap server asli belum dapat dilakukan.

---

## Notes
Project ini memisahkan **business logic**, **presentation**, dan **data layer** agar setiap requirement dapat diuji dan dikembangkan secara terpisah.

Untuk API integration, implementasi Retrofit telah disiapkan berdasarkan kontrak endpoint yang diberikan pada soal. Karena actual API base URL tidak tersedia, validasi terhadap server production/real endpoint belum dilakukan.
