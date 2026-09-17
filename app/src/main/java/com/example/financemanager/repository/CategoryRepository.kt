package com.example.financemanager.repository

import com.example.financemanager.firebase.FirebaseModule
import com.example.financemanager.firebase.FirestorePaths
import com.example.financemanager.model.Category
import com.example.financemanager.model.DefaultCategories
import com.example.financemanager.model.TransactionType
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository {
    private val firestore = FirebaseModule.firestore
    private fun categoriesRef(uid: String) =
        firestore.collection(FirestorePaths.USERS).document(uid).collection(FirestorePaths.CATEGORIES)

    fun observeCategories(uid: String): Flow<List<Category>> = callbackFlow {
        val reg = categoriesRef(uid).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.documents?.mapNotNull { it.toObject(Category::class.java) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    /** Called once after sign-up (or on first dashboard load if the list is empty) to seed defaults. */
    suspend fun seedDefaultCategoriesIfEmpty(uid: String) {
        val existing = categoriesRef(uid).limit(1).get().await()
        if (!existing.isEmpty) return

        val batch = firestore.batch()
        DefaultCategories.EXPENSE.forEach { name ->
            val id = categoriesRef(uid).document().id
            batch.set(categoriesRef(uid).document(id), Category(id, uid, name, TransactionType.EXPENSE, isDefault = true))
        }
        DefaultCategories.INCOME.forEach { name ->
            val id = categoriesRef(uid).document().id
            batch.set(categoriesRef(uid).document(id), Category(id, uid, name, TransactionType.INCOME, isDefault = true))
        }
        batch.commit().await()
    }

    suspend fun addCategory(uid: String, name: String, type: String): Resource<Category> = try {
        val id = categoriesRef(uid).document().id
        val category = Category(id, uid, name, type)
        categoriesRef(uid).document(id).set(category).await()
        Resource.Success(category)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not add category")
    }

    suspend fun deleteCategory(uid: String, categoryId: String, hasTransactions: Boolean): Resource<Unit> {
        if (hasTransactions) {
            return Resource.Error("This category has transactions. Reassign or delete them first.")
        }
        return try {
            categoriesRef(uid).document(categoryId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Could not delete category")
        }
    }
}
