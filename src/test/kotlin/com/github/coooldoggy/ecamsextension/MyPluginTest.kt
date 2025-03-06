package com.github.coooldoggy.ecamsextension

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.components.service
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import com.github.coooldoggy.ecamsextension.services.EcamsSyncService
import java.nio.file.Paths
import java.nio.file.Files

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

    fun testXMLFile() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        assertNotNull(xmlFile.rootTag)

        xmlFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }

    fun testMockProjectService() {
        val service = project.service<EcamsSyncService>()

        // TEST_MODE 활성화
        service.setTestMode(true)

        // getChangedFiles()가 Mock 데이터를 반환하는지 확인
        val changedFiles = service.getChangedFiles()
        assertNotNull("변경된 파일 목록이 null입니다.", changedFiles)
        assertEquals("Mock 데이터 개수가 맞지 않습니다.", 3, changedFiles.size)
        assertTrue("testFile1.txt가 존재해야 합니다.", changedFiles.contains("testFile1.txt"))
        assertTrue("testFile2.kt가 존재해야 합니다.", changedFiles.contains("testFile2.kt"))
        assertTrue("testFile3.java가 존재해야 합니다.", changedFiles.contains("testFile3.java"))

        // Mock 동기화 실행
        val syncResult = service.syncFilesToEcams(changedFiles)
        assertTrue("Mock 데이터 동기화가 실패했습니다.", syncResult)
    }


    override fun getTestDataPath() = "src/test/testData/rename"
}
