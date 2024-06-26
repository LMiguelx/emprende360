package com.example.emprende360

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import android.os.CountDownTimer

class CuestionarioActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var timerProgress: ProgressBar
    private var totalTime = 25 * 1000 // Tiempo total en milisegundos (25 segundos)

    private lateinit var lblPregunta: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioBtn1: RadioButton
    private lateinit var radioBtn2: RadioButton
    private lateinit var radioBtn3: RadioButton
    private lateinit var radioBtn4: RadioButton
    private lateinit var btnResponder: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnSalir: Button
    private lateinit var timerText: TextView
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var db: FirebaseFirestore
    private lateinit var cuestionariosRef: CollectionReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var codigoAcceso: String
    private var listaCuestionarios = mutableListOf<Map<String, Any>>()
    private var indexPreguntaActual = 0
    private var puntosCuestionarioActual = 0
    private var totalPuntosAcumulados = 0
    private var cuestionarioCompletado = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuestionario)

        // Inicialización de vistas
        lblPregunta = findViewById(R.id.lblPregunta)
        radioGroup = findViewById(R.id.radioGroup)
        radioBtn1 = findViewById(R.id.radioBtn1)
        radioBtn2 = findViewById(R.id.radioBtn2)
        radioBtn3 = findViewById(R.id.radioBtn3)
        radioBtn4 = findViewById(R.id.radioBtn4)
        btnResponder = findViewById(R.id.btnResponder)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        btnSalir = findViewById(R.id.btnSalir)

        // Inicialización de Firebase Firestore
        db = FirebaseFirestore.getInstance()
        cuestionariosRef = db.collection("cuestionarios")

        // Inicialización de SharedPreferences
        sharedPreferences = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        codigoAcceso = sharedPreferences.getString("codigoAcceso", "") ?: ""

        // Obtener el id_cuestionario enviado desde la actividad anterior
        val idCuestionario = intent.getStringExtra("eventId")
        if (idCuestionario != null) {
            obtenerCuestionarios(idCuestionario)
        } else {
            Log.e(TAG, "Error: No se recibió el id_cuestionario desde la actividad anterior")
            finish()
        }

        // Listener para el botón Responder
        btnResponder.setOnClickListener {
            validarRespuesta()
        }

        // Listener para el botón Siguiente Pregunta
        btnSiguiente.setOnClickListener {
            mostrarSiguientePregunta()
        }

        // Listener para el botón Salir del Cuestionario
        btnSalir.setOnClickListener {
            finish()
        }

        // Verificar si el cuestionario ya fue completado previamente
        verificarCuestionarioCompletado()

        // Initialize ProgressBar
        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = 0

        timerText = findViewById(R.id.timer_text)
        timerProgress = findViewById(R.id.timer_progress)
    }

    private fun iniciarTemporizador() {
        countDownTimer = object : CountDownTimer(totalTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerText.text = secondsRemaining.toString()
                timerProgress.progress = (25 - secondsRemaining.toInt()).toInt()
            }

            override fun onFinish() {
                timerText.text = "0"
                timerProgress.progress = 25
                mostrarSiguientePregunta()
            }
        }.start()
    }

    private fun mostrarSiguientePregunta() {
        // Cancelar el contador de tiempo actual
        countDownTimer.cancel()

        // Avanzar al índice de la siguiente pregunta
        indexPreguntaActual++

        // Update progress bar based on the current question
        progressBar.progress = ((indexPreguntaActual + 1).toFloat() / listaCuestionarios.size * 100).toInt()
        findViewById<TextView>(R.id.progress_text).text = "${indexPreguntaActual + 1}/${listaCuestionarios.size}"

        // Mostrar la siguiente pregunta si no se ha llegado al final
        if (indexPreguntaActual < listaCuestionarios.size) {
            mostrarPregunta(indexPreguntaActual)
            iniciarTemporizador() // Iniciar el temporizador para la nueva pregunta
            btnResponder.isEnabled = true // Habilitar el botón Responder
        } else {
            // Mostrar el botón de salir al completar todas las preguntas
            btnSalir.visibility = View.VISIBLE
            cuestionarioCompletado = true

            // Update the progress bar to 100%
            progressBar.progress = 100
            findViewById<TextView>(R.id.progress_text).text = "${listaCuestionarios.size}/${listaCuestionarios.size}"
            // Al finalizar todas las preguntas, enviar los puntos acumulados a Firestore
            actualizarPuntosFirestore()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel() // Importante cancelar el contador al destruir la actividad
    }

    private fun obtenerCuestionarios(idCuestionario: String) {
        cuestionariosRef.whereEqualTo("id_cuestionario", idCuestionario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    listaCuestionarios.add(document.data)
                }
                if (listaCuestionarios.isNotEmpty()) {
                    listaCuestionarios.shuffle()
                    listaCuestionarios = listaCuestionarios.take(5).toMutableList() // Limit to 5 questions
                    mostrarPregunta(indexPreguntaActual)
                    iniciarTemporizador() // Iniciar el temporizador para la primera pregunta
                } else {
                    Log.d(TAG, "No se encontraron cuestionarios con id_cuestionario: $idCuestionario")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al obtener cuestionarios: $exception")
            }
    }

    private fun mostrarPregunta(index: Int) {
        val cuestionario = listaCuestionarios[index]
        lblPregunta.text = cuestionario["pregunta"].toString()

        // Mostrar las opciones del cuestionario
        val opciones = cuestionario["opciones"] as Map<String, String>
        val respuestas = opciones.values.toMutableList()

        // Obtener la respuesta correcta y agregarla como una de las opciones
        val respuestaCorrecta = cuestionario["respuesta"].toString()
        respuestas.add(respuestaCorrecta)

        // Asignar las opciones a los RadioButtons de forma aleatoria
        respuestas.shuffle()
        radioBtn1.text = respuestas[0]
        radioBtn2.text = respuestas[1]
        radioBtn3.text = respuestas[2]
        radioBtn4.text = respuestas[3]

        // Limpiar selección previa del RadioGroup
        radioGroup.clearCheck()

        // Ocultar el botón Siguiente si es la última pregunta
        btnSiguiente.visibility = if (index < listaCuestionarios.size - 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun validarRespuesta() {
        val selectedRadioButton = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        val respuestaSeleccionada = selectedRadioButton.text.toString()
        val respuestaCorrecta = listaCuestionarios[indexPreguntaActual]["respuesta"].toString()

        if (selectedRadioButton.tag == null) {
            selectedRadioButton.tag = false
        }

        if (respuestaSeleccionada == respuestaCorrecta) {
            // Verificar si la respuesta ya fue marcada como correcta
            if (!(selectedRadioButton.tag as Boolean)) {
                // Respuesta correcta: Incrementar puntos
                puntosCuestionarioActual++
                selectedRadioButton.tag = true // Marcar la respuesta como correcta
            }
        }

        // Cancelar el contador de tiempo actual
        countDownTimer.cancel()

        // Deshabilitar el botón Responder
        btnResponder.isEnabled = false

        // Mostrar automáticamente la siguiente pregunta
        mostrarSiguientePregunta()
    }

    private fun actualizarPuntosFirestore() {
        // Actualizar puntos acumulados
        totalPuntosAcumulados += puntosCuestionarioActual

        // Actualizar o crear documento de estudiante en Firestore
        db.collection("estudiantes")
            .document(codigoAcceso)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // El documento del estudiante existe, actualizar puntos_cuestionario
                    val puntosAnteriores = documentSnapshot.getLong("puntos_cuestionario") ?: 0
                    val nuevosPuntos = puntosAnteriores + puntosCuestionarioActual

                    db.collection("estudiantes")
                        .document(codigoAcceso)
                        .update("puntos_cuestionario", nuevosPuntos)
                        .addOnSuccessListener {
                            Log.d(TAG, "Puntos actualizados en Firestore")
                            // Navegar a DetalleCuestionarioActivity después de actualizar los puntos
                            navegarADetalleCuestionario()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al actualizar puntos: $e")
                        }
                } else {
                    // El documento del estudiante no existe, crear uno nuevo
                    val nuevoEstudiante = hashMapOf(
                        "codigoAcceso" to codigoAcceso,
                        "puntos_cuestionario" to puntosCuestionarioActual
                    )

                    db.collection("estudiantes")
                        .document(codigoAcceso)
                        .set(nuevoEstudiante)
                        .addOnSuccessListener {
                            Log.d(TAG, "Nuevo estudiante creado con puntos")
                            // Navegar a DetalleCuestionarioActivity después de crear el nuevo estudiante
                            navegarADetalleCuestionario()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al crear nuevo estudiante: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al verificar existencia de estudiante: $e")
            }
    }

    private fun navegarADetalleCuestionario() {
        val intent = Intent(this, DetalleCuestionarioActivity::class.java)
        intent.putExtra("puntosAcumulados", totalPuntosAcumulados)
        intent.putExtra("listaCuestionarios", ArrayList(listaCuestionarios)) // Pasar la lista de cuestionarios
        startActivity(intent)
        finish()
    }

    private fun iniciarCuestionario() {
        indexPreguntaActual = 0
        puntosCuestionarioActual = 0
        cuestionarioCompletado = false
        totalPuntosAcumulados = 0 // Reiniciar puntos acumulados
        mostrarPregunta(indexPreguntaActual)
        iniciarTemporizador() // Iniciar el temporizador para la primera pregunta
    }

    private fun verificarCuestionarioCompletado() {
        // Implementa la lógica necesaria para verificar si el cuestionario está completado
        // Puedes establecer la lógica para cuestionarioCompletado según tu flujo de la aplicación
    }

    companion object {
        private const val TAG = "CuestionarioActivity"
    }
}
