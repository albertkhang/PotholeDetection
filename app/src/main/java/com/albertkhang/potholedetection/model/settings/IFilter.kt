package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class IFilter {
    @SerializedName("average_milestone")
    val averageMilestone = 0.2f

    @SerializedName("bad_milestone")
    val badMilestone = 0.3f

    @SerializedName("min_speed")
    val minSpeed: Float = 1.38889f
    override fun toString(): String {
        return "IFilter(averageMilestone=$averageMilestone, badMilestone=$badMilestone, minSpeed=$minSpeed)"
    }


}