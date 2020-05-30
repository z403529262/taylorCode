package test.taylor.com.taylorcode.retrofit.repository_livedata.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(news: List<News>)

    @Query("select * from news")
    fun queryNews(): LiveData<List<News>?>
}