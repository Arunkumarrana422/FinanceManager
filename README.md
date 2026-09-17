# Expense Manager (Personal Finance — Native Android, Kotlin + Compose)

A single-user personal finance tracker: accounts, income/expense/transfer transactions, category budgets, and a daily/monthly dashboard.

**Not compiled in this environment** (no Android SDK/Gradle available here) — written to compile against the pinned versions in `app/build.gradle.kts`, but open it in Android Studio and sync before assuming it's clean. See the checklist near the bottom.

## 1. Scope of this build

This was deliberately scoped down from a much larger 46-section spec to a working core first, per your instruction. **Included:**

- Email/password auth (Firebase Auth), session persistence, password reset, profile (name + currency), password change
- Accounts (Cash / Bank / UPI / Wallet / Credit Card / Other) with an auto-maintained running balance
- Add Expense, Add Income (shared form, default categories seeded on first login), Transfer between accounts
- Full transaction ledger: search, filter by type/category/account/month, sort, edit, delete, duplicate
- Category-based (and "Overall") monthly budgets with spent/remaining/percent-used and an over-budget indicator
- Dashboard: today's income/expense/net, this month's income/expense/savings, remaining overall budget, quick actions, recent transactions
- Firestore security rules scoped to `request.auth.uid` — a user can only ever read/write their own subtree

**Explicitly deferred** (per your "core first" instruction) — none of these are faked or stubbed with dead buttons, they simply aren't built yet:
- **Hilt DI** — manual constructor-default DI is used instead (`RepositoryX()` default args), which is what lets this project open with zero extra setup. Swapping to Hilt later is a mechanical refactor, not a redesign.
- **Room database / offline-first / sync queue** — this build talks to Firestore directly. Firestore's own persistent local cache (enabled in `FirebaseModule.kt`) means reads/writes still work offline and sync when the connection returns, but there's no separate Room source-of-truth or conflict-resolution layer.
- **WorkManager / reminders / recurring transactions / bill manager** — not built.
- **PDF/CSV export, receipts (camera/gallery), biometric app lock, goals, debt/loan tracking, analytics charts** — not built.

If you want any of these added next, say which ones — building all of them at once is exactly the scope this pass was meant to avoid.

## 2. Tech stack

Kotlin, Jetpack Compose + Material 3, Navigation Compose, MVVM (manual DI), Kotlin Coroutines + Flow, Firebase Auth + Firestore (with persistent local cache).

## 3. Requirements

Android Studio Koala (2024.1.1)+, JDK 17, Android SDK 34, a Firebase project (free Spark plan is enough).

## 4. Opening the project

Unzip → Android Studio → **Open** → select the `FinanceManager` folder (contains `settings.gradle.kts`) → let Gradle sync (needs internet for the first sync).

## 5. Connect Firebase

1. [Firebase Console](https://console.firebase.google.com) → **Add project** → **Add app → Android**, package name `com.example.financemanager` (change it in `app/build.gradle.kts` too if you want your own).
2. Download `google-services.json`, place it at `app/google-services.json` (replace/delete the `.SAMPLE` placeholder).
3. **Authentication → Sign-in method** → enable **Email/Password**.
4. **Firestore Database → Create database** → production mode.
5. Deploy `firebase/firestore.rules` (Firebase CLI: `firebase init firestore` pointing at that file, then `firebase deploy --only firestore:rules`), or paste its contents into the Console's Rules editor.

## 6. Data model

```
users/{uid}                      - name, email, currency
users/{uid}/accounts/{id}        - name, type, openingBalancePaise, currentBalancePaise
users/{uid}/categories/{id}      - name, type (EXPENSE/INCOME), isDefault
users/{uid}/transactions/{id}    - type (EXPENSE/INCOME/TRANSFER), amountPaise, category, accountId...
users/{uid}/budgets/{id}         - category ("" = overall), monthKey, limitPaise
```

Money is always stored as **Long paise** (₹1 = 100 paise) — never floats — to avoid rounding drift; see `CurrencyUtils`.

## 7. How account balances stay correct

All balance math lives in one place: `TransactionRepository`. Adding an expense/income adjusts one account; a transfer adjusts two (debit one, credit the other, and is never counted as income or expense in summaries). Editing a transaction reverses its old effect and applies the new one in the same call, so changing the amount, account, or type never leaves a stale balance. See `FinanceCalculatorTest` and the repository's `effectsOf()` function if you're auditing this.

## 8. Building

```
./gradlew assembleDebug      # debug APK
./gradlew test               # runs FinanceCalculatorTest
```

The Gradle wrapper jar isn't bundled (binary file) — Android Studio regenerates it on first sync, or run `gradle wrapper` locally if you have Gradle installed.

## 9. First-build checklist

1. AGP/Kotlin/Compose-BOM version mismatches against your Android Studio version — bump in the root `build.gradle.kts` if prompted.
2. Missing/misnamed `google-services.json` — see section 5.
3. Compose API drift on `ExposedDropdownMenuBox` if you change the Compose BOM version.
4. Any Firestore query needing a composite index will fail with a Console link to auto-create it — click it.

## 10. Troubleshooting

- **Dashboard shows no categories in Add Expense:** categories are seeded once, right after first login/signup (`CategoryRepository.seedDefaultCategoriesIfEmpty`) — make sure you've logged in at least once after Firestore rules are deployed.
- **Balance looks wrong after editing a transaction:** check you're not calling `TransactionRepository.updateTransaction` with a stale `old` value — the reversal math depends on `old` being the transaction's state *before* your edit, which the UI (`AddTransactionScreen`) already threads through correctly via `existingTransaction`.
- **Budget percent stuck at 0%:** an "Overall" budget only counts total expenses; a category budget only counts that category — make sure you picked the one you meant in the Set Budget dialog.
