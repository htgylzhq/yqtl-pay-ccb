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
    private data class TestData(
        val targetFileName: String = "test.txt",
        val targetContent: String = "测试内容",
        val otherFileName: String = "other.txt",
        val otherContent: String = "其他文件内容",
        val targetPath: String = targetFileName
    )

    private fun createTestData() = TestData()
    
    private fun createNestedTestData() = TestData(
        targetPath = "dir/test.txt",
        targetContent = "嵌套目录测试内容",
        otherFileName = "other/file.txt"
    )

    @Test
    fun `should extract specific file`(@TempDir tempDir: Path) {
        val testData = createTestData()
        val zipFile = createTestZipFile(
            tempDir = tempDir,
            targetFileName = testData.targetFileName,
            targetContent = testData.targetContent,
            otherFileName = testData.otherFileName,
            otherContent = testData.otherContent
        )

        // 执行测试
        val extractedFile = ZipFileUtil.extractSpecificFile(
            zipFilePath = zipFile.toString(),
            targetFileName = testData.targetFileName,
            outputDir = tempDir.resolve("output").toString()
        )

        // 验证结果
        assertTrue(Files.exists(extractedFile))
        assertEquals(testData.targetContent, Files.readString(extractedFile, StandardCharsets.UTF_8))
    }

    @Test
    fun `should extract file from nested directory`(@TempDir tempDir: Path) {
        val testData = createNestedTestData()
        val zipFile = createNestedZipFile(
            tempDir = tempDir,
            targetPath = testData.targetPath,
            targetContent = testData.targetContent,
            otherPath = testData.otherFileName,
            otherContent = testData.otherContent
        )

        // 执行测试
        val extractedFile = ZipFileUtil.extractSpecificFile(
            zipFilePath = zipFile.toString(),
            targetFileName = testData.targetFileName,
            outputDir = tempDir.resolve("output").toString()
        )

        // 验证结果
        assertTrue(Files.exists(extractedFile))
        assertEquals(testData.targetContent, Files.readString(extractedFile, StandardCharsets.UTF_8))
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