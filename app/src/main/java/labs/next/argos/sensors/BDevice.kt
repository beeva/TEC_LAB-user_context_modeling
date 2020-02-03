package labs.next.argos.sensors

import android.bluetooth.BluetoothDevice

class BDevice (
    var profile: MutableList<String>,
    val device: BluetoothDevice,
    val name: String = device.name,
    val id: String = device.address
){

    fun addProfile(pfl: String){
        if (!profile.contains(pfl) && !pfl.equals("NONE")){
            profile.add(pfl)
        }
    }
}