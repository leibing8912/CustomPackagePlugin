# APK规范打包Gradle插件
android工程只需引入该插件即可输出符合公司规范的apk文件

## 用例
*   在全局build.gradle中引入插件库

    buildscript {
        repositories {
            jcenter()
            maven {
                url 'http://localhost:8081/nexus/content/repositories/jike_pkgplugin_release/'
            }
        }



        dependencies {
            classpath 'com.android.tools.build:gradle:1.5.0'
            classpath group: 'cn.jianke.gradle', name: 'JkwPackagePlugin', version: '1.0.0'
        }
    }

*   在module的build.gradle文件中申明该插件，并定义好apk nameHead

    apply plugin: 'jianke-package'
    jkPkg {
        nameHead "test"
    }


*   有些朋友不知道url 'http://localhost:8081/nexus/content/repositories/jike_pkgplugin_release/'从哪里来的，这里需要部署一个maven远程库，具体部署参考下面链接：
http://www.360doc.com/content/16/0817/13/35842774_583841981.shtml