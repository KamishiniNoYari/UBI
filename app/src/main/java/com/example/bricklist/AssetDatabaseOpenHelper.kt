import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Error

class AssetDatabaseOpenHelper(private val context: Context) {

    companion object {

        private val DB_NAME = "BrickList.db"
    }

    fun openDatabase(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(DB_NAME)


        if (!dbFile.exists()) {
            try {
                val checkDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null)

                checkDB?.close()
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }

        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
    }


    private fun copyDatabase(dbFile: File) {
        val a = context.assets.open(DB_NAME)
        val os = FileOutputStream(dbFile)

        val buffer = ByteArray(1024)
        while (a.read(buffer) > 0) {
            os.write(buffer)
            Log.d("#DB", "writing>>")
        }

        os.flush()
        os.close()
        a.close()
        Log.d("#DB", "completed..")
    }
}