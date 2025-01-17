package test.taylor.com.taylorcode.ui.recyclerview.anim

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import test.taylor.com.taylorcode.kotlin.ConstraintLayout
import test.taylor.com.taylorcode.kotlin.*
import test.taylor.com.taylorcode.ui.recyclerview.variety.VarietyAdapter2

class RecyclerViewAnimActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private val overlapAdapter by lazy {
        OverlapAdapter().apply {
            addItemBuilder(ImageProxy())
        }
    }
    private val contentView by lazy {
        ConstraintLayout {
            layout_width = match_parent
            layout_height = match_parent

            rv = RecyclerView {
                layout_id = "rv"
                layout_width = 90
                layout_height = 80
                center_horizontal = true
                center_vertical = true
                adapter = overlapAdapter
                layoutManager = LinearLayoutManager(this@RecyclerViewAnimActivity).apply { orientation = LinearLayoutManager.HORIZONTAL }
                background_color = "#00ff00"
                layoutDirection = View.LAYOUT_DIRECTION_RTL
            }

            TextView {
                layout_id = "tvChange"
                layout_width = wrap_content
                layout_height = wrap_content
                textSize = 20f
                textColor = "#000000"
                text = "scroll"
                gravity = gravity_center
                top_toBottomOf = "rv"
                center_horizontal = true
                onClick = {
                    rv.smoothScrollBy((-20).dp, 0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)

        overlapAdapter.dataList = listOf(
            Image("1djfkdsf//"),
            Image("2djfkdsf//"),
            Image("3djfkdsf//"),
            Image("4djfkdsf//"),
            Image("5djfkdsf//"),
            Image("6djfkdsf//"),
            Image("7djfkdsf//"),
            Image("8djfkdsf//"),
            Image("djfkdsf//"),
            Image("djfkdsf//"),
            Image("djfkdsf//"),
        )

        val itemTransformerAdapter = ItemTransformerAdapter(rv).apply {
            overlap = 10.dp
            itemTransformer = object : ItemTransformer {
                override fun onItemTransform(firstItem: View?, lastItem: View, offset: Float) {
                    firstItem?.apply {
                        alpha = 1 - offset
                        scaleX = 1 - offset
                        scaleY = 1 - offset
                    }
                    lastItem.apply {
                        alpha = offset
                        scaleX = offset
                        scaleY = offset
                    }
                }
            }
        }
        rv.addOnScrollListener(itemTransformerAdapter)
    }
}

class OverlapAdapter : VarietyAdapter2() {

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun getIndex(position: Int): Int = position % dataList.size
}


class ImageProxy : VarietyAdapter2.ItemBuilder<Image, ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = parent.context.run {
            TextView {
                layout_id = "tvChange"
                layout_width = 30
                layout_height = wrap_content
                textSize = 20f
                textColor = "#888888"
                gravity = gravity_center
                background_color = "#0000ff"
            }
        }
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, data: Image, index: Int, action: ((Any?) -> Unit)?) {
        holder.tv?.text = data.url
        /**
         * make item overlap
         */
        holder.itemView.margin_start = if (index == 0) 0 else -10
    }

}

data class Image(var url: String)

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv = itemView.find<TextView>("tvChange")
}