package pw.binom.services

import pw.binom.copyTo
import pw.binom.io.AsyncInput
import pw.binom.properties.ApplicationProperties
import pw.binom.s3.S3Client
import pw.binom.strong.inject
import pw.binom.strong.properties.injectProperty
import pw.binom.uuid.nextUuid
import kotlin.random.Random

class StorageService {
    private val s3Client: S3Client by inject()
    private val applicationProperties: ApplicationProperties by injectProperty()
    suspend fun delete(key: String) {
        s3Client.deleteObject(
            bucket = applicationProperties.storage.bucketName,
            key = key,
            regin = applicationProperties.storage.regin,
        )
    }

    suspend fun storage(key: String, stream: AsyncInput) {
        s3Client.putObjectContent(
            bucket = applicationProperties.storage.bucketName,
            key = key,
            regin = applicationProperties.storage.regin,
        ) { output ->
            stream.copyTo(output)
        }
    }

    suspend fun storage(stream: AsyncInput): String {
        val key = Random.Default.nextUuid().toString()
        storage(
            key = key,
            stream = stream
        )
        return key
    }

    suspend fun load(key: String) = s3Client.getObject(
        regin = applicationProperties.storage.regin,
        bucket = applicationProperties.storage.bucketName,
        key = key,
    )
}