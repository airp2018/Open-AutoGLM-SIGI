package com.autoglm.helper

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.io.ByteArrayInputStream

class HttpServer(private val service: AutoGLMAccessibilityService, port: Int = 8080) : NanoHTTPD(port) {
    
    companion object {
        private const val TAG = "AutoGLM-HttpServer"
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method
        
        Log.d(TAG, "Request: $method $uri")

        return try {
            when {
                uri == "/status" && method == Method.GET -> handleStatus()
                uri == "/screenshot" && method == Method.GET -> handleScreenshot()
                uri == "/tap" && method == Method.POST -> handleTap(session)
                uri == "/swipe" && method == Method.POST -> handleSwipe(session)
                uri == "/input" && method == Method.POST -> handleInput(session)
                uri == "/launch" && method == Method.POST -> handleLaunch(session)
                uri == "/action" && method == Method.POST -> handleAction(session)
                uri == "/switch_ime" && method == Method.POST -> handleSwitchIme(session)
                uri == "/restore_ime" && method == Method.POST -> handleRestoreIme()
                uri == "/adb_broadcast" && method == Method.POST -> handleAdbBroadcast(session)
                else -> newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    """{"error": "Not found"}"""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling request", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                """{"error": "${e.message}"}"""
            )
        }
    }

    private fun handleStatus(): Response {
        val json = JSONObject()
        json.put("status", "ok")
        json.put("service", "AutoGLM Helper")
        json.put("version", "1.0.0")
        json.put("accessibility_enabled", service.isAccessibilityEnabled())
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        )
    }

    private fun handleScreenshot(): Response {
        val screenshot = service.takeScreenshotBase64()
        
        return if (screenshot != null) {
            val json = JSONObject()
            json.put("success", true)
            json.put("image", screenshot)
            json.put("format", "base64")
            
            newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                json.toString()
            )
        } else {
            val json = JSONObject()
            json.put("success", false)
            json.put("error", "Failed to take screenshot")
            
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                json.toString()
            )
        }
    }

    private fun handleTap(session: IHTTPSession): Response {
        val body = getRequestBody(session)
        val json = JSONObject(body)
        
        val x = json.getInt("x")
        val y = json.getInt("y")
        
        val success = service.performTap(x, y)
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }

    private fun handleSwipe(session: IHTTPSession): Response {
        val body = getRequestBody(session)
        val json = JSONObject(body)
        
        val x1 = json.getInt("x1")
        val y1 = json.getInt("y1")
        val x2 = json.getInt("x2")
        val y2 = json.getInt("y2")
        val duration = json.optInt("duration", 300)
        
        val success = service.performSwipe(x1, y1, x2, y2, duration)
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }

    private fun handleInput(session: IHTTPSession): Response {
        val body = getRequestBody(session)
        val json = JSONObject(body)
        
        val text = json.getString("text")
        
        val success = service.performInput(text)
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }

    private fun handleLaunch(session: IHTTPSession): Response {
        val body = getRequestBody(session)
        val json = JSONObject(body)
        
        val packageName = json.getString("package")
        
        val success = service.launchApp(packageName)
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }

    private fun handleAction(session: IHTTPSession): Response {
        val body = getRequestBody(session)
        val json = JSONObject(body)
        
        val action = json.getString("action")
        var success = false
        
        if (action == "back") {
            success = service.performBack()
        } else if (action == "home") {
            success = service.performHome()
        }
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }

    private fun getRequestBody(session: IHTTPSession): String {
        val map = HashMap<String, String>()
        session.parseBody(map)
        return map["postData"] ?: ""
    }

    private fun handleSwitchIme(session: IHTTPSession): Response {
        val body = getRequestBody(session)
        val json = JSONObject(body)
        val ime = json.getString("ime")
        
        val success = service.switchInputMethod(ime)
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }

    private fun handleRestoreIme(): Response {
        val success = service.restoreInputMethod()
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }

    private fun handleAdbBroadcast(session: IHTTPSession): Response {
        val body = getRequestBody(session)
        val json = JSONObject(body)
        val action = json.getString("action")
        val extras = json.optJSONObject("extras")
        
        val success = service.sendAdbBroadcast(action, extras)
        
        val response = JSONObject()
        response.put("success", success)
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        )
    }
}
