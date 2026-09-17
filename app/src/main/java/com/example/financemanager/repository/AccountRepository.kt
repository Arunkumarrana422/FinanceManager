package com.example.financemanager.repository

import com.example.financemanager.firebase.FirebaseModule
import com.example.financemanager.firebase.FirestorePaths
import com.example.financemanager.model.Account
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AccountRepository {
    private val firestore = FirebaseModule.firestore
    private fun accountsRef(uid: String) =
        firestore.collection(FirestorePaths.USERS).document(uid).collection(FirestorePaths.ACCOUNTS)

    fun observeAccounts(uid: String): Flow<List<Account>> = callbackFlow {
        val reg = accountsRef(uid).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.documents?.mapNotNull { it.toObject(Account::class.java) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    suspend fun createAccount(uid: String, name: String, type: String, openingBalancePaise: Long): Resource<Account> = try {
        val id = accountsRef(uid).document().id
        val account = Account(
            accountId = id, userId = uid, name = name, type = type,
            openingBalancePaise = openingBalancePaise, currentBalancePaise = openingBalancePaise
        )
        accountsRef(uid).document(id).set(account).await()
        Resource.Success(account)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not create account")
    }

    suspend fun deleteAccount(uid: String, accountId: String): Resource<Unit> = try {
        accountsRef(uid).document(accountId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not delete account")
    }

    /** Adjusts a single account's balance by a signed delta (paise) inside a transaction. */
    suspend fun adjustBalance(uid: String, accountId: String, deltaPaise: Long) {
        val ref = accountsRef(uid).document(accountId)
        firestore.runTransaction { txn ->
            val snapshot = txn.get(ref)
            val current = snapshot.getLong("currentBalancePaise") ?: 0L
            txn.update(ref, "currentBalancePaise", current + deltaPaise)
        }.await()
    }
}
