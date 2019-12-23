
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import java.util.concurrent.TimeUnit


class MQTT_consumer : MqttCallback {
    var processBuilder = ProcessBuilder()
    var influx: InfluxDB =InfluxDBFactory.connect("http://192.168.99.100:8086","root","root")

    var dbName="\"compteur\""
    val rpName="aRetentionPolicy"

    init{
        influx.setLogLevel(InfluxDB.LogLevel.BASIC)


    }

    override fun connectionLost(throwable: Throwable) {
        println("Connection to MQTT broker lost!")
    }

    @Throws(Exception::class)
    override fun messageArrived(s: String, mqttMessage: MqttMessage) {
        println("Message received:\n\t" + String(mqttMessage.payload))

//        val response: Pong = this.influx.ping()
//        if (response.version.equals("unknown", ignoreCase = true)) {
//            println("Error pinging server.")
//            return
//        }

        influx.query(Query("CREATE RETENTION POLICY $rpName ON $dbName DURATION 30h REPLICATION 2 SHARD DURATION 30m DEFAULT",dbName,false))
        influx.setRetentionPolicy(rpName)

        val payload = String(mqttMessage.payload).split("\t")

        val time=payload[0].toLong()
        val compteur_id=payload[1].toLong()
        val puissance=payload[2].toLong()
        val conso_tot=payload[3].toLong()



        val point=Point.measurement(compteur_id.toString())
            .time(time, TimeUnit.SECONDS)
            .addField("puissance", puissance)
            .addField("conso_tot", conso_tot)
            .build()

        influx.write(point)

    }

    override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) { // not used in this example
    }
}