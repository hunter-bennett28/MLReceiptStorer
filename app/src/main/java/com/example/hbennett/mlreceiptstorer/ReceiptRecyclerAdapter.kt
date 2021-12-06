package com.example.hbennett.mlreceiptstorer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hbennett.mlreceiptstorer.dataclasses.Receipt
import java.text.NumberFormat

class ReceiptRecyclerAdapter(
    private val context: Context,
    private val receipts: ArrayList<Receipt>
) :
    RecyclerView.Adapter<ReceiptRecyclerAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private val namesPerView = 15

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReceiptRecyclerAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_receipt_layout, parent, false) as View
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var dateText: TextView = holder.view.findViewById<TextView>(R.id.textViewDate)
        dateText.text = receipts[position].date // Set it to use the date of the receipt
        holder.view.setOnClickListener() {
            val intent: Intent = Intent(context, ViewReceiptActivity::class.java, )
            intent.putExtra("image", receipts[position].image)
            context.startActivity(intent)
        }
        val numberFormatter: NumberFormat = NumberFormat.getCurrencyInstance()

        holder.view.findViewById<TextView>(R.id.textViewTotal).text =
            numberFormatter.format(receipts[position].total) //Set it to use the total of the receipt
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = receipts.size
}