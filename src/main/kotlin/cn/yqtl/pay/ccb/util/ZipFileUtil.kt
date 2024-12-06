package cn.yqtl.pay.ccb.util

import org.slf4j.LoggerFactory
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipInputStream

object ZipFileUtil {

    private val logger = LoggerFactory.getLogger(ZipFileUtil::class.java)

    /**
     * 从ZIP文件中提取指定文件名的文件
     * @param zipFilePath ZIP文件路径
     * @param targetFileName 目标文件名
     * @param outputDir 输出目录
     * @return 提取的文件Path对象
     * @throws IOException 如果发生IO错误
     * @throws FileNotFoundException 如果未找到指定文件
     */
    fun extractSpecificFile(zipFilePath: String, targetFileName: String, outputDir: String): Path {
        var outputPath: Path? = null

        ZipInputStream(FileInputStream(zipFilePath)).use { zipIn ->
            val outputDirPath = Paths.get(outputDir)
            Files.createDirectories(outputDirPath)

            var entry = zipIn.nextEntry
            while (entry != null) {
                val entryName = entry.name

                if (entryName.equals(targetFileName, ignoreCase = true) ||
                    entryName.substringAfterLast('/').equals(targetFileName, ignoreCase = true)
                ) {

                    outputPath = outputDirPath.resolve(targetFileName)
                    BufferedOutputStream(FileOutputStream(outputPath!!.toFile())).use { bos ->
                        val buffer = ByteArray(4096)
                        var read: Int
                        while (zipIn.read(buffer).also { read = it } != -1) {
                            bos.write(buffer, 0, read)
                        }
                        logger.info("成功提取文件: $targetFileName")
                    }
                    break
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        return outputPath ?: throw FileNotFoundException("在ZIP文件中未找到指定文件: $targetFileName")
    }
}