/*******************************************************************************
 * Copyright (c) 2021. Blademaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package dev.killjoy.interactions

import io.ktor.util.*
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.EdECPoint
import java.security.spec.EdECPublicKeySpec
import java.security.spec.NamedParameterSpec
import kotlin.experimental.and

class InteractionsVerifier(publicKey: String) {

    companion object {
        private val Algorithm = "ed25519"
        private val kf = KeyFactory.getInstance(Algorithm)
    }

    val signingKey = generateKeySpec(hex(publicKey))
    private val generatedPublicKey = kf.generatePublic(signingKey)

    fun verifyKey(requestBody: String, signature: String, timestamp: String): Boolean {
        val signedData = Signature.getInstance(Algorithm)
        signedData.initVerify(generatedPublicKey)

        signedData.update((timestamp + requestBody).toByteArray())
        return signedData.verify(hex(signature))
    }

    private fun generateKeySpec(pkByteArray: ByteArray): EdECPublicKeySpec {

        var byteArray = pkByteArray

        var xisodd = false
        val lastbyteInt = byteArray[byteArray.size - 1].toInt()
        if (lastbyteInt and 255 shr 7 == 1) {
            xisodd = true
        }

        byteArray[byteArray.size - 1] = byteArray[byteArray.size - 1] and 127

        byteArray = byteArray.reversedArray()
        val y = BigInteger(1, byteArray)
        val paramSpec = NamedParameterSpec(Algorithm)
        val ep = EdECPoint(xisodd, y)

        return EdECPublicKeySpec(paramSpec, ep)
    }
}