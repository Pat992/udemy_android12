package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.lang.ArithmeticException

class MainActivity : AppCompatActivity() {
    private var tvInput: TextView? = null
    private var lastNumeric: Boolean = false
    private var lastDot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvInput = findViewById(R.id.tvInput)
    }

    fun onDigit(view: View) {
        tvInput?.append((view as Button).text)
        lastNumeric = true
        lastDot = false
    }

    fun onClear(view: View) {
        tvInput?.text = ""
        lastNumeric = false
        lastDot = false
    }

    fun onDec(view: View) {
        if(lastNumeric && !lastDot) {
            tvInput?.append(".")
            lastNumeric = false
            lastDot = true
        }
    }

    fun onOperator(view: View) {
        tvInput?.text?.let { it ->
            if(lastNumeric && !isOperatorAdded(it.toString())) {
                tvInput?.append((view as Button).text)
                lastNumeric = false
                lastDot = false
            }
        }
    }

    fun onEqual(view: View) {
        if(lastNumeric) {
            var tvValue = tvInput?.text.toString()
            var prefix = ""
            try {
                if(tvValue.startsWith("-")) {
                    prefix = "-"
                    tvValue = tvValue.substring(1)
                }
                if(tvValue.contains("-")) {
                    val splitVal = tvValue.split("-")
                    val one = "$prefix${splitVal[0]}"
                    val two = splitVal[1]
                    tvInput?.text =(one.toDouble() - two.toDouble()).toString()
                }
                else if(tvValue.contains("+")) {
                    val splitVal = tvValue.split("+")
                    val one = "$prefix${splitVal[0]}"
                    val two = splitVal[1]
                    tvInput?.text =(one.toDouble() + two.toDouble()).toString()
                }
                else if(tvValue.contains("*")) {
                    val splitVal = tvValue.split("*")
                    val one = "$prefix${splitVal[0]}"
                    val two = splitVal[1]
                    tvInput?.text =(one.toDouble() * two.toDouble()).toString()
                }
                else if(tvValue.contains("/")) {
                    val splitVal = tvValue.split("/")
                    val one = "$prefix${splitVal[0]}"
                    val two = splitVal[1]
                    tvInput?.text =(one.toDouble() / two.toDouble()).toString()
                }

            } catch(e: ArithmeticException) {
                //tvInput?.text = e.toString()
            } catch(e: Exception) {
                //tvInput?.text = e.toString()
            }
        }
    }

    private fun removeZeroAfterDot(res: String): String {
        val arrRes = res.split(".")
        return if(arrRes[1].toInt() > 0){
            res
        } else {
            arrRes[0]
        }
    }

    private fun isOperatorAdded(value: String): Boolean {
        return if(value.startsWith("-")) {
            false
        } else{
            value.contains("/")
                    || value.contains("*")
                    || value.contains("+")
                    || value.contains("-")
        }
    }
}