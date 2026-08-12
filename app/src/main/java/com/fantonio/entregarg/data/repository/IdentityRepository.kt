package com.fantonio.entregarg.data.repository

import com.fantonio.entregarg.data.local.IdentityDao
import com.fantonio.entregarg.data.model.Identity

class IdentityRepository(private val identityDao: IdentityDao) {
    suspend fun search(query: String) = identityDao.searchIdentities(query)
    
    suspend fun update(identity: Identity) = identityDao.updateIdentity(identity)
    
    suspend fun insertAll(identities: List<Identity>) = identityDao.insertAll(identities)

    suspend fun getAll() = identityDao.getAllList()
    
    suspend fun clearAll() = identityDao.deleteAll()
}
