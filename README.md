
appcan-android
==============

appcan-android引擎

参考 http://newdocx.appcan.cn/

QQ交流群：173758265



### 生成引擎包步骤：

1. 新建Android Studio工程`Engine_AppCan`

2. clone 引擎代码到工程`Engine_AppCan`

   ```sh
   git clone https://github.com/AppCanOpenSource/appcan-android.git
   ```

3. 编辑工程根目录下的`settings.gradle`,在下面添加一行：
   ```groovy
   include ':appcan-android:Engine'
   ```

4. 按需修改引擎代码

5. 编辑`appcan-android/en_baseEngineProject/androidEngine.xml`里面的`description`信息，其他的不要改动

6. 在Android Studio自带的`Terminal`输入

   ```shell
   gradle buildEngine
   ```

7. 编译完成后，生成的引擎包在`appcan-android/Engine/build/outputs/engine`目录下



### 目录结构

```
├── Engine 
│   ├── Engine.iml
│   ├── build
│   ├── build.gradle
│   ├── libs  公用lib
│   ├── proguard.pro  混淆文件
│   ├── project.properties
│   └── src
├── LICENSE
├── README.md
├── appcan-android.iml
├── en_baseEngineProject
│   ├── WebkitCorePalm 
│   └── androidEngine.xml
└── gradle-plugin
    └── appcan-build-engine.jar
```

**Engine**：引擎工程，该工程的最终作用就是生成引擎的jar

当有除了`java`代码之外的改动，如添加图片资源，添加或修改布局文件，都需要同步改动到WebkitCorePalm（重要）

**en_baseEngineProject/WebkitCorePalm**：最终打包使用的工程，除了上面说的需要从`Engine`工程同步的内容，其他信息不要改动，

**Engine/build**:目录结构如下

```
├── appcan
│   ├── crosswalk
│   ├── x5
│   └── system
├── generated
├── intermediates
├── outputs
│   ├── engine
│   ├── jar
│   ├── aar 
│   ├── logs
│   └── mapping
└── tmp
```

**appcan**:生成引擎时产生的临时文件

**outputs/engine**:引擎输出目录

**outputs/jar**:`Engine`moudle编译出来的jar，以`-un-proguard.jar`结尾的为未混淆的jar，其他为混淆过的jar

**outputs/aar**:`Engine`module生成的aar

### 关于混淆

- 混淆文件为：`appcan-android/Engine/proguard.pro`
- 混淆只用于混淆`Engine`中的java文件 排除混淆

#### 排除混淆

- 提供给JS调用的接口,请在方法添加`@AppCanAPI`注解，混淆时会排除
- 提供给插件调用或不希望被混淆的，请添加`@Keep`注解，混淆时会排除



### AppCan Gradle插件

一般情况下生成引擎只需要调用`gradle buildEngine` 就可以生成所有的引擎包，如果有其他需要可以调用其他的task。目前提供的task有：

- **build{Flavor}Engine**：生成对应flavor的引擎

  如输出crosswalk引擎

  ```sh
  gradle buildCrosswalkEngine
  ```

  输出所有flavor的引擎

  ```sh
  gradle buildEngine
  ```

- **build{Flavor}JarTemp**:生成对应flavor的jar，不进行混淆

  如输出crosswalk引擎的jar

  ```sh
  gradle buildCrosswalkJarTemp
  ```

  如果要输出所有flavor的jar

  ```sh
  gradle buildJarTemp
  ```

- **build{Flavor}Jar**：生成对应flavor的jar，并且进行混淆

  如输出混淆过的crosswalk引擎jar

  ```sh
  gradle buildCrosswalkJar
  ```

  如果要输出所有flavor的jar

  ```sh
  gradle buildJar 
  ```

- **build{Flavor}Aar**：生成对应flavor的aar

  如输出混淆过的System引擎aar

  ```sh
  gradle buildSystemAar
  ```

  如果要输出所有flavor的aar

  ```sh
  gradle buildAar 
  ```
  **说明**：生成aar需要将`Engine`目录下的`build.gradle`文件中的

  ```groovy
  apply plugin: 'com.android.applicaiton'
  ```

  替换成

  ```groovy
  apply plugin: 'com.android.library'
  ```

  并注释掉

  ```groovy
  applicationId 'org.zywx.wbpalmstar.widgetone.uex'
  ```

### 已经适配到AndroidStudio3.5.0开发环境
  ​
> 后续更高的gradle版本，升级原理相同，本文档更新不及时的话，开发者可以自行更新工程配置。但是gradle版本更新过高可能会导致引擎出包脚本使用的gradle插件不兼容，需要等待后续进行适配。目前经过完整测试的是可以适配到3.5.0。

在3.5.0遇到Gradle插件和脚本运行出错，是因为工程中的AppCanGradle插件未做高版本的适配。有两种方式解决：

#### 1. 降级gradle

目前，本工程的默认配置为适配AS3.5.0，Gradle版本为5.4.1，AndroidGradle构建插件版本为3.5.0。如果开发者没有升级AndroidStudio，按照以下操作降级：

- 修改Engine/gradle/wrapper/gradle-wrapper.properties，其中版本改为4.1；

- 修改build.gradle中的``` classpath 'org.appcan.gradle.plugins:appcan-gradle-plugin:2.3.1' ```，其中2.3.1修改为2.2.4。

- 经过以上操作后，理论上可以编译通过。不过还是建议升级AS。

#### 2. 依赖新版AppCanGradle插件

1. 修改Engine/gradle/wrapper/gradle-wrapper.properties，其中版本改为5.4.1；

2. 修改Engine/build.gradle文件中。其中，repositories增加一个github的maven库，dependencies中将原来的依赖本地的gradle插件改为依赖线上的，版本目前是2.4.0，相关仓库见文档后面。修改部分参考下面：

```groovy
buildscript {
    repositories {
        google()
        jcenter()
        maven {
            url 'https://raw.githubusercontent.com/android-plugin/mvn-repo/master/'
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.5.3'
//        classpath fileTree(dir: '../gradle-plugin', include: '*.jar')
        classpath 'org.appcan.gradle.plugins:appcan-gradle-plugin:2.4.0'
    }
}
```

3. dependencies中com.android.tools.build:gradle设置为3.0.1或更高版本之后，需要在所有的repositories中增加google()，否则会找不到Android新版的官方gradle相关插件库而报错；

4. 若buildToolsVersion改为26或更高后，还会要求修改flavor的定义，如下修改即可：

```groovy
    //声明flavorDimension
    flavorDimensions "kernel"

    productFlavors {
        crosswalk {
            dimension "kernel"
        }
        system {
            dimension "kernel"
        }
        x5 {
            dimension "kernel"
        }
    }
```

5. 关于此，如果仍有问题，欢迎在提issue或者QQ群中互相讨论，或者关注这个issue：https://github.com/AppCanOpenSource/appcan-android/issues/136

### 插件开发gradle依赖引擎配置

1. repositories中增加下面的maven地址：

```
repositories {
    maven {
        url 'https://raw.githubusercontent.com/android-plugin/mvn-repo/master/'
    }
}
```

2. dependencies中增加引擎的远程依赖包。注意：远程引擎包不会经常更新，仅当有涉及插件接口变化的重大更新时才会更新对应的调试版本**仅用于插件开发调试使用**，无关紧要的小bug不一定会更新在此处。使用正式引擎还需要通过AppCan官方的打包服务。

```
dependencies {
    //implementation 'org.appcan:engine:4.0.0'
    //implementation 'org.appcan:engine:4.3.23'
    //implementation 'org.appcan:engine:4.4.27'
    implementation 'org.appcan:engine:4.5.30_dev'
}
```

### 相关的其他仓库传送门

1. mvn-repo：用于存放仅供插件开发调试使用的远程依赖引擎包

https://github.com/android-plugin/mvn-repo

2. gradle-plugin：插件辅助编译gradle脚本

https://github.com/android-plugin/gradle-plugin

3. appcan-gradle-plugin：引擎辅助编译出包gradle插件源码

https://github.com/sandy1108/appcan-gradle-plugin

### 大版本更新列举（涉及插件开发的变动）

#### 4.1版本

1. JS交互逻辑变更，为了适配安全问题。

#### 4.2版本

1. JS交互逻辑再次变更，修复回调超过10240个字符时会被截断的问题。

#### 4.3版本

1. 全面增加Android动态权限申请，需要相关插件适配。引擎增加了针对插件申请权限的API。
2. 补充内置了部分常用的support库，更新版本至26。
3. minSdkVersion提升至16，targetSdkVersion提升至26

#### 4.4版本

1. 新增arm64等其他常见架构的libappcan.so库，引擎包基础工程的gradle中进行了过滤（v7和arm64），若有引擎定制需求，建议只需要修改gradle脚本的过滤，无需增删so的架构文件。另外，配合新版打包服务，可以无需定制引擎即可选择架构。
2. support相关库升级至28。
3. minSdkVersion提升至18，targetSdkVersion提升至28

#### 4.5版本

1. minSdkVersion提升至22，targetSdkVersion依然是28
2. gson库升级至2.8.5，补充大量常用的support库，版本依然是28。（support库最后一个版本就是28，已经成为历史，下一步要迁移为androidx）
3. 工程配置升级，支持JDK8编译（需要打包服务编译环境配合升级）。compileOptions中指定jdk版本为1.8，com.android.tools.build:gradle升级至3.1.3

#### 4.6版本

1. minSdkVerssion依然22保持不变，targetSdkVersion提升至30，compileSdkVersion也提升至30；
2. 由于targetSdkVersion升级到30，Android相关新特性和新的适配需要插件开发者关注，比如分区存储权限的变更等；
3. 由于业内现在的普遍规定，App启动时引擎框架内默认不会强制申请任何权限（4.6以下版本的引擎会强制申请三个权限：WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, ACCESS_COARSE_LOCATION）。对于原生插件来说，则需要自行检查自己的首次启动的权限申请是否妥当（比如是否缺失了上述三个权限是否会导致应用闪退或者异常，自动申请权限是否提前弹出了对用户友好的引导提示等等）；
4. 工程配置升级，com.android.tools.build:gradle升级至4.1.2

