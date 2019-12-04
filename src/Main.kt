import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) {
    println("Hello World!")
    val client = MqttClient("tcp://192.168.99.100:1883", MqttClient.generateClientId())
    client.setCallback(MQTT_consumer())
    client.connect()

    client.subscribe("data")
}