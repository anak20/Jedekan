package com.example.papb_pa.Game

import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.papb_pa.R
import com.example.papb_pa.data.Pesan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_gambar.view.*
import kotlinx.android.synthetic.main.view_color_palette.view.*
import kotlinx.android.synthetic.main.view_gambar.view.*
import java.io.ByteArrayOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [gambar.newInstance] factory method to
 * create an instance of this fragment.
 */
class gambar : Fragment() {
    // TODO: Rename and change types of parameters
    private var id : String? = null
    private  var code : String? = null
    var onDraw : Boolean = true
    private lateinit var viewF : View
    private val database = FirebaseDatabase.getInstance("https://jedekan-gambar-default-rtdb.asia-southeast1.firebasedatabase.app/")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_PARAM1)
            code = it.getString(ARG_PARAM2)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        onDraw = if (hidden){
            viewF.draw_view.clearCanvas()
            false
        }else{
            true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewF = inflater.inflate(R.layout.fragment_gambar, container, false)
        setUpDrawTools(viewF)
        colorSelector(viewF)
        setPaintAlpha(viewF)
        setPaintWidth(viewF)
        var ref = database.reference.child("room").child(code.toString())
        var listener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var numb : String = ""
                var soal : String = ""
                var timer : String = "0"
                numb = snapshot.child("numb").value.toString()
                timer = snapshot.child("timer").value.toString()
                if (numb != "null")
                    soal = snapshot.child("soal").child((Integer.parseInt(numb)-1).toString()).value.toString()
                if (soal != "null")
                    viewF.soal_gambar.text = soal
                if(timer != "null")
                    viewF.timer_gambar.progress = Integer.parseInt(timer)
                if (onDraw && viewF.draw_view.height > 0){
                    uploadGambar(convert(viewF.draw_view.getBitmap()))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        ref.addValueEventListener(listener)
        getPesan(viewF)
        return viewF
    }

    private fun uploadGambar(string: String) {
        database.reference.child("room").child(code.toString()).child("gambar").setValue(string)
    }

    private fun getPesan(view: View){
        var pesan = arrayListOf<Pesan>()
        val pesanAdapter = PesanAdapter(pesan)
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                pesan.clear()
                for (userSnapshot in dataSnapshot.children) {
                    pesan.add(
                            Pesan(
                                    userSnapshot.child("id").value.toString(),
                                    userSnapshot.child("jeneng").value.toString(),
                                    userSnapshot.child("pesan").value.toString()
                            )
                    )
                }
                pesanAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(view.context, "Fail to Load Data", Toast.LENGTH_SHORT).show()
            }
        }
        val ref = database.reference.child("room").child(code.toString()).child("pesan")
        ref.addValueEventListener(postListener)
        view.rv_pesan_gambar.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = pesanAdapter
        }
    }

    private fun setUpDrawTools(view : View) {
        view.circle_view_opacity.setCircleRadius(100f)
        view.image_draw_eraser.setOnClickListener {
            view.draw_view.toggleEraser()
            view.image_draw_eraser.isSelected = view.draw_view.isEraserOn
            toggleDrawTools(view.draw_tools, false)
        }
        view.image_draw_eraser.setOnLongClickListener {
            view.draw_view.clearCanvas()
            toggleDrawTools(view.draw_tools, false)
            true
        }
        view.image_draw_width.setOnClickListener {
            if (view.draw_tools.translationY == (56).toPx) {
                toggleDrawTools(view.draw_tools, true)
            } else if (view.draw_tools.translationY == (0).toPx && view.seekBar_width.visibility == View.VISIBLE) {
                toggleDrawTools(view.draw_tools, false)
            }
            view.circle_view_width.visibility = View.VISIBLE
            view.circle_view_opacity.visibility = View.GONE
            view.seekBar_width.visibility = View.VISIBLE
            view.seekBar_opacity.visibility = View.GONE
            view.draw_color_palette.visibility = View.GONE
        }
        view.image_draw_opacity.setOnClickListener {
            if (view.draw_tools.translationY == (56).toPx) {
                toggleDrawTools(view.draw_tools, true)
            } else if (view.draw_tools.translationY == (0).toPx && view.seekBar_opacity.visibility == View.VISIBLE) {
                toggleDrawTools(view.draw_tools, false)
            }
            view.circle_view_width.visibility = View.GONE
            view.circle_view_opacity.visibility = View.VISIBLE
            view.seekBar_width.visibility = View.GONE
            view.seekBar_opacity.visibility = View.VISIBLE
            view.draw_color_palette.visibility = View.GONE
        }
        view.image_draw_color.setOnClickListener {
            if (view.draw_tools.translationY == (56).toPx) {
                toggleDrawTools(view.draw_tools, true)
            } else if (view.draw_tools.translationY == (0).toPx && view.draw_color_palette.visibility == View.VISIBLE) {
                toggleDrawTools(view.draw_tools, false)
            }
            view.circle_view_width.visibility = View.GONE
            view.circle_view_opacity.visibility = View.GONE
            view.seekBar_width.visibility = View.GONE
            view.seekBar_opacity.visibility = View.GONE
            view.draw_color_palette.visibility = View.VISIBLE
        }
        view.image_draw_undo.setOnClickListener {
            view.draw_view.undo()
            toggleDrawTools(view.draw_tools, false)
        }
        view.image_draw_redo.setOnClickListener {
            view.draw_view.redo()
            toggleDrawTools(view.draw_tools, false)
        }
    }

    private fun toggleDrawTools(view: View, showView: Boolean = true) {
        if (showView) {
            view.animate().translationY((0).toPx)
        } else {
            view.animate().translationY((56).toPx)
        }
    }

    private fun colorSelector(view : View) {
        view.image_color_black.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_black, null)
            view.draw_view.setColor(color)
            view.circle_view_opacity.setColor(color)
            view.circle_view_width.setColor(color)
            scaleColorView(view, it.image_color_black)
        }
        view.image_color_red.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_red, null)
            view.draw_view.setColor(color)
            view.circle_view_opacity.setColor(color)
            view.circle_view_width.setColor(color)
            scaleColorView(view,it.image_color_red)
        }
        view.image_color_yellow.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_yellow, null)
            view.draw_view.setColor(color)
            view.circle_view_opacity.setColor(color)
            view.circle_view_width.setColor(color)
            scaleColorView(view,it.image_color_yellow)
        }
        view.image_color_green.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_green, null)
            view.draw_view.setColor(color)
            view.circle_view_opacity.setColor(color)
            view.circle_view_width.setColor(color)
            scaleColorView(view,it.image_color_green)
        }
        view.image_color_blue.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_blue, null)
            view.draw_view.setColor(color)
            view.circle_view_opacity.setColor(color)
            view.circle_view_width.setColor(color)
            scaleColorView(view,it.image_color_blue)
        }
        view.image_color_pink.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_pink, null)
            view.draw_view.setColor(color)
            view.circle_view_opacity.setColor(color)
            view.circle_view_width.setColor(color)
            scaleColorView(view,it.image_color_pink)
        }
        view.image_color_brown.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_brown, null)
            view.draw_view.setColor(color)
            view.circle_view_opacity.setColor(color)
            view.circle_view_width.setColor(color)
            scaleColorView(view, it.image_color_brown)
        }
    }

    private fun scaleColorView(view: View, viewSelect : View) {
        //reset scale of all views
        view.image_color_black.scaleX = 1f
        view.image_color_black.scaleY = 1f

        view.image_color_red.scaleX = 1f
        view.image_color_red.scaleY = 1f

        view.image_color_yellow.scaleX = 1f
        view.image_color_yellow.scaleY = 1f

        view.image_color_green.scaleX = 1f
        view.image_color_green.scaleY = 1f

        view.image_color_blue.scaleX = 1f
        view.image_color_blue.scaleY = 1f

        view.image_color_pink.scaleX = 1f
        view.image_color_pink.scaleY = 1f

        view.image_color_brown.scaleX = 1f
        view.image_color_brown.scaleY = 1f

        //set scale of selected view
        viewSelect.scaleX = 1.5f
        viewSelect.scaleY = 1.5f
    }

    private fun setPaintWidth(view : View) {
        view.seekBar_width.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                view.draw_view.setStrokeWidth(progress.toFloat())
                view.circle_view_width.setCircleRadius(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setPaintAlpha(view : View) {
        view.seekBar_opacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                view.draw_view.setAlpha(progress)
                view.circle_view_opacity.setAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private val Int.toPx: Float
        get() = (this * Resources.getSystem().displayMetrics.density)

    private fun convert(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment gambar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            gambar().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}