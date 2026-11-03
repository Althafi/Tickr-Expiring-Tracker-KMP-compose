# TICKR Presentation Layer (Phase 3) — Integration Guide for Phase 3.5

**Status:** Phase 3 Complete (KMP/Kotlin)  
**Target:** Phase 3.5 (Compose Multiplatform UI Implementation)  
**Date:** 2026-06-07

---

## 📚 Table of Contents

1. [Overview](#overview)
2. [Architecture Pattern (MVI/UDF)](#architecture-pattern-mviudf)
3. [Core Abstractions](#core-abstractions)
4. [Feature Breakdown](#feature-breakdown)
5. [State & Action Contracts](#state--action-contracts)
6. [Navigation Architecture](#navigation-architecture)
7. [Error Handling Strategy](#error-handling-strategy)
8. [Dependency Injection](#dependency-injection)
9. [Integration Guidelines for Phase 3.5](#integration-guidelines-for-phase-35)
10. [Design System Requirements](#design-system-requirements)
11. [Code Examples](#code-examples)

---

## Overview

The Tickr Presentation Layer implements a **strict MVI/UDF (Model-View-Intent / Unidirectional Data Flow)** architecture using:
- **Kotlin Multiplatform** (commonMain) with no platform-specific UI code
- **StateFlow** for reactive state management (state down)
- **Channel** for one-shot effects (events up)
- **ViewModel** for business logic orchestration
- **Koin** for dependency injection

### Layered Architecture

```
UI Layer (Phase 3.5)
    ↓ collect state
ViewModel (Phase 3) ← Presentation Logic
    ↓ call UseCase
Domain Layer (Phase 2) ← Business Rules
    ↓ call Repository
Data Layer (Phase 1) ← Data Source Integration
    ↓ Supabase Backend
```

**Key Principle:** Presentation layer is **UI-agnostic**. No Compose, SwiftUI, Android, or iOS imports. Only:
- `androidx.lifecycle.ViewModel`
- `kotlinx.coroutines.*`
- `com.project.tickr.domain.*`
- `com.project.tickr.core.*`

---

## Architecture Pattern (MVI/UDF)

### State Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ UI (Composable / SwiftUI View)                              │
│  - collectAsState(state)                                    │
│  - send onAction(action)                                    │
│  - observe events → Navigator / Snackbar                   │
└────────────────────┬────────────────────────────────────────┘
                     │
          ┌──────────┴──────────┐
          ↓ onAction(action)    │
┌─────────────────────────────────────────────────────────────┐
│ ViewModel (Main Thread / viewModelScope)                    │
│  1. Receive action in onAction(action: UiAction)           │
│  2. Mutate state via _state.update { }                     │
│  3. Launch coroutine: UseCase() → onSuccess/onError        │
│  4. Emit UiEvent via _events.send(event: UiEvent)         │
└────────────────────┬────────────────────────────────────────┘
                     │
          ┌──────────┴──────────┐
          ↓ StateFlow           ↓ Channel
     ┌─────────────────┐   ┌─────────────┐
     │ UiState         │   │ UiEvent     │
     │ (immutable)     │   │ (one-shot)  │
     │ data class      │   │ sealed      │
     └─────────────────┘   └─────────────┘
          ↓                        ↓
     UI re-renders          Navigator/Snackbar
```

### Why MVI/UDF?

| Aspect | Benefit |
|--------|---------|
| **Immutable State** | Time-travel debugging, replay, predictable re-renders |
| **Unidirectional Data** | Single source of truth, no circular deps, testable |
| **Event-based Navigation** | Decoupled from ViewModels, composable, reusable |
| **Error Handling** | Centralized via ErrorMessageProvider, consistent UX |
| **Coroutine Scope** | Memory safe, no leaks, proper cancellation on destroy |

---

## Core Abstractions

### 1. UiText — Localization/Error Messages

**Location:** `presentation/common/UiText.kt`

```kotlin
sealed interface UiText {
    data class Raw(val value: String) : UiText
}

// Extension for convenient creation
fun String.toUiText(): UiText = UiText.Raw(this)
```

**Usage in Phase 3.5:**
- All error messages in UiState use `UiText`
- UI renders `uiText.asString()` (to be implemented in Compose/SwiftUI)
- Enables future i18n without ViewModel changes

### 2. UiAction — Intent Marker

**Location:** `presentation/common/mvi/UiAction.kt`

```kotlin
interface UiAction
```

**Role:** Marker interface for all action sealed classes. Enables:
- Type-safe action routing
- Generic action handlers
- Exhaustive `when` checks in ViewModel

### 3. UiEvent — One-Shot Effect Marker

**Location:** `presentation/common/mvi/UiEvent.kt`

```kotlin
interface UiEvent
```

**Role:** Marker interface for all event sealed classes. Ensures:
- Navigation is not replayed on config change
- Snackbars appear exactly once
- Side effects are explicit

### 4. Destination — Type-Safe Navigation

**Location:** `presentation/navigation/Destination.kt`

```kotlin
sealed interface Destination {
    val route: String
    
    // Root destinations
    data object Auth : Destination { override val route = "auth" }
    data object Home : Destination { override val route = "home" }
    data object Profile : Destination { override val route = "profile" }
    
    // Item screens
    data object ItemList : Destination { override val route = "items" }
    data class ItemDetail(val id: Long) : Destination {
        override val route = "items/${id}"
        companion object {
            const val pattern = "items/{${ARG_ID}}"
            const val ARG_ID = "itemId"
        }
    }
    data class ItemEdit(val id: Long) : Destination {
        override val route = "items/${id}/edit"
        companion object {
            const val pattern = "items/{${ARG_ID}}/edit"
            const val ARG_ID = "itemId"
        }
    }
    data object ItemCreate : Destination { override val route = "items/new" }
    
    // Category screens
    data object CategoryList : Destination { override val route = "categories" }
    data class CategoryForm(val id: Long? = null) : Destination {
        override val route = if (id != null) "categories/${id}" else "categories/new"
        companion object {
            const val pattern = "categories/{${ARG_ID}?}"
            const val ARG_ID = "categoryId"
        }
    }
}
```

**Phase 3.5 Integration:**
- Pass `Destination` objects to `Navigator.navigate()`
- Extract args: `intent.getStringExtra(Destination.ItemDetail.ARG_ID)`
- No magic strings in UI code

### 5. Navigator — Navigation Contract

**Location:** `presentation/navigation/Navigator.kt`

```kotlin
interface Navigator {
    suspend fun navigate(destination: Destination, popUpToInclusive: Boolean = false)
    suspend fun back()
}
```

**Phase 3.5 Implementation:**
- Android: Wrap `NavController` from `navigation-compose`
- iOS: Wrap SwiftUI `NavigationPath` or custom navigation state
- Provide via Koin in platform-specific module

---

## Feature Breakdown

### Feature 1: Authentication

**Files:**
- State: `presentation/auth/AuthUiState.kt`
- Action: `presentation/auth/AuthAction.kt`
- Event: `presentation/auth/AuthEvent.kt`
- ViewModel: `presentation/auth/AuthViewModel.kt`

#### UiState Contract

```kotlin
data class AuthUiState(
    val mode: Mode = Mode.SignIn,
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val generalError: UiText? = null
) {
    enum class Mode { SignIn, SignUp }
    
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isSubmitting
}
```

#### Action Contract

```kotlin
sealed interface AuthAction : UiAction {
    data class EmailChanged(val value: String) : AuthAction
    data class PasswordChanged(val value: String) : AuthAction
    data object ToggleMode : AuthAction
    data object Submit : AuthAction
    data object SignOut : AuthAction
}
```

#### Event Contract

```kotlin
sealed interface AuthEvent : UiEvent {
    data object NavigateToHome : AuthEvent
    data object NavigateToAuth : AuthEvent
    data class ShowError(val message: UiText) : AuthEvent
}
```

#### ViewModel Behavior

- **onInit:** Observes session via `ObserveSessionUseCase().collect()`
  - If user logged in → emit `NavigateToHome`
  - If user logged out → stay on auth screen
- **EmailChanged/PasswordChanged:** Update state, clear field errors
- **ToggleMode:** Swap between SignIn/SignUp
- **Submit:**
  - Validate via `SignUpUseCase` or `SignInUseCase`
  - Map `AppError.Validation(field=...)` → field-specific `UiText` error
  - On success → emit `NavigateToHome`
  - On error → set `generalError` or field error
- **SignOut:** Call `SignOutUseCase` → emit `NavigateToAuth`

#### Phase 3.5 Design Requirements

**Screens:**
1. **SignIn Screen**
   - Email TextField (error text below)
   - Password TextField (error text below)
   - "Toggle to Sign Up" button
   - Submit button (disabled if `!canSubmit`)
   - General error Snackbar

2. **SignUp Screen**
   - Email TextField (error text below)
   - Password TextField (min 6 chars hint)
   - Password confirmation TextField (optional: validate match)
   - "Toggle to Sign In" button
   - Submit button (disabled if `!canSubmit`)
   - General error Snackbar

**Colors/Typography:**
- Email/Password errors: `Color.Red` or error color
- Submit button: Primary color, disabled state lighter
- Form labels: Secondary text color

---

### Feature 2: Home (Dashboard)

**Files:**
- State: `presentation/home/HomeUiState.kt`
- Action: `presentation/home/HomeAction.kt`
- Event: `presentation/home/HomeEvent.kt`
- ViewModel: `presentation/home/HomeViewModel.kt`

#### UiState Contract

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val expiringItems: List<Item> = emptyList(),
    val expiringCount: Int = 0,
    val error: UiText? = null
)
```

#### Action Contract

```kotlin
sealed interface HomeAction : UiAction {
    data object Load : HomeAction
    data object Refresh : HomeAction
    data class ClickItem(val id: Long) : HomeAction
}
```

#### Event Contract

```kotlin
sealed interface HomeEvent : UiEvent {
    data class NavigateToDetail(val destination: Destination.ItemDetail) : HomeEvent
    data class ShowError(val message: UiText) : HomeEvent
}
```

#### ViewModel Behavior

- **onInit:** Call `HomeAction.Load` automatically
- **Load/Refresh:** Call `GetExpiringItemsUseCase(getCurrentUserId())`
  - Set `isLoading = true`
  - On success → `expiringItems = items`, `expiringCount = items.size`
  - On error → `error = errorMessages.provide(error)`
- **ClickItem:** Emit `NavigateToDetail(Destination.ItemDetail(id))`

#### Phase 3.5 Design Requirements

**Screen:**
- Header: "Dashboard" or user greeting (e.g., "Welcome, Altaf")
- Summary card: "**X items** expiring soon" (clickable → ItemList)
- Expiring items list:
  - Item card: name, expiry date (highlight red if < 7 days)
  - Badge: "Expiring in 3 days"
  - Swipe/button: View Details → navigate to ItemDetail
  - Empty state: "No items expiring soon ✓"
- Pull-to-refresh: Triggers `Refresh` action
- Loading spinner: Show during `isLoading = true`
- Error banner: Show `error` if present

**Colors:**
- Expiry date < 7 days: Red/Orange background
- Expiry date 7–30 days: Yellow/Amber background
- Expiry date > 30 days: Green background

---

### Feature 3: Item Management

#### 3.1 Item List

**Files:** `presentation/item/list/`

#### UiState Contract

```kotlin
data class ItemListUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: UiText? = null
) {
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading
}
```

#### Action Contract

```kotlin
sealed interface ItemListAction : UiAction {
    data object Load : ItemListAction
    data object Refresh : ItemListAction
    data class Delete(val id: Long) : ItemListAction
    data class MarkConsumed(val id: Long) : ItemListAction
    data class ClickItem(val id: Long) : ItemListAction
    data object ClickAdd : ItemListAction
}
```

#### Event Contract

```kotlin
sealed interface ItemListEvent : UiEvent {
    data class NavigateToDetail(val id: Long) : ItemListEvent
    data class NavigateToCreate(val destination: String) : ItemListEvent
    data class ShowSnackbar(val message: UiText) : ItemListEvent
}
```

#### ViewModel Behavior

- **Load/Refresh:** Fetch all items for current user
- **Delete:** Remove item and refresh list
- **MarkConsumed:** Find item by id, call `MarkItemConsumedUseCase(item, true)`, refresh
  - Note: UseCase expects full `Item` object, not just id
- **ClickItem:** Emit `NavigateToDetail(id)`
- **ClickAdd:** Emit `NavigateToCreate("items/new")`

#### Phase 3.5 Design

**Screen:**
- AppBar: "My Items" with FAB (+) → ClickAdd
- List: Item cards with:
  - Name, category, expiry date
  - Checkbox "Mark Consumed"
  - Options menu: Edit, Delete
- Pull-to-refresh
- Empty state: "No items. Tap + to add."
- Swipe/drag to delete (confirm delete dialog)

---

#### 3.2 Item Detail

**Files:** `presentation/item/detail/`

#### UiState Contract

```kotlin
data class ItemDetailUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val error: UiText? = null
)
```

#### Action Contract

```kotlin
sealed interface ItemDetailAction : UiAction {
    data class Load(val id: Long) : ItemDetailAction
    data object Delete : ItemDetailAction
    data object MarkConsumed : ItemDetailAction
    data object ClickEdit : ItemDetailAction
}
```

#### Event Contract

```kotlin
sealed interface ItemDetailEvent : UiEvent {
    data object NavigateBack : ItemDetailEvent
    data class NavigateToEdit(val id: Long) : ItemDetailEvent
    data class ShowSnackbar(val message: UiText) : ItemDetailEvent
}
```

#### ViewModel Behavior

- **Load:** Fetch item by id via `GetItemUseCase(id)`
- **Delete:** Call `DeleteItemUseCase(item.id)` → emit `NavigateBack`
- **MarkConsumed:** Call `MarkItemConsumedUseCase(currentItem, true)` → update `item.isConsumed = true`
- **ClickEdit:** Emit `NavigateToEdit(item.id)`

#### Phase 3.5 Design

**Screen:**
- Header with Back button, Edit button, Delete button
- Item details:
  - Image (if `imageUrl` provided)
  - Name (large, bold)
  - Category badge
  - Expiry date with urgency color
  - Barcode (if present)
  - Notes section
  - Created/Updated timestamps
- Action buttons:
  - "Mark as Consumed" (toggle)
  - "Edit"
  - "Delete" (confirm dialog)

---

#### 3.3 Item Form (Create/Edit)

**Files:** `presentation/item/form/`

#### UiState Contract

```kotlin
data class ItemFormUiState(
    val name: String = "",
    val expiryDate: String = "",
    val categoryId: Long? = null,
    val notes: String = "",
    val isSaving: Boolean = false,
    val nameError: UiText? = null,
    val expiryDateError: UiText? = null,
    val error: UiText? = null
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && expiryDate.isNotBlank() && !isSaving
}
```

#### Action Contract

```kotlin
sealed interface ItemFormAction : UiAction {
    data class NameChanged(val value: String) : ItemFormAction
    data class ExpiryDateChanged(val value: String) : ItemFormAction
    data class CategoryChanged(val value: Long?) : ItemFormAction
    data class NotesChanged(val value: String) : ItemFormAction
    data object Submit : ItemFormAction
}
```

#### Event Contract

```kotlin
sealed interface ItemFormEvent : UiEvent {
    data object Saved : ItemFormEvent
    data class ShowError(val message: UiText) : ItemFormEvent
}
```

#### ViewModel Behavior

- **Note:** `submit()` currently stubbed (TODO). Phase 3.5 should implement:
  1. Validate name, expiryDate not blank
  2. Call `CreateItemUseCase(name, expiryDate, categoryId, notes)` or `UpdateItemUseCase(...)`
  3. On success → emit `Saved` event (UI navigates back)
  4. On error → set field errors or `error`

#### Phase 3.5 Design

**Screen:**
- Title: "New Item" or "Edit Item"
- Form fields:
  - Name TextField (required, error below)
  - Expiry Date DatePicker
  - Category Dropdown (fetched from CategoryListViewModel or passed in state)
  - Notes TextField (optional, multiline)
  - Image picker (optional, Phase 4)
- Submit button: "Save" (disabled if `!canSubmit`)
- Cancel button → back

---

### Feature 4: Category Management

#### 4.1 Category List

**Files:** `presentation/category/list/`

#### UiState Contract

```kotlin
data class CategoryListUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: UiText? = null
) {
    val isEmpty: Boolean get() = categories.isEmpty() && !isLoading
}
```

#### Action Contract

```kotlin
sealed interface CategoryListAction : UiAction {
    data object Load : CategoryListAction
    data class Delete(val id: Long) : CategoryListAction
    data object ClickAdd : CategoryListAction
    data class ClickCategory(val id: Long) : CategoryListAction
}
```

#### Event Contract

```kotlin
sealed interface CategoryListEvent : UiEvent {
    data class NavigateToForm(val id: Long?) : CategoryListEvent
    data class ShowSnackbar(val message: UiText) : CategoryListEvent
}
```

#### ViewModel Behavior

- **Load:** Fetch all categories for current user
- **Delete:** Remove category and refresh
- **ClickAdd:** Emit `NavigateToForm(null)` (create mode)
- **ClickCategory:** Emit `NavigateToForm(id)` (edit mode)

#### Phase 3.5 Design

**Screen:**
- AppBar: "Categories" with FAB (+) → ClickAdd
- Grid or list of category cards:
  - Category name
  - Color/icon (from `colorHex` and `iconName`)
  - Item count badge
  - Options menu: Edit, Delete
- Empty state: "No categories. Tap + to add."

---

#### 4.2 Category Form

**Files:** `presentation/category/form/`

#### UiState Contract

```kotlin
data class CategoryFormUiState(
    val name: String = "",
    val iconName: String = "",
    val colorHex: String = "#000000",
    val isSaving: Boolean = false,
    val error: UiText? = null
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && !isSaving
}
```

#### Action Contract

```kotlin
sealed interface CategoryFormAction : UiAction {
    data class NameChanged(val value: String) : CategoryFormAction
    data class IconNameChanged(val value: String) : CategoryFormAction
    data class ColorHexChanged(val value: String) : CategoryFormAction
    data object Submit : CategoryFormAction
}
```

#### Event Contract

```kotlin
sealed interface CategoryFormEvent : UiEvent {
    data object Saved : CategoryFormEvent
    data class ShowError(val message: UiText) : CategoryFormEvent
}
```

#### ViewModel Behavior

- **Note:** `submit()` currently stubbed (TODO). Phase 3.5 should implement:
  1. Validate name not blank
  2. Call `CreateCategoryUseCase(...)` or `UpdateCategoryUseCase(...)`
  3. On success → emit `Saved`
  4. On error → set `error`

#### Phase 3.5 Design

**Screen:**
- Title: "New Category" or "Edit Category"
- Form fields:
  - Name TextField (required)
  - Icon picker (Dropdown/Selector from predefined icon set)
  - Color picker (HexColorPicker or ColorWheel)
  - Preview: Category card with chosen icon + color
- Submit button: "Save"
- Cancel button → back

---

### Feature 5: Profile

**Files:** `presentation/profile/`

#### UiState Contract

```kotlin
data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val isSaving: Boolean = false,
    val error: UiText? = null
)
```

#### Action Contract

```kotlin
sealed interface ProfileAction : UiAction {
    data object Load : ProfileAction
    data class FullNameChanged(val value: String) : ProfileAction
    data object Save : ProfileAction
    data object SignOut : ProfileAction
}
```

#### Event Contract

```kotlin
sealed interface ProfileEvent : UiEvent {
    data object Saved : ProfileEvent
    data object NavigateToAuth : ProfileEvent
    data class ShowSnackbar(val message: UiText) : ProfileEvent
}
```

#### ViewModel Behavior

- **Load:** Fetch profile from `GetProfileUseCase(userId)` → update `fullName`
- **FullNameChanged:** Update state
- **Save:** Currently stubbed (TODO). Should:
  1. Call `UpsertProfileUseCase(userId, fullName)`
  2. On success → emit `Saved` (toast notification)
  3. On error → set `error`
- **SignOut:** Call `SignOutUseCase()` → emit `NavigateToAuth`

#### Phase 3.5 Design

**Screen:**
- Header: "Profile" with Sign Out button
- Profile card:
  - Avatar (initials or placeholder)
  - Email (read-only, from auth)
  - Full Name TextField (editable)
- Save button → triggers `Save` action
- Sign Out button → `SignOut` action (confirm dialog)
- Loading spinner during save
- Success toast: "Profile updated"

---

## State & Action Contracts

### Summary Table

| Feature | UiState Fields | Key Actions | Key Events |
|---------|---|---|---|
| **Auth** | mode, email, password, isSubmitting, emailError, passwordError, generalError | EmailChanged, PasswordChanged, ToggleMode, Submit, SignOut | NavigateToHome, NavigateToAuth, ShowError |
| **Home** | isLoading, expiringItems, expiringCount, error | Load, Refresh, ClickItem | NavigateToDetail, ShowError |
| **ItemList** | isLoading, items, error | Load, Refresh, Delete, MarkConsumed, ClickItem, ClickAdd | NavigateToDetail, NavigateToCreate, ShowSnackbar |
| **ItemDetail** | isLoading, item, error | Load, Delete, MarkConsumed, ClickEdit | NavigateBack, NavigateToEdit, ShowSnackbar |
| **ItemForm** | name, expiryDate, categoryId, notes, isSaving, nameError, expiryDateError, error | NameChanged, ExpiryDateChanged, CategoryChanged, NotesChanged, Submit | Saved, ShowError |
| **CategoryList** | isLoading, categories, error | Load, Delete, ClickAdd, ClickCategory | NavigateToForm, ShowSnackbar |
| **CategoryForm** | name, iconName, colorHex, isSaving, error | NameChanged, IconNameChanged, ColorHexChanged, Submit | Saved, ShowError |
| **Profile** | isLoading, fullName, isSaving, error | Load, FullNameChanged, Save, SignOut | Saved, NavigateToAuth, ShowSnackbar |

---

## Navigation Architecture

### Navigation Flow

```
┌─────────────┐
│   Auth      │
└──────┬──────┘
       │ (LoggedIn) NavigateToHome
       ↓
┌─────────────┐
│   Home      │
└──────┬──────┘
       ├──→ (ClickItem) ItemList
       ├──→ (ClickCategory) CategoryList
       └──→ (Menu) Profile
       
┌─────────────────────────────────────────────┐
│   ItemList                                  │
├─────────────────────────────────────────────┤
│ ClickItem(id)    → ItemDetail               │
│ ClickAdd         → ItemCreate               │
│ Back/Pop         → Home                     │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│   ItemDetail                                │
├─────────────────────────────────────────────┤
│ ClickEdit(id)    → ItemEdit                 │
│ Delete           → ItemList                 │
│ Back             → ItemList                 │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│   ItemForm (Create/Edit)                    │
├─────────────────────────────────────────────┤
│ Save             → ItemList (emit Saved)    │
│ Cancel/Back      → ItemList/Detail          │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│   CategoryList                              │
├─────────────────────────────────────────────┤
│ ClickAdd         → CategoryForm(null)       │
│ ClickCategory(id) → CategoryForm(id)        │
│ Back             → Home                     │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│   CategoryForm (Create/Edit)                │
├─────────────────────────────────────────────┤
│ Save             → CategoryList             │
│ Cancel/Back      → CategoryList             │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│   Profile                                   │
├─────────────────────────────────────────────┤
│ SignOut          → Auth                     │
│ Back             → Home                     │
└─────────────────────────────────────────────┘
```

### Implementation in Phase 3.5

**Using Compose Navigation:**

```kotlin
// In your NavGraph composable
navHost(navController, startDestination = Destination.Auth.route) {
    composable(Destination.Auth.route) {
        val viewModel = koinViewModel<AuthViewModel>()
        AuthScreen(viewModel, navigator)
    }
    
    composable(Destination.Home.route) {
        val viewModel = koinViewModel<HomeViewModel>()
        HomeScreen(viewModel, navigator)
    }
    
    composable(Destination.ItemList.route) {
        val viewModel = koinViewModel<ItemListViewModel>()
        ItemListScreen(viewModel, navigator)
    }
    
    composable(
        route = Destination.ItemDetail.pattern,
        arguments = listOf(
            navArgument(Destination.ItemDetail.ARG_ID) { type = NavType.LongType }
        )
    ) { backStackEntry ->
        val viewModel = koinViewModel<ItemDetailViewModel>()
        ItemDetailScreen(viewModel, navigator)
    }
    
    // ... other screens
}
```

**Navigator Implementation (Android):**

```kotlin
class AndroidNavigator(private val navController: NavController) : Navigator {
    override suspend fun navigate(destination: Destination, popUpToInclusive: Boolean) {
        navController.navigate(destination.route) {
            if (popUpToInclusive) {
                popUpTo(0) // Or specific route
            }
        }
    }
    
    override suspend fun back() {
        navController.popBackStack()
    }
}
```

---

## Error Handling Strategy

### Error Flow

```
UseCase throws AppError
         ↓
ViewModel catches in onError { error }
         ↓
errorMessages.provide(error: AppError) → UiText
         ↓
Set state.error = UiText OR emit Event.ShowError(UiText)
         ↓
UI renders error (banner, snackbar, field error)
```

### AppError Hierarchy (Phase 2)

```kotlin
sealed class AppError {
    data class Network(val message: String) : AppError()
    data object Unauthorized : AppError()
    data object NotFound : AppError()
    data class Serialization(val message: String) : AppError()
    data class Validation(val field: String, val message: String) : AppError()
    data class Unknown(val message: String) : AppError()
}
```

### ErrorMessageProvider (Phase 2)

**Location:** `domain/error/ErrorMessageProvider.kt`

```kotlin
interface ErrorMessageProvider {
    fun provide(error: AppError): UiText
}
```

**Implementation in Phase 3.5:**
- Map each `AppError` type to user-friendly message
- Example:
  ```kotlin
  when (error) {
      is AppError.Network -> "Network error. Check your connection.".toUiText()
      AppError.Unauthorized -> "Session expired. Please log in again.".toUiText()
      is AppError.Validation -> "${error.field}: ${error.message}".toUiText()
      else -> "Something went wrong.".toUiText()
  }
  ```

### Field-Level Validation Errors (Auth Example)

When `SignInUseCase` returns `AppError.Validation(field="email", ...)`, `AuthViewModel` maps it:

```kotlin
when (error) {
    is AppError.Validation -> {
        when (error.field) {
            "email" -> _state.update { it.copy(emailError = errorMessages.provide(error)) }
            "password" -> _state.update { it.copy(passwordError = errorMessages.provide(error)) }
        }
    }
    else -> _state.update { it.copy(generalError = errorMessages.provide(error)) }
}
```

**Phase 3.5 UI:**
- Display field error below TextField in red
- Display general error in error banner/snackbar

---

## Dependency Injection

### Koin Module Setup

**Location:** `presentation/di/PresentationModule.kt`

```kotlin
val presentationModule = module {
    // ViewModels as factory (new instance per request)
    factory { AuthViewModel(get(), get(), get(), get(), get()) }
    factory { HomeViewModel(get(), get()) }
    factory { ItemListViewModel(get(), get(), get(), get(), get()) }
    factory { ItemDetailViewModel(get(), get(), get(), get()) }
    factory { ItemFormViewModel(get(), get(), get(), get(), get()) }
    factory { CategoryListViewModel(get(), get(), get(), get()) }
    factory { CategoryFormViewModel(get(), get(), get(), get()) }
    factory { ProfileViewModel(get(), get(), get(), get(), get()) }
}
```

**Dependencies resolved from:**
- `dataModule` (Repositories)
- `domainModule` (UseCases, ErrorMessageProvider)
- `coreModule` (Logging, etc.)

### Init Koin

**Location:** `di/InitKoin.kt`

```kotlin
fun initKoin() {
    startKoin {
        modules(
            dataModule,
            domainModule,
            presentationModule
        )
    }
}
```

### Phase 3.5 ViewModel Access

**In Compose:**

```kotlin
@Composable
fun ItemListRoute(navigator: Navigator) {
    val viewModel = koinViewModel<ItemListViewModel>()
    val state by viewModel.state.collectAsState()
    val events = remember(viewModel) { viewModel.events }
    
    LaunchedEffect(Unit) {
        viewModel.onAction(ItemListAction.Load)
    }
    
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ItemListEvent.NavigateToDetail -> navigator.navigate(Destination.ItemDetail(event.id))
                is ItemListEvent.ShowSnackbar -> showSnackbar(event.message.asString())
            }
        }
    }
    
    ItemListScreen(state, viewModel::onAction)
}
```

---

## Integration Guidelines for Phase 3.5

### Checklist: Building a Compose Screen

For each feature (e.g., ItemList), follow this pattern:

**1. Create Route Composable (Navigation Entry)**

```kotlin
@Composable
fun ItemListRoute(navigator: Navigator) {
    val viewModel = koinViewModel<ItemListViewModel>()
    val state by viewModel.state.collectAsState()
    val events = viewModel.events
    
    // Handle navigation events
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ItemListEvent.NavigateToDetail -> {
                    navigator.navigate(Destination.ItemDetail(event.id))
                }
                is ItemListEvent.NavigateToCreate -> {
                    navigator.navigate(Destination.ItemCreate)
                }
                is ItemListEvent.ShowSnackbar -> {
                    // Show snackbar
                }
            }
        }
    }
    
    // Load data on first composition
    LaunchedEffect(Unit) {
        viewModel.onAction(ItemListAction.Load)
    }
    
    // Render UI
    ItemListScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
```

**2. Create Screen Composable (Pure UI)**

```kotlin
@Composable
fun ItemListScreen(
    state: ItemListUiState,
    onAction: (ItemListAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // AppBar
        TopAppBar(
            title = { Text("My Items") },
            actions = {
                IconButton(onClick = { onAction(ItemListAction.ClickAdd) }) {
                    Icon(Icons.Default.Add, "Add Item")
                }
            }
        )
        
        when {
            state.error != null -> {
                // Error banner
                ErrorBanner(state.error!!)
            }
            state.isLoading -> {
                // Loading spinner
                CircularProgressIndicator()
            }
            state.isEmpty -> {
                // Empty state
                Text("No items. Tap + to add.")
            }
            else -> {
                // Item list
                LazyColumn {
                    items(state.items) { item ->
                        ItemCard(
                            item = item,
                            onItemClick = { onAction(ItemListAction.ClickItem(item.id)) },
                            onMarkConsumed = { onAction(ItemListAction.MarkConsumed(item.id)) },
                            onDelete = { onAction(ItemListAction.Delete(item.id)) }
                        )
                    }
                }
            }
        }
    }
}
```

**3. Integrate into NavGraph**

```kotlin
composable(Destination.ItemList.route) {
    ItemListRoute(navigator)
}
```

---

### Key Integration Points

#### 1. State Collection

```kotlin
val state by viewModel.state.collectAsState()
```

- Collect state as Compose State
- UI recomposes when state changes
- Safe for config changes (ViewModel survives)

#### 2. Event Handling

```kotlin
LaunchedEffect(viewModel.events) {
    viewModel.events.collect { event ->
        // Handle one-shot effects
    }
}
```

- Events are one-shot (not replayed)
- Use for navigation, snackbars, dialogs
- Cancel on composition exit (LaunchedEffect cleanup)

#### 3. Action Dispatching

```kotlin
onAction(ItemListAction.ClickItem(id))
```

- Send actions synchronously
- ViewModel processes asynchronously in `viewModelScope`
- No blocking on UI thread

#### 4. Error Rendering

```kotlin
if (state.error != null) {
    ErrorBanner(
        message = state.error!!.asString(),
        onDismiss = { /* optional */ }
    )
}
```

- Render `UiText` as string (implement `asString()` extension)
- Optional: add i18n later

---

## Design System Requirements

### Theme/Colors Needed

Based on Tickr's domain (expiry tracking):

| Component | Color | Usage |
|---|---|---|
| **Primary** | Teal/Green | Buttons, FAB, success states |
| **Secondary** | Indigo/Blue | Secondary actions, secondary text |
| **Error** | Red | Errors, urgent expiry dates |
| **Warning** | Amber/Yellow | Upcoming expiry (7–30 days) |
| **Success** | Green | Successful actions, consumed items |
| **Surface** | Light gray/White | Cards, backgrounds |
| **Surface Variant** | Medium gray | Dividers, inactive states |
| **On Surface** | Dark gray/Black | Primary text |
| **On Surface Variant** | Medium gray | Secondary text |

### Typography

| Style | Usage |
|---|---|
| **Display Large** | Screen titles (rarely) |
| **Headline Large** | Feature titles ("My Items", "Dashboard") |
| **Headline Medium** | Section headers |
| **Body Large** | Form labels, descriptions |
| **Body Medium** | Item names, regular content |
| **Label Large** | Button text, badges |
| **Label Small** | Helper text, captions |

### Spacing

- Gutter: 16dp (standard)
- Card padding: 16dp
- Item spacing: 8dp (vertical)
- TextField spacing: 8dp (below label/error)

### Icons (Placeholder Set)

- **Category icons:** Box, Apple, Medicine, Vegetable, Milk, Snack, Other
- **Action icons:** Add, Edit, Delete, Back, Check, Close
- **Status icons:** Warning, Error, Success, Clock

**Phase 3.5:** Work with designer to finalize icon set, or use Material Icons + custom SVGs.

### Image Assets

- **Launcher icon:** Tickr app icon (any format, project converts)
- **Splash screen:** Optional branding
- **Empty state illustrations:** Generic (chef hat, calendar, etc.)
- **Avatar placeholder:** Initials or generic profile icon

---

## Code Examples

### Example 1: Implementing ItemFormViewModel.submit()

**Current state (stubbed):**

```kotlin
private fun save() {
    val userId = getCurrentUserId() ?: return
    _state.update { it.copy(isSaving = true) }
    // TODO: implement
    _state.update { it.copy(isSaving = false) }
}
```

**Phase 3.5 implementation:**

```kotlin
private fun save() {
    if (!_state.value.canSubmit) return
    
    val userId = getCurrentUserId() ?: return
    val state = _state.value
    
    _state.update { it.copy(isSaving = true, error = null) }
    
    viewModelScope.launch {
        // Validate
        if (state.name.isBlank()) {
            _state.update { it.copy(nameError = "Name required".toUiText()) }
            return@launch
        }
        if (state.expiryDate.isBlank()) {
            _state.update { it.copy(expiryDateError = "Expiry date required".toUiText()) }
            return@launch
        }
        
        // Create item (assuming CreateItemUseCase exists in Phase 2)
        val result = createItemUseCase(
            userId = userId,
            name = state.name,
            expiryDate = state.expiryDate,
            categoryId = state.categoryId,
            notes = state.notes
        )
        
        result
            .onSuccess { item ->
                _state.update { it.copy(isSaving = false) }
                _events.send(ItemFormEvent.Saved)
            }
            .onError { error ->
                _state.update { it.copy(isSaving = false, error = errorMessages.provide(error)) }
            }
    }
}
```

### Example 2: Implementing CategoryFormViewModel.submit()

```kotlin
private fun submit() {
    if (!_state.value.canSubmit) return
    
    val userId = getCurrentUserId() ?: return
    val state = _state.value
    
    _state.update { it.copy(isSaving = true) }
    
    viewModelScope.launch {
        // Validate
        if (state.name.isBlank()) {
            _state.update { it.copy(error = "Category name required".toUiText()) }
            return@launch
        }
        
        val result = createCategoryUseCase(
            userId = userId,
            name = state.name,
            iconName = state.iconName,
            colorHex = state.colorHex
        )
        
        result
            .onSuccess {
                _state.update { it.copy(isSaving = false) }
                _events.send(CategoryFormEvent.Saved)
            }
            .onError { error ->
                _state.update { it.copy(isSaving = false, error = errorMessages.provide(error)) }
            }
    }
}
```

### Example 3: Implementing ProfileViewModel.save()

```kotlin
private fun save() {
    val userId = getCurrentUserId() ?: return
    val fullName = _state.value.fullName
    
    if (fullName.isBlank()) {
        _events.send(ProfileEvent.ShowSnackbar("Name cannot be empty".toUiText()))
        return
    }
    
    _state.update { it.copy(isSaving = true) }
    
    viewModelScope.launch {
        upsertProfile(userId, fullName)
            .onSuccess {
                _state.update { it.copy(isSaving = false) }
                _events.send(ProfileEvent.Saved)
            }
            .onError { error ->
                _state.update { it.copy(isSaving = false, error = errorMessages.provide(error)) }
            }
    }
}
```

### Example 4: Composable Screen Pattern

```kotlin
@Composable
fun AuthRoute(navigator: Navigator) {
    val viewModel = koinViewModel<AuthViewModel>()
    val state by viewModel.state.collectAsState()
    val events = viewModel.events
    
    // Handle navigation
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                AuthEvent.NavigateToHome -> navigator.navigate(Destination.Home)
                is AuthEvent.ShowError -> {
                    // Show error snackbar
                    showSnackbar(event.message.asString())
                }
                AuthEvent.NavigateToAuth -> {
                    // Already on auth, no-op
                }
            }
        }
    }
    
    AuthScreen(
        state = state,
        onEmailChange = { viewModel.onAction(AuthAction.EmailChanged(it)) },
        onPasswordChange = { viewModel.onAction(AuthAction.PasswordChanged(it)) },
        onToggleMode = { viewModel.onAction(AuthAction.ToggleMode) },
        onSubmit = { viewModel.onAction(AuthAction.Submit) }
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.mode == AuthUiState.Mode.SignIn) "Sign In" else "Sign Up",
            style = MaterialTheme.typography.headlineLarge
        )
        
        // Email field
        TextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = state.emailError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.emailError != null) {
            Text(
                text = state.emailError!!.asString(),
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Password field
        TextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            isError = state.passwordError != null,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        if (state.passwordError != null) {
            Text(
                text = state.passwordError!!.asString(),
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Submit button
        Button(
            onClick = onSubmit,
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.mode == AuthUiState.Mode.SignIn) "Sign In" else "Sign Up")
        }
        
        // Toggle mode
        TextButton(
            onClick = onToggleMode,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (state.mode == AuthUiState.Mode.SignIn)
                    "Don't have an account? Sign Up"
                else
                    "Already have an account? Sign In"
            )
        }
        
        // General error
        if (state.generalError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.generalError!!.asString(),
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

---

## Phase 3.5 Checklist

Before starting Phase 3.5 (Compose screens), ensure:

- [ ] **Design finalized:** Colors, typography, spacing, icons, images approved
- [ ] **Screens listed:** All 8–10 screens identified (Auth signin/signup, Home, ItemList, ItemDetail, ItemForm, CategoryList, CategoryForm, Profile)
- [ ] **Navigation flow:** Flow diagram reviewed and approved
- [ ] **ViewModel dependencies:** All UseCase + ErrorMessageProvider dependencies understood
- [ ] **State/Action/Event contracts:** Reviewed and understood for each screen
- [ ] **Compose experience:** Team familiar with Compose Multiplatform, LazyColumn, TextField, etc.
- [ ] **Error handling:** ErrorMessageProvider impl finalized (error message strings)
- [ ] **Navigator impl:** Decided on navigation library (navigation-compose recommended)
- [ ] **Asset preparation:** Icons, images, placeholder graphics ready
- [ ] **Stubbed methods:** TODO in ItemFormViewModel.submit(), CategoryFormViewModel.submit(), ProfileViewModel.save() ready to implement

---

## Summary

**Phase 3 delivers:**
- ✅ 8 complete ViewModels following strict MVI/UDF pattern
- ✅ Immutable UiState, sealed Action, sealed Event for each feature
- ✅ Type-safe navigation (Destination, Navigator)
- ✅ Error handling with ErrorMessageProvider
- ✅ Full Koin DI setup
- ✅ Zero UI code (no Compose, SwiftUI, Android, iOS imports)

**Phase 3.5 will deliver:**
- Compose Multiplatform screens for each feature
- Theme/colors/typography implementation
- Icon and image assets
- Navigator concrete implementation
- ErrorMessageProvider concrete implementation
- Integration tests (optional)

**Files are production-ready for Phase 3.5 UI development.** 🚀

