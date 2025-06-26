package com.example.nvpbudgetapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class GoalActivity : AppCompatActivity() {

    private lateinit var etGoalTitle: EditText
    private lateinit var etGoalAmount: EditText
    private lateinit var btnPickDate: Button
    private lateinit var btnSaveGoal: Button
    private lateinit var listGoals: ListView
    private lateinit var btnBackToDashboard: Button

    private lateinit var database: DatabaseReference
    private val goals = mutableListOf<Goal>()
    private lateinit var adapter: ArrayAdapter<String>

    private var selectedDate: Long = 0L
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        // Initialize UI components
        etGoalTitle = findViewById(R.id.etGoalTitle)
        etGoalAmount = findViewById(R.id.etGoalAmount)
        btnPickDate = findViewById(R.id.btnPickDate)
        btnSaveGoal = findViewById(R.id.btnSaveGoal)
        listGoals = findViewById(R.id.listGoals)
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard)

        // Firebase reference
        database = FirebaseDatabase.getInstance().getReference("goals")

        // Date picker dialog
        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedDate = cal.timeInMillis
                    btnPickDate.text = dateFormatter.format(Date(selectedDate))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save goal to Firebase
        btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        // Go back to dashboard
        btnBackToDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        // Load existing goals
        loadGoalsFromFirebase()

        // Mark goal as achieved on click
        listGoals.setOnItemClickListener { _, _, position, _ ->
            val goal = goals[position]
            if (!goal.achieved && goal.id != null) {
                database.child(goal.id!!).child("achieved").setValue(true)
                Toast.makeText(this, "Marked as achieved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveGoal() {
        val title = etGoalTitle.text.toString().trim()
        val amountStr = etGoalAmount.text.toString().trim()

        if (title.isEmpty() || amountStr.isEmpty() || selectedDate == 0L) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val goalId = database.push().key ?: return
        val goal = Goal(goalId, title, amount, selectedDate, achieved = false)

        database.child(goalId).setValue(goal).addOnSuccessListener {
            Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show()
            clearFields()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        etGoalTitle.text.clear()
        etGoalAmount.text.clear()
        selectedDate = 0L
        btnPickDate.text = "Pick Deadline"
    }

    private fun loadGoalsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                goals.clear()
                for (item in snapshot.children) {
                    val goal = item.getValue(Goal::class.java)
                    if (goal != null) {
                        goal.id = item.key
                        goals.add(goal)
                    }
                }

                val goalStrings = goals.map { goal ->
                    val formattedDate = dateFormatter.format(Date(goal.dueDate))
                    val status = if (goal.achieved) "✅ Achieved" else "⏳ In Progress"
                    "${goal.title} - R${goal.amount}\nDue: $formattedDate\nStatus: $status"
                }

                adapter = ArrayAdapter(this@GoalActivity, android.R.layout.simple_list_item_1, goalStrings)
                listGoals.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GoalActivity, "Failed to load goals", Toast.LENGTH_SHORT).show()
            }
        })
    }
}