package com.example.emprende360

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class FormularioActivity : AppCompatActivity() {

    private lateinit var etNombreCompleto: EditText
    private lateinit var spSemestre: Spinner
    private lateinit var spSeccion: Spinner
    private lateinit var etCodigoEstudiante: EditText
    private lateinit var spCarrera: Spinner
    private lateinit var etCodigoAcceso: EditText
    private lateinit var btnGenerar: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var correo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        etNombreCompleto = findViewById(R.id.etNombreCompleto)
        spSemestre = findViewById(R.id.spSemestre)
        spSeccion = findViewById(R.id.spSeccion)
        etCodigoEstudiante = findViewById(R.id.etCodigoEstudiante)
        spCarrera = findViewById(R.id.spCarrera)
        etCodigoAcceso = findViewById(R.id.etCodigoAcceso)
        btnGenerar = findViewById(R.id.btnGenerar)

        configurarSpinner(spSemestre, R.array.Semestre)
        configurarSpinner(spSeccion, R.array.Seccion)
        configurarSpinner(spCarrera, R.array.Carreras)

        configurarFlecha(spSemestre, R.id.spIcono1)
        configurarFlecha(spSeccion, R.id.spIcono2)
        configurarFlecha(spCarrera, R.id.spIcono3)

        db = FirebaseFirestore.getInstance()

        // Obtener el UID y el correo del intent
        userId = intent.getStringExtra("userId").toString()
        correo = intent.getStringExtra("correo").toString()

        btnGenerar.setOnClickListener {
            if (validarCampos()) {
                enviarDatosAFirestore()
            } else {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarSpinner(spinner: Spinner, arrayResId: Int) {
        ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun configurarFlecha(spinner: Spinner, iconoResId: Int) {
        val iconoFlecha = findViewById<ImageView>(iconoResId)
        iconoFlecha.setOnClickListener {
            spinner.performClick()
        }
    }

    private fun validarCampos(): Boolean {
        return etNombreCompleto.text.isNotEmpty() &&
                spSemestre.selectedItem != null &&
                spSeccion.selectedItem != null &&
                etCodigoEstudiante.text.isNotEmpty() &&
                spCarrera.selectedItem != null &&
                etCodigoAcceso.text.isNotEmpty()
    }

    private fun enviarDatosAFirestore() {
        val nombreCompleto = etNombreCompleto.text.toString()
        val semestre = spSemestre.selectedItem.toString()
        val seccion = spSeccion.selectedItem.toString()
        val codigoEstudiante = etCodigoEstudiante.text.toString()
        val carrera = spCarrera.selectedItem.toString()
        val codigoAcceso = etCodigoAcceso.text.toString()

        val estudiante = hashMapOf(
            "nombreCompleto" to nombreCompleto,
            "semestre" to semestre,
            "seccion" to seccion,
            "codigoEstudiante" to codigoEstudiante,
            "carrera" to carrera,
            "codigoAcceso" to codigoAcceso,
            "correo" to correo
        )

        db.collection("estudiantes").document(userId)
            .set(estudiante)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show()
                abrirDatosPasaporteActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al enviar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirDatosPasaporteActivity() {
        val intent = Intent(this, DatosPasaporteActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("correo", correo)
        }
        startActivity(intent)
    }
}
