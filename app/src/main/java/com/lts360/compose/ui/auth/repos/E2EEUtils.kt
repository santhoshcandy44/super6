package com.lts360.compose.ui.auth.repos

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec


fun generateRSAKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")

    // Set up the Keystore KeyGenParameterSpec
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        "e2ee_key_alias",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setKeySize(2048)  // RSA key size
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)  // Use OAEP padding
        .setDigests(KeyProperties.DIGEST_SHA256)  // Ensure SHA-256 is the digest used
        .build()

    keyPairGenerator.initialize(keyGenParameterSpec)
    return keyPairGenerator.generateKeyPair() // RSA Key Pair is now generated and stored in Keystore
}

// Generate a random AES key
fun generateAESKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(256)  // 256 bits AES key size
    return keyGenerator.generateKey()

}


fun encryptMessage(originalMessage: String, encodedPublicKey: String): String {

    // Step 1: Generate a random AES key for encrypting the message
    val aesKey = generateAESKey()

    // Step 2: Encrypt the message using AES
    val encryptedMessage = encryptMessageAES(originalMessage, aesKey)

    // Step 3: Encrypt the AES key with the recipient's RSA public key
    val recipientPublicKey = decodeBase64ToPublicKey(encodedPublicKey)
    val encryptedAESKey = encryptAESKeyWithRSA(aesKey, recipientPublicKey)

    // Step 4: Return a string that contains both the encrypted AES key and the encrypted message
    return "$encryptedAESKey:$encryptedMessage"
}


// Encrypt the message using AES-GCM
fun encryptMessageAES(message: String, aesKey: SecretKey): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    // Generate a random 12-byte IV
    val iv = ByteArray(12)
    SecureRandom().nextBytes(iv)

    val gcmParameterSpec = GCMParameterSpec(128, iv) // 128-bit authentication tag length

    cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec)

    // Encrypt the message
    val encryptedMessage = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

    // Combine IV and encrypted message, then return it (Base64 encoded for transmission)
    val encryptedData = iv + encryptedMessage
    return Base64.encodeToString(encryptedData, Base64.NO_WRAP)
}

// Encrypt the AES key using RSA (recipient's public key)


fun encryptAESKeyWithRSA(aesKey: SecretKey, recipientPublicKey: PublicKey): String {
    val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding")

    // Setup OAEP parameters (SHA-256 with MGF1 and SHA-1)
    val oaepSpec = OAEPParameterSpec(
        "SHA-256", // Hash algorithm for the OAEP padding
        "MGF1",    // Mask Generation Function
        MGF1ParameterSpec.SHA1,  // MGF1 with SHA-1
        PSource.PSpecified.DEFAULT // Default PSource
    )

    rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey, oaepSpec)

    // Encrypt the AES key with RSA

    return Base64.encodeToString(rsaCipher.doFinal(aesKey.encoded), Base64.NO_WRAP)
}


fun encryptAESKeyWithRSAFile(aesKey: SecretKey, recipientPublicKey: PublicKey): ByteArray {
    val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding")

    // Setup OAEP parameters (SHA-256 with MGF1 and SHA-1)
    val oaepSpec = OAEPParameterSpec(
        "SHA-256", // Hash algorithm for the OAEP padding
        "MGF1",    // Mask Generation Function
        MGF1ParameterSpec.SHA1,  // MGF1 with SHA-1
        PSource.PSpecified.DEFAULT // Default PSource
    )

    rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey, oaepSpec)

    // Encrypt the AES key with RSA
    return rsaCipher.doFinal(aesKey.encoded)  // Returning raw byte data, not Base64 encoded
}

// Decrypt the AES key using RSA (recipient's private key)
fun decryptAESKeyWithRSA(encryptedAESKey: String, privateKey: PrivateKey): SecretKey {

    // Decode the Base64-encoded encrypted AES key
    val encryptedAESKeyBytes = Base64.decode(encryptedAESKey, Base64.NO_WRAP)

    val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding")

    // Setup OAEP parameters (SHA-256 with MGF1 and SHA-1)
    val oaepSpec = OAEPParameterSpec(
        "SHA-256",  // Hash algorithm for the OAEP padding
        "MGF1",     // Mask Generation Function
        MGF1ParameterSpec.SHA1,  // MGF1 with SHA-1
        PSource.PSpecified.DEFAULT  // Default PSource
    )

    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepSpec)

    // Decrypt the AES key with RSA
    val aesKeyBytes = rsaCipher.doFinal(encryptedAESKeyBytes)

    // Return the decrypted AES key as a SecretKey
    return SecretKeySpec(aesKeyBytes, "AES")

}


// Decrypt the message using AES-GCM
fun decryptMessageAES(encryptedData: String, aesKey: SecretKey): String {
    // Decode the Base64-encoded encrypted data
    val encryptedDataBytes = Base64.decode(encryptedData, Base64.NO_WRAP)

    // Extract the IV and encrypted message
    val iv = encryptedDataBytes.copyOfRange(0, 12)
    val encryptedMessage = encryptedDataBytes.copyOfRange(12, encryptedDataBytes.size)

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    // Initialize AES cipher with the decrypted AES key and the extracted IV
    val gcmParameterSpec = GCMParameterSpec(128, iv)
    cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmParameterSpec)

    // Decrypt the message
    val decryptedMessage = cipher.doFinal(encryptedMessage)

    // Return the decrypted message as a String
    return String(decryptedMessage, Charsets.UTF_8)
}


fun getPrivateKeyFromKeystore(): PrivateKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)

    // Check if the private key is available
    val privateKey = keyStore.getKey("e2ee_key_alias", null) as PrivateKey

    return privateKey
}


fun decodeBase64ToPublicKey(base64PublicKey: String): PublicKey {

    // Decode the Base64 string into byte array
    val decodedBytes = Base64.decode(base64PublicKey, Base64.DEFAULT)

    // Convert the byte array to PublicKey using X509EncodedKeySpec
    val keyFactory = KeyFactory.getInstance("RSA")
    val keySpec = X509EncodedKeySpec(decodedBytes)
    return keyFactory.generatePublic(keySpec)
}


// Full process to decrypt the message using AES and RSA
fun decryptMessage(encryptedData: String): DecryptionStatus {

    try {
        // Step 1: Split the encrypted data into the encrypted AES key and the encrypted message
        val (encryptedAESKeyBase64, encryptedMessageBase64) = encryptedData.split(":")

        // Step 2: The recipient decrypts the AES key using their private RSA key
        val privateKey = getPrivateKeyFromKeystore()  // Get the private key from the Keystore
        val decryptedAESKey = decryptAESKeyWithRSA(encryptedAESKeyBase64, privateKey)

        // Step 3: The recipient decrypts the message using the decrypted AES key
        val decryptedMessage = decryptMessageAES(encryptedMessageBase64, decryptedAESKey)

        // Return the successful decryption result
        return DecryptionStatus.Success(decryptedMessage)

    } catch (e: IllegalBlockSizeException) {
        e.printStackTrace()
        // Use DecryptionFailed when an error occurs, such as when the message is signed with an old public key
        return DecryptionStatus.DecryptionFailed
    }
}


/*

fun encryptFile(inputBytes: ByteArray, publicKeyBase64: String): ByteArray {
    // Step 1: Generate a random AES key for encrypting the byte array
    val aesKey = generateAESKey()

    // Step 2: Encrypt the byte array using AES
    val encryptedByteArray = encryptByteArrayAES(inputBytes, aesKey)

    // Step 3: Encrypt the AES key with the recipient's RSA public key
    val recipientPublicKey = decodeBase64ToPublicKey(publicKeyBase64)
    val encryptedAESKey = encryptAESKeyWithRSAFile(aesKey, recipientPublicKey)

    // Step 4: Combine the encrypted AES key and encrypted file data as a byte array
    val combinedData = ByteArray(encryptedAESKey.size + encryptedByteArray.size)
    System.arraycopy(encryptedAESKey, 0, combinedData, 0, encryptedAESKey.size)
    System.arraycopy(
        encryptedByteArray,
        0,
        combinedData,
        encryptedAESKey.size,
        encryptedByteArray.size
    )

    // Return the combined byte array (no Base64 encoding)
    return combinedData
}

*/

//fun encryptFile(
//    inputStream: InputStream,
//    outputStream: OutputStream,
//    publicKeyBase64: String
//) {
//    // Step 1: Generate a random AES key for file encryption
//    val aesKey = generateAESKey()
//
//    // Step 2: Encrypt the AES key with RSA public key
//    val recipientPublicKey = decodeBase64ToPublicKey(publicKeyBase64)
//    val encryptedAESKey = encryptAESKeyWithRSAFile(aesKey, recipientPublicKey)
//
//    // Step 3: Write the encrypted AES key to the output stream first
//    outputStream.use { output ->
//        output.write(encryptedAESKey)
//        output.flush()  // You can leave this here to be extra sure the AES key is written out
//
//        // Step 4: Encrypt the file using AES and write the encrypted data to the output stream
//        val buffer = ByteArray(1024 * 1024)  // Define buffer size (adjustable)
//
//        // Read the input stream in chunks, encrypt each chunk, and write the result
//        inputStream.use { input ->
//            var bytesRead: Int
//            while (input.read(buffer).also { bytesRead = it } != -1) {
//                // Encrypt the chunk read from the input stream using AES
//                val encryptedChunk = encryptByteArrayAES(buffer.copyOf(bytesRead), aesKey)
//                output.write(encryptedChunk)
//            }
//        }
//
//        // Optional: You may not need this as outputStream will be automatically flushed by `use`
//        output.flush()
//    }
//}


fun encryptFile(inputStream: InputStream, outputStream: OutputStream, publicKeyBase64: String) {
    // Step 1: Generate a random AES key for file encryption
    val aesKey = generateAESKey()

    // Step 2: Encrypt the AES key with RSA public key
    val recipientPublicKey =
        decodeBase64ToPublicKey(publicKeyBase64)  // Assume this decodes your public key
    val encryptedAESKey =
        encryptAESKeyWithRSAFile(aesKey, recipientPublicKey)  // Encrypt the AES key with RSA

    // Step 3: Write the encrypted AES key to the output stream first
    outputStream.use { output ->
        output.write(encryptedAESKey)  // Write the RSA-encrypted AES key to the output stream
        output.flush()  // Ensure the AES key is written out

        // Step 4: Generate a random 16-byte IV (Initialization Vector) for AES-CTR
        val iv = ByteArray(16)  // 16 bytes for AES-CTR
        SecureRandom().nextBytes(iv)

        // Step 5: Write the IV to the output stream (important for decryption)
        output.write(iv)  // Write the IV to the output stream
        output.flush()  // Ensure the IV is written out

        // Step 6: Initialize the AES cipher in CTR mode
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        val ivSpec = IvParameterSpec(iv)  // Set the counter/IV for CTR mode
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec)


        // Step 7: Create a CipherInputStream to read the file and encrypt data as it is read
        CipherInputStream(inputStream, cipher)
            .use { cipherInputStream ->


                val buffer = ByteArray(1024 * 1024)  // 1 MB buffer size (adjustable)
                var bytesRead: Int

                // Step 8: Read the input stream in chunks, encrypt each chunk, and write to output stream
                while (cipherInputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer.copyOf(bytesRead))  // Write the encrypted chunk to the output stream
                }
            }

        output.flush()  // Ensure everything is written out
    }
}


fun encryptByteArrayAES(inputBytes: ByteArray, aesKey: SecretKey): ByteArray {
    // Generate a random 16-byte IV (Initialization Vector) for AES-CTR
    val iv = ByteArray(16)  // 16 bytes for AES-CTR, suitable for AES block size
    SecureRandom().nextBytes(iv)

    // Initialize the AES cipher in CTR mode
    val cipher = Cipher.getInstance("AES/CTR/NoPadding")
    val ivSpec = IvParameterSpec(iv)  // Set the counter/IV

    cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec)

    // Encrypt the byte array
    val encryptedBytes = cipher.doFinal(inputBytes)

    val encryptedData = ByteArray(iv.size + encryptedBytes.size)
    System.arraycopy(iv, 0, encryptedData, 0, iv.size)
    System.arraycopy(encryptedBytes, 0, encryptedData, iv.size, encryptedBytes.size)

    // Return the combined data as raw bytes
    return encryptedData
}


/*
fun decryptFile(encryptedData: ByteArray): DecryptionFileStatus {
    try {
        // Step 1: Split the encrypted data into the encrypted AES key and the encrypted file content
        val encryptedAESKeySize = 256  // Example size of RSA encrypted AES key (adjust as needed)
        val encryptedAESKey = encryptedData.copyOfRange(0, encryptedAESKeySize)

        val encryptedFileBytes = encryptedData.copyOfRange(encryptedAESKeySize, encryptedData.size)

        // Step 2: Decrypt the AES key using the recipient's RSA private key
        val privateKey = getPrivateKeyFromKeystore()  // Get the private key from the Keystore
        val decryptedAESKey = decryptAESKeyWithRSAByteArray(encryptedAESKey, privateKey)

        // Step 3: Decrypt the file content using the decrypted AES key
        val decryptedFileBytes = decryptAESFileWithAESKey(encryptedFileBytes, decryptedAESKey)

        // Return the decrypted file (as a byte array)
        return DecryptionFileStatus.Success(decryptedFileBytes)

    } catch (e: Exception) {
        e.printStackTrace()
        // Handle decryption errors (e.g., invalid RSA private key, tampered data)
        return DecryptionFileStatus.DecryptionFailed
    }
}
*/

/*

fun decryptFile(encryptedFile: File, outputFile: File): DecryptionFileStatus {


    return try {
        // Step 1: Initialize FileInputStream to read the encrypted file
        val fileInputStream = FileInputStream(encryptedFile)

        // Step 2: Define the size of the RSA-encrypted AES key (typically 256 bytes for RSA 2048)
        val encryptedAESKeySize = 256
        val encryptedAESKey = ByteArray(encryptedAESKeySize)

        // Read the first 256 bytes to get the RSA-encrypted AES key
        if (fileInputStream.read(encryptedAESKey) != encryptedAESKeySize) {
            throw IOException("Failed to read the RSA-encrypted AES key from the file.")
        }

        // Step 3: Decrypt the AES key using RSA private key
        val privateKey = getPrivateKeyFromKeystore()  // Retrieve the private key from Keystore
        val decryptedAESKey = decryptAESKeyWithRSAByteArray(encryptedAESKey, privateKey)

        // Step 5: Initialize ByteArrayOutputStream to collect the decrypted file data
        FileOutputStream(outputFile).use { fileOutputStream ->
            // Step 6: Create a buffer and read the encrypted file in 1MB chunks
            val buffer = ByteArray(1024 * 1024)  // 1MB buffer size
            var bytesRead: Int


            fileInputStream.use { input ->
                // Read in chunks and decrypt each chunk
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val decryptedChunk = decryptAESFileWithAESKey(buffer.copyOf(bytesRead), decryptedAESKey)
                    fileOutputStream.write(decryptedChunk)
                    fileOutputStream.flush()
                }
            }
        }

        // Step 9: Return the decrypted file as a byte array
        DecryptionFileStatus.Success(outputFile)

    } catch (e: Exception) {
        e.printStackTrace()
        // Handle decryption errors (e.g., invalid RSA private key, tampered data)
        DecryptionFileStatus.DecryptionFailed
    }
}

*/





fun decryptFile(encryptedFile: File, outputFile: File): DecryptionFileStatus {
    return try {
        val privateKey = getPrivateKeyFromKeystore()  // Retrieve the private key from Keystore

        // Step 1: Initialize FileInputStream to read the encrypted file
        val encryptedFileInputStream = FileInputStream(encryptedFile)

        // Step 2: Define the size of the RSA-encrypted AES key (256 bytes for RSA-2048)
        val encryptedAESKeySize = 256  // Adjust based on your RSA key size (e.g., RSA-2048)
        val encryptedAESKey = ByteArray(encryptedAESKeySize)

        // Read the first 256 bytes to get the RSA-encrypted AES key
        if (encryptedFileInputStream.read(encryptedAESKey) != encryptedAESKeySize) {
            throw IOException("Failed to read the RSA-encrypted AES key from the file.")
        }

        // Step 3: Decrypt the AES key using RSA private key
        val decryptedAESKey = decryptAESKeyWithRSAByteArray(encryptedAESKey, privateKey)

        // Step 4: Read the IV (First 16 bytes of the encrypted file)
        val iv = ByteArray(16)
        val bytesReadIV =
            encryptedFileInputStream.read(iv)  // Read the IV from the start of the file
        if (bytesReadIV != iv.size) {
            throw IOException("Failed to read the IV from the encrypted file.")
        }

        val ivSpec = IvParameterSpec(iv)  // Initialize IvParameterSpec for AES-CTR

        // Step 5: Initialize AES cipher in CTR mode for decryption
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, decryptedAESKey, ivSpec)

        // Step 6: Use CipherInputStream to handle decryption of the file in chunks
        FileOutputStream(outputFile).use { fileOutputStream ->
            CipherInputStream(encryptedFileInputStream, cipher).use { cipherInputStream ->
                val buffer = ByteArray(1024 * 1024)  // 1MB buffer size
                var bytesRead: Int

                // Step 7: Read the encrypted file in chunks and write the decrypted data
                while (cipherInputStream.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer.copyOf(bytesRead))  // Write the decrypted data to the output stream
                }
            }
        }

        // Return success if decryption completed without error
        DecryptionFileStatus.Success(outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        // Return failure status on error
        DecryptionFileStatus.DecryptionFailed
    }
}


// Decrypt the AES key using RSA (recipient's private key)
fun decryptAESKeyWithRSAByteArray(encryptedAESKey: ByteArray, privateKey: PrivateKey): SecretKey {


    val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding")

    // Setup OAEP parameters (SHA-256 with MGF1 and SHA-1)
    val oaepSpec = OAEPParameterSpec(
        "SHA-256",  // Hash algorithm for the OAEP padding
        "MGF1",     // Mask Generation Function
        MGF1ParameterSpec.SHA1,  // MGF1 with SHA-1
        PSource.PSpecified.DEFAULT  // Default PSource
    )

    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepSpec)

    // Decrypt the AES key with RSA
    val aesKeyBytes = rsaCipher.doFinal(encryptedAESKey)

    // Return the decrypted AES key as a SecretKey
    return SecretKeySpec(aesKeyBytes, "AES")

}


fun decryptAESFileWithAESKey(encryptedFileBytes: ByteArray, aesKey: SecretKey): ByteArray {
    // Extract the 16-byte IV from the first 16 bytes of the encrypted data
    val iv = encryptedFileBytes.copyOfRange(0, 16)
    val encryptedContent = encryptedFileBytes.copyOfRange(16, encryptedFileBytes.size)

    // Initialize the cipher for decryption using AES-CTR
    val cipher = Cipher.getInstance("AES/CTR/NoPadding")
    val ivSpec = IvParameterSpec(iv)  // AES-CTR uses the IV as a parameter

    cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec)

    // Decrypt the file content using the cipher
    return cipher.doFinal(encryptedContent)  // AES-CTR decryption in one step
}


sealed class DecryptionFileStatus {


    // Represents a successful decryption result
    data class Success(val decryptedFile: File) : DecryptionFileStatus()

    // Represents a specific failure where decryption failed
    object DecryptionFailed : DecryptionFileStatus()

    // Represents an error for unknown errors
    object UnknownError : DecryptionFileStatus()


}

sealed class DecryptionStatus {


    // Represents a successful decryption result
    data class Success(val decryptedMessage: String) : DecryptionStatus()


    // Represents a specific failure where decryption failed
    object DecryptionFailed : DecryptionStatus()

    // Represents an error for unknown errors
    object UnknownError : DecryptionStatus()

}