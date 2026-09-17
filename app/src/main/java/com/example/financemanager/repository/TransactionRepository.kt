package com.example.financemanager.repository

import com.example.financemanager.firebase.FirebaseModule
import com.example.financemanager.firebase.FirestorePaths
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Owns both the transaction ledger AND keeping Account.currentBalancePaise correct.
 * Every add/edit/delete recomputes the account-balance delta(s) so callers never
 * have to touch AccountRepository.adjustBalance themselves — this keeps balance
 * logic in exactly one place (requirement: "no duplicated calculation logic").
 */
class TransactionRepository(
    private val accountRepository: AccountRepository = AccountRepository()
) {
    private val firestore = FirebaseModule.firestore
    private fun txRef(uid: String) =
        firestore.collection(FirestorePaths.USERS).document(uid).collection(FirestorePaths.TRANSACTIONS)

    fun observeTransactions(uid: String): Flow<List<Transaction>> = callbackFlow {
        val reg = txRef(uid)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    /** Signed effect of a transaction on each account it touches: accountId -> deltaPaise. */
    private fun effectsOf(t: Transaction): Map<String, Long> = when (t.type) {
        TransactionType.EXPENSE -> mapOf(t.accountId to -t.amountPaise)
        TransactionType.INCOME -> mapOf(t.accountId to t.amountPaise)
        TransactionType.TRANSFER -> mapOf(
            t.fromAccountId to -t.amountPaise,
            t.toAccountId to t.amountPaise
        )
        else -> emptyMap()
    }

    suspend fun addTransaction(uid: String, transaction: Transaction): Resource<Transaction> = try {
        val id = txRef(uid).document().id
        val toSave = transaction.copy(transactionId = id, userId = uid)
        txRef(uid).document(id).set(toSave).await()
        effectsOf(toSave).forEach { (accountId, delta) -> accountRepository.adjustBalance(uid, accountId, delta) }
        Resource.Success(toSave)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not save transaction")
    }

    suspend fun updateTransaction(uid: String, old: Transaction, updated: Transaction): Resource<Unit> = try {
        // Reverse the old effect, then apply the new one — handles amount/account/type changes correctly.
        val reversal = effectsOf(old).mapValues { (_, v) -> -v }
        val forward = effectsOf(updated)
        val netDeltas = mutableMapOf<String, Long>()
        (reversal.keys + forward.keys).forEach { accId ->
            if (accId.isBlank()) return@forEach
            netDeltas[accId] = (reversal[accId] ?: 0L) + (forward[accId] ?: 0L)
        }
        val toSave = updated.copy(transactionId = old.transactionId, userId = uid, updatedAt = System.currentTimeMillis())
        txRef(uid).document(old.transactionId).set(toSave).await()
        netDeltas.forEach { (accountId, delta) -> if (delta != 0L) accountRepository.adjustBalance(uid, accountId, delta) }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not update transaction")
    }

    suspend fun deleteTransaction(uid: String, transaction: Transaction): Resource<Unit> = try {
        val reversal = effectsOf(transaction).mapValues { (_, v) -> -v }
        txRef(uid).document(transaction.transactionId).delete().await()
        reversal.forEach { (accountId, delta) -> if (accountId.isNotBlank()) accountRepository.adjustBalance(uid, accountId, delta) }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not delete transaction")
    }
}
