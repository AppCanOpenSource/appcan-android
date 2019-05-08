
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

### AndroidStudio3.0.1开发环境适配（举例）
  ​
> 后续更高的gradle版本，升级原理相同，本文档更新不及时的话，开发者可以自行更新工程配置。但是gradle版本更新过高可能会导致引擎出包脚本使用的gradle插件不兼容，需要等待后续进行适配。目前经过完整测试的是可以适配到3.0.1。

在3.0.1遇到Gradle插件和脚本运行出错，是因为工程中的AppCanGradle插件未做高版本的适配。有两种方式解决：

#### 1. 降级gradle

修改Engine/gradle/wrapper/gradle-wrapper.properties，其中版本改为2.14.1；其他部分维持原状即可编译通过。

#### 2. 依赖新版AppCanGradle插件（beta版）

1. 修改Engine/gradle/wrapper/gradle-wrapper.properties，其中版本改为4.1；

2. 修改Engine/build.gradle文件中。其中，repositories增加一个github的maven库，dependencies中将原来的依赖本地的gradle插件改为依赖线上的，版本目前是2.2.3，可以在此仓库关注更新 https://github.com/android-plugin/mvn-repo。修改部分参考下面：

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
        classpath 'com.android.tools.build:gradle:3.0.1'
//        classpath fileTree(dir: '../gradle-plugin', include: '*.jar')
        classpath 'org.appcan.gradle.plugins:appcan-gradle-plugin:2.2.3'
    }
}
```

3. dependencies中com.android.tools.build:gradle设置为3.0.1之后，需要在所有的repositories中增加google()，否则会找不到Android新版的官方gradle相关插件库而报错；

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

5. 为了方便开发者使用，修改后的gradle文件已经放在了工程根目录，名为**build.gradle.3.0.1**，由于在实验阶段，没有替换原有的。

6. 关于此，如果仍有问题，欢迎在提issue或者QQ群中互相讨论，或者关注这个issue：https://github.com/AppCanOpenSource/appcan-android/issues/136

### 插件开发gradle依赖引擎配置

1. repositories中增加下面的maven地址：

```
repositories {
    
    maven {
        url 'https://raw.githubusercontent.com/android-plugin/mvn-repo/master/'
    }
}
```

2. dependencies中增加依赖包。其中，版本号+号是指随便获取一个版本（不一定是最新版本）。如果要指定版本号，可以将+号改为4.0.0，或4.3.21等等。未来还会有更多版本。

```
dependencies {
    implementation 'org.appcan:engine:+:systemRelease@aar'//依赖远程引擎
}
```
