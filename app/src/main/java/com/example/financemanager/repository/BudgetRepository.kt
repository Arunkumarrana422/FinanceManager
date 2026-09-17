package com.example.financemanager.repository

import com.example.financemanager.firebase.FirebaseModule
import com.example.financemanager.firebase.FirestorePaths
import com.example.financemanager.model.Budget
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BudgetRepository {
    private val firestore = FirebaseModule.firestore
    private fun budgetsRef(uid: String) =
        firestore.collection(FirestorePaths.USERS).document(uid).collection(FirestorePaths.BUDGETS)

    fun observeBudgets(uid: String, monthKey: String): Flow<List<Budget>> = callbackFlow {
        val reg = budgetsRef(uid).whereEqualTo("monthKey", monthKey)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(Budget::class.java) } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun setBudget(uid: String, category: String, monthKey: String, limitPaise: Long): Resource<Unit> = try {
        // one budget doc per (category, month) — upsert by deterministic id so re-saving doesn't duplicate
        val docId = "${monthKey}_${category.ifBlank { "overall" }}"
        val budget = Budget(budgetId = docId, userId = uid, category = category, monthKey = monthKey, limitPaise = limitPaise)
        budgetsRef(uid).document(docId).set(budget).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not save budget")
    }

    suspend fun deleteBudget(uid: String, budgetId: String): Resource<Unit> = try {
        budgetsRef(uid).document(budgetId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not delete budget")
    }
}
