package cn.jianke.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @className: PackagePlugin
 * @classDescription: This is the plugin entrance, the build will pass the project instance
 * to the plugin by calling the apply method.
 * @author: leibing
 * @createTime: 2016/08/17
 */
class PackagePlugin implements Plugin<Project> {
    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        project.extensions.create('jkPkg', PackagePluginExtension)

        project.afterEvaluate {
            //Package beta apk, modifyVersion++, betaVersion++
            project.tasks.assembleBeta.doLast {
                def newModifyVersion = Integer.parseInt(getModifyVersion()) + 1
                def newPakageTime = getNow()
                def versionName = project.extensions.android.defaultConfig.versionName
                def betaVersion = Integer.parseInt(getBetaVersion()) + 1
                updatePropFile(newModifyVersion, newPakageTime, versionName, betaVersion)
            }

            //Package release apk, modifyVersion++
            project.tasks.assembleRelease.doLast {
                def newModifyVersion = Integer.parseInt(getModifyVersion()) + 1
                def newPakageTime = getNow()
                def versionName = project.extensions.android.defaultConfig.versionName
                def betaVersion = getBetaVersion()
                updatePropFile(newModifyVersion, newPakageTime, versionName, betaVersion)
            }

            //Package debug apk
            project.tasks.assembleDebug.doLast {
                //Do nothing
            }
        }

        //Rename
        project.android.applicationVariants.all { variant ->
            nameApk(variant)
        }
    }

    def getPropFile() {
        return new File(project.getProjectDir().toString() + '\\buildCfg.properties')
    }

    def getModifyVersion() {
        File propFile = getPropFile();
        //If the file not exists, create it.
        if (!propFile.exists()) {
            propFile.createNewFile();
            updatePropFile(1, getNow(), project.extensions.android.defaultConfig.versionName, 1);
        }
        def Properties prop = new Properties();
        prop.load(new FileInputStream(propFile));
        def lastPackTime = prop.getProperty('PACK_TIME');
        def buildVerion = prop.getProperty('MODIFIED_VERSION');
        if (!getNow().equals(lastPackTime))
            buildVerion = "1";
        return buildVerion;
    }

    def getBetaVersion() {
        File propFile = getPropFile();
        if (!propFile.exists())
            return;
        def Properties prop = new Properties();
        prop.load(new FileInputStream(propFile));
        def lastVersion = prop.getProperty('LAST_VERSION');
        def betaVerion = prop.getProperty('BETA_VERSION');
        if (project.extensions.android.defaultConfig.versionName != lastVersion)
            betaVerion = "0";
        return betaVerion;
    }

    def updatePropFile(modifyVersion, packageTime, versionName, betaVerion) {
        File propFile = getPropFile();
        if (!propFile.exists())
            return;
        def printWriter = propFile.newPrintWriter();
        printWriter.write("# 修正版本号，每天清零" + '\n');
        printWriter.write('MODIFIED_VERSION = ' + modifyVersion + '\n');
        printWriter.write('PACK_TIME = ' + packageTime + '\n');
        printWriter.write('LAST_VERSION = ' + versionName + '\n');
        printWriter.write('BETA_VERSION = ' + betaVerion + '\n');
        printWriter.flush()
        printWriter.close()
    }

    def getNow() {
        return new Date().format("yyyyMMdd")
    }

    def nameApk(variant) {
        def buildType = variant.buildType.name;
        def outputFile = variant.outputs[0].outputFile;
        def versionName = project.extensions.android.defaultConfig.versionName;
        def releaseTime = getNow();
        def modifyVersion = getModifyVersion();
        def fileName = project.ymPkg.nameHead+ "_" + versionName + ".build-" + releaseTime + "" + modifyVersion;
        switch (buildType) {
            case "debug":
                return;
            case "beta":
                fileName += ".beta.apk";
                break;
            case "release":
                fileName += ".release.apk";
                break;
            default:
                fileName += ".apk";
                break;
        }
        variant.outputs[0].outputFile = new File(outputFile.parent, fileName)
    }
}
