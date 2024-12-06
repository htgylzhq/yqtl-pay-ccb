package cn.yqtl.pay.ccb.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZipFileUtilTest {
    
    @Test
    fun `should extract specific file`(@TempDir tempDir: Path) {
        // 准备测试数据
        val testContent = "测试内容"
        val targetFileName = "test.txt"
        val otherContent = "其他文件内容"
        val otherFileName = "other.txt"
        val zipFile = createTestZipFile(
            tempDir = tempDir,
            targetFileName = targetFileName,
            targetContent = testContent,
            otherFileName = otherFileName,
            otherContent = otherContent
        )

        // 执行测试
        val extractedFile = ZipFileUtil.extractSpecificFile(
            zipFilePath = zipFile.toString(),
            targetFileName = targetFileName,
            outputDir = tempDir.resolve("output").toString()
        )

        // 验证结果
        assertTrue(Files.exists(extractedFile))
        assertEquals(testContent, Files.readString(extractedFile, StandardCharsets.UTF_8))
    }

    @Test
    fun `should extract file from nested directory`(@TempDir tempDir: Path) {
        // 准备测试数据
        val testContent = "嵌套目录测试内容"
        val targetFileName = "test.txt"
        val otherContent = "其他文件内容"
        val otherFileName = "other/file.txt"
        val zipFile = createNestedZipFile(
            tempDir = tempDir,
            targetPath = "dir/$targetFileName",
            targetContent = testContent,
            otherPath = otherFileName,
            otherContent = otherContent
        )

        // 执行测试
        val extractedFile = ZipFileUtil.extractSpecificFile(
            zipFilePath = zipFile.toString(),
            targetFileName = targetFileName,
            outputDir = tempDir.resolve("output").toString()
        )

        // 验证结果
        assertTrue(Files.exists(extractedFile))
        assertEquals(testContent, Files.readString(extractedFile, StandardCharsets.UTF_8))
    }

    @Test
    fun `should throw exception when file not found`(@TempDir tempDir: Path) {
        // 准备测试数据
        val zipFile = createEmptyZipFile(tempDir)

        // 验证异常
        val exception = assertFailsWith<FileNotFoundException> {
            ZipFileUtil.extractSpecificFile(
                zipFilePath = zipFile.toString(),
                targetFileName = "nonexistent.txt",
                outputDir = tempDir.resolve("output").toString()
            )
        }

        assertTrue(exception.message?.contains("未找到指定文件") == true)
    }

    @Test
    fun `should throw exception when zip file not exists`(@TempDir tempDir: Path) {
        assertFailsWith<FileNotFoundException> {
            ZipFileUtil.extractSpecificFile(
                zipFilePath = "nonexistent.zip",
                targetFileName = "test.txt",
                outputDir = tempDir.resolve("output").toString()
            )
        }
    }

    @Test
    fun `shoud extract bill file`(@TempDir tempDir: Path) {
        val zipFilePath = "/Users/rochuukyou/Developer/Projects/yqtl-pay-ccb/src/test/resources/sample-data/SHOP.105000080626868.20241203.20241203.20241204140658056.MmWR.zip"
        var billFile = ZipFileUtil.extractSpecificFile(
            zipFilePath = zipFilePath,
            targetFileName = "SHOP.105000080626868.20241203.txt",
            outputDir = tempDir.resolve("output").toString()
        )
        println(Files.readString(billFile))
    }

    private fun createTestZipFile(
        tempDir: Path,
        targetFileName: String,
        targetContent: String,
        otherFileName: String,
        otherContent: String
    ): Path {
        val zipFile = tempDir.resolve("test.zip")
        ZipOutputStream(FileOutputStream(zipFile.toFile())).use { zos ->
            // 添加目标文件
            ZipEntry(targetFileName).let { entry ->
                zos.putNextEntry(entry)
                zos.write(targetContent.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
            
            // 添加其他文件
            ZipEntry(otherFileName).let { entry ->
                zos.putNextEntry(entry)
                zos.write(otherContent.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
        }
        return zipFile
    }

    private fun createNestedZipFile(
        tempDir: Path,
        targetPath: String,
        targetContent: String,
        otherPath: String,
        otherContent: String
    ): Path {
        val zipFile = tempDir.resolve("nested.zip")
        ZipOutputStream(FileOutputStream(zipFile.toFile())).use { zos ->
            // 添加目标文件
            ZipEntry(targetPath).let { entry ->
                zos.putNextEntry(entry)
                zos.write(targetContent.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
            
            // 添加其他文件
            ZipEntry(otherPath).let { entry ->
                zos.putNextEntry(entry)
                zos.write(otherContent.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
        }
        return zipFile
    }

    private fun createEmptyZipFile(tempDir: Path): Path {
        val zipFile = tempDir.resolve("empty.zip")
        ZipOutputStream(FileOutputStream(zipFile.toFile())).use { /* 创建空的ZIP文件 */ }
        return zipFile
    }
}