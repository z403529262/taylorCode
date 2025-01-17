package test.taylor.com.taylorcode.ui.pagers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.viewpager2_activity.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import test.taylor.com.taylorcode.R
import test.taylor.com.taylorcode.kotlin.TextView
import test.taylor.com.taylorcode.kotlin.*
import test.taylor.com.taylorcode.kotlin.extension.onPageVisibilityChange
import test.taylor.com.taylorcode.kotlin.extension.onVisibilityChange
import test.taylor.com.taylorcode.ui.recyclerview.variety.Diff
import test.taylor.com.taylorcode.ui.recyclerview.variety.VarietyAdapter2

/**
 * ViewPager2 with views
 */
class ViewPager2Activity : AppCompatActivity() {

    private val imgUrls = mutableListOf(
        "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3030050658,3694586235&fm=26&gp=0.jpg",
        "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2571315283,182922750&fm=26&gp=0.jpg",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1591790054139&di=627d2e1d16d93f1f2fdcac074a623d39&imgtype=0&src=http%3A%2F%2Fpngimg.com%2Fuploads%2Fdonald_trump%2Fdonald_trump_PNG56.png"
    )

    private val tabTitles by lazy {
        listOf("home", "video", "audio")
    }

    private val viewPagerFlow = MutableStateFlow<List<BaseBean>?>(null)

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewpager2_activity)
        /**
         * case: ViewPager2 with views
         */
        val viewPagerAdapter = VarietyAdapter2().apply {
            addItemBuilder(ViewPagerProxy())
            addItemBuilder(ViewPagerEmptyProxy())
        }
        vp2.adapter = viewPagerAdapter
        /**
         * case: ViewPager2 with Tab
         */
        TabLayoutMediator(tablayout, vp2) { tab, position ->
            /**
             * case: TabLayout text and icon which is above title by default
             */
//            tab.text = tabTitles.getOrElse(position) { "unknown" }
//            tab.icon = selector {
//                items = mapOf(
//                    intArrayOf(state_selected) to ContextCompat.getDrawable(this@ViewPager2Activity, R.drawable.material_ic_arrow_remote_center_up),
//                    intArrayOf(state_unselected) to ContextCompat.getDrawable(
//                        this@ViewPager2Activity,
//                        R.drawable.material_ic_arrow_remote_center_down
//                    ),
//                )
//            }

            tab.customView = LinearLayout {
                layout_width = wrap_content
                orientation = horizontal
                layout_height = 60

                ImageView {
                    layout_id = "ivIcon"
                    layout_width = 18
                    layout_height = 18
                    scaleType = scale_fit_xy
                    layout_gravity = gravity_center_vertical
                    imageDrawable = selector {
                        items = mapOf(
                            intArrayOf(state_selected) to ContextCompat.getDrawable(
                                this@ViewPager2Activity,
                                R.drawable.material_ic_arrow_remote_center_down
                            ),
                            intArrayOf(state_unselected) to ContextCompat.getDrawable(
                                this@ViewPager2Activity,
                                R.drawable.material_ic_arrow_remote_center_up
                            ),
                        )
                    }
                }
                TextView {
                    layout_id = "tvChange"
                    layout_width = wrap_content
                    layout_height = wrap_content
                    layout_gravity = gravity_center_vertical
                    textSize = 14f
                    textColor = "#ffffff"
                    text = tabTitles.getOrElse(position) { "unknow" }
                    gravity = gravity_center
                }
            }
        }.attach()
        vp2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.v("ttaylor", "onPageSelected() position = $position")
//                lifecycleScope.launch { fetch(position) }
            }
        })
//        bannerViewPager()
//
//        fl.dispatchEvent = { ev ->
//            if (ev?.action == MotionEvent.ACTION_MOVE) {
//                handler.removeCallbacksAndMessages(null)
//            } else if (ev?.action == MotionEvent.ACTION_UP) {
//                handler.postDelayed(showNext, 1000)
//            }
//        }

        //load data
        lifecycleScope.launch {
            delay(500)
            viewPagerFlow.value = listOf(
                // it must be 0 ,1,2,3 which is according to the real index in adapter,or DiffUtil will consider it as remove and insert, then ViewPager will scroll automatically
                EmptyString(0, "a"),
                EmptyString(1, "b"),
                EmptyString(2, "c"),
                EmptyString(3, "d"),
                EmptyString(4, "e"),
                EmptyString(5, "f"),
                EmptyString(6, "g"),
                EmptyString(7, "h"),
                EmptyString(8, "i"),
                EmptyString(9, "j"),
            )
        }

        vp2.onPageVisibilityChange { index, isVisible ->
            Log.d("ttaylor", "ViewPager2Activity.onCreate[index($index), isVisible($isVisible)]: ")
        }

        // observe data
        lifecycleScope.launch {
            viewPagerFlow.collect { list ->
                list ?: return@collect
                val oldData = viewPagerAdapter.dataList.toMutableList()
                if (oldData.isEmpty()) {
                    viewPagerAdapter.dataList = list
                } else {
                    list.forEach { item -> oldData.set(item.id, item) }
                    viewPagerAdapter.dataList = oldData
                }
            }
        }
    }

    private suspend fun fetch(id: Int) {
        delay(1500)
        viewPagerFlow.value = listOf(EmptyString(id, "wisdomtl${id}"))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.v("ttaylor", "tag=asdf, ViewPager2Activity.onTouchEvent()  event=${event?.action}")
        return super.onTouchEvent(event)
    }


    /**
     * case3: make ViewPager auto-scroll like banner
     */
    val showNext = object : Runnable {
        override fun run() {
            vp2.currentItem = (++vp2.currentItem)
            handler.postDelayed(this, 1000)
        }
    }

    private fun bannerViewPager() {
        if (imgUrls.size <= 1) return
        handler.postDelayed(showNext, 1000)
    }
}

class ViewPagerProxy : VarietyAdapter2.ItemBuilder<DataText, ViewPagerViewHolder2>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = parent.context.run {
            TextView {
                layout_id = "tvChange"
                layout_width = match_parent
                layout_height = match_parent
                textSize = 50f
                textColor = "#888888"
                fontFamily = R.font.pingfang
                gravity = gravity_center
            }
        }
        itemView.onVisibilityChange { view, isVisible ->
            Log.d("ttaylor", "ViewPagerProxy.onCreateViewHolder[view(${view.tag}), isVisible(${isVisible})]: ")
        }
        return ViewPagerViewHolder2(itemView)
    }

    override fun onBindViewHolder(
        holder: ViewPagerViewHolder2,
        data: DataText,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.tv?.text = data.str
        holder.tv?.tag = data.str
    }

}

class DataText(idd: Int, var str: String) : BaseBean(idd)

class ViewPagerViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv = itemView.find<TextView>("tvChange")
}


class ViewPagerEmptyProxy : VarietyAdapter2.ItemBuilder<EmptyString, ViewPagerEmptyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = parent.context.run {
            ConstraintLayout {
                layout_width = match_parent
                layout_height = match_parent
                background_color = "#777777"

                TextView {
                    layout_id = "tvChange"
                    layout_width =200
                    layout_height = 100
                    textSize = 40f
                    textColor = "#00ff00"
                    fontFamily = R.font.pingfang
                    gravity = gravity_center
                }
            }
        }
        return ViewPagerEmptyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ViewPagerEmptyViewHolder,
        data: EmptyString,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.tvChange?.text = data.str
        Log.d("ttaylor", "ViewPagerProxy.onBindViewHolder[holder, data, index, action]: ")
        holder.tvChange?.onVisibilityChange { view, isShow ->
            Log.w("ttaylor", "ViewPagerProxy.onBindViewHolder[onVisibilityChange]: tv(${data.str}) isShow=${isShow} ")
        }
    }
}

class EmptyString(var idd: Int, var str: String) : BaseBean(idd), Diff {
    override fun diff(other: Any?): Any? {
        return null
    }

    override fun sameAs(other: Any?): Boolean {
        return when {
            other !is EmptyString -> false
            other.idd == this.idd -> true
            else -> false
        }
    }

    override fun contentSameAs(other: Any?): Boolean {
        return when {
            other !is EmptyString -> false
            other.str == this.str -> true
            else -> false
        }
    }
}

class ViewPagerEmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvChange = itemView.find<TextView>("tvChange")
}

open class BaseBean(var id: Int)