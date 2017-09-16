package com.github.sshaddicts.neuralclient.encoding

import org.apache.commons.codec.binary.Base64


class CommonBase64Coder : Base64Coder {
    override fun encode(bytes: ByteArray) = Base64.encodeBase64String(bytes)!!
    override fun decode(str: String) = Base64.decodeBase64(str)!!
}