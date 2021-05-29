package group19.lab1.ratingdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = FirebaseFirestore.getInstance()
        val btn1 = findViewById<Button>(R.id.btn1)
        val rb1 = findViewById<RatingBar>(R.id.rb1)
        val avgRatingIndicator =findViewById<TextView>(R.id.avgRating)
        val drivers = db.collection("drivers")
        val driver = drivers.document("driver1")
        driver.addSnapshotListener {
                value,err->
            if(err !=null) throw err
            if (value !=null) {
                val ratingsArr = value["rating"] as ArrayList<Number>
                var avgRating = 0f
                var numTotalRatings = 0
                for(item in ratingsArr){
                    numTotalRatings++
                    avgRating = avgRating + item.toInt()
                }
                avgRating = avgRating/numTotalRatings
                rb1.rating = avgRating.toFloat()
                avgRatingIndicator.text = "%.1f".format(avgRating).toString()
            }
        }

        btn1.setOnClickListener{
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.rating_alert,null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Rate us")

            val rateBtn = mDialogView.findViewById<Button>(R.id.button1)
            val ratebar = mDialogView.findViewById<RatingBar>(R.id.ratingBar)
            var ratingValue = 0f
            val mAlertDialog = mBuilder.show()


            ratebar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                Log.d("debug", "Rating is $rating")
                rateBtn.isEnabled=true
                ratingValue = rating
            }

            rateBtn.isEnabled = false
            rateBtn.setOnClickListener {
                Log.d("debug","Sending rating to cloud")
                Toast.makeText(this,"sending data to cloud", Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()
                val driverRatings = db.collection("drivers").document("driver1")
                driverRatings.update("rating", FieldValue.arrayUnion(ratingValue))
            }
            mDialogView.findViewById<Button>(R.id.button2).setOnClickListener {
                Log.d("debug","Rating not given")
                mAlertDialog.dismiss()
            }
        }

    }
}

data class Rating(val rating:Double, val comment:String)