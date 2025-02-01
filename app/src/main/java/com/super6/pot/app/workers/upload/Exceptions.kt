package com.super6.pot.app.workers.upload



class InvalidKeyVersionException(message: String) : Exception(message)

class UserNotActiveException(message: String) : Exception(message)

class UploadFailedException(message: String) : Exception(message)

class AcknowledgmentTimeoutException(message: String) : Exception(message)

