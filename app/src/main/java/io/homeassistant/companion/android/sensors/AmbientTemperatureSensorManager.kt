package io.homeassistant.companion.android.sensors

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import io.homeassistant.companion.android.domain.integration.SensorRegistration
import kotlin.math.roundToInt

class AmbientTemperatureSensorManager : SensorManager, SensorEventListener {
    companion object {

        private const val TAG = "AmbientTemperatureSM"
        private val ambientTempSensor = SensorManager.BasicSensor(
            "ambient_temperature_sensor",
            "sensor",
            "Ambient Temperature Sensor",
            "temperature",
            "°C"
        )
        private var tempReading: String = "unavailable"
        lateinit var mySensorManager: android.hardware.SensorManager
    }

    override val name: String
        get() = "Ambient Temperature Sensors"

    override val availableSensors: List<SensorManager.BasicSensor>
        get() = listOf(ambientTempSensor)

    override fun requiredPermissions(): Array<String> {
        return emptyArray()
    }

    override fun getSensorData(
        context: Context,
        sensorId: String
    ): SensorRegistration<Any> {
        return when (sensorId) {
            ambientTempSensor.id -> getAmbientTempSensor(context)
            else -> throw IllegalArgumentException("Unknown sensorId: $sensorId")
        }
    }

    private fun getAmbientTempSensor(context: Context): SensorRegistration<Any> {

        mySensorManager = context.getSystemService(SENSOR_SERVICE) as android.hardware.SensorManager

        val tempSensors = mySensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (tempSensors != null) {
            mySensorManager.registerListener(
                this,
                tempSensors,
                SENSOR_DELAY_NORMAL)
        }

        val icon = "mdi:thermometer"

        return ambientTempSensor.toSensorRegistration(
            tempReading,
            icon,
            mapOf()
        )
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Nothing happening here but we are required to call onAccuracyChanged for sensor events
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                tempReading = event.values[0].roundToInt().toString()
            }
        }
        mySensorManager.unregisterListener(this)
    }
}
