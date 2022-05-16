package me.alexpetrakov.morty.common.data.network.converters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import me.alexpetrakov.morty.common.domain.model.VitalStatus

class VitalStatusConverter {

    private val jsonToVitalStatus = mapOf(
        "Alive" to VitalStatus.ALIVE,
        "Dead" to VitalStatus.DEAD,
        "unknown" to VitalStatus.UNKNOWN
    )

    private val vitalStatusToJson = mapOf(
        VitalStatus.ALIVE to "Alive",
        VitalStatus.DEAD to "Dead",
        VitalStatus.UNKNOWN to "unknown"
    )

    @FromJson
    fun fromJson(json: String): VitalStatus {
        return jsonToVitalStatus[json]
            ?: throw IllegalStateException("Unexpected vital status: $json")
    }

    @ToJson
    fun toJson(vitalStatus: VitalStatus): String {
        return vitalStatusToJson[vitalStatus]
            ?: throw IllegalStateException("Unexpected vital status: $vitalStatus")
    }
}