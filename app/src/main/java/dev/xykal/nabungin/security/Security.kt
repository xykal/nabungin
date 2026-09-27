package dev.xykal.nabungin.security

import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PIN disimpan sebagai PBKDF2-HMAC-SHA256 + salt acak 128-bit. Tidak pernah plaintext. */
object PinHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun newSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hash(pin: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val bytes = try {
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun verify(pin: String, saltBase64: String, expectedHash: String): Boolean {
        val actual = hash(pin, saltBase64)
        return constantTimeEquals(actual, expectedHash)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        Arrays.equals(a.toByteArray(), b.toByteArray())
}

object Biometrics {
    fun canUse(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        val result = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun prompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    onFailure("Sidik jari tidak dikenali")
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Buka Nabungin")
                .setSubtitle("Verifikasi identitas untuk lihat data tabungan")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                )
                .build(),
        )
    }
}
