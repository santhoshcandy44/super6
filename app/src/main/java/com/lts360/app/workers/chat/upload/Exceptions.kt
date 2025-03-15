package com.lts360.app.workers.chat.upload



class InvalidKeyVersionException(message: String) : Exception(message)

class UserNotActiveException(message: String) : Exception(message)

class UploadFailedException(message: String) : Exception(message)

class AcknowledgmentTimeoutException(message: String) : Exception(message)

