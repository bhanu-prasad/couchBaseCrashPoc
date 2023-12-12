package com.appcooking.couchct_poc

import android.content.Context
import com.couchbase.lite.Collection
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import java.util.Date

class CouchbaseManager(context: Context) {
    private var database: Database

    init {
        CouchbaseLite.init(context)
        database = Database(DATABASE_NAME, DatabaseConfiguration())
        createCollection()
    }

    private fun createCollection() {
        database.createCollection("testColl")
    }

    fun getCollection(): Collection? {
        return database.getCollection("testColl")
    }

    fun insertDoc(doc: MutableDocument) {
        database.getCollection("testColl")?.apply {
            save(doc)
        }
    }

    fun removeDocs(ids: List<String>) {
        getCollection()?.apply{
            for (id in ids) {
                getDocument(id)?.let {
                    setDocumentExpiration(
                        id,
                        Date(System.currentTimeMillis() - 100000)
                    )
                }
            }
        }
    }

    fun clearDB(){
        database.deleteCollection("testColl")
        createCollection()
    }

    companion object {
        private const val DATABASE_NAME = "my_database"
    }
}