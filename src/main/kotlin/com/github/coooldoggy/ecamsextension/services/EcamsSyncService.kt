package com.github.coooldoggy.ecamsextension.services

import com.github.coooldoggy.ecamsextension.settings.EcamsSettingsState
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.CharsetUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service(Service.Level.PROJECT)
class EcamsSyncService(private val project: Project) {
    private var isLoggedIn = false
    private var TEST_MODE: Boolean = false

    init {
        loadTestMode()
    }

    private fun loadTestMode() {
        val properties = Properties()
        try {
            val path = Paths.get("${project.basePath}/build.properties")
            if (Files.exists(path)) {
                Files.newBufferedReader(path).use { properties.load(it) }
                TEST_MODE = properties.getProperty("TEST_MODE", "false").toBooleanStrictOrNull() ?: false
            }
        } catch (e: Exception) {
            TEST_MODE = false
        }
    }

    fun setTestMode(value: Boolean) {
        TEST_MODE = value
    }

    fun isLoggedIn(): Boolean {
        return isLoggedIn
    }

    /**
     * 변경된 파일 목록을 얻어온 뒤, changed_files.log에 기록
     * TEST_MODE가 true면 Mock 데이터 반환.
     */
    fun getChangedFiles(): List<String> {
        if (TEST_MODE) {
            println("TEST_MODE 활성화: 가상 데이터 반환")
            return listOf("testFile1.txt", "testFile2.kt", "testFile3.java")
        }

        val changeListManager = ChangeListManager.getInstance(project)
        val changedFiles = mutableListOf<String>()

        for (changeList in changeListManager.changeLists) {
            for (change in changeList.changes) {
                val file = change.virtualFile
                if (file != null) {
                    changedFiles.add(file.path)
                }
            }
        }

        if (changedFiles.isNotEmpty()) {
            val path = Paths.get("${project.basePath}/changed_files.log")
            try {
                Files.write(path, changedFiles, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                println("changed_files.log 기록 중 오류 발생: ${e.message}")
            }
        }

        return changedFiles
    }


    /**
     * 변경된 파일들을 ECAMS 서버로 전송(동기화) 시도
     */
    fun syncFilesToEcams(files: List<String>): Boolean {
        return if (TEST_MODE) {
            println("가상 모드: ECAMS 동기화 성공")
            true
        } else {
            if (!isLoggedIn) {
                println("ECAMS 동기화 실패: 로그인 필요")
                return false
            }
            return try {
                val host = EcamsSettingsState.instance.serverIp
                val port = EcamsSettingsState.instance.serverPort
                val username = EcamsSettingsState.instance.username
                val password = EcamsSettingsState.instance.password

                val group = NioEventLoopGroup()
                val bootstrap = Bootstrap()
                bootstrap.group(group)
                    .channel(NioSocketChannel::class.java)
                    .handler(object : ChannelInitializer<Channel>() {
                        override fun initChannel(ch: Channel) {
                            ch.pipeline().addLast(EcamsClientHandler(files, username, password))
                        }
                    })

                val channelFuture = bootstrap.connect(host, port).sync()
                channelFuture.channel().closeFuture().sync()
                group.shutdownGracefully()
                true
            } catch (e: Exception) {
                println("ECAMS 동기화 실패: ${e.message}")
                false
            }
        }
    }

    /**
     * ECAMS 서버에 로그인 시도
     */
    fun loginToEcams(): Boolean {
        return try {
            val host = EcamsSettingsState.instance.serverIp
            val port = EcamsSettingsState.instance.serverPort
            val username = EcamsSettingsState.instance.username
            val password = EcamsSettingsState.instance.password

            val group = NioEventLoopGroup()
            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline().addLast(EcamsLoginHandler(username, password))
                    }
                })

            val channelFuture = bootstrap.connect(host, port).sync()
            channelFuture.channel().closeFuture().sync()
            group.shutdownGracefully()

            isLoggedIn = true
            true
        } catch (e: Exception) {
            println("ECAMS 로그인 실패: ${e.message}")
            isLoggedIn = false
            false
        }
    }

    /**
     * ECAMS 로그인 핸들러
     */
    private class EcamsLoginHandler(private val username: String, private val password: String)
        : SimpleChannelInboundHandler<String>() {

        override fun channelActive(ctx: ChannelHandlerContext) {
            val request = "LOGIN:$username:$password"
            ctx.writeAndFlush(Unpooled.copiedBuffer(request, CharsetUtil.UTF_8))
        }

        override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
            println("ECAMS 서버 로그인 응답: $msg")
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cause.printStackTrace()
            ctx.close()
        }
    }

    /**
     * ECAMS 동기화 핸들러
     */
    private class EcamsClientHandler(
        private val files: List<String>,
        private val username: String,
        private val password: String
    ) : SimpleChannelInboundHandler<String>() {

        override fun channelActive(ctx: ChannelHandlerContext) {
            val request = "LOGIN:$username:$password\nSYNC:${files.joinToString(",")}"
            ctx.writeAndFlush(Unpooled.copiedBuffer(request, CharsetUtil.UTF_8))
        }

        override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
            println("ECAMS 서버 응답: $msg")
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cause.printStackTrace()
            ctx.close()
        }
    }
}
