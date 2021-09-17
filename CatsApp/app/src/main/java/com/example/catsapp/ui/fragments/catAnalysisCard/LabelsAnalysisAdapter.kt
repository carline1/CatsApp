package com.example.catsapp.ui.fragments.catAnalysisCard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.api.models.res.ImageAnalysisResponseLabel

class LabelsAnalysisAdapter(
    private val labels: List<ImageAnalysisResponseLabel>
) : RecyclerView.Adapter<LabelsAnalysisAdapter.LabelViewHolder>() {

    override fun getItemCount(): Int {
        return labels.size
    }

    private fun getItem(position: Int): ImageAnalysisResponseLabel {
        return labels[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        return LabelViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.analysis_view_holder, parent, false))
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val analysisVhName = itemView.findViewById<TextView>(R.id.analysis_vh_name)
        private val analysisVhConfidence = itemView.findViewById<TextView>(R.id.analysis_vh_confidence)
        private val analysisVhParents = itemView.findViewById<TextView>(R.id.analysis_vh_parents)

        fun bind(item: ImageAnalysisResponseLabel?) {
            analysisVhName.text = itemView.resources.getString(R.string.colon, item?.name)

            val roundConfidence = String.format("%.2f", item?.confidence)
            analysisVhConfidence.text = roundConfidence

            if (item?.imageAnalysisResponseParents?.isNotEmpty() == true) {
                analysisVhParents.visibility = View.VISIBLE

                var parents = " "
                item.imageAnalysisResponseParents.forEach {
                    parents += it.name + ", "
                }
                parents = parents.substring(0, parents.length - 2)
                analysisVhParents.text = itemView.resources.getString(R.string.analysis_vh_parents, parents)
            }
            else {
                analysisVhParents.visibility = View.GONE
            }
        }
    }
}