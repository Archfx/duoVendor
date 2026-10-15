package com.surface.touchpenservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemProperties
import android.util.Log
import vendor.surface.touchpen.V1_0.ITouchPen
import vendor.surface.touchpen.V1_3.ITouchPen
import vendor.surface.touchpen.V1_0.IPenCallback


/*
 * This service exists only for the sole purpose of allowing Duo2 to recognise this app as
 * a vendor com.surface.touchpenservice app. 
 */
class MainDummyService : Service() {
    private val TAG = "TouchPenDummyService"
    private var touchPenHal: ITouchPen? = null
    private var isDuo2: Boolean = false
    private var touchHal: ITouchPenV1_0? = null
    private var touchHalV3: ITouchPenV1_3? = null
    private var penCallback : PenCallback = null
    
    override fun onCreate() {
        super.onCreate()
        
        if (SystemProperties.get("ro.hardware", "N/A") == "surfaceduo2") {
            isDuo2 = true
        }
        
        Log.d(TAG, "Dummy Service active. Checking if TouchPen HAL is alive..")
        Init()
        
        touchpenCallback()
    }
    
    private fun touchpenCallback(){
        penCallback = PenCallback()
        if(isDuo2){
            touchHalV3?.registerPenCallback(penCallback)
            Log.d(TAG, "registered pen callback")
            return;
        }
        touchHal?.registerPenCallback(penCallback)
        Log.d(TAG, "registered pen callback")
    }
    
    private fun connectTouchHal() {
        if (isDuo2){
            touchHalV3 = ITouchPenV1_2.getService(true)
            touchHalV3?.linkToDeath(this, TOUCHPEN_HAL_DEATH_COOKIE)
            Log.d(TAG, "Successfully bound to TouchPen v1.3 HAL.")
            return
        }
        
        touchHal = ITouchPenV1_0.getService(true)
        touchHal?.linkToDeath(this, TOUCHPEN_HAL_DEATH_COOKIE)
        Log.d(TAG, "Successfully bound to TouchPen v1 HAL.")
    }

    private fun Init() {
        try{
            connectTouchHal()
            if(isDuo2){
                touchHalV3.setInteractive(true);
            }
            else{
                touchHal.setInteractive(true);
            }
        }
        catch (e : Throwable){
            Log.e(TAG, "Failed to bind to hardware interface: ${e.message}")
        }   
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    inner class PenCallback: IPenCallback() {
        override fun notifyPenDetected(bArr: ArrayList<Byte>, b: Byte) {
            //Do nothing    
        }
    }
}