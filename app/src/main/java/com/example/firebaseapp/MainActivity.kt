package com.example.firebaseapp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.firebaseapp.Const.CORRECT_ANSWERS
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mCurrentPosition: Int = 1
    private var mQuestionsList = ArrayList<Question>()

    private var mSelectedOptionPosition: Int = 0
    private var mCorrectAnswers: Int = 0

    private lateinit var TempDialog: ProgressDialog
    private lateinit var timer: CountDownTimer
    private var i: Int = 0
    private var answer: Int = 0

    private var count: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TempDialog= ProgressDialog(this@MainActivity)
        TempDialog.setMessage("Please wait...")
        TempDialog.setCancelable(false)
        TempDialog.setProgress(i)
        TempDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        TempDialog.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))

        var dbquestions= FirebaseDatabase.getInstance().reference.child("quizapp")
        var data = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mQuestionsList.clear()
                for (i in snapshot.children){

                    var id1= i.key as String
                    var id = id1.toInt()
                    var question = i.child("question").getValue() as String
                    answer = when(id){
                        1 -> 2
                        2 -> 1
                        3 -> 2
                        4 -> 1
                        else -> 1
                    }
                    val q =
                            Question(
                                    id ,
                                    question,
                                    "True",
                                    "False",
                                    answer
                            )
                    mQuestionsList.add(q)
                }
            }
        }
        dbquestions.addValueEventListener(data)
        dbquestions.addListenerForSingleValueEvent(data)
        TempDialog.show()
        timer = object: CountDownTimer(5000, 1000){
            override fun onFinish() {
                TempDialog.dismiss()
                setQuestion()
            }

            override fun onTick(millisUntilFinished: Long) {
                TempDialog.setMessage("Please wait...")
            }

        }.start()

        tvTrue.setOnClickListener {
            selectedView(tvTrue, 1)
        }

        tvFalse.setOnClickListener {
            selectedView(tvFalse, 2)
        }
        showanswer.setOnClickListener {
            val myQuestion = mQuestionsList.get(mCurrentPosition - 1)

            if(count<3){
                val newintent =
                    Intent(this@MainActivity, AnswerActivity::class.java)
                newintent.putExtra(CORRECT_ANSWERS, myQuestion!!.correctAnswer)
                startActivity(newintent)
            }
            else{
                showanswer.text = "No more"
            }
            count++
        }

        next.setOnClickListener {
            if (mSelectedOptionPosition == 0) {
                mCurrentPosition++

                when {

                    mCurrentPosition <= mQuestionsList!!.size -> {
                        setQuestion()
                    }
                    else -> {
                        val intent =
                            Intent(this@MainActivity, ResultActivity::class.java)
                        intent.putExtra(CORRECT_ANSWERS, mCorrectAnswers)
                        startActivity(intent)
                        finish()
                        // END
                    }
                }
            } else {
                val question = mQuestionsList.get(mCurrentPosition - 1)

                if (question!!.correctAnswer == mSelectedOptionPosition) {
                    mCorrectAnswers++
                }

                if (mCurrentPosition == mQuestionsList!!.size) {
                    next.text = "FINISH"
                } else {
                    next.text = "Next"
                }

                mSelectedOptionPosition = 0
            }
        }
    }

    private fun Long.toIntOrNull(): Int? {
        return if (this < Int.MIN_VALUE || this > Int.MAX_VALUE) {
            null
        } else {
            this.toInt()
        }
    }
    private fun setQuestion() {
        val question = mQuestionsList!!.get(mCurrentPosition - 1) // Getting the question from the list with the help of current position.

        defaultView(android.R.color.darker_gray)

        if (mCurrentPosition == mQuestionsList!!.size) {
            next.text = "FINISH"
        }

        tvQuestion.text = question.question
        tvTrue.text = question.optionOne
        tvFalse.text = question.optionTwo
    }

    private fun selectedView(tv: TextView, selectedOptionNum: Int) {
        defaultView(android.R.color.darker_gray)
        mSelectedOptionPosition = selectedOptionNum
    }

    private fun defaultView(drawableView: Int) {

        val options = ArrayList<TextView>()
        options.add(0, tvTrue)
        options.add(1, tvFalse)

        for (option in options) {
            ViewCompat.setBackgroundTintList(option, ContextCompat.getColorStateList(this, drawableView))
        }
    }


    }

